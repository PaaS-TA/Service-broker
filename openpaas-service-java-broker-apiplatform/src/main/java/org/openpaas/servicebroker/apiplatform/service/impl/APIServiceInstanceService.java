package org.openpaas.servicebroker.apiplatform.service.impl;

import java.util.UUID;

import org.openpaas.servicebroker.apiplatform.common.ApiPlatformUtils;
import org.openpaas.servicebroker.apiplatform.dao.APIServiceInstanceDAO;
import org.openpaas.servicebroker.apiplatform.exception.APIServiceInstanceException;
import org.openpaas.servicebroker.apiplatform.model.APIServiceInstance;
import org.openpaas.servicebroker.apiplatform.model.APIUser;
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.openpaas.servicebroker.common.JsonUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.openpaas.servicebroker.exception.ServiceInstanceExistsException;
import org.openpaas.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.UpdateServiceInstanceRequest;
import org.openpaas.servicebroker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import sun.security.jca.ServiceId;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class APIServiceInstanceService implements ServiceInstanceService {

	private static final Logger logger = LoggerFactory.getLogger(APIServiceInstanceService.class);
	
	@Autowired
	private Environment env;
	
	@Autowired
	APIServiceInstanceDAO dao;
	
	@Autowired
	LoginService loginService;
	
	@Autowired
	APICatalogService apiCatalogService;
	
	@Override
	public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request) throws ServiceInstanceExistsException, ServiceBrokerException 
	{		
		logger.info("create ServiceInstance");
		
		String dashboardUrl = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformDashboardPort")+env.getProperty("APIPlatformDashboardURL");
		
		String organizationGuid =request.getOrganizationGuid();
		String serviceInstanceId = request.getServiceInstanceId();
		String spaceGuid = request.getSpaceGuid();
		String serviceId = request.getServiceDefinitionId();
		String planId = request.getPlanId();
		
		String planName = planId.split(" ")[2];
		String serviceName = serviceId.split(" ")[0];
		String serviceVersion = serviceId.split(" ")[1];
		String serviceProvider = apiCatalogService.svc.getMetadata().get("providerDisplayName").toString();
		
		HttpEntity<String> httpEntity;
		ResponseEntity<String> responseEntity;
		HttpHeaders headers;
		
		String cookie = "";
		

	//설정된 플랜명중에서 요청된 플랜명이 존재하는지 확인, 설정되어있는 플랜명이라면, 플랜명이 적절한지 확인한다.
		//TODO
		boolean planAvailable = false;
		String planSetted;
		int i = 1;
		do{
			planSetted = env.getProperty("AvailablePlan"+i);
			if(planSetted.equals(planName)){
				if(planId.equals(serviceId+" "+planName)){
					planAvailable = true;					
					break;
				}
				else{
					//플랜명은 적절하지만, 플랜ID는 잘못된 케이스 OR 서비스 ID가 잘못 된 케이스 - P006, P008, P007, P016, P018
					logger.error("Invalid ServiceID or Invalid PlanID. ServiceID :["+serviceId+"], PlanId :["+planId+"]");
					throw new ServiceBrokerException("Invalid ServiceID or Invalid PlanID. ServiceID :["+serviceId+"], PlanId :["+planId+"] - P006,P007,P008,P016,P018");
				}
			}
			i++;
		} while((env.getProperty("AvailablePlan"+i)!=null));

		if(!planAvailable){
			//플랜명이 잘못된 케이스 - P005
			logger.error("Invalid PlanID :["+planId+"]");
			throw new ServiceBrokerException("Invalid PlanID :["+planId+"] - P005"); 
		}
		

	//해당 인스턴스 아이디로 DB에 저장된 정보가 있는지 확인한다.
		boolean instanceExsistsAtDB = false;
		APIServiceInstance apiServiceInstacne = new APIServiceInstance();
		try {
			apiServiceInstacne=dao.getAPIServiceByInstance(serviceInstanceId);
			//이미 삭제된 인스턴스와 동일한 인스턴스 ID 및 org ID로 요청이 들어온 경우 - P017
			//TODO 삭제된 인스턴스 처리를 어떻게 할지에 대한 결정에 따라 변경
			if(apiServiceInstacne.getDelyn().equals("Y")){
				logger.info("Service Instance : ["+serviceInstanceId+"] already removed" );
				throw new ServiceBrokerException("already removed Service Instance : ["+serviceInstanceId+"] - P017");
			}
			instanceExsistsAtDB = true;

		} catch (EmptyResultDataAccessException e) {
			//EmptyResultDataAccessException이 던져지는 경우는 DB에 해당 인스턴스 아이디가 없다는 의미이므로instanceExsistsAtDB 값을 false로 변경한다.
			instanceExsistsAtDB = false;
			logger.debug("not found Service at APIPlatform Database");
		} catch (Exception e){
			//다른 DB에러에 대한 예외를 처리한다.
			logger.error("APIPlatform Database Error - getAPIServiceByInstanceID");
			throw new ServiceBrokerException(e.getMessage());
		}
		
	//해당 인스턴스 아이디로 DB에 저장된 서비스 인스턴스 정보와 요청이 들어온 서비스 인스턴스의 내용이 다를 경우, 409 예외를 발생시킨다.
		if(instanceExsistsAtDB){
			//인스턴스 아이디에 대한 DB의 org아이디와 요청된 org아이디가 다른 경우이다. - P014
			if(!apiServiceInstacne.getOrganization_id().equals(organizationGuid)){
				logger.error("Incorrect OrganizationID :["+organizationGuid+"]. Valid OrganizationID: ["+apiServiceInstacne.getOrganization_id()+"]");
				ServiceInstance instance = new ServiceInstance(request).withDashboardUrl(dashboardUrl);
				throw new ServiceInstanceExistsException(instance);			
			}
			//DB에 저장된 서비스아이디, 플랜아이디와 일치하지 않으면 예외를 발생시킨다. - P002
			if(!apiServiceInstacne.getService_id().equals(serviceId)&&!apiServiceInstacne.getPlan_id().equals(planId)){
				logger.error("Invalid ServiceID :["+serviceId+"]. Valid ServiceID :["+apiServiceInstacne.getService_id()+"]"
						+ "Invalid PlanID :["+planId+"]. Valid PlanName :["+apiServiceInstacne.getPlan_id()+"]");
				ServiceInstance instance = new ServiceInstance(request).withDashboardUrl(dashboardUrl);
				throw new ServiceInstanceExistsException(instance);
			}
			//DB에 저장된 서비스 아이디와 일치하지 않는 경우 - P016
			else if(!apiServiceInstacne.getService_id().equals(serviceId)){
				logger.error("Invalid ServiceID :["+serviceId+"]. Valid ServiceID :["+apiServiceInstacne.getService_id()+"]");
				ServiceInstance instance = new ServiceInstance(request).withDashboardUrl(dashboardUrl);
				throw new ServiceInstanceExistsException(instance);
			}
			//DB에 저장된 플랜아이디와 일치하지 않는 경우 - P004
			else if(!apiServiceInstacne.getPlan_id().equals(planId)){
				logger.error("Invalid PlanID :["+planId+"]. Valid PlanID :["+apiServiceInstacne.getPlan_id()+"]");
				ServiceInstance instance = new ServiceInstance(request).withDashboardUrl(dashboardUrl);
				throw new ServiceInstanceExistsException(instance);
			}
			//DB에 저장된 서비스아이디, 플랜아이디, org아이디와 일치한다면 이미 존재하는 인스턴스이다. 이경우 예외를 발생시키지 않는다.
			else{
				logger.info("Service Instance already exists. InstanceID :["+serviceInstanceId+"]");
			}
		}

	//DB에서 organizationGuid를 확인하여 API플랫폼 로그인 아이디와 비밀번호를 획득한다.
		
		APIUser userInfo = new APIUser();
		try{
			userInfo = dao.getAPIUserByOrgID(organizationGuid);
		} catch(EmptyResultDataAccessException e){
			//새로운 유저인 경우, API User DB에 유저정보가 없기 때문에 DAO에서 예외를 던진다.
			logger.info("not found User at APIPlatform Database");
		} catch (Exception e){
			//다른 DB에러에 대한 예외를 처리한다.
			logger.error("APIPlatform Database Error - getAPIUserByOrgID");
			throw new ServiceBrokerException(e.getMessage());
		}
		
		String userId = userInfo.getUser_id();
		String userPassword = userInfo.getUser_password();
		logger.info("User Id : "+userId);
		
	//API플랫폼에 등록되지 않은 유저일때, 유저아이디를 생성하여 등록한다.
		boolean userIdDuplication;
		if(userId==null){
			logger.info("not registered API Platform User");
			userPassword=env.getProperty("UserSignupPassword");
			
			//API플랫폼에서 유저아이디의 중복여부를 확인하여, 중복인 경우 유저아이디를 재생성하여 등록한다.
			do{
				userId = makeUserId();
				userIdDuplication = userSignup(userId,userPassword);
				logger.info("API Platform User Duplication Check");
			} while(userIdDuplication);
			logger.info("API Platform User Created userId : "+userId);
			
			//DB에 생성한 유저정보를 저장하고 문제가 있을 경우 예외를 발생시킨다.
			try {
				dao.insertAPIUser(organizationGuid, userId, userPassword);				
			} catch (Exception e) {
				logger.error("Insert API User Error");
				throw new ServiceBrokerException("Database Error - Insert API User");	
			}
			logger.info("APIUser insert finished");
			
			//생성한 user ID로 로그인하고 쿠키값을 변수에 저장한다.
			cookie = loginService.getLogin(userId, userPassword);
			logger.info("API Platform Login - UserID : "+userId);
		}
		//요청받은 organizationGuid와 매칭되는 API플랫폼 유저정보를 DB에서 찾은 경우이다.
		else {
			try {
				cookie = loginService.getLogin(userId, userPassword);
			} catch (Exception e) {
				//DB에는 유저정보가 저장되어 있으나 API플랫폼에서는 유저가 삭제된 경우이다.
				if(e.getMessage().equals("No User")){
					//DB에 저장된 유저정보로 API플랫폼의 유저를 생성하고 로그인하여 쿠키값을 저장한다.
					userSignup(userId,userPassword);
					cookie = loginService.getLogin(userId, userPassword);
					logger.info("API Platform Login - UserID : "+userId);
				} else{
					//로그인시 유저가 없는 경우를 제외한 나머지 예외가 발생시 처리한다.
					throw new ServiceBrokerException(e.getMessage());	
				}
			}		
		}
		
	//Add an Application API를 사용한다. error:false와 status: APPROVED가 리턴되어야 정상
		boolean applicationExsists = false;
		
		logger.info("Add An Application API Start");
		
		headers = new HttpHeaders();
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		//Add an Application API를 보낸다.
		
		String addApplicationUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.AddAnApplication");
		String addApplicationParameters = 
				"action=addApplication&application="+serviceInstanceId+"&tier="+planName+"&description=&callbackUrl=";
		httpEntity = new HttpEntity<String>(addApplicationParameters, headers);

		responseEntity = HttpClientUtils.send(addApplicationUri, httpEntity, HttpMethod.POST);
		
		JsonNode addApplicationResponseJson = null;

		try {
			addApplicationResponseJson = JsonUtils.convertToJson(responseEntity);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(addApplicationResponseJson);
			applicationExsists = true;
			//어플리케이션이 정상적으로 생성 되었다면 넘어간다.
			logger.info("Application "+serviceInstanceId+" created");		
		} catch (ServiceBrokerException e) {
			if(e.getMessage().equals("Duplication")){
				//어플리케이션이 이미 존재하는 경우이므로 applicatioExsists를 true로 변경한다.
				logger.info("application already exists");
				applicationExsists = true;
			} else{
				//그밖의 예외처리
				logger.error("API Platform response error - Add an Application");
				throw new ServiceBrokerException(e.getMessage());
			}
		}
		
		logger.info("API Platform Application added");
		
	//Generate Key API를 사용하여 API플랫폼 어플리케이션의 키값을 생성한다.
		logger.info("API Platform Generate Key Started. Application : "+serviceInstanceId);
		//두가지 키타입으로 키를 생성해야하기 때문에 두번 실행한다.
		for(i=1;i<3;i++) {	
			logger.debug("API Platform Generate Key Started - KEYTYPE : "+env.getProperty("Keytype"+i));
			headers.set("Cookie", cookie);
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			String generateKeyUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GenerateAnApplicationKey");
			String generateKeyParameters = 
					"action=generateApplicationKey&application="+serviceInstanceId+"&keytype="+env.getProperty("Keytype"+i)+"&callbackUrl=&authorizedDomains=ALL&validityTime=360000";
		
			httpEntity = new HttpEntity<String>(generateKeyParameters, headers);
			responseEntity = HttpClientUtils.send(generateKeyUri, httpEntity, HttpMethod.POST);
	
			JsonNode generateKeyResponseJson = null;
			
			try {
				generateKeyResponseJson = JsonUtils.convertToJson(responseEntity);
				ApiPlatformUtils.apiPlatformErrorMessageCheck(generateKeyResponseJson);
				logger.debug(env.getProperty("Keytype"+i)+" Key generated. Application : "+serviceInstanceId);
			} catch (ServiceBrokerException e) {
				if(e.getMessage().equals("Key already generated"))
				{
					logger.debug(env.getProperty("Keytype"+i)+" Key already generated");
				} else {
					logger.error("API Platform response error - Generate Key KEYTYPE : "+env.getProperty("Keytype"+i));
					throw new ServiceBrokerException(e.getMessage());					
				}
				logger.debug("API Platform Generate Key finished - KEYTYPE : "+env.getProperty("Keytype"+i));
			}
		}
		logger.info("API Platform Generate Key finished. Application : "+serviceInstanceId);

		
		
		
		
	//Add Subscription API를 사용하여 API플랫폼 어플리케이션에 API를 사용등록한다.

		boolean subscriptionExists=false;
		logger.info("API Platform Add a Subscription started");
		String addSubscriptionUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.AddSubscription");
		String addSubscriptionParameters = 
				"action=addAPISubscription&name="+serviceName+"&version="+serviceVersion+"&provider="+serviceProvider+"&tier="+planName+"&applicationName="+serviceInstanceId;
		
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		httpEntity = new HttpEntity<String>(addSubscriptionParameters, headers);

		ResponseEntity<String> addSubscriptionResponseHttp = HttpClientUtils.send(addSubscriptionUri, httpEntity, HttpMethod.POST);
		
		JsonNode addSubscriptionResponseJson = null;

		try {
			
			addSubscriptionResponseJson = JsonUtils.convertToJson(addSubscriptionResponseHttp);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(addSubscriptionResponseJson);
			//사용등록이 정상적으로 이루어진 경우
			subscriptionExists = true;		
		} catch (ServiceBrokerException e){
			if(e.getMessage().equals("Duplication"))
			{	//이미 사용등록이 되어있는 경우
				subscriptionExists = true;
				logger.info("API already subscribed");
			}
			else{
				logger.error("API Platform response error - Add a Subscription");
				throw new ServiceBrokerException(e.getMessage());
			}
		}
		logger.info("API Platform Subscription finished");
		
		ServiceInstance instance = new ServiceInstance(request).withDashboardUrl(dashboardUrl);
		//인스턴스ID가 존재하고 해당 어플리케이션, 서비스가 모두 존재하는 경우
		//TODO 
		if(instanceExsistsAtDB&&applicationExsists&&subscriptionExists){
			logger.info("Service instance already exists");
			instance.setHttpStatusOK();
			return instance;
		}
		else {
		//API서비스 인스턴스 정보를 API플랫폼 DB에 저장한다.
			try {
				dao.insertAPIServiceInstance(organizationGuid,serviceInstanceId,spaceGuid,serviceId,planId);
			} catch (Exception e) {
				logger.error("API Service Instance insert failed "+e.getMessage());
				throw new APIServiceInstanceException("Database Error: API ServiceInstance insert");		
			}
			logger.info("API Service Instance insert finished");
		}
		
		logger.info("createServiceInstance() completed");
		return instance;
	}

	
	@Override
	public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request) throws ServiceBrokerException 
	{
		
		String serviceInstanceId = request.getServiceInstanceId();
		String serviceId = request.getServiceId();
		String planId = request.getPlanId();
		
		APIServiceInstance apiServiceInstance;
		try {
			apiServiceInstance = dao.getAPIInfoByInstanceID(serviceInstanceId);	
			logger.debug("get API Information");
		} catch (EmptyResultDataAccessException e) {
			//인스턴스 ID를 DB에서 찾을 수 없거나 해당 유저정보를 DB에서 찾을 수 없는 경우
			logger.error("not found information about service instnace or APIPlatform user. InstanceID :["+serviceInstanceId+"]");
			apiServiceInstance=null;
			try {
				apiServiceInstance = dao.getAPIServiceByInstance(serviceInstanceId);				
			} catch (EmptyResultDataAccessException e2) {
				//DB에서 서비스 인스턴스 정보를 찾을 수 없는 케이스 - D003
				logger.error("Service Instnace information not found. ServiceInstanceID :["+serviceInstanceId+"] - D003");
				return null;
			}
			String organizationGuid = apiServiceInstance.getOrganization_id();
			try {
				dao.getAPIUserByOrgID(organizationGuid);
			} catch (EmptyResultDataAccessException e2) {
				//DB에서 유저정보가 삭제된 케이스 - D008
				logger.error("APIPlatform user information not found. OrganizationGUID :["+organizationGuid+"] - D008");
				return null;
			}
			return null;
		} catch (Exception e){
			logger.error("Database Error - getServiceInstanceByInstanceID()");
			throw new ServiceBrokerException(e.getMessage());
		}
		//이미 삭제된 인스턴스 ID인 경우 - D011
		if(apiServiceInstance.getDelyn().equals("Y")){
			logger.info("Service Instance : ["+serviceInstanceId+"] already removed" );
			throw new ServiceBrokerException("already removed Service Instance : ["+serviceInstanceId+"] - D011");
		}
		//DB의 인스턴스 정보와 CF에서 디프로비전을 요청한 인스턴스 정보가 일치하는지 확인한다. 
		//플랜ID가 DB의 인스턴스 정보와 다른 경우 - D002
		if(!apiServiceInstance.getService_id().equals(serviceId)){
			logger.error("Invalid ServiceID :["+serviceId+"] Valid ServiceID: ["+apiServiceInstance.getService_id()+"]");
			throw new ServiceBrokerException("Invalid ServiceID :["+serviceId+"] Valid ServiceID: ["+apiServiceInstance.getService_id()+"] - D002");
		}
		//서비스 ID가 DB의 인스턴스 정보와 다른 경우 - D001
		else if(!apiServiceInstance.getPlan_id().equals(planId)){
			logger.error("Invalid PlanID :["+planId+"] Valid PlanID: ["+apiServiceInstance.getPlan_id()+"]");
			throw new ServiceBrokerException("Invalid PlanID :["+planId+"] Valid PlanID: ["+apiServiceInstance.getPlan_id()+"] - D001");
		}
		
	//로그인한다.
		String userId = apiServiceInstance.getUser_id();
		String userPassword = apiServiceInstance.getUser_password();
		String cookie = "";
		
		try {
			cookie = loginService.getLogin(userId, userPassword);
			logger.info("API Platform login success");
		} catch (ServiceBrokerException e) {
			if(e.getMessage().equals("No User")){
			//유저가 API플랫폼에서 삭제 되어있을 경우. 유저를 다시 생성하여, 사용등록 해제와 어플리케이션 삭제를 진행한다.
				//API플랫폼은 유저를 삭제해도 사용등록 정보 등이 남아있기 때문에, 유저를 삭제 하더라도 사용등록을 해제하고 삭제하는 편이 좋다고 생각한다.
				logger.warn("not found API Platform User");
				userSignup(userId, userPassword);
				cookie = loginService.getLogin(userId, userPassword);
//				throw new ServiceBrokerException("API Platfotm user["+userId+"] already removed.");
			}
			else {
				logger.error("APIPlatform User Signup Error");
				throw new ServiceBrokerException("APIPlatform Error :"+e.getMessage());
			}
		}
		
	//getApplication API를 이용하여 어플리케이션 ID를 가져온다.
		HttpEntity<String> httpEntity;
		ResponseEntity<String> responseEntity;
		HttpHeaders headers = new HttpHeaders();
	
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	
		String getApplicationsUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GetApplications");
		String getApplicationParameter = "action=getApplications";
		
		httpEntity = new HttpEntity<String>("", headers);
		responseEntity = HttpClientUtils.send(getApplicationsUri+"?"+getApplicationParameter, httpEntity, HttpMethod.GET);
		
		JsonNode getApplicationsResponseJson = null;
		
		try {
			getApplicationsResponseJson =JsonUtils.convertToJson(responseEntity);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(getApplicationsResponseJson);
			logger.debug("Get Applications completed");
		} catch (Exception e) {
			throw new ServiceBrokerException("APIPlatform Error :"+e.getMessage());
		}
		
		JsonNode applications = getApplicationsResponseJson.get("applications");
		
		int applicationId = 0;
		boolean applicationExists= false;
		for(JsonNode application:applications){
			if(application.get("name").asText().equals(serviceInstanceId)){
				applicationId = application.get("id").asInt();
				applicationExists = true;
				break;
			}
		}
		
	//어플리케이션이 존재하는 경우 Remove a Subscription API를 이용해 사용등록을 해제한다.
		if(applicationExists){
		//API의 공급자 정보를 가져오기 위해 Get Published APIs by Application API를 사용한다.
		//WSO2 공식 문서의 명칭은  Get Published APIs by Application이지만 실제로는 라이프사이클과 무관하게 어플리케이션에 사용등록 된 API의 정보를 가져온다.
			String listAPIsUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GetAPIsByApplication");
			String listAPIsParameters = "action=getSubscriptionByApplication&app="+serviceInstanceId;		
			boolean subscriptionExists =true;
			httpEntity = new HttpEntity<String>("", headers);
			responseEntity = HttpClientUtils.send(listAPIsUri+"?"+listAPIsParameters, httpEntity, HttpMethod.GET);
			
			JsonNode getAPIsResponseJson = null;
			
			try {
				getAPIsResponseJson = JsonUtils.convertToJson(responseEntity);
				ApiPlatformUtils.apiPlatformErrorMessageCheck(getAPIsResponseJson);
			} catch (ServiceBrokerException e) {
				//API사용등록이 정상적으로 처리되어 있는지 확인한다.
				if(e.getMessage().equals("No subscribed API")){
					logger.error("UserID: ["+userId+"], ApplicationName: ["+serviceInstanceId+"], API ["+serviceId+"] dose not exist.");
					//이미 사용등록이 해제 되었다면 사용등록 해제 절차를 건너뛰기 위해 subscriptionExists변수 값을 변경한다. - D006
					subscriptionExists=false;
				}
				else {
					//위의 경우를 제외한 나머지 예외가 발생한 경우
					logger.error("API Platform response error - Get Published APIs by Application");
					throw new ServiceBrokerException(e.getMessage());
				}
			}
			
			//Get Published APIs by Application API를 통해 가져온 API정보에서 공급자명을 찾아 변수에 담는다.
			//라이프사이클이 변경된 경우에도 정상적으로 사용등록을 해제해야하기 때문에 라이프사이클을 따로 확인하지 않는다. - D007
			if(!subscriptionExists){
				String serviceProvider = null;
				JsonNode apis = getAPIsResponseJson.get("apis");
				for(JsonNode api :apis){
					if(serviceId.equals(api.get("apiName").asText()+" "+api.get("apiVersion").asText())){
						serviceProvider = api.get("apiProvider").asText();
						logger.debug("API ["+serviceId+"] found");
						break;
					}
				}
				String serviceName = serviceId.split(" ")[0];
				String serviceVersion = serviceId.split(" ")[1];
				String removeSubscriptionUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.RemoveSubscription");
				String removeSubscriptionParameters = "action=removeSubscription&name="+serviceName+"&version="+serviceVersion+"&provider="+serviceProvider+"&applicationId="+applicationId;
				
				httpEntity = new HttpEntity<String>(removeSubscriptionParameters, headers);
				responseEntity = HttpClientUtils.send(removeSubscriptionUri, httpEntity, HttpMethod.POST);
				
				JsonNode removeSubscriptionResponseJson = null;

				try {
					removeSubscriptionResponseJson =JsonUtils.convertToJson(responseEntity);
					ApiPlatformUtils.apiPlatformErrorMessageCheck(removeSubscriptionResponseJson);
					logger.debug("Remove a Subscription completed");
				} catch(ServiceBrokerException e){
					//Remove a Subscription API를 사용할때, 보내는 파라미터가 적절하지 않은 경우이다.
					if(e.getMessage().equals("invalid parameters")){
						logger.error("Invalid parameters Service name : ["+serviceName+"], Version : ["+serviceVersion+"], API Provider : ["+serviceProvider+"], Application ID : ["+applicationId+"]");
						throw new ServiceBrokerException("API Platform Error : Invalid Parameters Service "+serviceName);
					}
				} catch (Exception e) {
					throw new ServiceBrokerException("APIPlatform Error :"+e.getMessage());
				}
			}
		
		//Remove an Application API로 어플리케이션을 삭제한다.
			
			String removeApplicationUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.RemoveApplication");
			String removeApplicationParameters = "action=removeApplication&application="+serviceInstanceId;
			
			httpEntity = new HttpEntity<String>(removeApplicationParameters, headers);
			responseEntity = HttpClientUtils.send(removeApplicationUri, httpEntity, HttpMethod.POST);
			
			JsonNode removeApplicationResponseJson = null;
			
			try {
				removeApplicationResponseJson =JsonUtils.convertToJson(responseEntity);
				ApiPlatformUtils.apiPlatformErrorMessageCheck(removeApplicationResponseJson);
				logger.debug("Remove an Application completed");
			} catch(ServiceBrokerException e){
				//어플리케이션이 없거나 파라미터가 잘못 들어간 경우이다.
				if(e.getMessage().equals("No Application")){
					logger.debug("not found Application :"+serviceInstanceId);
				}
				
			} catch (Exception e) {
				throw new ServiceBrokerException("APIPlatform Error :"+e.getMessage());
			}
		}
		//API플랫폼에 해당 어플리케이션이 존재하지 않는 경우이다.
		//디프로비전 절차 자체가 어플리케이션 삭제이기 때문에 어플리케이션이 존재하지 않는다면 특별한 작업을 하지않고 DB만 변경한다. - D005
		else {		
			logger.warn("APIPlatform Application not Exists.");
		}
		if(serviceInstanceId.contains("test")){
			//테스트 진행을 위해 인스턴스 아이디에 test라는 단어가 포함되어 있으면 DB변경을 건너뛴다.
			//TODO 최종적으로는 반드시 삭제
		}
		else{
			//DB에서 서비스 인스턴스 정보를 삭제상태로 변경한다.
			String oranizationGuid = apiServiceInstance.getOrganization_id();
			try {
				dao.deleteAPIServiceInstance(oranizationGuid, serviceInstanceId, serviceId, planId);			
			} catch (Exception e) {
				logger.error("Database Error - deleteAPIServiceInstance");
				System.out.println(e.getMessage());
			}
		}

		
		return new ServiceInstance(request);
	}
	
