package org.openpaas.servicebroker.apiplatform.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.openpaas.servicebroker.common.ProvisionBody;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * 시작전에 Spring Boot로 Service Broker를 띄워 놓구 진행합니다.
 * 향후에 Spring Configuration?으로 서비스를 시작하게 만들 예정
 * 
 * @author ahnchan
 */
public class ProvisionRestTest {
	
	private static Properties prop = new Properties();
	
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
	
	/**
	 * Provision data Validation
	 * - valid data 
	 */
	@Test
	public void sendProvision_validData() {
		
		System.out.println("Start - valid data");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = UUID.randomUUID().toString();
		String organization_guid = UUID.randomUUID().toString();
		String space_guid = UUID.randomUUID().toString();
		
//		String body = new ProvisionBody(prop.getProperty("provision_service_id"), prop.getProperty("provision_plan_id"), organization_guid, space_guid).convertToJsonString();
		ProvisionBody body = new ProvisionBody(prop.getProperty("provision_service_id"), prop.getProperty("provision_plan_id"), organization_guid, space_guid);
		
//		HttpEntity<String> entity = new HttpEntity<String>(body, headers);		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		System.out.println("url:"+url);
		System.out.println("body:"+body);
		
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
	
		assertTrue(response.getBody().contains("dashboard_url"));
		assertEquals(response.getStatusCode(), HttpStatus.CREATED);
		
		System.out.println("End - valid data");
	}


	/**
	 * Provision data Validation 
	 * - no Service id
	 */
//	@Test
//	public void sendProvision_noMandatory_serviceID() {
//		
//		System.out.println("Start - no mandatory service id");
//		
//		HttpHeaders headers = new HttpHeaders();	
//		headers.setContentType(MediaType.APPLICATION_JSON);
//		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
//		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
//		
//		String instance_id = UUID.randomUUID().toString();
//		String organization_guid = UUID.randomUUID().toString();
//		String space_guid = UUID.randomUUID().toString();
//		
//		ProvisionBody body = new ProvisionBody(prop.getProperty("provision_service_id_annother"), prop.getProperty("provision_plan_id"), organization_guid, space_guid);
//		
//		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
//		ResponseEntity<String> response = null;
//
//
//		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
//		System.out.println("url:"+url);
//		System.out.println("body:"+body);
//		
//		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
//			
//		assertTrue(response.getBody().contains("ServiceDefinition does not exist: id"));
//		assertEquals(response.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY);
//		
//		System.out.println("End - no mandatory service id");
//	}

