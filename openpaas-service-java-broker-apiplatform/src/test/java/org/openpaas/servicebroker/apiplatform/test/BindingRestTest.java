package org.openpaas.servicebroker.apiplatform.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openpaas.servicebroker.common.BindingBody;
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.sun.org.apache.xml.internal.security.utils.Base64;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BindingRestTest {

private static Properties prop = new Properties();


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
	
	
	//정상적인 파라미터가 입력된경우 - 201 CREATED
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
	
	//존재하지 않는(프로퍼티 파일의 AvailablePlan으로 정의되지 않은) 플랜 아이디가 요청되었을 경우 - 500 INTERNAL_SERVER_ERROR
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
		assertTrue(response.getBody().contains("Invalid PlanID"));
		
		System.out.println("End - invalid_parameters_test_plan");
	}
	
	//존재하지 않는(프로퍼티 파일의 AvailablePlan으로 정의되지 않은)플랜명을 포함한 플랜 아이디가 요청되었을 경우 - 500 INTERNAL_SERVER_ERROR
		@Test
		public void invalid_parameters_test_plan_name() {
			
			System.out.println("Start - invalid_parameters_test_plan_name");
			
			String instance_id = prop.getProperty("test_instance_id");
			String plan_id = prop.getProperty("binding_plan_name_fail");
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
			assertTrue(response.getBody().contains("Invalid PlanName"));
			
			System.out.println("End - invalid_parameters_test_plan_name");
		}
		
		//API플랫폼에 존재하지 않는 잘못된 서비스명을 포함한 서비스 아이디로 요청이 들어온 케이스 - 500 INTERNAL_SERVER_ERROR
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
			assertTrue(response.getBody().contains("Invalid ServiceID"));
			
			System.out.println("End - invalid_parameters_test_service");
		}
	
	//API플랫폼에 존재하지 않는 잘못된 서비스명을 포함한 서비스 아이디로 요청이 들어온 케이스 - 500 INTERNAL_SERVER_ERROR
	@Test
	public void invalid_parameters_test_service_name() {
		System.out.println("Start - invalid_parameters_test_service");
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("binding_service_name_fail");
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
		assertTrue(response.getBody().contains("Invalid ServiceName"));
		
		System.out.println("End - invalid_parameters_test_service");
	}

	
	//API플랫폼에는 존재하지만 DB에 저장된 내용과는 다른 서비스 아이디가 요청되었을 경우 - 500 INTERNAL_SERVER_ERROR
	@Test
	public void invalid_parameters_test_other_service() {
		System.out.println("Start - invalid_parameters_test_other_service");
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("binding_other_service_id");
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
		assertTrue(response.getBody().contains("Invalid ServiceName"));
		
		System.out.println("invalid_parameters_test_other_service end");
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

		System.out.println("End - not_found_apiplatform_application_test");
	}
	
	//API플랫폼에서 API의 라이프사이클이 변경된 API를 바인딩 요청할 경우
	@Test
	public void lifecycle_changed_API_test() {
		
		System.out.println("Start - lifecycle_changed_API_test");
		
		String instance_id = prop.getProperty("binding_lifecycle_change_instance_id");
		String plan_id = prop.getProperty("binding_lifecycle_change_plan_id");
		String service_id= prop.getProperty("binding_lifecycle_change_service_id");
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
		assertTrue(response.getBody().contains("APIPlatform API not published"));
		
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

		System.out.println("End - subscription_removed_API_test");
	}
	
	//각각의 파라미터가 빈값으로 요청된 케이스
	//플랜아이디가 빈값으로 요청된 케이스 - 422 UNPROCESSABLE_ENTITY
	@Test
	public void no_plan_id_test() {
		System.out.println("Start - no_plan_id_test");
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = null;
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
		
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
		assertTrue(response.getBody().contains("Missing required fields: planId"));

		System.out.println("End - no_plan_id_test");
	}
	//서비스 아이디가 빈값으로 요청된 케이스 - 422 UNPROCESSABLE_ENTITY
	@Test
	public void no_service_id_test() {
		System.out.println("Start - no_service_id_test");
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= null;
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
		
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
		assertTrue(response.getBody().contains("Missing required fields: serviceDefinitionId"));

		System.out.println("End - no_service_id_test");
	}
	//app guid 아이디가 빈값으로 요청된 케이스 - 422 UNPROCESSABLE_ENTITY
	@Test
	public void no_app_guid_test() {
		System.out.println("Start - no_app_guid_test");
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		String app_guid = "";
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
		
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
		assertTrue(response.getBody().contains("Missing required fields: appGuid"));

		System.out.println("End - no_app_guid_test");
	}
	
	//바인딩 아이디가 빈값으로 요청된 케이스 - 404 NOT_FOUND
	@Test
	public void no_binding_id_test() {
		System.out.println("Start - no_binding_id_test");
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		String app_guid = UUID.randomUUID().toString();
		String binding_id = "";
	
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
		
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertTrue(response.getBody().contains("No message available"));

		System.out.println("End - no_binding_id_test");
	}
	
	//인스턴스 아이디가 빈값으로 요청된 케이스 - 404 NOT_FOUND
	@Test
	public void no_instance_id_test() {
		System.out.println("Start - no_instance_id_test");
		
		String instance_id = "";
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
		
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertTrue(response.getBody().contains("No message available"));

		System.out.println("End - no_instance_id_test");
	}
}