//서비스 인스턴스 정보를 가져온다.
	@Override
	public ServiceInstance getServiceInstance(String serviceInstanceId) throws ServiceBrokerException
	{
		APIServiceInstance apiServiceInstance = new APIServiceInstance();		
		CreateServiceInstanceRequest request = new CreateServiceInstanceRequest();
	
		try {
			apiServiceInstance=dao.getAPIServiceByInstance(serviceInstanceId);
			logger.debug("get API Information");
			//이미 delete명령으로 제거한 서비스인스턴스의 인스턴스 아이디로 요청이 들어온 경우 - U009
			//TODO 삭제된 인스턴스를 DB에서 어떻게 처리할지에 대해서 결정이 되면 수정
			if(apiServiceInstance.getDelyn().equals("Y")){
				logger.info("Service Instance : ["+serviceInstanceId+"] already removed - U009");
				throw new ServiceBrokerException("already removed Service Instance :["+serviceInstanceId+"] - U009");
			}
		} catch (EmptyResultDataAccessException e) {
			//존재하지 않는 인스턴스 아이디가 요청된 경우 - U002
			logger.error("not found information about instance ID :["+serviceInstanceId+"] - U002");
			return null;
		} catch (Exception e){
			logger.error("Database Error - getServiceInstanceByInstanceID()");
			throw new ServiceBrokerException(e.getMessage());
		}
		String organizationGuid = apiServiceInstance.getOrganization_id();
		String spaceId = apiServiceInstance.getSpace_guid();
		String serviceId = apiServiceInstance.getService_id();
		String planId = apiServiceInstance.getPlan_id();
		
		request = new CreateServiceInstanceRequest(serviceId,planId,organizationGuid,spaceId);
		
		ServiceInstance instance = new ServiceInstance(request.withServiceInstanceId(serviceInstanceId));
		logger.info("get Service Instance completed InstanceId : ["+serviceInstanceId+"] ServiceId : ["+serviceId+"]");
		
		return instance;
	}

	
