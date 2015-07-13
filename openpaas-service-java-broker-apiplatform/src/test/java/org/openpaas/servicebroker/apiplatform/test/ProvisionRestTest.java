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
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
	//P001 적절한 데이터로 요청이 들어온 케이스 - 201 CREATED
	@Test
	public void P001_sendProvision_validData() {
		
		System.out.println("Start - valid data");
		
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
		System.out.println("End - valid data");
	}

	//P002 요청된 인스턴스 아이디가 DB에 존재할때, API플랫폼에는 존재하지만 DB에 저장된 인스턴스 정보와는 다른 서비스ID(서비스명+버전)와 플랜아이디가 요청된 케이스
	//409 Conflict
	@Test
	public void P002_sendProvision_duplicate_instance_other_serviceID() {
		
		System.out.println("Start - duplicate_instance_other_serviceID");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String organization_guid = prop.getProperty("test_org_guid");
		String space_guid = prop.getProperty("test_space_guid");
		
		ProvisionBody body = new ProvisionBody(prop.getProperty("test_other_service_id"), prop.getProperty("test_other_plan_id"), organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());
		assertTrue(response.getBody().equals("{}"));
		assertEquals(response.getStatusCode(), HttpStatus.CONFLICT);
		System.out.println("End - duplicate_instance_other_serviceID");
	}
	
	//P003 요청된 인스턴스 아이디가 DB에 존재할때, 존재하지 않는 서비스아이디를 요청한 경우 - 422 UNPROCESSABLE_ENTITY
	@Test
	public void P003_sendProvision_duplicate_instance_fail_serviceID() {
		
		System.out.println("Start - duplicate_instance_fail_serviceID");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String organization_guid = prop.getProperty("test_org_guid");
		String space_guid = prop.getProperty("test_space_guid");
		
		ProvisionBody body = new ProvisionBody(prop.getProperty("test_service_id_fail"), prop.getProperty("test_plan_id"), organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());

		assertTrue(response.getBody().contains("ServiceDefinition does not exist: id = "+prop.getProperty("test_service_id_fail")));
		assertEquals(response.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY);
		System.out.println("End - duplicate_instance_fail_serviceID");
	}
	

	//P004 요청된 인스턴스 아이디가 DB에 존재할때, 사용가능한 플랜이지만 DB에 저장된 인스턴스 정보와는 다른 플랜아이디를 요청한 경우  - 409 CONFLICT
	//aplication-mvc.properties 파일에 AvailablePlan을 추가하고  테스트 해야함.
