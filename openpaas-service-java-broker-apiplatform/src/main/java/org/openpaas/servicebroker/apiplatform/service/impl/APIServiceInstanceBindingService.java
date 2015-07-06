package org.openpaas.servicebroker.apiplatform.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.apiplatform.common.ApiPlatformUtils;
import org.openpaas.servicebroker.apiplatform.dao.APIServiceInstanceDAO;
import org.openpaas.servicebroker.apiplatform.model.APIServiceInstance;
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.openpaas.servicebroker.common.JsonUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceBindingRequest;
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

	//인스턴스 정보를  확인하고 orgId를 받아온다.	
		APIServiceInstance apiServiceInstance;
		try {	
			apiServiceInstance = dao.getAPIInfoByInstanceID(serviceInstanceId);
		} catch (EmptyResultDataAccessException e) {
			//해당 인스턴스 아이디에 해당하는 유저정보가 DB에 저장 되어있어야 하지만 그렇지 않은 경우이다.
			//getServiceInstance메소드에서 이미 인스턴스 정보가 apiplatform_services 테이블에 저장되어 있음을 확인했기 때문에 예외 발생 시, 유저정보가 없는 경우임을 확인할 수 있다.
			logger.error("not found infomation about instance ID : "+serviceInstanceId+". Try again, after Provision");
			throw new ServiceBrokerException("not found instance information");
			
		} catch (Exception e){
			logger.error("Database Error - getAPIInfoByInstanceId");
			throw new ServiceBrokerException(e.getMessage());
		}

		//요청된 서비스 인스턴스 정보와 DB에 저장된 서비스 인스턴스 정보가 같은지 확인한다.
		if(!apiServiceInstance.getService_id().equals(serviceId)&&!apiServiceInstance.getPlan_id().equals(planId)){
			logger.error("invalid Instance infomation");
			logger.debug("Try again, after provisioning - Provisioning response status must be 201 'CREATED'");
			throw new ServiceBrokerException("Try again, after provisioning");		
		}else if(!apiServiceInstance.getService_id().equals(serviceId)|!apiServiceInstance.getPlan_id().equals(planId)){
			logger.error("invalid Instance infomation");
			throw new ServiceBrokerException("Recheck the request information - InstanceID : "+serviceInstanceId+", ServiceID : "+serviceId+", PlanID : "+planId);
		}

		String serviceName = serviceId.split(" ")[0];
		String serviceVersion = serviceId.split(" ")[1];
		String serviceProvider = "";
		
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
				//유저가 API플랫폼에 등록이 되어있지 않을 경우, 유저등록을 한다.
				logger.warn("not found API Platform User");
				apiServiceInstanceService.userSignup(userId, userPassword);
				//다시 로그인을 시도한다.
				cookie = loginService.getLogin(userId, userPassword);
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
		String sandConsumerKey = null;
		String sandConsumerSecret = null;
		
		String listApplicationsUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.ListSubscriptions");
		String listApplicationsParameters = "action=getAllSubscriptions";		
		
		httpEntity = new HttpEntity<String>("", headers);
		responseEntity = HttpClientUtils.send(listApplicationsUri+"?"+listApplicationsParameters, httpEntity, HttpMethod.GET);
		
		JsonNode getApplicationResponseJson = null;
		
		try {
			getApplicationResponseJson =JsonUtils.convertToJson(responseEntity);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(getApplicationResponseJson);
		} catch (ServiceBrokerException e) {
			throw new ServiceBrokerException(e.getMessage());
		}
		logger.info("API Platform List Subscriptions finished. Application : "+serviceInstanceId);
		
	//응답받은 json객체에서 키값들을 받아 변수에 담는다.
		boolean applicationExists = false;
		JsonNode applications = getApplicationResponseJson.get("subscriptions");
		for(JsonNode application :applications){
			if(application.get("name").asText().equals(serviceInstanceId)){
				//TODO 어플리케이션과 사용등록된 API가 모두 subscriptions필드로 나오기때문에 확인이 필요함
				//어플리케이션의 키값들을 변수에 담는다.
				logger.debug("Application Exists. Application Name : "+serviceInstanceId);
				prodConsumerKey = application.get("prodConsumerKey").asText();
				prodConsumerSecret = application.get("prodConsumerSecret").asText();
				sandConsumerKey = application.get("sandboxConsumerKey").asText();
				sandConsumerSecret = application.get("sandboxConsumerSecret").asText();
				applicationExists = true;
				break;
			}
		}
		//어플리케이션이 존재하지 않으므로 예외를 발생시킨다.
		if(!applicationExists){
			logger.error("UserID : "+userId+", ApplicationName : "+serviceInstanceId+" dose not exist.");
			throw new ServiceBrokerException("API Platform Application dose not exist. UserID : "+userId+", ApplicationName : "+serviceInstanceId);
		}
		else{
			//consumer key와 consumer secret이 각각 두가지 키타입으로 총 4개가 생성되어 있지 않으면 예외를 발생시킨다.
			if(prodConsumerKey.equals("null")||"null".equals(prodConsumerSecret)||"null".equals(sandConsumerKey)||"null".equals(sandConsumerSecret)){
				logger.error("Key not generated. UsernId : "+userId+", Application : "+serviceInstanceId);
				throw new ServiceBrokerException("Credential not found. Application : "+serviceInstanceId+", Try again, after provisioning");
			} else {			
				logger.info("get Application Credentials completed. UserId : "+userId+", Application : "+serviceInstanceId);
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
			
			//어플리케이션 생성 또는 API사용등록이 모두 정상적으로 처리되어있는지 확인한다.
			if(e.getMessage().equals("No Application or Subscription")){
				logger.debug("Try again, after provisioning - Provisioning response status must be 200 'OK'");
				throw new ServiceBrokerException("Try again, after provisioning");
			}
			else {
			
				//위의 경우를 제외한 나머지 예외가 발생한 경우
				logger.error("API Platform response error - Get Published APIs by Application");
				throw new ServiceBrokerException(e.getMessage());
			}		
		}
	
	//Get Published APIs by Application API를 통해 가져온 API정보에서 공급자명을 찾아 변수에 담는다.
		logger.debug("API : "+serviceName);
		JsonNode apis = getAPIsResponseJson.get("apis");
		for(JsonNode api :apis){
			if(api.get("apiName").asText().equals(serviceName)&&
					api.get("apiVersion").asText().equals(serviceVersion)){
				serviceProvider = api.get("apiProvider").asText();
				break;
			}
		}
		if(serviceProvider.equals("null")){
			logger.error("Service Provider not found - Service : "+serviceName);
			throw new ServiceBrokerException("Service not found - Service : "+serviceName);
		} else {
			logger.info("get Service Provider - Service : "+serviceName+", Provider : "+serviceProvider);
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
		
		
		//TODO 하나의 API가 복수의 resource path를 지니는 경우가 있을 수 있기 때문에 리스트에 담는다.
		
		List<String> resourceList = new ArrayList<String>();
		String resourcePath = null;
		int i =0;
		for(JsonNode resource : resources) {
			resourcePath = resource.get("path").asText();
			resourceList.add(i,resourcePath);
			i++;
		}
		
	// Get API Information Detail API를 사용한다.
		List<JsonNode> apiList = new ArrayList<JsonNode>();
		String basePath = null;
		JsonNode  getAPIInfoDetailResponseJson = null;
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
			
			apis = getAPIInfoDetailResponseJson.get("apis");
			apiList.add(apis);
		}
		
		basePath = getAPIInfoDetailResponseJson.get("basePath").asText();
		
		
/*		
		List<Map> resourceMapList = new ArrayList<Map>();
		Map<String, Object> resourceMap = null;
		for(i=0;i<resourceList.size();i++){
			String getAPIInfoDetailUrl = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GetAPIDetailInformation")
					+serviceProvider+"/"+serviceName+"/"+serviceVersion+"/"+resourceList.get(i);
			
			httpEntity = new HttpEntity<String>("", headers);
			responseEntity = HttpClientUtils.send(getAPIInfoDetailUrl, httpEntity, HttpMethod.GET);
			
			JsonNode  getAPIInfoDetailResponseJson = null;
			
			try {
				getAPIInfoDetailResponseJson =JsonUtils.convertToJson(responseEntity);
				ApiPlatformUtils.apiPlatformErrorMessageCheck(getAPIInfoDetailResponseJson);
			} catch (ServiceBrokerException e) {
				throw new ServiceBrokerException(e.getMessage());
			}
			resourceMap = new LinkedHashMap<String, Object>();
			String basePath = getAPIInfoDetailResponseJson.get("basePath").asText();
			resourcePath = getAPIInfoDetailResponseJson.get("resourcePath").asText();
			resourceMap.put("basePath", basePath);
			resourceMap.put("resourcePath", resourcePath);
			
			apis = getAPIInfoDetailResponseJson.get("apis");
			List<Map> methodList = new ArrayList<Map>();
			for(JsonNode api:apis)
			{
				JsonNode operations = api.get("operations");
				for(JsonNode method : operations)
				{
					JsonNode parameters = method.get("parameters");
					List<Map> parameterList = new ArrayList<Map>();
					Map<String, Object> methodMap = new LinkedHashMap<String, Object>();
					methodMap.put("method", method.get("method").asText());
					int j=1;
					for(JsonNode parameter : parameters)
					{
						if(j==1) {
							methodMap.put("description", parameter.get("description").asText());
							methodMap.put("allowMultiple", parameter.get("allowMultiple").asBoolean());
						}
						else{
							Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();
							parameterMap.put("name", parameter.get("name").asText());
							parameterMap.put("type", parameter.get("type").asText());
							parameterMap.put("required", parameter.get("required").asBoolean());
							parameterList.add(parameterMap);
						}
						j++;

					}
					methodMap.put("parameters", parameterList);
					methodList.add(methodMap);
				}
			}	
			resourceMap.put("methods", methodList);
			resourceMapList.add(resourceMap);
		}
*/
		credentials.put("uri", basePath);
		credentials.put("username", userId);
		credentials.put("password", userPassword);
		credentials.put("host","");
		credentials.put("port","");
		credentials.put("database","");
		credentials.put("prodConsumerKey", prodConsumerKey);
		credentials.put("prodConsumerSecret", prodConsumerSecret);
		credentials.put("sandConsumerKey", sandConsumerKey);
		credentials.put("sandConsumerSecret", sandConsumerSecret);
		credentials.put("apis", apiList);
		
		
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
	public ServiceInstanceBinding deleteServiceInstanceBinding(
			DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException {
		
		String bindingId = request.getBindingId();
		String serviceInstanceId = request.getServiceId();
		Map<String,Object> credentials = new LinkedHashMap<String, Object>();
		String syslogDrainUrl = null;
		String appGuid = "dummy";
		//TODO appGuid를 브로커에서 가지고 있어야 하는지.
		return new ServiceInstanceBinding(bindingId, serviceInstanceId, credentials, syslogDrainUrl, appGuid);
	}

	
	
	
	
}
