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
import org.openpaas.servicebroker.apiplatform.service.impl.APICatalogService;
import org.openpaas.servicebroker.common.BindingBody;
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.openpaas.servicebroker.common.JsonUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.model.ServiceDefinition;
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

import sun.security.jca.ServiceId;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.sun.xml.internal.messaging.saaj.soap.JpegDataContentHandler;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BindingRestTest {

private static Properties prop = new Properties();
	
private static APIPlatformAPI api;


private APICatalogService apiCatalogService;

	@BeforeClass
	public static void init() {
		
		System.out.println("== Started test Binding API ==");

		// Initialization
		// Get properties information
		String propFile = "test.properties";
 
		InputStream inputStream = BindingRestTest.class.getClassLoader().getResourceAsStream(propFile);
		
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
		System.out.println("valid_parameters_test start");
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
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
		
		System.out.println("valid_parameters_test end");
	}
	
	//존재하지 않는 플랜 아이디가 요청되었을 경우 - 201 CREATED
	@Test
	public void invalid_parameters_test_plan() {
		
		System.out.println("Start - invalid_parameters_test_plan");
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("binding_plan_id_fail");
		String service_id= prop.getProperty("test_service_id");
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
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid Plan Name"));
		
		System.out.println("End - invalid_parameters_test_plan");
	}
	
	//API 플랫폼에 존재하지 않는 서비스 아이디가 요청되었을 경우 - 500 INTERNAL_SERVER_ERROR
	@Test
	public void invalid_parameters_test_service() {
		System.out.println("Start - invalid_parameters_test_service");
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("binding_service_id_fail");
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
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid Service Name"));
		
		System.out.println("End - invalid_parameters_test_service");
	}
	
	//API플랫폼에 존재하지 않는 잘못된 서비스 아이디로 요청이 들어온 케이스 - 500 INTERNAL_SERVER_ERROR
	@Test
	public void invalid_parameters_test_fail_service() {
		System.out.println("Start - invalid_parameters_test_fail_service");
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("binding_service_id_fail");
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
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid Service Name"));
		
		System.out.println("End - invalid_parameters_test_fail_service");
	}	
	
	//API플랫폼에는 존재하지만 DB에 저장된 내용과는 다른 서비스 아이디가 요청되었을 경우 - 500 INTERNAL_SERVER_ERROR
	@Test
	public void invalid_parameters_test_different_service() {
		System.out.println("Start - invalid_parameters_test_different_service");
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("binding_different_service_id");
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
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid Service Name"));
		
		System.out.println("invalid_parameters_test_different_service end");
	}
	
	
	
	//API플랫폼에서 유저가 삭제된 경우 
	//직접 API플랫폼 관리콘솔에서 유저를 삭제하고 테스트 
//	@Test
	public void not_found_apiplatform_user_test() {
		
		System.out.println("Start - not_found_apiplatform_user_test");
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
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
		
		System.out.println("End - not_found_apiplatform_user_test");
	}
	
	//API플랫폼에 어플리케이션이 존재하지 않을 경우 - 500 INTERNAL_SERVER_ERROR
	@Test
	public void not_found_apiplatform_application_test() {
		
		System.out.println("Start - not_found_apiplatform_application_test");
		
		String instance_id = prop.getProperty("binding_no_app_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		String app_guid = UUID.randomUUID().toString();
		String binding_id = UUID.randomUUID().toString();
/*		
		String cookie ="";
		String planName = plan_id.split(" ")[2];
	
		//로그인
		String uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.Login");
		String parameters = "action=login&username="+prop.getProperty("test_apiplatform_user")+"&password="+prop.getProperty("test_apiplatform_password");
		cookie = api.login(uri,parameters);
		
		//어플리케이션 삭제
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.RemoveApplication");
		parameters = "action=removeApplication&application="+instance_id;
		api.removeApplication(uri,parameters,cookie);
*/	
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
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("API Platform Application dose not exist."));
/*		
		//어플리케이션 생성
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.AddAnApplication");
		parameters = 
				"action=addApplication&application="+instance_id+"&tier="+planName+"&description=&callbackUrl=";
		api.addApplication(uri, parameters, cookie);
		
		//키생성
		
		for(int i=1;i<3;i++) {
			uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.GenerateAnApplicationKey");
			parameters = "action=generateApplicationKey&application="+instance_id+"&keytype="+prop.getProperty("Keytype"+i)+"&callbackUrl=&authorizedDomains=ALL&validityTime=360000";
			api.generateKey(uri,parameters,cookie);
		}
		
*/		System.out.println("End - not_found_apiplatform_application_test");
	}
	
	//API플랫폼에서 API의 라이프사이클이 변경된 API를 바인딩 요청할 경우
	//API의 사용등록상태는 유지되므로 status를 확인할 수 있는 부분을 추가한다.
	@Test
	public void lifecycle_changed_API_test() {
		
		System.out.println("Start - lifecycle_changed_API_test");
		
		String instance_id = prop.getProperty("binding_lifecycle_change_instance_id");
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
		
		System.out.println("End - lifecycle_changed_API_test");
	}
	
	//API플랫폼에서 API의 사용등록이 해제된 경우 - 500 INTERNAL_SERVER_ERROR
	@Test
	public void subscription_removed_API_test() {
		System.out.println("Start - subscription_removed_API_test");
		
		String instance_id = prop.getProperty("binding_no_subs_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		String app_guid = UUID.randomUUID().toString();
		String binding_id = UUID.randomUUID().toString();
/*		
		String cookie = api.login("test_user","openpaas");
		String planName = plan_id.split(" ")[2];
		
		String serviceName = service_id.split(" ")[0];
		String serviceVersion = service_id.split(" ")[1];

		//API공급자 정보 획득
		ServiceDefinition serviceDefinition = null;
		try {
			serviceDefinition = apiCatalogService.getServiceDefinition(service_id);
		} catch (ServiceBrokerException e1) {
			e1.printStackTrace();
		}
		String serviceProvider = serviceDefinition.getMetadata().get("providerDisplayName").toString();
		
		//어플리케이션ID획득
		String uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.GetApplications");
		String parameters = "action=getApplications";
		int applicationId = api.getAppID(uri, parameters, cookie,service_id);
		
		//사용등록 해제
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.RemoveSubscription");
		parameters = "action=removeSubscription&name="+serviceName+"&version="+serviceVersion+"&provider="+serviceProvider+"&applicationId="+applicationId;
		api.removeSubscription(uri,parameters,cookie);	
*/		
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
		
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("no subscribed API."));
/*		
		//API사용등록
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.AddAnApplication");
		parameters = 
				"action=addApplication&application="+service_id+"&tier="+planName+"&description=&callbackUrl=";		
		api.addSubscription(uri, parameters, cookie);	
*/
		System.out.println("End - subscription_removed_API_test");
	}
	
	//존재하지 않는 인스턴스 아이디
	

	
	
}
