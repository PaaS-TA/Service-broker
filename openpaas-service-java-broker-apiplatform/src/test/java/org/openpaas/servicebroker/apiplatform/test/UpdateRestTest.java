package org.openpaas.servicebroker.apiplatform.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.openpaas.servicebroker.common.UpdateProvisionBody;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpdateRestTest {
	
private static Properties prop = new Properties();
	
	@BeforeClass
	public static void init() {

		System.out.println("== Started test Update API ==");

		// Initialization
		// Get properties information
		String propFile = "test.properties";
 
		InputStream inputStream = UpdateRestTest.class.getClassLoader().getResourceAsStream(propFile);
		
		try {
			prop.load(inputStream);
	 	} catch (IOException e) {
	 		
			e.printStackTrace();
	 		System.err.println(e);
	 	}
	}
	
	//UP01 정상적으로 플랜이 업데이트 되는 케이스
	@Test
	public void UP01_valid_plan_id() {
		
		System.out.println("UP01 Start - valid_plan_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id_other");
		UpdateProvisionBody body = new UpdateProvisionBody(plan_id);
		
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());

		assertTrue(response.getBody().equals("{}"));
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		System.out.println("UP01 End - valid_plan_id");
	}
	
	//UP02 이미 설정된 플랜아이디로 요청이 들어온 케이스
	@Test
	public void UP02_same_plan_id() {
		
		System.out.println("UP02 Start - same_plan_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id_other");
		UpdateProvisionBody body = new UpdateProvisionBody(plan_id);
		
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());

		assertTrue(response.getBody().contains("Plan :["+plan_id.split(" ")[2]+"] already setted"));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("UP02 End - same_plan_id");
	}
	
	//UP03 요청된 플랜아이디에 포함된 플랜명이 설정되어 있지 않은 플랜명인 케이스
	@Test
	public void UP03_invalid_plan_name() {
		
		System.out.println("UP03 Start - invalid_plan_name");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id_plan_name_fail");
		UpdateProvisionBody body = new UpdateProvisionBody(plan_id);
		
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());

		assertTrue(response.getBody().contains("not supported Plan :["+plan_id.split(" ")[2]+"]"));
		assertEquals(response.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY);
		System.out.println("UP03 End - invalid_plan_name");
	}
	
	//UP04 요청된 플랜아이디에 포함된 서비스명과 서비스버전이 DB에 저장된 서비스 정보와 일치하지 않는 케이스
	@Test
	public void UP04_invalid_plan_id() {
		
		System.out.println("UP04 Start - invalid_plan_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id_fail");
		UpdateProvisionBody body = new UpdateProvisionBody(plan_id);
		
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());

		assertTrue(response.getBody().contains("invalid PlanID :["+plan_id+"]. valid PlanID"));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("UP04 End - invalid_plan_id");
	}
	
	//UP05 인스턴스에 해당하는 유저가 API플랫폼에서 삭제된 케이스
	//API플랫폼 관리콘솔에서 유저를 직접 삭제하고 테스트해야한다.
	@Test
	public void UP05_removed_user() {
		
		System.out.println("UP05 Start - removed_user");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("removed_user_instance_id");
		String plan_id = prop.getProperty("test_plan_id_other");
		UpdateProvisionBody body = new UpdateProvisionBody(plan_id);
		
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());

		assertTrue(response.getBody().equals("{}"));
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		System.out.println("UP05 End - removed_user");
	}
	
	//유저정보를 DB에서 찾을 수 없는 케이스
	@Test
	public void UP06_removed_user_DB() {
			
		System.out.println("UP06 Start - removed_user_DB");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("removed_user_instance_id_DB");
		String plan_id = prop.getProperty("test_plan_id_other");
		UpdateProvisionBody body = new UpdateProvisionBody(plan_id);
		
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());

		assertTrue(response.getBody().contains("APIPlatform user information not found. OrganizationGUID "));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("UP06 End - removed_user_DB");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//인스턴스 정보를 DB에서 찾을 수 없는 케이스
		@Test
		public void UP09_removed_instance_id_DB() {
				
			System.out.println("UP09 Start - removed_instance_id_DB");
			
			HttpHeaders headers = new HttpHeaders();	
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
			headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
			
			String instance_id = prop.getProperty("test_instance_id_fail");
			String plan_id = prop.getProperty("test_plan_id");
			UpdateProvisionBody body = new UpdateProvisionBody(plan_id);
			
			HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
			ResponseEntity<String> response = null;

			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

			response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
			System.out.println(response.getStatusCode());
			System.out.println(response.getBody());

			assertTrue(response.getBody().contains("Service Instnace information not found. ServiceInstanceID "));
			assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
			System.out.println("UP09 End - removed_instance_id_DB");
		}
	
	@AfterClass
	public static void plan_change() {
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		UpdateProvisionBody body = new UpdateProvisionBody(plan_id);
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		
		
		instance_id = prop.getProperty("removed_user_instance_id");
		plan_id = prop.getProperty("test_plan_id");
		entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		response = null;
		url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
	}
}
