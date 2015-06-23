package org.openpaas.servicebroker.apiplatform.service.impl;


import org.openpaas.servicebroker.apiplatform.common.HttpClientUtils;
import org.openpaas.servicebroker.apiplatform.common.JsonUtils;
import org.openpaas.servicebroker.apiplatform.dao.APIServiceInstanceDAO;
import org.openpaas.servicebroker.apiplatform.exception.APIServiceInstanceException;
import org.openpaas.servicebroker.apiplatform.model.APIUser;
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
public class APIServiceInstanceService implements ServiceInstanceService{

	private static final Logger logger = LoggerFactory.getLogger(APIServiceInstanceService.class);
	
	@Autowired
	private Environment env;
	
	@Autowired
	APIServiceInstanceDAO dao;
	
	@Autowired
	LoginService loginService;
	
	@Autowired
	APICatalogService APICatalogService;
	
	@Autowired
	ServiceInstance instance;
	
	@Override
	public ServiceInstance createServiceInstance(CreateServiceInstanceRequest createServiceInstaceRequest) throws ServiceInstanceExistsException, ServiceBrokerException 
	{

		System.out.println("createServiceInstance");
		String organizationGuid =createServiceInstaceRequest.getOrganizationGuid();
		
		APIUser userInfo = dao.getAPIUserByOrgID(organizationGuid);

		if(userInfo.getUser_id()==null){
			APIUser newUser;
			logger.info("not registered API Platform User");
			newUser = makeUserId(organizationGuid);	
			
			logger.debug("send New User Infomation to insertUser()");
			dao.insertAPIUser(newUser.getOrganization_guid(), newUser.getUser_id(), newUser.getUser_password());
			logger.debug("complete insert new user");
			userInfo = newUser;
			organizationGuid = userInfo.getOrganization_guid();
			
		}

		String cookie = "";
		cookie = loginService.getLogin(organizationGuid, userInfo.getUser_password());
		logger.debug("Set Cookie");		
		
		//User Signup 실행
		logger.debug("Start makeUserId()");
		HttpHeaders headers = new HttpHeaders();
		
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		String uri = env.getProperty("URI.UserSignup");
		String parameters = "action=addUser&username="+organizationGuid+"&password="+env.getProperty("UserSignupPassword")+"&allFieldsValues=";
		
		
		HttpEntity<String> entity = new HttpEntity<String>(parameters, headers);

		ResponseEntity<String> userSignupHttpResponse;
		userSignupHttpResponse = HttpClientUtils.send(uri, entity, HttpMethod.POST);
		int status = 0;
		
			try {
				JsonUtils.convertToJson(userSignupHttpResponse);
			} catch (ServiceBrokerException e) {			
				throw new APIServiceInstanceException(status,e.getMessage());
			}
			
		instance = new ServiceInstance(createServiceInstaceRequest);
		logger.debug("create ServiceInstance");
		return instance;
	}
	
	
	private APIUser makeUserId(String organizationGuid) throws ServiceBrokerException{

		String cookie = "";
		int status = 0;
		//User Signup 실행
		logger.debug("Start makeUserId()");
		HttpHeaders headers = new HttpHeaders();
		
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		String uri = env.getProperty("URI.UserSignup");
		String parameters = "action=addUser&username="+organizationGuid+"&password="+env.getProperty("UserSignupPassword")+"&allFieldsValues=";
		
		
		HttpEntity<String> entity = new HttpEntity<String>(parameters, headers);

		ResponseEntity<String> userSignupHttpResponse;
		userSignupHttpResponse = HttpClientUtils.send(uri, entity, HttpMethod.POST);
		
		try {
			JsonUtils.convertToJson(userSignupHttpResponse);
		} catch (ServiceBrokerException e) {			
			throw new APIServiceInstanceException(status,e.getMessage());
		}
		logger.debug("API Platform User Created");
		
		APIUser newUser = new APIUser();
		newUser.setOrganization_guid(organizationGuid);
		newUser.setUser_id(organizationGuid);
		newUser.setUser_password(env.getProperty("UserSignupPassword"));
		
		return newUser;
	}
	
	private String getProvider(JsonNode json){
		
		
		
		return null;
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
