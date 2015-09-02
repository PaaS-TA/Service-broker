package org.openpaas.servicebroker.publicapi.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openpaas.servicebroker.publicapi.common.HttpClientUtils;
import org.openpaas.servicebroker.common.ProvisionBody;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;


/**
 * 시작전에 Spring Boot로 Service Broker를 띄워 놓구 진행합니다.
 * 향후에 Spring Configuration?으로 서비스를 시작하게 만들 예정
 * 
 * @author ahnchan
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProvisionRestTest {
	
	private static Properties prop = new Properties();
	
	private static String uri;
	private static String parameters;
	private static String cookie;
	
	@BeforeClass
	public static void init() {
		
		System.out.println("== Started test Provision API ==");

		// Initialization
		// Get properties information
		String propFile = "test.properties";
 
		InputStream inputStream = ProvisionRestTest.class.getClassLoader().getResourceAsStream(propFile);
		
		try {
			prop.load(inputStream);
	 	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	 		System.err.println(e);
	 	}
	}
	//P001 적절한 데이터로 요청이 들어온 케이스 - 201 CREATED
	@Test
	public void P001_validData() {
		
		System.out.println("P001_Start - valid data");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = UUID.randomUUID().toString();
		String organization_guid = prop.getProperty("test_org_guid");
		String space_guid = prop.getProperty("test_space_guid");
		
//		String body = new ProvisionBody(prop.getProperty("provision_service_id"), prop.getProperty("provision_plan_id"), organization_guid, space_guid).convertToJsonString();
		ProvisionBody body = new ProvisionBody(prop.getProperty("test_service_id"), prop.getProperty("test_plan_id"), organization_guid, space_guid);
		
//		HttpEntity<String> entity = new HttpEntity<String>(body, headers);		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
	
		System.out.println(response.getBody());
		assertTrue(response.getBody().contains("dashboard_url"));
		assertEquals(response.getStatusCode(), HttpStatus.CREATED);
		System.out.println("P001_End - valid data");
	}
	
	//P010 각각의 파라미터들이 빈값으로 요청되었을 때
	//인스턴스 아이디로 빈값이 들어왔을때 - 404 Not found
	@Test	
	public void P010_empty_parameters_instance_id() {
		
		System.out.println("P010_Start - empty parameters_instance id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = "";
		String organization_guid = prop.getProperty("test_org_guid");
		String space_guid =  prop.getProperty("test_space_guid");
		String service_id= prop.getProperty("test_other_service_id");
		String plan_id= prop.getProperty("test_plan_id");
		
		ProvisionBody body = new ProvisionBody(service_id,plan_id, organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());
		
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		System.out.println("P010_End - empty parameters_instance id");
	}
	//P011 org아이디로 빈값이 들어왔을때 - 422 UNPROCESSABLE_ENTITY
	@Test	
	public void P011_empty_parameters_organization_guid() {
		
		System.out.println("P011_Start - empty parameters_organization id");
		
		String instance_id = prop.getProperty("test_instance_id");
		String organization_guid ="";
		String space_guid = prop.getProperty("test_space_guid");
		String service_id= prop.getProperty("test_service_id");
		String plan_id=prop.getProperty("test_plan_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		

		ProvisionBody body = new ProvisionBody(service_id,plan_id, organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());
		
		assertTrue(response.getBody().contains("Missing required fields: organizationGuid"));
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
		System.out.println("P011_End - empty parameters_organization id");
	}
	
	//P012 서비스 아이디로 빈값이 들어왔을때 - 422 UNPROCESSABLE_ENTITY
	@Test	
	public void P012_empty_parameters_service_id() {
		
		System.out.println("P012_Start - empty parameters_service_id");
		
		String instance_id = prop.getProperty("test_instance_id");
		String organization_guid =prop.getProperty("test_org_guid");
		String space_guid = prop.getProperty("test_space_guid");
		String service_id= "";
		String plan_id=prop.getProperty("test_plan_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		ProvisionBody body = new ProvisionBody(service_id,plan_id, organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
		assertTrue(response.getBody().contains("Missing required fields: serviceDefinitionId"));
		System.out.println("P012_End - empty parameters_service_id");
	}

	//P013 플랜아이디로 빈값이 들어왔을때 - 422 UNPROCESSABLE_ENTITY
	@Test	
	public void P013_empty_parameters_plan_id() {
		
		System.out.println("P013_Start - empty parameters_plan_id");
		
		String instance_id = UUID.randomUUID().toString();
		String organization_guid =UUID.randomUUID().toString();
		String space_guid = UUID.randomUUID().toString();
		String service_id= prop.getProperty("test_service_id");
		String plan_id = "";
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		ProvisionBody body = new ProvisionBody(service_id, plan_id, organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
	
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
		assertTrue(response.getBody().contains("Missing required fields: planId"));

		System.out.println("P013_End - empty parameters_plan_id");
	}
	
	
//	@Test	
//	public void sendProvision_intance_id_other_org() {
//		
//		System.out.println("Start - intance_id_other_org");
//		
//		String instance_id = "instance_id";
//		String organization_guid =UUID.randomUUID().toString();
//		String space_guid = UUID.randomUUID().toString();
//		String service_id= prop.getProperty("provision_service_id");
//		String plan_id = prop.getProperty("provision_plan_id");
//		
//		HttpHeaders headers = new HttpHeaders();	
//		headers.setContentType(MediaType.APPLICATION_JSON);
//		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
//		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
//
//		ProvisionBody body = new ProvisionBody(service_id, plan_id, organization_guid, space_guid);
//		
//		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
//		ResponseEntity<String> response = null;
//
//		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
//		System.out.println("url:"+url);
//		System.out.println("body:"+body);
//		
//		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
//		System.out.println(response.getBody());
//		
//		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
//		assertTrue(response.getBody().contains("incorrect OrgId : ["+organization_guid+"]"));
//	
//		System.out.println("End - intance_id_other_org");
//	}
	
}
