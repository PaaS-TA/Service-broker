package org.openpaas.servicebroker.apiplatform.service.impl;

import java.util.Map;

import org.openpaas.servicebroker.apiplatform.common.ApiPlatformUtils;
import org.openpaas.servicebroker.apiplatform.dao.APIServiceInstanceDAO;
import org.openpaas.servicebroker.apiplatform.model.APIServiceInstance;
import org.openpaas.servicebroker.apiplatform.model.APIUser;
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.openpaas.servicebroker.common.JsonUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.ServiceDefinition;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.service.CatalogService;
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
		String planName = planId.split(" ")[2];
		String organizationId;
		
		String serviceName = serviceId.split(" ")[0];
		String serviceVersion = serviceId.split(" ")[1];
		String serviceProvider = "";
		String id = bindingRequest.getBindingId();
		String appGuid = bindingRequest.getAppGuid();
		String syslogDrainUrl;
		
		
		Map<String,Object> credentials;
		
		String cookie = "";
		
		ServiceInstanceBinding serviceInstanceBinding;
		
	//요청된 서비스 인스턴스 정보와 DB에 저장된 서비스 인스턴스 정보가 같은지 확인한다.
		APIServiceInstance apiServiceInstance = dao.getAPIServiceByInstanceID(serviceInstanceId);
		//인스턴스 정보를  확인하고 orgId를 받아온다.
		organizationId = apiServiceInstance.getOrganization_id();
		
		if(!apiServiceInstance.getService_id().equals(serviceId)|!apiServiceInstance.getPlan_id().equals(planId)){
			logger.error("invalid Instance infomation");
			throw new ServiceBrokerException("dsawdwadasadad");
			//TODO 익셉션 처리, 혹은 확인 절차 자체를 삭제함			
		}
		

		
		//인스턴스 아이디로 DB를 검색해 매칭되는 유저정보를 가져온다.
		APIUser apiUser = new APIUser();
		try {
			apiUser = dao.getAPIUserByOrgID(organizationId);
		} catch (EmptyResultDataAccessException e) {
			//해당 인스턴스 아이디에 해당하는 유저정보가 DB에 저장 되어있어야 하지만 그렇지 않은 경우이다.
			logger.error("not found infomation about instance ID : "+serviceInstanceId+" at APIUsers table");
			throw new ServiceBrokerException("not found user information");
			
		} catch (Exception e){
			logger.error("Database Error - getAPIUserByInstanceId");
			throw new ServiceBrokerException(e.getMessage());
		}
		
	//인스턴스에 해당하는 유저정보로 로그인을 시도한다.
		String userId =apiUser.getUser_id();
		String userPassword = apiUser.getUser_password();
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
	//API의 공급자 정보를 가져오기 위해 Get Published APIs by Application API를 사용한다.
		boolean getAPIsSuccess;
		
		HttpHeaders headers = new HttpHeaders();	
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		String listAPIsUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GetAPIsByApplication");
		String listAPIsParameters = "action=getSubscriptionByApplication&app="+serviceInstanceId;		
		
		HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);

		ResponseEntity<String> responseEntity = HttpClientUtils.send(listAPIsUri+"?"+listAPIsParameters, httpEntity, HttpMethod.GET);
		
		JsonNode getAPIsResponseJson = null;
		
		try {
			getAPIsResponseJson = JsonUtils.convertToJson(responseEntity);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(getAPIsResponseJson);
			getAPIsSuccess = true;
		} catch (ServiceBrokerException e) {
			
			//어플리케이션 생성 또는 API사용등록이 모두 정상적으로 처리되어있는지 확인한다.
			if(e.getMessage().equals("No Application or Subscription")){
				getAPIsSuccess = false;
				//추후, 하단의 어플리케이션 생성, 키생성, 사용등록 부분을 삭제하고 예외를 발생시켜 처리
			}
			else {
			
				//위의 경우를 제외한 나머지 예외가 발생한 경우
				logger.error("API Platform response error - Get Published APIs by Application");
				throw new ServiceBrokerException(e.getMessage());
			}		
		}
		
	//getAPIsSuccess가 true인 경우는 어플리케이션이 생성되지 않았거나, 사용등록이 되지 않은 경우이다.
		if(!getAPIsSuccess){
			
			boolean applicationExsists = false;
			logger.info("Add An Application API Start");
			
			headers = new HttpHeaders();
			headers.set("Cookie", cookie);
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			//Add an Application API를 보낸다.
			
			String addApplicationUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.AddAnApplication");
			String addApplicationParameters = 
					"action=addApplication&application="+serviceInstanceId+"&tier="+env.getProperty("APIPlatformApplicationPlan")+"&description=&callbackUrl=";		
			httpEntity = new HttpEntity<String>(addApplicationParameters, headers);

			responseEntity = HttpClientUtils.send(addApplicationUri, httpEntity, HttpMethod.POST);
			
			JsonNode addApplicationResponseJson = null;

			try {
				addApplicationResponseJson = JsonUtils.convertToJson(responseEntity);
				ApiPlatformUtils.apiPlatformErrorMessageCheck(addApplicationResponseJson);
				
				//어플리케이션이 정상적으로 생성 되었다면 넘어간다.
				applicationExsists = true;
				logger.info("Application "+serviceInstanceId+" created");		
			
			} catch (ServiceBrokerException e) {
				if(e.getMessage().equals("Duplication")){
					//어플리케이션이 이미 존재하는 경우이므로 applicatioExsists를 true로 변경한다.
					applicationExsists = true;
					logger.info("application already exists");
				} else{
					//그밖의 예외처리
					logger.error("API Platform response error - Add an Application");
					throw new ServiceBrokerException(e.getMessage());
				}
			}
			logger.info("API Platform Application added");

			//어플리케이션이 예외가 발생하지 않았다면, 어플리케이션이 생성되었기 때문에 API의 사용등록 여부를 확인한다.
			if(applicationExsists){
			
				APICatalogService apiCatalogService = new APICatalogService();
				ServiceDefinition svc = apiCatalogService.getServiceDefinition(serviceId);
				//공급자명은 getServiceDefinition을 통해 가져왔지만 사용등록이 되어있지 않기 때문에 사용등록을 진행한다.
				serviceProvider = svc.getMetadata().get("providerDisplayName").toString();

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
				} catch (ServiceBrokerException e){
					if(e.getMessage().equals("Duplication"))
					{	//이미 사용등록이 되어있는 경우
						logger.info("API already subscribed");
					}
					else{
						logger.error("API Platform response error - Add a Subscription");
						throw new ServiceBrokerException(e.getMessage());
					}
				}
				logger.info("API Platform Subscription finished");	
			}
		}
		//Get Published APIs by Application API를 통해 가져온 API정보에서 공급자명을 찾아 변수에 담는다.
		else if(getAPIsSuccess){
			System.out.println(serviceName);
			
			JsonNode apis = getAPIsResponseJson.get("apis");
			for(JsonNode api :apis){
				if(api.get("apiName").asText().equals(serviceName)&&
						api.get("apiVersion").asText().equals(serviceVersion)){
					serviceProvider = api.get("apiProvider").toString();
					break;
				}
			}
			if(serviceProvider.isEmpty()){
				logger.error("Service Provider not found - Service : "+serviceName);
				throw new ServiceBrokerException("Service not found- Service : "+serviceName);
			}
			logger.debug("get Service Provider - Service : "+serviceName+", Provider : "+serviceProvider);
		}	
		
		System.out.println(serviceProvider);
			
			
		
			
			
			
			
			
			
			
			
		
		
		
		return null;
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(
			DeleteServiceInstanceBindingRequest arg0)
			throws ServiceBrokerException {
		// TODO Auto-generated method stub
		return null;
	}

}
