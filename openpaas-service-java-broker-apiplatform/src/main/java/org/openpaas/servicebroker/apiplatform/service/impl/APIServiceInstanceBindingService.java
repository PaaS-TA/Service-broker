package org.openpaas.servicebroker.apiplatform.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.apiplatform.common.ApiPlatformUtils;
import org.openpaas.servicebroker.apiplatform.dao.APIServiceInstanceDAO;
import org.openpaas.servicebroker.apiplatform.model.APIServiceInstance;
import org.openpaas.servicebroker.apiplatform.model.APIUser;
import org.openpaas.servicebroker.apiplatform.common.HttpClientUtils;
import org.openpaas.servicebroker.common.JsonUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.service.ServiceInstanceBindingService;
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
public class APIServiceInstanceBindingService implements ServiceInstanceBindingService{
	
	private static final Logger logger = LoggerFactory.getLogger(APIServiceInstanceBindingService.class);
	
	@Autowired
	private Environment env;
	
	@Autowired
	APIServiceInstanceDAO dao;
	
	@Autowired
	LoginService loginService;
	
	@Autowired
	APIServiceInstanceService apiServiceInstanceService;
	
	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(CreateServiceInstanceBindingRequest bindingRequest) throws ServiceInstanceBindingExistsException, ServiceBrokerException {

		String serviceInstanceId = bindingRequest.getServiceInstanceId();
		String serviceId = bindingRequest.getServiceDefinitionId();
		String planId = bindingRequest.getPlanId();
		String serviceName = serviceId.split(" ")[0];

		
	//인스턴스 정보를  확인하고 API정보를 받아온다.	
		APIServiceInstance apiServiceInstance;
		try {	
			apiServiceInstance = dao.getAPIInfoByInstanceID(serviceInstanceId);
		} catch (EmptyResultDataAccessException e) {
			//해당 인스턴스 아이디에 해당하는 유저정보가 DB에 저장 되어있어야 하지만 그렇지 않은 경우이다. - B016
			//getServiceInstance메소드에서 이미 인스턴스 정보가 apiplatform_services 테이블에 저장되어 있음을 확인했기 때문에 예외 발생 시, 유저정보가 없는 경우임을 확인할 수 있다.
			logger.error("not found APIPlatform user information about InstanceID :["+serviceInstanceId+"] - B016");
			throw new ServiceBrokerException("not found APIPlatform user information about InstanceID :["+serviceInstanceId+"]");
			
		} catch (Exception e){
			logger.error("Database Error - getAPIInfoByInstanceId");
			throw new ServiceBrokerException(e.getMessage());
		}

		//요청된 서비스 ID와 DB에 저장된 서비스 인스턴스의 서비스 ID가 일치하는지 확인한다. - B002
		if(!apiServiceInstance.getService_id().equals(serviceId)){
			logger.error("Invalid Information requested. ServiceID : "+serviceId+", Valid ServiceID : "+apiServiceInstance.getService_id());
			throw new ServiceBrokerException("Invalid ServiceID :["+serviceId+"], Valid ServiceID :["+apiServiceInstance.getService_id()+"]");
		}
		//요청된 플랜ID와 DB에 저장된 서비스 인스턴스의 플랜ID가 일치하는지 확인한다. - B003
		if(!apiServiceInstance.getPlan_id().equals(planId)){
			logger.error("Invalid Information requested. PlanID :["+planId+"], Valid PlanID :["+apiServiceInstance.getPlan_id()+"]");
			throw new ServiceBrokerException("Invalid PlanID :["+planId+"], Valid PlanID :["+apiServiceInstance.getPlan_id()+"]");
		}

		String serviceVersion = serviceId.split(" ")[1];
		
		String bindingId =bindingRequest.getBindingId();
		Map<String,Object> credentials = new LinkedHashMap<String, Object>();
		String syslogDrainUrl = null;
		String appGuid = bindingRequest.getAppGuid();

		String cookie = "";
		
	//인스턴스에 해당하는 유저정보로 로그인을 시도한다.	
		String userId =apiServiceInstance.getUser_id();
		String userPassword = apiServiceInstance.getUser_password();

		try {
			cookie = loginService.getLogin(userId, userPassword);
			logger.info("API Platform login success");
		} catch (ServiceBrokerException e) {
			if(e.getMessage().equals("No User")){
				logger.warn("not found API Platform User");				
				//유저가 API플랫폼에 등록이 되어있지 않을 경우, 예외를 발생시킨다. - B007
				//TODO 유저생성을 해줄 수도 있다. 상황에 따라 가변
				throw new ServiceBrokerException("API Platform User ["+userId+"] removed - B007");				
	
//				apiServiceInstanceService.userSignup(userId, userPassword);
//				cookie = loginService.getLogin(userId, userPassword);
			}
			else {
				logger.error("User Signup Error");
				throw new ServiceBrokerException(e.getMessage());
			}
		}
		
	//API 플랫폼과의 통신에 사용할 entity와 header를 준비한다.	
		HttpEntity<String> httpEntity;
		ResponseEntity<String> responseEntity;
		HttpHeaders headers = new HttpHeaders();
	
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	
		
	//어플리케이션 정보를 응답하는 List Subscriptions API를 이용하여 어플리케이션의 CosumerKey, ConsumerSecret을 가져온다.	
		logger.info("API Platform List Subscriptions started. Application : "+serviceInstanceId);
		
		String prodConsumerKey = null;
		String prodConsumerSecret = null;
		String prodKey = null;
		
		String sandConsumerKey = null;
		String sandConsumerSecret = null;
		String sandKey = null;
		
		String listApplicationsUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.ListSubscriptions");
		String listApplicationsParameters = "action=getAllSubscriptions";		
		
		httpEntity = new HttpEntity<String>("", headers);
		responseEntity = HttpClientUtils.send(listApplicationsUri+"?"+listApplicationsParameters, httpEntity, HttpMethod.GET);
		
		JsonNode listApplicationResponseJson = null;
		
		try {
			listApplicationResponseJson =JsonUtils.convertToJson(responseEntity);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(listApplicationResponseJson);
		} catch (ServiceBrokerException e) {
			throw new ServiceBrokerException(e.getMessage());
		}
		logger.info("API Platform List Subscriptions finished. Application : "+serviceInstanceId);
		
	//응답받은 json객체에서 키값들을 받아 변수에 담는다.
		boolean applicationExists = false;
		JsonNode applications = listApplicationResponseJson.get("subscriptions");
		for(JsonNode application :applications){
			if(application.get("name").asText().equals(serviceInstanceId)){
				applicationExists = true;
				//어플리케이션의 키값들을 변수에 담는다.
				logger.debug("Application Exists. Application Name : "+serviceInstanceId);
				
				prodConsumerKey = application.get("prodConsumerKey").asText();
				prodConsumerSecret = application.get("prodConsumerSecret").asText();
				prodKey = application.get("prodKey").asText();
				
				sandConsumerKey = application.get("sandboxConsumerKey").asText();
				sandConsumerSecret = application.get("sandboxConsumerSecret").asText();
				sandKey = application.get("sandboxKey").asText();
				break;
			}
		}
		
		//어플리케이션이 존재하지 않으므로 예외를 발생시킨다. - B008
		if(!applicationExists){
			logger.error("UserID: ["+userId+"], ApplicationName: ["+serviceInstanceId+"] dose not exist. "
					+ "Login APIPlatform username ["+userId+"] and create application ["+serviceInstanceId+"] and subscribe API ["+serviceName+"]");

			throw new ServiceBrokerException("API Platform Application dose not exist. UserID: ["+userId+"], ApplicationName: ["+serviceInstanceId+"] - B008");
		}
		else{
			//consumer key와 consumer secret이 각각 두가지 키타입으로 총 4개가 생성되어 있지 않으면 예외를 발생 or 키생성을 해준다.
//			throw new ServiceBrokerException("Credential not found. Application : ["+serviceInstanceId+"]");
			if(prodConsumerKey.equals("null")||"null".equals(prodConsumerSecret)||"null".equals(sandConsumerKey)||"null".equals(sandConsumerSecret)){
				logger.error("Key not generated. UsernID : ["+userId+"], Application : ["+serviceInstanceId+"]");
				//Generate Key API를 사용하여 API플랫폼 어플리케이션의 키값을 생성한다.
				logger.info("API Platform Generate Key Started. Application : "+serviceInstanceId);
				//두가지 키타입으로 키를 생성해야하기 때문에 두번 실행한다.
				for(int i=1;i<3;i++) {	
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

			} else {			
				logger.info("get Application Credentials completed. UserID :["+userId+"], Application :["+serviceInstanceId+"]");
			}
		}

	//API의 공급자 정보를 가져오기 위해 Get Published APIs by Application API를 사용한다.
		String listAPIsUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GetAPIsByApplication");
		String listAPIsParameters = "action=getSubscriptionByApplication&app="+serviceInstanceId;		
		
		httpEntity = new HttpEntity<String>("", headers);
		responseEntity = HttpClientUtils.send(listAPIsUri+"?"+listAPIsParameters, httpEntity, HttpMethod.GET);
		
		JsonNode getAPIsResponseJson = null;
		
		try {
			getAPIsResponseJson = JsonUtils.convertToJson(responseEntity);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(getAPIsResponseJson);
		} catch (ServiceBrokerException e) {
			//API사용등록이 정상적으로 처리되어 있지 않으면 예외를 발생시킨다. - B010
			if(e.getMessage().equals("No subscribed API")){
				logger.error("UserID: ["+userId+"], ApplicationName: ["+serviceInstanceId+"], API ["+serviceName+"] dose not exist. "
						+ "Login APIPlatform username ["+userId+"] and create application ["+serviceInstanceId+"] and subscribe API ["+serviceName+"]");
				throw new ServiceBrokerException("no subscribed API. UserID: ["+userId+"], ApplicationName: ["+serviceInstanceId+"], API: ["+serviceName+"] - B010");
			}
			else {
				//위의 경우를 제외한 나머지 예외가 발생한 경우
				logger.error("API Platform response error - Get Published APIs by Application");
				throw new ServiceBrokerException(e.getMessage());
			}
		}
	
	//Get Published APIs by Application API를 통해 가져온 API정보에서 공급자명을 찾아 변수에 담는다.
		String serviceProvider = null;
		String lifecycle = null;
		logger.debug("API : "+serviceName);
		JsonNode apis = getAPIsResponseJson.get("apis");
		for(JsonNode api :apis){
			if(api.get("apiName").asText().equals(serviceName)&&
					api.get("apiVersion").asText().equals(serviceVersion)){
				serviceProvider = api.get("apiProvider").asText();
				lifecycle = api.get("status").asText();
				break;
			}
		}
		//서비스 공급자명을 찾을 수 없는 경우는 API플랫폼에서 API가 삭제되었거나 해당 어플리케이션에서 사용등록이 해제된 경우로 간주 
		if(serviceProvider==null){
			logger.error("Service Provider not found - Service : ["+serviceId+"]");
			throw new ServiceBrokerException("APIPlatform API not found - API Name : ["+serviceId+"]");
		}
		//프로비전까지 완료된 상태의 API의 라이프 사이클을 API플랫폼 publisher에서 변경한 경우 - B009
		else if(!lifecycle.equals("PUBLISHED")){
			logger.error("APIPlatform API not published - Service : ["+serviceId+"]", "Status :["+lifecycle+"]" );
			throw new ServiceBrokerException("APIPlatform API not published - API Name : ["+serviceId+"], Status :["+lifecycle+"] - B009");		
		}
		else {
			logger.info("get Service Provider - Service : ["+serviceId+"], Provider : ["+serviceProvider+"]");
		}
		
	//API의 리소스 패스를 얻기 위해, Get API Information API를 사용한다.
		String getAPIInfoUrl = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GetAPIDetailInformation")+serviceProvider+"/"+serviceName+"/"+serviceVersion;
		
		httpEntity = new HttpEntity<String>("", headers);
		responseEntity = HttpClientUtils.send(getAPIInfoUrl, httpEntity, HttpMethod.GET);
		
		JsonNode  getAPIInfoResponseJson = null;
		
		try {
			getAPIInfoResponseJson =JsonUtils.convertToJson(responseEntity);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(getAPIInfoResponseJson);
		} catch (ServiceBrokerException e) {
			throw new ServiceBrokerException(e.getMessage());
		}
		
		JsonNode resources = getAPIInfoResponseJson.get("apis");
		
		
	//하나의 API가 복수의 resource path를 지니는 경우가 있을 수 있기 때문에 리스트에 담는다.
		List<String> resourceList = new ArrayList<String>();
		String resourcePath = null;
		int i =0;
		for(JsonNode resource : resources) {
			resourcePath = resource.get("path").asText();
			resourceList.add(i,resourcePath);
			i++;
		}
		
	// Get API Information Detail API를 사용한다.
		logger.info("Get API Information Detail");
		String basePath = null;
		JsonNode  getAPIInfoDetailResponseJson = null;
		List<Map> resourceMapList = new ArrayList<Map>();
		Map<String, Object> resourceMap = null;
		
		for(i=0;i<resourceList.size();i++){
			String getAPIInfoDetailUrl = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GetAPIDetailInformation")
					+serviceProvider+"/"+serviceName+"/"+serviceVersion+"/"+resourceList.get(i);
			
			httpEntity = new HttpEntity<String>("", headers);
			responseEntity = HttpClientUtils.send(getAPIInfoDetailUrl, httpEntity, HttpMethod.GET);
			

			try {
				getAPIInfoDetailResponseJson =JsonUtils.convertToJson(responseEntity);
				ApiPlatformUtils.apiPlatformErrorMessageCheck(getAPIInfoDetailResponseJson);
			} catch (ServiceBrokerException e) {
				throw new ServiceBrokerException(e.getMessage());
			}
			resourceMap = new LinkedHashMap<String, Object>();
			basePath = getAPIInfoDetailResponseJson.get("basePath").asText();
			resourcePath = getAPIInfoDetailResponseJson.get("resourcePath").asText();
			resourceMap.put("basePath", basePath);
			resourceMap.put("resourcePath", resourcePath);
			
			apis = getAPIInfoDetailResponseJson.get("apis");
			List<Map> methodList = new ArrayList<Map>();
			for(JsonNode api:apis)
			{
				
				
				JsonNode operations = api.get("operations");
				String path = api.get("path").asText();
				for(JsonNode method : operations)
				{	
					JsonNode parameters = method.get("parameters");
					List<JsonNode> parameterList = new ArrayList<JsonNode>();
					Map<String, Object> methodMap = new LinkedHashMap<String, Object>();
					methodMap.put("method", method.get("method").asText());
					methodMap.put("path",path);
//					Map<String, JsonNode> parameterMap = new LinkedHashMap<String, JsonNode>();
					
					for(JsonNode parameter : parameters)
					{
						parameterList.add(parameter);					
					}
					methodMap.put("parameters", parameterList);
					methodList.add(methodMap);
				}
			}	
			resourceMap.put("methods", methodList);
				
			resourceMapList.add(resourceMap);
			
		}
		logger.info("Get Credential");
		
		credentials.put("uri", basePath);
		credentials.put("username", userId);
		credentials.put("password", userPassword);
		credentials.put("host","");
		credentials.put("port","");
		credentials.put("database","");
		credentials.put("prodConsumerKey", prodConsumerKey);
		credentials.put("prodConsumerSecret", prodConsumerSecret);
		credentials.put("prodAccessToken", prodKey);
		credentials.put("sandConsumerKey", sandConsumerKey);
		credentials.put("sandConsumerSecret", sandConsumerSecret);
		credentials.put("sandAccessToken", sandKey);
		credentials.put("resources", resourceMapList);

		
		
		ServiceInstanceBinding serviceInstanceBinding = new ServiceInstanceBinding(bindingId, serviceId, credentials, syslogDrainUrl, appGuid);

		
		return serviceInstanceBinding;
	}
		
//		//getAPIsSuccess가 true인 경우는 어플리케이션이 생성되지 않았거나, 사용등록이 되지 않은 경우이다.
//				if(!getAPIsSuccess){
//					
//					boolean applicationExsists = false;
//					logger.info("Add An Application API Start");
//					
//					headers = new HttpHeaders();
//					headers.set("Cookie", cookie);
//					headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//					//Add an Application API를 보낸다.
//					
//					String addApplicationUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.AddAnApplication");
//					String addApplicationParameters = 
//							"action=addApplication&application="+serviceInstanceId+"&tier="+env.getProperty("APIPlatformApplicationPlan")+"&description=&callbackUrl=";		
//					httpEntity = new HttpEntity<String>(addApplicationParameters, headers);
//
//					responseEntity = HttpClientUtils.send(addApplicationUri, httpEntity, HttpMethod.POST);
//					
//					JsonNode addApplicationResponseJson = null;
//
//					try {
//						addApplicationResponseJson = JsonUtils.convertToJson(responseEntity);
//						ApiPlatformUtils.apiPlatformErrorMessageCheck(addApplicationResponseJson);
//						
//						//어플리케이션이 정상적으로 생성 되었다면 넘어간다.
//						applicationExsists = true;
//						logger.info("Application "+serviceInstanceId+" created");		
//					
//					} catch (ServiceBrokerException e) {
//						if(e.getMessage().equals("Duplication")){
//							//어플리케이션이 이미 존재하는 경우이므로 applicatioExsists를 true로 변경한다.
//							applicationExsists = true;
//							logger.info("application already exists");
//						} else{
//							//그밖의 예외처리
//							logger.error("API Platform response error - Add an Application");
//							throw new ServiceBrokerException(e.getMessage());
//						}
//					}
//					logger.info("API Platform Application added");
//
//					//어플리케이션이 예외가 발생하지 않았다면, 어플리케이션이 생성되었기 때문에 API의 사용등록 여부를 확인한다.
//					if(applicationExsists){
//					
//						APICatalogService apiCatalogService = new APICatalogService();
//						ServiceDefinition svc = apiCatalogService.getServiceDefinition(serviceId);
//						//공급자명은 getServiceDefinition을 통해 가져왔지만 사용등록이 되어있지 않기 때문에 사용등록을 진행한다.
//						serviceProvider = svc.getMetadata().get("providerDisplayName").toString();
//
//						logger.info("API Platform Add a Subscription started");
//						String addSubscriptionUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.AddSubscription");
//						String addSubscriptionParameters = 
//								"action=addAPISubscription&name="+serviceName+"&version="+serviceVersion+"&provider="+serviceProvider+"&tier="+planName+"&applicationName="+serviceInstanceId;
//						
//						headers.set("Cookie", cookie);
//						headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//						httpEntity = new HttpEntity<String>(addSubscriptionParameters, headers);
//
//						ResponseEntity<String> addSubscriptionResponseHttp = HttpClientUtils.send(addSubscriptionUri, httpEntity, HttpMethod.POST);
//						
//						JsonNode addSubscriptionResponseJson = null;
//
//						try {
//							addSubscriptionResponseJson = JsonUtils.convertToJson(addSubscriptionResponseHttp);
//							ApiPlatformUtils.apiPlatformErrorMessageCheck(addSubscriptionResponseJson);
//							//사용등록이 정상적으로 이루어진 경우
//						} catch (ServiceBrokerException e){
//							if(e.getMessage().equals("Duplication"))
//							{	//이미 사용등록이 되어있는 경우
//								logger.info("API already subscribed");
//							}
//							else{
//								logger.error("API Platform response error - Add a Subscription");
//								throw new ServiceBrokerException(e.getMessage());
//							}
//						}
//						logger.info("API Platform Subscription finished");	
//					}
//				}
//						
			

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException {
		
		ServiceInstance instance = request.getInstance();
		
		String organizationGuid =instance.getOrganizationGuid();
		String bindingId = request.getBindingId();
		String serviceInstanceId = instance.getServiceInstanceId();
		Map<String,Object> credentials = new LinkedHashMap<String, Object>();
		String syslogDrainUrl = null;
		String appGuid = "";
		
	//인스턴스의 orgID로 API플랫폼의 유저정보를 가져온다.
	
		APIUser apiUser;
		try {	
			apiUser = dao.getAPIUserByOrgID(organizationGuid);
		} catch (EmptyResultDataAccessException e) {
			//해당 orgID에 해당하는 유저정보가 DB에 저장 되어 있어야 하지만 그렇지 않은 경우이다. - U010
			logger.error("not found APIPlatform user information. Instance ID : ["+serviceInstanceId+"] - U010");
			throw new ServiceBrokerException("not found APIPlatform user information. Instance ID : ["+serviceInstanceId+"] - U010");
		} catch (Exception e){
			logger.error("Database Error - getAPIUserByOrgID");
			throw new ServiceBrokerException("Database Error :"+e.getMessage());
		}
		
		String serviceId = request.getServiceId();
		String planId = request.getPlanId();

	//요청된 서비스 인스턴스 정보와 DB에 저장된 서비스 인스턴스 정보가 일치하는지 확인한다.
		if(!instance.getServiceDefinitionId().equals(serviceId)&&!instance.getPlanId().equals(planId)){
			logger.error("Invalid ServiceID :["+serviceId+"], PlanID :["+planId+"]. Valid ServiceID :["+instance.getServiceDefinitionId()+"], PlanID :["+instance.getPlanId()+"]");
			throw new ServiceBrokerException("Invalid Instance infomation. ServiceID :["+serviceId+"], PlanID :["+planId+"]");
		//서비스 아이디가 잘못된 경우 - U003
		}else if(!instance.getServiceDefinitionId().equals(serviceId)){
			logger.error("Invalid ServiceID :["+serviceId+"] Valid ServiceID :["+instance.getServiceDefinitionId()+"]");
			throw new ServiceBrokerException("Invalid ServiceID :["+serviceId+"] Valid ServiceID :["+instance.getServiceDefinitionId()+"]");	
		//플랜아이디가 잘못된 경우 - U004
		}else if(!instance.getPlanId().equals(planId)){
			logger.error("Invalid PlanID :["+planId+"] Valid PlanID :["+instance.getPlanId()+"]");
			throw new ServiceBrokerException("Invalid PlanID :["+planId+"] Valid PlanID :["+instance.getPlanId()+"]");
		}
		
	//로그인한다.
		String userId = apiUser.getUser_id();
		String userPassword = apiUser.getUser_password();
		String cookie = "";
		
		try {
			cookie = loginService.getLogin(userId, userPassword);
			logger.info("API Platform login success");
		} catch (ServiceBrokerException e) {
			if(e.getMessage().equals("No User")){
			//유저가 API플랫폼에 등록이 되어있지 않을 경우 예외를 던지거나 자동으로 유저생성을 하도록 바꿀 수 있다.
				logger.warn("not found API Platform User :["+userId+"]");
				apiServiceInstanceService.userSignup(userId, userPassword);
				cookie = loginService.getLogin(userId, userPassword);
//				throw new ServiceBrokerException("API Platform user removed. User :["+userId+"]");
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
	//어플리케이션이 존재하지 않는 경우이다. 어플리케이션을 생성하고 언바인딩 절차를 이어간다.
	//어플리케이션과 API가 1:1관계이기 때문에 어플리케이션 삭제와 API 사용등록 해제를 구분할 필요는 없다.
	//어플리케이션이 존재하지 않는게 확인되면 DB만 변경하고 언바인딩 과정을 끝내도 무방하다.
		if(!applicationExists){
			String planName = planId.split(" ")[2];
			logger.error("Application ["+serviceInstanceId+"] not exists.");
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
				
				//어플리케이션이 정상적으로 생성 되었다면 넘어간다.
				logger.info("Application "+serviceInstanceId+" created");		
			} catch (ServiceBrokerException e) {
				if(e.getMessage().equals("Duplication")){
					//어플리케이션이 이미 존재하는 경우이므로 applicatioExsists를 true로 변경한다.
					logger.info("application already exists");
					
				} else{
					//그밖의 예외처리
					logger.error("API Platform response error - Add an Application");
					throw new ServiceBrokerException(e.getMessage());
				}
			}
			headers.set("Cookie", cookie);
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
			getApplicationsUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GetApplications");
			getApplicationParameter = "action=getApplications";
			
			httpEntity = new HttpEntity<String>("", headers);
			responseEntity = HttpClientUtils.send(getApplicationsUri+"?"+getApplicationParameter, httpEntity, HttpMethod.GET);
			
			getApplicationsResponseJson = null;
			
			try {
				getApplicationsResponseJson =JsonUtils.convertToJson(responseEntity);
				ApiPlatformUtils.apiPlatformErrorMessageCheck(getApplicationsResponseJson);
				logger.debug("Get Applications completed");
			} catch (Exception e) {
				throw new ServiceBrokerException("APIPlatform Error :"+e.getMessage());
			}
			
			applications = getApplicationsResponseJson.get("applications");
			
			applicationId = 0;
			for(JsonNode application:applications){
				if(application.get("name").asText().equals(serviceInstanceId)){
					applicationId = application.get("id").asInt();
					break;
				}
			}
			
			logger.info("API Platform Application added");
		}

	//API의 공급자 정보를 가져오기 위해 Get Published APIs by Application API를 사용한다.
	//WSO2 공식 문서의 명칭은  Get Published APIs by Application이지만 실제로는 라이프사이클과 무관하게 어플리케이션에 사용등록 된 API의 정보를 가져온다.

		String listAPIsUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GetAPIsByApplication");
		String listAPIsParameters = "action=getSubscriptionByApplication&app="+serviceInstanceId;		
		
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
				//이미 사용등록이 해제 되었다면 정상적으로 과정을 종료해도 무관하므로 객체를 리턴한다.
				return new ServiceInstanceBinding(bindingId, serviceInstanceId, credentials, syslogDrainUrl, appGuid);
			}
			else {
				//위의 경우를 제외한 나머지 예외가 발생한 경우
				logger.error("API Platform response error - Get Published APIs by Application");
				throw new ServiceBrokerException(e.getMessage());
			}
		}
		
		//Get Published APIs by Application API를 통해 가져온 API정보에서 공급자명을 찾아 변수에 담는다.

		String serviceProvider = null;
		String lifecycle = null;
		JsonNode apis = getAPIsResponseJson.get("apis");
		for(JsonNode api :apis){
			if(serviceId.equals(api.get("apiName").asText()+" "+api.get("apiVersion").asText())){
				serviceProvider = api.get("apiProvider").asText();
				lifecycle = api.get("status").asText();
				logger.debug("API ["+serviceId+"] found");
				break;
			}
		}
		//공급자를 찾을 수 없는 경우는 API플랫폼에서 이미 사용등록이 해제된 경우이다.
		if(serviceProvider==null){
			logger.error("Service Provider not found - Service : ["+serviceId+"]");
			//이미 사용등록이 해제 되었다면 정상적으로 과정을 종료해도 무관하므로 객체를 리턴한다.
			return new ServiceInstanceBinding(bindingId, serviceInstanceId, credentials, syslogDrainUrl, appGuid);
		}
		//바인딩까지 완료된 상태의 API의 라이프 사이클을 API플랫폼 publisher에서 변경한 경우
		else if(!lifecycle.equals("PUBLISHED")){
			logger.error("APIPlatform API not published - Service : ["+serviceId+"]", "Status :["+lifecycle+"]" );
			//API의 라이프 사이클이 변경되어 있어도	사용등록을 해제하는 것이 가능하기 때문에, 예외를 발생시키지 않고 사용등록을 해제한다.
		}
		else {
			logger.info("get Service Provider - Service : ["+serviceId+"], Provider : ["+serviceProvider+"]");
		}
		
		//사용등록 해제를 위해 Remove a Subscription API를 사용한다. 
		//API매니저 1.9버전부터는 어플리케이션ID없이 어플리케이션 이름만으로 API를 사용할 수 있다.
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
			//Remove a Subscription API를 사용하는데 잘못된 파라미터가 입력된 경우이다.
			if(e.getMessage().equals("invalid parameters")){
				logger.error("Invalid parameters Service name : ["+serviceName+"], Version : ["+serviceVersion+"], API Provider : ["+serviceProvider+"], Application ID : ["+applicationId+"]");
				throw new ServiceBrokerException("API Platform Error : Invalid Parameters [Remove an Subscription API]");
			}
		} catch (Exception e) {
			throw new ServiceBrokerException("APIPlatform Error :"+e.getMessage());
		}
	
		
		
		
		
		return new ServiceInstanceBinding(bindingId, serviceInstanceId, credentials, syslogDrainUrl, appGuid);
	}
	
	
}
