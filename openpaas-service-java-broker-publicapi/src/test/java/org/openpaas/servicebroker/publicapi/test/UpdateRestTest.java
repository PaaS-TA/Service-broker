package org.openpaas.servicebroker.publicapi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openpaas.servicebroker.publicapi.common.HttpClientUtils;
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

	//UP03 요청된 플랜아이디에 포함된 플랜명이 설정되어 있지 않은 플랜명인 케이스
	@Test
	public void UP03_invalid_plan_name() {
		
		System.out.println("UP03 Start - invalid_plan_name");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("tets_invalid_plan_id");
		UpdateProvisionBody body = new UpdateProvisionBody(plan_id);
		
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());

		assertTrue(response.getBody().contains("Invalid PlanID : ["+plan_id+"]"));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("UP03 End - invalid_plan_name");
	}

	//UP11 하나의 플랜만 사용가능하도록 설정된 케이스
	@Test
	public void UP11_only_one_plan_available() {
			
		System.out.println("UP11 Start - only_one_plan_available");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_one_plan_id");
		UpdateProvisionBody body = new UpdateProvisionBody(plan_id);
		
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;
		
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		
		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		
		assertTrue(response.getBody().contains("Service instance update not supported"));
		assertEquals(response.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY);
		System.out.println("UP11 End - only_one_plan_available");
	}
	
	//UP04 해당 서비스에 사용가능한 플랜이 없는 케이스
	@Test
	public void UP04_no_plan_available() {
			
		System.out.println("UP04 Start - no plan available");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_no_plan_id");
		UpdateProvisionBody body = new UpdateProvisionBody(plan_id);
		
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;
		
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		
		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		
		assertTrue(response.getBody().contains("There is no plan information."));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("UP04 End - no plan available");
	}
}