//	@Test
	public void P004_sendProvision_duplicate_instance_other_planID() {
		
		System.out.println("Start - duplicate_instance_fail_planID");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String organization_guid = prop.getProperty("test_org_guid");
		String space_guid = prop.getProperty("test_space_guid");
		
		ProvisionBody body = new ProvisionBody(prop.getProperty("test_service_id"), prop.getProperty("test_other_plan_id"), organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);

		System.out.println(response.getBody());
		assertTrue(response.getBody().contains("{}"));
		assertEquals(response.getStatusCode(), HttpStatus.CONFLICT);
		System.out.println("End - duplicate_instance_fail_planID");
	}
	//P005 플랜ID에 포함된 플랜명이 잘못된(API플랫폼에 존재하지 않는, 사용가능 하도록 설정된 플랜이 아닌) 케이스  - 500 INTERNAL_SERVER_ERROR
	@Test
	public void P005_sendProvision_duplicate_instance_fail_planName() {
		
		System.out.println("Start - duplicate_instance_fail_planName");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String organization_guid = prop.getProperty("test_org_guid");
		String space_guid = prop.getProperty("test_space_guid");
		
		ProvisionBody body = new ProvisionBody(prop.getProperty("test_service_id"), prop.getProperty("test_plan_id_plan_name_fail"), organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);

		System.out.println(response.getBody());

		assertTrue(response.getBody().contains("invalid PlanId"));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("End - duplicate_instance_fail_planName");
	}
	
	//P006 플랜명은 정상이나, 플랜ID가 잘못된 케이스  - 500 INTERNAL_SERVER_ERROR	
	@Test
	public void P006_sendProvision_duplicate_instance_fail_planID() {
		
		System.out.println("Start - duplicate_instance_fail_planID");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String organization_guid = prop.getProperty("test_org_guid");
		String space_guid = prop.getProperty("test_space_guid");
		
		ProvisionBody body = new ProvisionBody(prop.getProperty("test_service_id"), prop.getProperty("test_plan_id_fail"), organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);

		System.out.println(response.getBody());

		assertTrue(response.getBody().contains("invalid PlanID"));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("End - duplicate_instance_fail_planID");
	}
	
	
	//P007 요청된 인스턴스 아이디가 DB에 존재하지 않을 때,존재하지 않는 서비스아이디가 요청들어온 경우  - 500 INTERNAL_SERVER_ERROR
	@Test
	public void P007_sendProvision_fail_serviceID() {
		
		System.out.println("Start - fail service id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = UUID.randomUUID().toString();
		String organization_guid = prop.getProperty("test_org_guid");
		String space_guid = prop.getProperty("test_space_guid");
		
		ProvisionBody body = new ProvisionBody(prop.getProperty("test_service_id_fail"), prop.getProperty("test_plan_id"), organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
	
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);

		System.out.println(response.getBody());
		assertTrue(response.getBody().contains("ServiceDefinition does not exist: id = "+prop.getProperty("test_service_id_fail")));
		assertEquals(response.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY);
		System.out.println("End - fail service id");
	}
	
	//P008 요청된 인스턴스 아이디가 DB에 존재하지 않을 때,존재하지 않는 플랜아이디가 요청들어온 경우 - 500 INTERNAL_SERVER_ERROR
	@Test
	public void P008_sendProvision_fail_planID() {
		
		System.out.println("Start - fail plan id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = UUID.randomUUID().toString();
		String organization_guid = prop.getProperty("test_org_guid");
		String space_guid = prop.getProperty("test_space_guid");
		
		ProvisionBody body = new ProvisionBody(prop.getProperty("test_service_id"), prop.getProperty("test_plan_id_fail"), organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
	
		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);

		System.out.println(response.getBody());
		assertTrue(response.getBody().contains("invalid PlanId :["));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("End - fail plan id");
	}
	
	
	//P009 모든 파라미터가 이미 DB에 저장된 것과 같은 값으로 들어온 경우 - 200 OK
	@Test	
	public void P009_sendProvision_duplicate_instance() {
		
		System.out.println("Start - duplicate instance");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String organization_guid = prop.getProperty("test_org_guid");
		String space_guid = prop.getProperty("test_space_guid");
		String service_id= prop.getProperty("test_service_id");
		String plan_id=prop.getProperty("test_plan_id");
		
		ProvisionBody body = new ProvisionBody(service_id,plan_id , organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;
		
			
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());

		assertTrue(response.getBody().contains("dashboard_url"));
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		System.out.println("End - duplicate instance");
	}
	
	//P010 각각의 파라미터들이 빈값으로 요청되었을 때
	//인스턴스 아이디로 빈값이 들어왔을때 - 404 Not found
	@Test	
	public void P010_sendProvision_empty_parameters_instance_id() {
		
		System.out.println("Start - empty parameters_instance id");
		
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
		System.out.println("End - empty parameters_instance id");
	}
	//P011 org아이디로 빈값이 들어왔을때 - 422 UNPROCESSABLE_ENTITY
	@Test	
	public void P011_sendProvision_empty_parameters_organization_guid() {
		
		System.out.println("Start - empty parameters_organization id");
		
		String instance_id = prop.getProperty("test_instance_id");
		String organization_guid ="";
		String space_guid = prop.getProperty("test_space_guid");
		String service_id= prop.getProperty("test_other_service_id");
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
		System.out.println("End - empty parameters_organization id");
	}
	
	//P012 서비스 아이디로 빈값이 들어왔을때 - 422 UNPROCESSABLE_ENTITY
	@Test	
	public void P012_sendProvision_empty_parameters_service_id() {
		
		System.out.println("Start - empty parameters_service_id");
		
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
		System.out.println("End - empty parameters_service_id");
	}

	//P013 플랜아이디로 빈값이 들어왔을때 - 422 UNPROCESSABLE_ENTITY
	@Test	
	public void P013_sendProvision_empty_parameters_plan_id() {
		
		System.out.println("Start - empty parameters_plan_id");
		
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

		System.out.println("End - empty parameters_plan_id");
	}
	
	
	//P014 인스턴스 아이디가 같고 해당인스턴스 아이디로 DB에 저장된 org아이디와 다른 org아이디 요청이 들어온 케이스 - 409 Conflict
	public void P014_sendProvision_duplicate_instance_other_orgID() {
		
		System.out.println("Start - duplicate_instance_other_orgID");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String organization_guid = UUID.randomUUID().toString();
		String space_guid =  prop.getProperty("test_space_guid");
		
		ProvisionBody body = new ProvisionBody(prop.getProperty("test_service_id"), prop.getProperty("test_plan_id"), organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);

		System.out.println(response.getBody());
		assertTrue(response.getBody().equals("{}"));
		assertEquals(response.getStatusCode(), HttpStatus.CONFLICT);
		System.out.println("End - duplicate_instance_other_orgID");
	}
	
	
	//P015 같은 org,스페이스에서 같은 서비스, 같은 플랜 but 다른 인스턴스 아이디로 요청된 케이스 - 201 CREATED
	
	public void P015_sendProvision_duplicate_all_except_instanceID() {
		
		System.out.println("Start - duplicate_all_except_instanceID");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = UUID.randomUUID().toString();
		String organization_guid = prop.getProperty("test_org_guid");
		String space_guid = prop.getProperty("test_space_id");
		
		ProvisionBody body = new ProvisionBody(prop.getProperty("test_service_id"), prop.getProperty("test_plan_id"), organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());
		assertTrue(response.getBody().contains("dashboard_url"));
		assertEquals(response.getStatusCode(), HttpStatus.CREATED);
		
		System.out.println("End - duplicate_all_except_instanceID");
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