	/**
	 * Provision data Validation 
	 * - fail Service id
	 */
//	@Test
	public void sendProvision_fail_serviceID() {
		
		System.out.println("Start - fail service id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = UUID.randomUUID().toString();
		String organization_guid = UUID.randomUUID().toString();
		String space_guid = UUID.randomUUID().toString();
		
		ProvisionBody body = new ProvisionBody(prop.getProperty("provision_service_id_fail"), prop.getProperty("provision_plan_id"), organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		System.out.println("url:"+url);
		System.out.println("body:"+body);
		
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);

		assertTrue(response.getBody().contains("ServiceDefinition does not exist: id"));
		assertEquals(response.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY);
		
		System.out.println("End - fail service id");
	}
	
	//모든 파라미터가이미 DB에 저장된 것과 같은 값으로 들어온 경우, 200 OK
	@Test	
	public void sendProvision_duplicate_instance() {
		
		System.out.println("Start - duplicate instance");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("provision_duplicate_instance_id");
		String organization_guid = prop.getProperty("provision_duplicate_org_guid");
		String space_guid = prop.getProperty("provision_duplicate_space_id");
		String service_id= prop.getProperty("provision_duplicate_service_id");
		String plan_id=prop.getProperty("provision_duplicate_plan_id");
		
		ProvisionBody body = new ProvisionBody(service_id,plan_id , organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;
		
			
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		System.out.println("url:"+url);
		System.out.println("body:"+body);
		
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());
		assertTrue(response.getBody().contains("dashboard_url"));
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		
		System.out.println("End - duplicate instance");
	}
	
	//409 Conflict를 확인하기 위해 인스턴스 아이디는 동일하게 서비스 아이디는 다르게 요청을 보냄
	@Test	
	public void sendProvision_instance_different_service_id() {
		
		System.out.println("Start - duplicate instance and different service id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("provision_duplicate_instance_id");
		String organization_guid = UUID.randomUUID().toString();
		String space_guid = UUID.randomUUID().toString();
		String service_id= prop.getProperty("provision_different_service_id");
		String plan_id=prop.getProperty("provision_plan_id");
		
		ProvisionBody body = new ProvisionBody(service_id,plan_id, organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;
		
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		System.out.println("url:"+url);
		System.out.println("body:"+body);
		
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());
		assertEquals(response.getStatusCode(), HttpStatus.CONFLICT);
		assertEquals(response.getBody(),"{}");
		
		System.out.println("End - duplicate instance and different service id");
	}
	
//각각의 파라미터들이 빈값으로 요청되었을 때
	//인스턴스 아이디로 빈값이 들어왔을때
	@Test	
	public void sendProvision_empty_parameters_instance_id() {
		
		System.out.println("Start - empty parameters_instance id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = "";
		String organization_guid = UUID.randomUUID().toString();
		String space_guid = UUID.randomUUID().toString();
		String service_id= prop.getProperty("provision_different_service_id");
		String plan_id= prop.getProperty("provision_duplicate_plan_id");
		
		ProvisionBody body = new ProvisionBody(service_id,plan_id, organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		System.out.println("url:"+url);
		System.out.println("body:"+body);
		
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

		System.out.println("End - empty parameters_instance id");
	}
	//org아이디로 빈값이 들어왔을때
	@Test	
	public void sendProvision_empty_parameters_organization_guid() {
		
		System.out.println("Start - empty parameters_organization id");
		
		String instance_id = prop.getProperty("provision_duplicate_instance_id");
		String organization_guid ="";
		String space_guid = UUID.randomUUID().toString();
		String service_id= prop.getProperty("provision_different_service_id");
		String plan_id=prop.getProperty("provision_duplicate_plan_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		

		ProvisionBody body = new ProvisionBody(service_id,plan_id, organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		System.out.println("url:"+url);
		System.out.println("body:"+body);
		
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		
		assertTrue(response.getBody().contains("Missing required fields: organizationGuid"));
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
	
		System.out.println("End - empty parameters_organization id");
	}
	//서비스 아이디로 빈값이 들어왔을때
	@Test	
	public void sendProvision_empty_parameters_service_id() {
		
		System.out.println("Start - empty parameters_service_id");
		
		String instance_id = prop.getProperty("provision_duplicate_instance_id");
		String organization_guid =UUID.randomUUID().toString();
		String space_guid = UUID.randomUUID().toString();
		String service_id= "";
		String plan_id=prop.getProperty("provision_duplicate_plan_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		

		ProvisionBody body = new ProvisionBody(service_id,plan_id, organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		System.out.println("url:"+url);
		System.out.println("body:"+body);
		
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
		assertTrue(response.getBody().contains("Missing required fields: serviceDefinitionId"));
	
		System.out.println("End - empty parameters_service_id");
	}

	//플랜아이디로 빈값이 들어왔을때
	@Test	
	public void sendProvision_empty_parameters_plan_id() {
		
		System.out.println("Start - empty parameters_plan_id");
		
		String instance_id = UUID.randomUUID().toString();
		String organization_guid =UUID.randomUUID().toString();
		String space_guid = UUID.randomUUID().toString();
		String service_id= prop.getProperty("provision_duplicate_service_id");
		String plan_id = "";
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		

		ProvisionBody body = new ProvisionBody(service_id, plan_id, organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		System.out.println("url:"+url);
		System.out.println("body:"+body);
		
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
		assertTrue(response.getBody().contains("Missing required fields: planId"));
	
		System.out.println("End - empty parameters_plan_id");
	}
	
	//존재하지 않는 플랜 아이디를 요청한 경우
	@Test	
	public void sendProvision_fail_plan_id() {
		
		System.out.println("Start - fail Plan Id");
		
		String instance_id = UUID.randomUUID().toString();
		String organization_guid =UUID.randomUUID().toString();
		String space_guid = UUID.randomUUID().toString();
		String service_id= prop.getProperty("provision_service_id");
		String plan_id = prop.getProperty("provision_plan_id_fail");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		

		ProvisionBody body = new ProvisionBody(service_id, plan_id, organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		System.out.println("url:"+url);
		System.out.println("body:"+body);
		
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());
		
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid Plan Name - Plan Name must be 'Unlimited'"));
	
		System.out.println("End - fail Plan Id");
	}
	
	
	//인스턴스 아이디가 같고 org아이디가 다른케이스
	@Test	
	public void sendProvision_intance_id_different_org() {
		
		System.out.println("Start - intance_id_different_org");
		
		String instance_id = "instance_id";
		String organization_guid =UUID.randomUUID().toString();
		String space_guid = UUID.randomUUID().toString();
		String service_id= prop.getProperty("provision_service_id");
		String plan_id = prop.getProperty("provision_plan_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		ProvisionBody body = new ProvisionBody(service_id, plan_id, organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		System.out.println("url:"+url);
		System.out.println("body:"+body);
		
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());
		
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("incorrect OrgId : ["+organization_guid+"]"));
	
		System.out.println("End - intance_id_different_org");
	}
	
}
