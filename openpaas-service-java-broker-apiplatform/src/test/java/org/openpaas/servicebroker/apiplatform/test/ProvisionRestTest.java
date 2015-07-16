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
import org.openpaas.servicebroker.apiplatform.common.APIPlatformAPI;
import org.openpaas.servicebroker.common.HttpClientUtils;
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
	private static APIPlatformAPI api = new APIPlatformAPI();
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
		//스토어 로그인
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.Login");
		parameters = "action=login&username=test-user&password=openpaas";
		cookie = api.login(uri, parameters);
		
		//삭제된 어플리케이션
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.RemoveApplication");
		parameters = "action=removeApplication&application="+prop.getProperty("removed_app_instance_id");
		api.removeApplication(uri, parameters, cookie);
		
		//사용등록 해제
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.RemoveSubscription");
		parameters = "action=removeSubscription&name=Naver&version=1.0.0&provider=apicreator&applicationId=421";
		api.removeSubscription(uri, parameters, cookie);
		
	//라이프사이클 변경 케이스
		//퍼블리셔로그인
		String cookie2 =null;
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.PublisherLogin");
		parameters = "action=login&username=apipublisher&password=qwer1234";
		cookie2 = api.login(uri, parameters);
		
		//라이프사이클 변경 PUBLISHED -> CREATED
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.LifecycleChange");
		parameters ="action=updateStatus&name=PhoneVerification&version=2.0.0&provider=apicreator&status=CREATED&publishToGateway=true&requireResubscription=true";
		api.lifecycleChange(uri, parameters, cookie2);
	}
	//P001 적절한 데이터로 요청이 들어온 케이스 - 201 CREATED
//	@Test
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

	//P002 요청된 인스턴스 아이디가 DB에 존재할때, API플랫폼에는 존재하지만 DB에 저장된 인스턴스 정보와는 다른 서비스ID(서비스명+버전)와 플랜아이디가 요청된 케이스
	//409 Conflict
	@Test
	public void P002_duplicate_instance_other_serviceID_planID() {
		
		System.out.println("P002_Start - duplicate_instance_other_serviceID");
		
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
		System.out.println("P002_End - duplicate_instance_other_serviceID");
	}
	//P016 요청된 인스턴스 아이디가 DB에 존재할때, API플랫폼에는 존재하지만 DB에 저장된 인스턴스 정보와는 다른 서비스ID가 요청된 케이스
	//500 INTERNAL_SERVER_ERROR
	@Test
	public void P016_duplicate_instance_other_serviceID() {
		
		System.out.println("P016_Start - duplicate_instance_other_serviceID");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("test_instance_id");
		String organization_guid = prop.getProperty("test_org_guid");
		String space_guid = prop.getProperty("test_space_guid");
		
		ProvisionBody body = new ProvisionBody(prop.getProperty("test_other_service_id"), prop.getProperty("test_plan_id"), organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);

		System.out.println(response.getBody());

		assertTrue(response.getBody().contains("Invalid PlanID"));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("P016_End - duplicate_instance_other_serviceID");
	}
	
	//P003 요청된 인스턴스 아이디가 DB에 존재할때, 존재하지 않는 서비스아이디를 요청한 경우 - 422 UNPROCESSABLE_ENTITY
	@Test
	public void P003_duplicate_instance_fail_serviceID() {
		
		System.out.println("P003_Start - duplicate_instance_fail_serviceID");
		
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
		System.out.println("P003_End - duplicate_instance_fail_serviceID");
	}
	

	//P004 요청된 인스턴스 아이디가 DB에 존재할때, 사용가능한 플랜이지만 DB에 저장된 인스턴스 정보와는 다른 플랜아이디를 요청한 경우  - 409 CONFLICT
	//aplication-mvc.properties 파일에 AvailablePlan을 추가하고  테스트 해야함.
