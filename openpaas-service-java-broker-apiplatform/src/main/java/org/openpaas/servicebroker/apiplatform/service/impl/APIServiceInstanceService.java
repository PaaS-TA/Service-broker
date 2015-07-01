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
		
		String planName = env.getProperty("APIPlatformApplicationPlan");
		
		boolean instanceExsists;
		boolean instanceIdExsists;
		boolean insertAPIUserResult;
		boolean insertAPIServiceInstanceResult;
		boolean subscriptionExists=false;
		
	//서비스 브로커의 DB에서 organizationGuid를 확인하여 API플랫폼 로그인 아이디와 비밀번호를 획득한다.
		APIUser userInfo = dao.getAPIUserByOrgID(organizationGuid);
		
		String userId = userInfo.getUser_id();
		String userPassword = userInfo.getUser_password();
		
	//API플랫폼에 등록되지 않은 유저일때, 유저아이디를 생성하여 등록한다.
		boolean userIdDuplication;
		
		if(userInfo.getUser_id()==null){
			logger.info("not registered API Platform User");
			userPassword=env.getProperty("UserSignupPassword");
			
			//API플랫폼에서 유저아이디의 중복여부를 확인하여, 중복인 경우 유저아이디를 재생성하여 등록한다.
			do{
				userId=makeUserId();
				userIdDuplication=userSignup(userId,userPassword);
				logger.info("API Platform User Duplication Check");
			} while(userIdDuplication);
			
			logger.info("API Platform User Created userId : "+userId);
			insertAPIUserResult=dao.insertAPIUser(organizationGuid, userId, userPassword);
			
			if(!insertAPIUserResult){
				logger.error("API User insert failed");
				throw new APIServiceInstanceException("Database Error");		
			}
			logger.info("APIUser insert finished");
		}
		
	//Add An Application API를 사용한다. error:false와 status: APPROVED가 리턴되어야 정상
		boolean applicationExsists = false;
		logger.info("Add An Application API Start");
		
		//AIP플랫폼에 로그인한다.
		String cookie = "";
		
		logger.info("API Platform Login - UserID : "+userId);
		cookie = loginService.getLogin(userId, userPassword);
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	
		String addApplicationUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.AddAnApplication");
		String addApplicationParameters = 
				"action=addApplication&application="+serviceInstanceId+"&tier="+planName+"&description=&callbackUrl=";		
		HttpEntity<String> httpEntity = new HttpEntity<String>(addApplicationParameters, headers);
		
		ResponseEntity<String> responseEntity = HttpClientUtils.send(addApplicationUri, httpEntity, HttpMethod.POST);
		
		JsonNode addApplicationResponseJson = null;
		
		//DB에서 서비스 인스턴스 Id의 중복여부를 확인한다.
		instanceIdExsists=dao.serviceInstanceIdDuplicationCheck(serviceInstanceId);
		
	//인스턴스 아이디가 DB에 존재하는 경우와 하지 않는 경우로 분기를 나누어 진행한다. 
		//인스턴스 아이디가 존재하는 경우이다.
		if(instanceIdExsists){
			try {
				addApplicationResponseJson = JsonUtils.convertToJson(responseEntity);
				ApiPlatformUtils.apiPlatformErrorMessageCheck(addApplicationResponseJson);
				//어플리케이션이 정상적으로 생성 되었다면 넘어간다.
				logger.info("Application "+serviceInstanceId+" created");
			} catch (ServiceBrokerException e) {
				if(e.getMessage().equals("Duplication")){
					//해당 어플리케이션 명이 존재
					logger.info("application already exists");
					applicationExsists = true;
				} else{
					//그밖의 예외처리
					logger.error("API Platform response error - Add an Application");
					throw new ServiceBrokerException(e.getMessage());
				}
			}
		}
		
		//인스턴스 아이디가 DB에 존재하지 않는 경우이다. 일반적인 상황이므로 어플리케이션을 생성한다.
		else{
			try {
				addApplicationResponseJson = JsonUtils.convertToJson(responseEntity);
				ApiPlatformUtils.apiPlatformErrorMessageCheck(addApplicationResponseJson);
				applicationExsists = true;
			} catch (ServiceBrokerException e) {
				
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
		if(instanceIdExsists&&applicationExsists){
			
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
			instanceExsists=dao.serviceInstanceDuplicationCheck(organizationGuid,serviceInstanceId,serviceDefinitionId,planId);
			if(instanceExsists)
			{
				//orgID,인스턴스ID,서비스ID,플랜ID가 모두 정상적으로 DB에 저장되어 있는 경우이다.
				//API플랫폼에서 어플리케이션 생성과 API사용등록, DB저장 정보까지 해당 파라미터로 정상적으로 이루어져있음을 확인하였고
				//해당 정보로 DB에도 정상적으로 저장되어 있음을 확인했기 때문에 status를 200 OK로 보낸다. 
				
				//TODO 200 OK 처리
				logger.info("Service instance already exists");
				instance.setHttpStatusOK();
				return instance;
			}
			else {
				//DB에 저장된 서비스 인스턴스 정보에서 서비스와 org가 동일하나 플랜이 다른경우
				
				throw new ServiceBrokerException("invalid Plan ID");
			}

		}
		else if(instanceIdExsists){
			throw new ServiceBrokerException("Instance ID duplicate");
		}
		
		else {	
		//API서비스 인스턴스 정보를 API플랫폼 DB에 저장한다.
			insertAPIServiceInstanceResult=dao.insertAPIServiceInstance(
					organizationGuid,
					serviceInstanceId,
					spaceGuid,
					serviceDefinitionId,
					planId);
			
			if(!insertAPIServiceInstanceResult){
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