//서비스 인스턴스의 플랜정보를 업데이트 한다.
	@Override
	public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest request) throws ServiceInstanceUpdateNotSupportedException,
			ServiceBrokerException, ServiceInstanceDoesNotExistException 
	{
		String serviceInstanceId = request.getServiceInstanceId();
		String planId = request.getPlanId();
		String planName = planId.split(" ")[2];
		APIServiceInstance apiServiceInstance = null;

	//요청된 서비스 인스턴스에 대해 DB에 저장된 정보를 가져온다.
		try {
			apiServiceInstance =dao.getAPIInfoByInstanceID(serviceInstanceId);			
		} catch (EmptyResultDataAccessException e) {
			logger.error("not found InstanceID : "+serviceInstanceId);
			try {
				apiServiceInstance = dao.getAPIServiceByInstance(serviceInstanceId);				
			} catch (EmptyResultDataAccessException e2) {
				//DB에서 서비스 인스턴스 정보를 찾을 수 없는 케이스 - UP09
				logger.error("Service Instnace information not found. ServiceInstanceID :["+serviceInstanceId+"] - UP09");
				throw new ServiceBrokerException("Service Instnace information not found. ServiceInstanceID :["+serviceInstanceId+"] - UP09");
			}
			String organizationGuid = apiServiceInstance.getOrganization_id();
			try {
				dao.getAPIUserByOrgID(organizationGuid);
			} catch (EmptyResultDataAccessException e2) {
				//DB에서 유저정보가 삭제된 케이스 - UP06
				logger.error("APIPlatform user information not found. OrganizationGUID :["+organizationGuid+"] - UP06");
				throw new ServiceBrokerException("APIPlatform user information not found. OrganizationGUID :["+organizationGuid+"] - UP06");
			}
		} catch (Exception e) {
			logger.error("Database Error - getAPIInfoByInstanceID");
			throw new ServiceBrokerException(e.getMessage());
		}
		
		String serviceId = apiServiceInstance.getService_id();
		//DB에 저장된 플랜 아이디와 요청된 플랜아이디가 같을 경우 예외를 발생시킨다. - UP02
		if(apiServiceInstance.getPlan_id().equals(planId)){
			logger.info("already setted plan requseted");
			throw new ServiceBrokerException("Plan :["+planName+"] already setted");
		}
		//요청된 플랜아이디가 적절한지 확인한다.
		else if(!serviceId.equals(planId.split(" ")[0]+" "+planId.split(" ")[1])){
			//플랜아이디에 포함된 서비스 명과 버전이 DB에 저장된 서비스 아이디의 서비스명, 버전과 다른 경우 예외를 발생시킨다.
			logger.debug("invalid PlanID :["+planId+"]");
			throw new ServiceBrokerException("invalid PlanID :["+planId+"]. valid PlanID :["+serviceId+" 'Valid PlanName']");
		}
/*		
//	플랜의 업데이트 가능 여부를 확인한다.
		String planUpdatable;
		planUpdatable = env.getProperty("PlanUpdatable");
		
		//서비스 브로커의 플랜 업데이트 설정이 되어있지 않다면, 예외를 발생시킨다. 		
		if (!planUpdatable.equals("yes")){
			logger.info("Service Broker plan update not allowed");
			throw new ServiceInstanceUpdateNotSupportedException("Service Plan not updatable. Service Broker Updatable setting : ["+planUpdatable+"]");	
		}
	
*/	
	//업데이트 요청이 들어온 플랜명이 사용가능한 플랜명인지 확인한다.
		boolean planAvailable = false;
		String nextPlan = null;
		int i =1;
		do{
			if(env.getProperty("AvailablePlan"+i).equals(planName)){
				planAvailable =true;
				logger.debug("Plan :["+planName+"] availbale");
			}
			else {
				i++;
				nextPlan= env.getProperty("AvailablePlan"+i);
			}
		} while(planAvailable==false&&nextPlan!=null);
		
		//요청된 플랜아이디에 포함된 플랜명이 설정되어 있지 않은 플랜명인 케이스 - UP03
		if(!planAvailable){
			logger.error("not supported Plan :["+planName+"]");
			throw new ServiceInstanceUpdateNotSupportedException("not supported Plan :["+planName+"] - UP03");
		}
		
	//API플랫폼 어플리케이션의 티어값을 업데이트한다.
		//요청된 인스턴스에 대한 API플랫폼 유저아이디와 패스워드로 API플랫폼에 로그인한다.
		String userId = apiServiceInstance.getUser_id();
		String userPassword = apiServiceInstance.getUser_password();
		String cookie = "";
		try {
			cookie = loginService.getLogin(userId, userPassword);						
		} catch (ServiceBrokerException e) {
			if(e.getMessage().equals("No User")){
				//DB에 저장된 유저정보로 API플랫폼의 유저를 생성하고 로그인하여 쿠키값을 저장한다.
				userSignup(userId,userPassword);
				cookie = loginService.getLogin(userId, userPassword);
				logger.info("API Platform Login - UserID : ["+userId+"]");
			}
		} catch (Exception e) {
			logger.info("API Platform login error - UserID : ["+userId+"]");
			throw new ServiceBrokerException(e.getMessage());
		}

		//Update an Application API를 사용한다.
		HttpEntity<String> entity;
		ResponseEntity<String> response;
		HttpHeaders headers = new HttpHeaders();
		
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		String updateApplicationUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.UpdateApplication");
		String updateApplicationParameters = "action=updateApplication&applicationOld="+serviceInstanceId+"&applicationNew="+serviceInstanceId+"&callbackUrlNew=&descriptionNew=&tier="+planName;
		
		entity = new HttpEntity<String>(updateApplicationParameters, headers);
		response = HttpClientUtils.send(updateApplicationUri, entity, HttpMethod.POST);
		
		JsonNode updateApplicationResponseJson = null;
		
		try {
			updateApplicationResponseJson =JsonUtils.convertToJson(response);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(updateApplicationResponseJson);
			
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
		logger.debug("Update an Application completed.");
		
		//Update an Application API는 에러메시지를 보내지 않기 때문에 Get Apllications API를 사용하여 API플랫폼에서 플랜업데이트가 정상적으로 이루어졌는지 확인한다. 
		String getApplicationsUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GetApplications");
		String getApplicationParameter = "action=getApplications";
		
		entity = new HttpEntity<String>("", headers);
		response = HttpClientUtils.send(getApplicationsUri+"?"+getApplicationParameter, entity, HttpMethod.GET);
		
		JsonNode getApplicationsResponseJson = null;
		
		try {
			getApplicationsResponseJson =JsonUtils.convertToJson(response);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(getApplicationsResponseJson);
			logger.debug("Get Applications completed");
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}

		
		//Json객체를 통해 API플랫폼에 등록된 어플리케이션과 플랜값이 업데이트를 요청한 어플리케이션, 플랜값과 일치하는지 확인한다.
		JsonNode applications =getApplicationsResponseJson.get("applications");
		String applicationName = null;
		String applicationTier=null;
		int applicationId = 0;
		boolean applicationExists=false;
		boolean applicationUpdateSuccess =false;
		for(JsonNode application: applications){
			applicationName = application.get("name").asText();
			if(applicationName.equals(serviceInstanceId)){
				applicationTier = application.get("tier").asText();
				applicationId = application.get("id").asInt();
				if(applicationTier.equals(planName)){
					applicationUpdateSuccess = true;
					break;
				}
				else{
					applicationExists=true;
					break;
				}
			}
		}
		
	//API의 Tier를 업데이트하는 기능이 없으므로, 사용등록을 해제하고 다시 사용등록을 한다.
		//API의 공급자 정보를 가져오기 위해 Get Published API를 사용한다.
		String getApisUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GetAllPaginatedPublishedAPIs");
		String getApisParameters ="action=getAllPaginatedPublishedAPIs&tenant=carbon.super&start=1&end=100";
		
		entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> getApisResponseHttp;
		
		getApisResponseHttp = HttpClientUtils.send(getApisUri+"?"+getApisParameters, entity, HttpMethod.GET);
		
		JsonNode getApisResponseJson = null;
		
		try {
			getApisResponseJson = JsonUtils.convertToJson(getApisResponseHttp);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(getApisResponseJson);
		} catch (Exception e) {			
			throw new ServiceBrokerException(e.getMessage());
		}
		
		JsonNode apis = getApisResponseJson.get("apis");
		String serviceName = serviceId.split(" ")[0];
		String serviceProvider = null;
		for (JsonNode api : apis) {
			if(api.get("name").asText().equals(serviceName)){
				serviceProvider = api.get("provider").asText();
				break;
			}
		}
	
		//API의 사용등록을 해제한다.
		String serviceVersion = serviceId.split(" ")[1];
		String removeSubscriptionUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.RemoveSubscription");
		String removeSubscriptionParameters = "action=removeSubscription&name="+serviceName+"&version="+serviceVersion+"&provider="+serviceProvider+"&applicationId="+applicationId;
		
		entity = new HttpEntity<String>(removeSubscriptionParameters, headers);
		response = HttpClientUtils.send(removeSubscriptionUri, entity, HttpMethod.POST);
		
		JsonNode removeSubscriptionResponseJson = null;

		try {
			removeSubscriptionResponseJson =JsonUtils.convertToJson(response);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(removeSubscriptionResponseJson);
		} catch(ServiceBrokerException e){
			throw new ServiceBrokerException("APIPlatform Error :"+e.getMessage());
		}
		logger.debug("Remove a Subscription completed");
		
		//Add Subscription API를 사용하여 API플랫폼 어플리케이션에 API를 사용등록한다.

		logger.info("API Platform Add a Subscription started");
		String addSubscriptionUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.AddSubscription");
		String addSubscriptionParameters = 
				"action=addAPISubscription&name="+serviceName+"&version="+serviceVersion+"&provider="+serviceProvider+"&tier="+planName+"&applicationName="+serviceInstanceId;
		
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		entity = new HttpEntity<String>(addSubscriptionParameters, headers);

		ResponseEntity<String> addSubscriptionResponseHttp = HttpClientUtils.send(addSubscriptionUri, entity, HttpMethod.POST);
		
		JsonNode addSubscriptionResponseJson = null;

		try {
			addSubscriptionResponseJson = JsonUtils.convertToJson(addSubscriptionResponseHttp);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(addSubscriptionResponseJson);	
		} catch (ServiceBrokerException e){
			logger.error("API Platform response error - Add a Subscription");
			throw new ServiceBrokerException(e.getMessage());
		}
		logger.info("API Platform Subscription finished");
		
	
		//API플랫폼에 해당 어플리케이션이 존재하지 않거나 요청된 플랜명을 API플랫폼의 티어에서 찾을 수 없는 경우이다.
		if(!applicationUpdateSuccess) {
			//어플리케이션은 생성되어 있으나 업데이트가 정상적으로 이루어지지 않은 케이스이다. 서비스 브로커에서 설정한 플랜명을 APIPlatform Tier 목록에서 찾을 수 없는 경우 발생할 수 있다.
			if(applicationExists){
				logger.error("APIPlatform update failed. ApplicationName :["+serviceInstanceId+"], PlanName :["+planName+"]" );
				throw new ServiceBrokerException("APIPlatform update failed. ApplicationName :["+serviceInstanceId+"],Current PlanName :["+applicationTier+"], Requested PlanName :["+planName+"]");
			} 
			//인스턴스 아이디에 해당하는 어플리케이션이 삭제된 경우
			else {
				logger.error("APIPlatform application not found. UserId :["+userId+"], Application :["+serviceInstanceId+"]");
				//요청이 들어온 플랜으로 어플리케이션을 생성한다. 
				String addApplicationUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.AddAnApplication");
				String addApplicationParameters = 
						"action=addApplication&application="+serviceInstanceId+"&tier="+planName+"&description=&callbackUrl=";
				entity = new HttpEntity<String>(addApplicationParameters, headers);

				response= HttpClientUtils.send(addApplicationUri, entity, HttpMethod.POST);
				
				JsonNode addApplicationResponseJson = null;

				try {
					addApplicationResponseJson = JsonUtils.convertToJson(response);
					ApiPlatformUtils.apiPlatformErrorMessageCheck(addApplicationResponseJson);
				} catch (ServiceBrokerException e) {
					logger.error("API Platform response error - Add an Application");
					throw new ServiceBrokerException(e.getMessage());
				}
				logger.info("Application "+serviceInstanceId+" created");	
				
				//Generate Key API를 사용하여 API플랫폼 어플리케이션의 키값을 생성한다.
				logger.info("API Platform Generate Key Started. Application : "+serviceInstanceId);
				//두가지 키타입으로 키를 생성해야하기 때문에 두번 실행한다.
				for(i=1;i<3;i++) {	
					logger.debug("API Platform Generate Key Started - KEYTYPE : "+env.getProperty("Keytype"+i));
					headers.set("Cookie", cookie);
					headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
					String generateKeyUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GenerateAnApplicationKey");
					String generateKeyParameters = 
							"action=generateApplicationKey&application="+serviceInstanceId+"&keytype="+env.getProperty("Keytype"+i)+"&callbackUrl=&authorizedDomains=ALL&validityTime=360000";
				
					entity = new HttpEntity<String>(generateKeyParameters, headers);
					response = HttpClientUtils.send(generateKeyUri, entity, HttpMethod.POST);
			
					JsonNode generateKeyResponseJson = null;
					
					try {
						generateKeyResponseJson = JsonUtils.convertToJson(response);
						ApiPlatformUtils.apiPlatformErrorMessageCheck(generateKeyResponseJson);
						logger.debug(env.getProperty("Keytype"+i)+" Key generated. Application : "+serviceInstanceId);
					} catch (ServiceBrokerException e) {
						if(e.getMessage().equals("Key already generated"))
						{
							logger.debug(env.getProperty("Keytype"+i)+" Key already generated");
						} else {
							logger.error("API Platform response error - Generate Key KEYTYPE : "+env.getProperty("Keytype"+i));
							throw new ServiceBrokerException(e.getMessage());					
						}
						logger.debug("API Platform Generate Key finished - KEYTYPE : "+env.getProperty("Keytype"+i));
					}
				}
				logger.info("API Platform Generate Key finished. Application : "+serviceInstanceId);

			//Add Subscription API를 사용하여 API플랫폼 어플리케이션에 API를 사용등록한다.

				logger.info("API Platform Add a Subscription started");
				addSubscriptionUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.AddSubscription");
				addSubscriptionParameters = 
						"action=addAPISubscription&name="+serviceName+"&version="+serviceVersion+"&provider="+serviceProvider+"&tier="+planName+"&applicationName="+serviceInstanceId;
				
				headers.set("Cookie", cookie);
				headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				entity = new HttpEntity<String>(addSubscriptionParameters, headers);

				response = HttpClientUtils.send(addSubscriptionUri, entity, HttpMethod.POST);
				
				addSubscriptionResponseJson = null;

				try {
					
					addSubscriptionResponseJson = JsonUtils.convertToJson(response);
					ApiPlatformUtils.apiPlatformErrorMessageCheck(addSubscriptionResponseJson);
					//사용등록이 정상적으로 이루어진 경우
				} catch (ServiceBrokerException e){
					logger.error("API Platform response error - Add a Subscription");
					throw new ServiceBrokerException(e.getMessage());
				}
				logger.info("API Platform Subscription finished");
				
			}
		}
		
		//DB에 수정된 정보를 업데이트 한다.
		try {
			dao.updateAPIServiceInstance(serviceInstanceId, planId);
			logger.debug("updateAPIServiceInstance() completed. InstanceID : "+serviceInstanceId+" Plan : "+planId);
		} catch (Exception e) {
			logger.error("Database Error - updateAPIServiceInstance");
			throw new ServiceBrokerException("Database Error - updateAPIServiceInstance");
		}
		
		String oldPlanName = apiServiceInstance.getPlan_id().split(" ")[2];
		logger.info("Plan changed. InstanceID : ["+serviceInstanceId+"] Plan ["+oldPlanName+"] to ["+planName+"]");

		ServiceInstance instance = new ServiceInstance(request);
		logger.info("Update ServiceInstance completed. InstanceID : "+serviceInstanceId+" Plan : "+planId);
		return instance;
	}


//API 플랫폼의 유저생성을 위해 30자 이내의 유니크 유저아이디를 생성 
	private String makeUserId(){
		
		logger.debug("Start makeUserId()");
		
		String userId = UUID.randomUUID().toString().replace("-", "").substring(0, 30); 
		//TODO 30자 내로 유니크 문자를  얻을 수 있는 다른 방법을 찾아본다.
		
		return userId;
	}
	
//API플랫폼의 유저를 생성하고  아이디 중복여부를 판단
	public boolean userSignup(String userId, String userPassword) throws ServiceBrokerException{
		logger.info("API Platform UserSignup started");
		String cookie = "";
		boolean duplication= false;
		
		HttpHeaders headers = new HttpHeaders();	
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		String signupUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.UserSignup");
		String signupParameters = "action=addUser&username="+userId+"&password="+userPassword+"&allFieldsValues=";		
		
		HttpEntity<String> httpEntity = new HttpEntity<String>(signupParameters, headers);

		ResponseEntity<String> responseEntity = HttpClientUtils.send(signupUri, httpEntity, HttpMethod.POST);
		
		JsonNode userSignupResponseJson = null;
		
		try {
			userSignupResponseJson = JsonUtils.convertToJson(responseEntity);
			
		//API플랫폼의 유저아이디를 생성하면서, 해당 아이디의 존재여부를 판단한다.
			if (userSignupResponseJson.get("error").asText()!="false") { 
				String apiPlatformMessage = userSignupResponseJson.get("message").asText();
				
				logger.info("API Platform Message : "+apiPlatformMessage);
				
				if(apiPlatformMessage.equals("User name already exists")){
					duplication = true;
					return duplication;
				}
				else{
					logger.error("API Platform response error - User Signup");
					throw new ServiceBrokerException(userSignupResponseJson.get("message").asText());
				}
			}
			
			ApiPlatformUtils.apiPlatformErrorMessageCheck(userSignupResponseJson);
		} catch (ServiceBrokerException e) {			
			throw new APIServiceInstanceException(e.getMessage());
		}
		logger.debug("API Platform User created");
		return duplication;
	}

}
