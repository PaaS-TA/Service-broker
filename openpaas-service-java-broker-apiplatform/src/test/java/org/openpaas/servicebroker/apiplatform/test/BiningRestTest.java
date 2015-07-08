package org.openpaas.servicebroker.apiplatform.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openpaas.servicebroker.apiplatform.common.ApiPlatformUtils;
import org.openpaas.servicebroker.apiplatform.dao.APIServiceInstanceDAO;
import org.openpaas.servicebroker.apiplatform.model.APIServiceInstance;
import org.openpaas.servicebroker.apiplatform.model.APIUser;
import org.openpaas.servicebroker.common.BindingBody;
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.openpaas.servicebroker.common.JsonUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.sun.xml.internal.messaging.saaj.soap.JpegDataContentHandler;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BiningRestTest {

private static Properties prop = new Properties();
	
	
	private APIServiceInstance apiServiceInstance;
	
	@BeforeClass
	public static void init() {
		
		System.out.println("== Started test Binding API ==");

		// Initialization
		// Get properties information
		String propFile = "test.properties";
 
		InputStream inputStream = CatalogRestTest.class.getClassLoader().getResourceAsStream(propFile);
		
		try {
			prop.load(inputStream);
	 	} catch (IOException e) {
	 		
			e.printStackTrace();
	 		System.err.println(e);
	 	}
		
	}
	

	//정상적인 파라미터가 입력된경우
	@Test
	public void valid_parameters_test() {
		
		String instance_id = "instance_id";
		String plan_id = prop.getProperty("provision_plan_id");
		String service_id= prop.getProperty("provision_service_id");
		String app_guid = UUID.randomUUID().toString();
		String binding_id = UUID.randomUUID().toString();
		
		BindingBody body =new BindingBody(service_id, plan_id, app_guid);
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<BindingBody> entity = new HttpEntity<BindingBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id;
		
		response = HttpClientUtils.sendBinding(url, entity, HttpMethod.PUT);
		System.out.println(response.getStatusCode());
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertTrue(response.getBody().contains("credentials"));
	}
	
	//존재하지 않는 플랜 아이디가 요청되었을 경우
	@Test
	public void invalid_parameters_test_plan() {
		
		String instance_id = "instance_id";
		String plan_id = prop.getProperty("provision_plan_id_fail");
		String service_id= prop.getProperty("provision_service_id");
		String app_guid = UUID.randomUUID().toString();
		String binding_id = UUID.randomUUID().toString();
		
		BindingBody body =new BindingBody(service_id, plan_id, app_guid);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<BindingBody> entity = new HttpEntity<BindingBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id;
		
		response = HttpClientUtils.sendBinding(url, entity, HttpMethod.PUT);
		System.out.println(response.getStatusCode());
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid Plan Name"));
	}
	
	//존재하지 않는 서비스 아이디가 요청되었을 경우
	@Test
	public void invalid_parameters_test_service() {
		
		String instance_id = "instance_id";
		String plan_id = prop.getProperty("provision_plan_id");
		String service_id= prop.getProperty("provision_service_id_fail");
		String app_guid = UUID.randomUUID().toString();
		String binding_id = UUID.randomUUID().toString();
		
		BindingBody body =new BindingBody(service_id, plan_id, app_guid);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<BindingBody> entity = new HttpEntity<BindingBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id;
		
		response = HttpClientUtils.sendBinding(url, entity, HttpMethod.PUT);
		System.out.println(response.getStatusCode());
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid Service Name"));
	}
	
	//다른 서비스 아이디가 요청되었을 경우
	@Test
	public void invalid_parameters_test_different_service() {
		
		String instance_id = "instance_id";
		String plan_id = prop.getProperty("binding_plan_id");
		String service_id= prop.getProperty("binding_service_id_annother");
		String app_guid = UUID.randomUUID().toString();
		String binding_id = UUID.randomUUID().toString();
		
		BindingBody body =new BindingBody(service_id, plan_id, app_guid);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<BindingBody> entity = new HttpEntity<BindingBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id;
		
		response = HttpClientUtils.sendBinding(url, entity, HttpMethod.PUT);
		System.out.println(response.getStatusCode());
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid Service Name"));
	}
	
	//API플랫폼 유저가 삭제된 경우
	@Test
	public void not_found_apiplatform_user_test() {
		
		String instance_id = "instance_id";
		String plan_id = prop.getProperty("binding_plan_id");
		String service_id= prop.getProperty("binding_service_id");
		String app_guid = UUID.randomUUID().toString();
		String binding_id = UUID.randomUUID().toString();
		
		BindingBody body =new BindingBody(service_id, plan_id, app_guid);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<BindingBody> entity = new HttpEntity<BindingBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id;
		
		response = HttpClientUtils.sendBinding(url, entity, HttpMethod.PUT);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertTrue(response.getBody().contains("credentials"));
	}
	
	//API플랫폼에 어플리케이션이 존재하지 않을 경우
	@Test
	public void not_found_apiplatform_application_test() {
		
		String instance_id = "instance_id";
		String plan_id = prop.getProperty("binding_plan_id");
		String service_id= prop.getProperty("binding_service_id");
		String app_guid = UUID.randomUUID().toString();
		String binding_id = UUID.randomUUID().toString();
		
		String cookie ="";
		String planName = plan_id.split(" ")[2];
		
		//어플리케이션 삭제
		cookie = login("691bb95447234b2c82c8f8b14810ed","openpaas");
		removeApplication(instance_id,cookie);
		
		BindingBody body =new BindingBody(service_id, plan_id, app_guid);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<BindingBody> entity = new HttpEntity<BindingBody>(body, headers);		
		ResponseEntity<String> response = null;
		
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id;
		
		response = HttpClientUtils.sendBinding(url, entity, HttpMethod.PUT);
	
		System.out.println(response.getStatusCode());	
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		System.out.println(response.getBody());
		assertTrue(response.getBody().contains("API Platform Application dose not exist."));
		
		//다시 어플리케이션 생성, 키생성
		addApplication(instance_id, cookie, planName);
		generateKey(instance_id,cookie);
	}
	
	
	
	
	
	
	
	public String login(String userId, String userPassword){
		
		String cookie ="";
		String loginUri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.Login");
		String loginParameters = "action=login&username="+userId+"&password="+userPassword;
		
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
	
	
	public void removeApplication(String serviceInstanceId, String cookie){
		HttpEntity<String> httpEntity;
		ResponseEntity<String> responseEntity;
		HttpHeaders headers = new HttpHeaders();
		
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		
		String removeApplicationUri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.RemoveApplication");
		String removeApplicationParameters = "action=removeApplication&application="+serviceInstanceId;
		
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
	
	public void addApplication(String serviceInstanceId, String cookie, String planName){
		HttpEntity<String> httpEntity;
		ResponseEntity<String> responseEntity;
		HttpHeaders headers = new HttpHeaders();
		
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		
		String addApplicationUri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.AddAnApplication");
		String addApplicationParameters = 
				"action=addApplication&application="+serviceInstanceId+"&tier="+planName+"&description=&callbackUrl=";
		
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
	
	public void generateKey(String serviceInstanceId, String cookie){
		HttpEntity<String> httpEntity;
		ResponseEntity<String> responseEntity;
		HttpHeaders headers = new HttpHeaders();
		
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		for(int i=1;i<3;i++) {	

			headers.set("Cookie", cookie);
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			String generateKeyUri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.GenerateAnApplicationKey");
			String generateKeyParameters = 
					"action=generateApplicationKey&application="+serviceInstanceId+"&keytype="+prop.getProperty("Keytype"+i)+"&callbackUrl=&authorizedDomains=ALL&validityTime=360000";
		
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
	}

	
	
}