//	@Test
	public void P004_duplicate_instance_other_planID() {
		
		System.out.println("P004_Start - duplicate_instance_fail_planID");
		
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
		System.out.println("P004_End - duplicate_instance_fail_planID");
	}
	
	//P005 플랜ID에 포함된 플랜명이 잘못된(API플랫폼에 존재하지 않는, 사용가능 하도록 설정된 플랜이 아닌) 케이스  - 500 INTERNAL_SERVER_ERROR
	@Test
	public void P005_duplicate_instance_fail_planName() {
		
		System.out.println("P005_Start - duplicate_instance_fail_planName");
		
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

		assertTrue(response.getBody().contains("Invalid PlanID"));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("P005_End - duplicate_instance_fail_planName");
	}
	
	//P006 플랜명은 정상이나, 플랜ID가 잘못된 케이스  - 500 INTERNAL_SERVER_ERROR	
	@Test
	public void P006_duplicate_instance_fail_planID() {
		
		System.out.println("P006_Start - duplicate_instance_fail_planID");
		
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

		assertTrue(response.getBody().contains("Invalid PlanID"));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("P006_End - duplicate_instance_fail_planID");
	}
	
	
	//P007 요청된 인스턴스 아이디가 DB에 존재하지 않을 때,존재하지 않는 서비스아이디가 요청들어온 경우  - 422 UNPROCESSABLE_ENTITY
	@Test
	public void P007_fail_serviceID() {
		
		System.out.println("P007_Start - fail service id");
		
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
		System.out.println("P007_End - fail service id");
	}
	
	//P008 요청된 인스턴스 아이디가 DB에 존재하지 않을 때,존재하지 않는 플랜아이디가 요청들어온 경우 - 500 INTERNAL_SERVER_ERROR
	@Test
	public void P008_fail_planID() {
		
		System.out.println("P008_Start - fail plan id");
		
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
		assertTrue(response.getBody().contains("Invalid ServiceID or Invalid PlanID."));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("P008_End - fail plan id");
	}
	
	
	//P009 모든 파라미터가 이미 DB에 저장된 것과 같은 값으로 들어온 경우 - 200 OK
	@Test	
	public void P009_duplicate_instance() {
		
		System.out.println("P009_Start - duplicate instance");
		
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
		System.out.println("P009_End - duplicate instance");
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
	
	
	//P014 인스턴스 아이디가 같고 해당인스턴스 아이디로 DB에 저장된 org아이디와 다른 org아이디 요청이 들어온 케이스 - 409 Conflict
	@Test
	public void P014_duplicate_instance_other_orgID() {
		
		System.out.println("P014_Start - duplicate_instance_other_orgID");
		
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
		System.out.println("P014_End - duplicate_instance_other_orgID");
	}
	
	
	//P015 같은 org,스페이스에서 같은 서비스, 같은 플랜 but 다른 인스턴스 아이디로 요청된 케이스 - 201 CREATED
//	@Test
	public void P015_duplicate_all_except_instanceID() {
		
		System.out.println("P015_Start - duplicate_all_except_instanceID");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = UUID.randomUUID().toString();
		String organization_guid = prop.getProperty("test_org_guid");
		String space_guid = prop.getProperty("test_space_guid");
		
		ProvisionBody body = new ProvisionBody(prop.getProperty("test_service_id"), prop.getProperty("test_plan_id"), organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());
		assertTrue(response.getBody().contains("dashboard_url"));
		assertEquals(response.getStatusCode(), HttpStatus.CREATED);
		
		System.out.println("P015_End - duplicate_all_except_instanceID");
	}
	
	//P017 API플랫폼 DB에서 이미 삭제처리된(delyn값이 Y인) 인스턴스 아이디로 요청이 들어온 경우 
	@Test
	public void P017_removed_instance_id() {
		
		System.out.println("P017_Start - removed_instance_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("removed_instance_id");
		String organization_guid = prop.getProperty("test_org_guid");
		String space_guid = prop.getProperty("test_space_guid");
		
		ProvisionBody body = new ProvisionBody(prop.getProperty("test_service_id"), prop.getProperty("test_plan_id"), organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);

		System.out.println(response.getBody());

		assertTrue(response.getBody().contains("already removed Service Instance"));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("P017_End - removed_instance_id");
	}
	//P018 서비스아이디의 서비스명과 플랜아이디의 서비스명이 일치하지 않는 케이스
	@Test
	public void P018_invalid_serviceID_planID() {
		
		System.out.println("P018_Start - invalid_serviceID_planID");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = UUID.randomUUID().toString();
		String organization_guid = prop.getProperty("test_org_guid");
		String space_guid = prop.getProperty("test_space_guid");
		
		ProvisionBody body = new ProvisionBody(prop.getProperty("test_service_id"), prop.getProperty("test_other_plan_id"), organization_guid, space_guid);
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);

		System.out.println(response.getBody());

		assertTrue(response.getBody().contains("Invalid ServiceID or Invalid PlanID."));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("P018_End - invalid_serviceID_planID");
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
