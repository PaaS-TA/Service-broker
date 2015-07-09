package org.openpaas.servicebroker.apiplatform.test;

import java.util.Properties;

import org.openpaas.servicebroker.apiplatform.common.ApiPlatformUtils;
import org.openpaas.servicebroker.apiplatform.service.impl.APICatalogService;
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.openpaas.servicebroker.common.JsonUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.model.ServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

public class APIPlatformAPI {


	public String login(String loginUri, String loginParameters){
		
		String cookie ="";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		ResponseEntity<String> loginResponseHttp = null;
		
		HttpEntity<String> httpEntity = new HttpEntity<String>(loginParameters, headers);
		loginResponseHttp = HttpClientUtils.send(loginUri, httpEntity, HttpMethod.POST);
		try {
			JsonNode loginResponseJson = JsonUtils.convertToJson(loginResponseHttp);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(loginResponseJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
		cookie = loginResponseHttp.getHeaders().getFirst("Set-Cookie");
		
		return cookie;
	}
	
	
	public void removeApplication(String removeApplicationUri,String removeApplicationParameters, String cookie){
		HttpEntity<String> httpEntity;
		ResponseEntity<String> responseEntity;
		HttpHeaders headers = new HttpHeaders();
		
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		httpEntity = new HttpEntity<String>(removeApplicationParameters, headers);
		responseEntity = HttpClientUtils.send(removeApplicationUri, httpEntity, HttpMethod.POST);
		
		JsonNode removeApplicationResponseJson = null;
		
		try {
			removeApplicationResponseJson =JsonUtils.convertToJson(responseEntity);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(removeApplicationResponseJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addApplication(String addApplicationUri, String addApplicationParameters, String cookie){
		HttpEntity<String> httpEntity;
		ResponseEntity<String> responseEntity;
		HttpHeaders headers = new HttpHeaders();
		
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		httpEntity = new HttpEntity<String>(addApplicationParameters, headers);
		responseEntity = HttpClientUtils.send(addApplicationUri, httpEntity, HttpMethod.POST);
		
		JsonNode addApplicationResponseJson = null;
		
		try {
			addApplicationResponseJson =JsonUtils.convertToJson(responseEntity);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(addApplicationResponseJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void generateKey(String generateKeyUri,String generateKeyParameters, String cookie){
		HttpEntity<String> httpEntity;
		ResponseEntity<String> responseEntity;
		HttpHeaders headers = new HttpHeaders();
		
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		httpEntity = new HttpEntity<String>(generateKeyParameters, headers);
		responseEntity = HttpClientUtils.send(generateKeyUri, httpEntity, HttpMethod.POST);

		JsonNode generateKeyResponseJson = null;
		
		try {
			generateKeyResponseJson = JsonUtils.convertToJson(responseEntity);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(generateKeyResponseJson);
		} catch (ServiceBrokerException e) {
			e.printStackTrace();
		}	
	}
	
	public void removeSubscription(String removeSubscriptionUri, String removeSubscriptionParameters,String cookie){
		
		HttpEntity<String> httpEntity;
		ResponseEntity<String> responseEntity;
		HttpHeaders headers = new HttpHeaders();
		
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		
		httpEntity = new HttpEntity<String>(removeSubscriptionParameters, headers);
		responseEntity = HttpClientUtils.send(removeSubscriptionUri, httpEntity, HttpMethod.POST);
		
		JsonNode removeSubscriptionResponseJson = null;

		try {
			removeSubscriptionResponseJson =JsonUtils.convertToJson(responseEntity);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(removeSubscriptionResponseJson);
		} catch(ServiceBrokerException e){
			if(e.getMessage().equals("invalid parameters")){
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public int getAppID(String getApplicationsUri, String getApplicationParameter, String cookie, String serviceInstanceId) {
		
		HttpEntity<String> httpEntity;
		ResponseEntity<String> responseEntity;
		HttpHeaders headers = new HttpHeaders();
	
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	
		httpEntity = new HttpEntity<String>("", headers);
		responseEntity = HttpClientUtils.send(getApplicationsUri+"?"+getApplicationParameter, httpEntity, HttpMethod.GET);
		
		JsonNode getApplicationsResponseJson = null;
		
		try {
			getApplicationsResponseJson =JsonUtils.convertToJson(responseEntity);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(getApplicationsResponseJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		JsonNode applications = getApplicationsResponseJson.get("applications");
		
		int applicationId = 0;
		for(JsonNode application:applications){
			if(application.get("name").asText().equals(serviceInstanceId)){
				applicationId = application.get("id").asInt();
				break;
			}
		}
		
		return applicationId;
	}

	public void addSubscription( String addApplicationUri, String addApplicationParameters,String cookie){
		
		HttpEntity<String> httpEntity;
		ResponseEntity<String> responseEntity;
		HttpHeaders headers = new HttpHeaders();
	
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		httpEntity = new HttpEntity<String>(addApplicationParameters, headers);

		responseEntity = HttpClientUtils.send(addApplicationUri, httpEntity, HttpMethod.POST);
		
		JsonNode addApplicationResponseJson = null;

		try {
			addApplicationResponseJson = JsonUtils.convertToJson(responseEntity);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(addApplicationResponseJson);
		} catch (ServiceBrokerException e) {
			e.printStackTrace();
		}
	}


}
