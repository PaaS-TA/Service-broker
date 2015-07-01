package org.openpaas.servicebroker.apiplatform.service.impl;

import java.util.UUID;



import org.openpaas.servicebroker.apiplatform.common.ApiPlatformUtils;
import org.openpaas.servicebroker.apiplatform.dao.APIServiceInstanceDAO;
import org.openpaas.servicebroker.apiplatform.exception.APIServiceInstanceException;
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
	public ServiceInstance createServiceInstance(CreateServiceInstanceRequest createServiceInstaceRequest) throws ServiceInstanceExistsException, ServiceBrokerException 
	{
		ServiceInstance instance = new ServiceInstance(createServiceInstaceRequest);
		logger.info("create ServiceInstance");
		
		String organizationGuid =instance.getOrganizationGuid();
		String serviceInstanceId = instance.getServiceInstanceId();
		String spaceGuid = instance.getSpaceGuid();
		String serviceDefinitionId = instance.getServiceDefinitionId();
		String planId = instance.getPlanId();
		String cookie = "";

	//서비스 브로커의 DB에서 organizationGuid를 확인하여 API플랫폼 로그인 아이디와 비밀번호를 획득한다.
		
		APIUser userInfo=null;
		try{
			userInfo = dao.getAPIUserByOrgID(organizationGuid);		
		} catch(EmptyResultDataAccessException e){
			logger.info("Could not found User at APIPlatform Database");
		}
		
		String userId = userInfo.getUser_id();
		String userPassword = userInfo.getUser_password();
		
	//API플랫폼에 등록되지 않은 유저일때, 유저아이디를 생성하여 등록한다.
		boolean userIdDuplication;

		if(userId==null){
			logger.info("not registered API Platform User");
			userPassword=env.getProperty("UserSignupPassword");
			
			//API플랫폼에서 유저아이디의 중복여부를 확인하여, 중복인 경우 유저아이디를 재생성하여 등록한다.
			do{
				userId=makeUserId();
				userIdDuplication=userSignup(userId,userPassword);
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
		
	//DB에서 요청된 서비스 인스턴스 Id의 중복여부를 확인하기 위해, 해당 서비스 인스턴스 ID를 Count한다. 
		int instanceIdCount=dao.getServiceInstanceIdCount(serviceInstanceId);
		
	//Add An Application API를 사용한다. error:false와 status: APPROVED가 리턴되어야 정상
		boolean applicationExsists = false;
		
		logger.info("Add An Application API Start");
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		//Add an Application API를 보낸다.
		String planName = env.getProperty("APIPlatformApplicationPlan");
		String addApplicationUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.AddAnApplication");
		String addApplicationParameters = 
				"action=addApplication&application="+serviceInstanceId+"&tier="+planName+"&description=&callbackUrl=";		
		HttpEntity<String> httpEntity = new HttpEntity<String>(addApplicationParameters, headers);
		
/*		인스턴스 아이디를 이름으로 하는 API플랫폼 어플리케이션을 생성하는 API를 보낸다. 응답되는 에러메세지를 통해 해당 어플리케이션의 존재여부를 확인한다.
		어플리케이션이 생성되어있다면, API플랫폼이 에러메시지를 보낼 것이고, 생성되어 있지 않다면 생성을 해야하기 때문에 어떤 경우인지를 구분하지 않고
		 먼저 Add an Application API를 보낸다. */
		ResponseEntity<String> responseEntity = HttpClientUtils.send(addApplicationUri, httpEntity, HttpMethod.POST);
		
		JsonNode addApplicationResponseJson = null;

		
	//인스턴스 아이디가 DB에 존재하는 경우와 하지 않는 경우로 분기를 나누어 진행한다. 
		//인스턴스 아이디가 존재하는 경우이다.
		if(instanceIdCount!=0){
			try {
				addApplicationResponseJson = JsonUtils.convertToJson(responseEntity);
				ApiPlatformUtils.apiPlatformErrorMessageCheck(addApplicationResponseJson);
				
				//어플리케이션이 정상적으로 생성 되었다면 넘어간다.
				logger.info("Application "+serviceInstanceId+" created");
				
			} catch (ServiceBrokerException e) {
				if(e.getMessage().equals("Duplication")){
					//해당 어플리케이션 명이 존재하는 경우이다. 이후의 분기를 나누기 위해 applicationExsists값을 변경한다.
					logger.info("application already exists");
					applicationExsists = true;
				} else{
					//그밖의 예외처리
					logger.error("API Platform response error - Add an Application");
					throw new ServiceBrokerException(e.getMessage());
				}
			}
		}
		
		else{
			try {
				addApplicationResponseJson = JsonUtils.convertToJson(responseEntity);
				ApiPlatformUtils.apiPlatformErrorMessageCheck(addApplicationResponseJson);
				applicationExsists = true;
			} catch (ServiceBrokerException e) {
				logger.error("API Platform response error - Add an Application");
				throw new ServiceBrokerException(e.getMessage());
			}
			logger.info("Application "+serviceInstanceId+" created");		
		}
		logger.info("API Platform Application added");
		
	//Generate Key API를 사용하여 API플랫폼 어플리케이션의 키값을 생성한다.
		logger.info("API Platform Generate Key Started");
		logger.info("ServiceInstanceID: "+serviceInstanceId);
		
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		String generateKeyUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GenerateAnApplicationKey");
		String generateKeyParameters = 
				"action=generateApplicationKey&application="+serviceInstanceId+"&keytype=PRODUCTION&callbackUrl=&authorizedDomains=ALL&validityTime=360000";
	
		httpEntity = new HttpEntity<String>(generateKeyParameters, headers);
		
		responseEntity = HttpClientUtils.send(generateKeyUri, httpEntity, HttpMethod.POST);

		JsonNode generateKeyResponseJson = null;
		
		try {
			generateKeyResponseJson = JsonUtils.convertToJson(responseEntity);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(generateKeyResponseJson);
		} catch (ServiceBrokerException e) {
			logger.error("API Platform response error - Generate Key");
			throw new ServiceBrokerException(e.getMessage());
		}
		logger.info("API Platform Key generated finished");
		

	//Add Subscription API를 사용하여 API플랫폼 어플리케이션에 API를 사용등록한다.	
		
		//Add Subscription API 사용에 필요한 값들을 찾아 변수에 담는다.
		String serviceName = serviceDefinitionId.split(" ")[0];
		String serviceVersion = serviceDefinitionId.split(" ")[1];
		String serviceProvider = apiCatalogService.svc.getMetadata().get("providerDisplayName").toString();
		
		//인스턴스 아이디가 이미 존재하고 해당 인스턴스 아이디로 API플랫폼의 어플리케이션이 존재하는 경우에는 사용등록된 서비스를 먼저 확인한다.
		boolean subscriptionExists=false;
		if(instanceIdCount!=0&&applicationExsists){
			
			//Get APIs By Application API를 사용해 해당 어플리케이션에 사용등록된 API를 확인한다.
			logger.info("Get APIs By Application API started");
			String getAPIsByApplicationUri =  env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GetAPIsByApplication");
			String getAPIsByApplicationParameters = "action=getSubscriptionByApplication&app="+serviceInstanceId;
			
			headers.set("Cookie", cookie);
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			httpEntity = new HttpEntity<String>("", headers);
			
			responseEntity = HttpClientUtils.send(getAPIsByApplicationUri+"?"+getAPIsByApplicationParameters, httpEntity, HttpMethod.GET);
			
			JsonNode getAPIsByApplicationJson =null;
			
			try {
				getAPIsByApplicationJson = JsonUtils.convertToJson(responseEntity);
				ApiPlatformUtils.apiPlatformErrorMessageCheck(getAPIsByApplicationJson);
			} catch (ServiceBrokerException e) {
				logger.error("API Platform response error - Get APIs By Application");
				throw new ServiceBrokerException(e.getMessage());
			}
			logger.info("Get APIs By Application API finished");
			
			JsonNode apis = getAPIsByApplicationJson.get("apis");

			
			//현재 인스턴스 생성 요청이 들어온 서비스와 어플리케이션에 사용등록이 확인 될 경우, DB확인 단계로 넘어간다.
			for (JsonNode api : apis){
				if(api.get("apiName").asText().equals(serviceName)){
					logger.info("Service already subscribed - Service Name : "+serviceName);
					subscriptionExists=true;
					break;
				}
			}	
			//현재 인스턴스 생성 요청이 들어온 서비스와 어플리케이션에 사용등록이 되지 않은 경우, 409 예외를 발생시킨다.
			if(!subscriptionExists){
				logger.info("not subscribed Service - Application Name : "+serviceInstanceId+", Service Name : "+serviceName);
				throw new ServiceInstanceExistsException(instance);
			}
		}

		//인스턴스 아이디와 동일한 어플리케이션이 존재하지 않는 경우이다. 일반적인 경우이다.
		else {
	
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
			} catch (ServiceBrokerException e){
				logger.error("API Platform response error - Add a Subscription");
				throw new ServiceBrokerException(e.getMessage());
			}
			logger.info("API Platform Subscription finished");
		}
		

		//요청한 서비스가 instanceID와 같은 이름의 어플리케이션에 정확히 사용등록이 되어있을 경우
		if(subscriptionExists){
			//DB에서 서비스 인스턴스의 중복여부를 체크한다. subscriptionExists
			
			int serviceCount= dao.getServiceCount(organizationGuid,serviceInstanceId,serviceDefinitionId,planId);
			
			if(serviceCount!=0)
			{
				//orgID,인스턴스ID,서비스ID,플랜ID가 모두 정상적으로 DB에 저장되어 있는 경우이다.
				//API플랫폼에서 어플리케이션 생성과 API사용등록, DB저장 정보까지 해당 파라미터로 정상적으로 이루어져있음을 확인하였고
				//해당 정보로 DB에도 정상적으로 저장되어 있음을 확인했기 때문에 status를 200 OK로 보낸다. 

				logger.info("Service instance already exists");
				instance.setHttpStatusOK();
				return instance;
			}
			else {
				//DB에 저장된 서비스 인스턴스 정보에서 서비스와 org가 동일하나 플랜이 다른경우
				logger.info("Service instance already exists. requested invalid Plan ID");
				throw new ServiceBrokerException("invalid Plan ID");
			}

		}
		else if(instanceIdCount!=0){
			throw new ServiceBrokerException("Instance ID duplicate");
		}
		
		else {	
		//API서비스 인스턴스 정보를 API플랫폼 DB에 저장한다.
			try {
				dao.insertAPIServiceInstance(organizationGuid,serviceInstanceId,spaceGuid,serviceDefinitionId,planId);
			} catch (Exception e) {
				logger.error("API Service Instance insert failed");
				throw new APIServiceInstanceException("Database Error: API ServiceInstance insert");		
				
			}
			logger.info("API Service Instance insert finished");
		}
		
		logger.info("createServiceInstance() completed");
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
	private boolean userSignup(String userId, String userPassword) throws ServiceBrokerException{
		logger.info("API Platform UserSignup started");
		String cookie = "";
		boolean duplication= false;
		
		HttpHeaders headers = new HttpHeaders();	
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		String signupUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.UserSignup");
		String parameters = "action=addUser&username="+userId+"&password="+userPassword+"&allFieldsValues=";		
		
		HttpEntity<String> entity = new HttpEntity<String>(parameters, headers);

		ResponseEntity<String> responseEntity = HttpClientUtils.send(signupUri, entity, HttpMethod.POST);
		
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
					throw new APIServiceInstanceException(userSignupResponseJson.get("message").asText());
				}
			}
			
			ApiPlatformUtils.apiPlatformErrorMessageCheck(userSignupResponseJson);
		} catch (ServiceBrokerException e) {			
			throw new APIServiceInstanceException(e.getMessage());
		}
		logger.debug("API Platform User created");
		return duplication;
	}
	
	
	
	
	
	
	@Override
	public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest arg0) throws ServiceBrokerException 
	{

		return null;
	}

	@Override
	public ServiceInstance getServiceInstance(String arg0) 
	{
		return null;
	}

	@Override
	public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest arg0) throws ServiceInstanceUpdateNotSupportedException,
			ServiceBrokerException, ServiceInstanceDoesNotExistException 
	{
		
		return null;
	}

}
