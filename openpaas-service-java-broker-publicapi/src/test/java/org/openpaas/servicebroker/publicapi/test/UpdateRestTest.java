package org.openpaas.servicebroker.publicapi.test;

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
import org.openpaas.servicebroker.publicapi.common.APIPlatformAPI;
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
	
	private static String uri;
	private static String parameters;
	private static APIPlatformAPI api = new APIPlatformAPI();
	private static String cookie;
	
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
	
	//UP06 유저정보를 DB에서 찾을 수 없는 케이스
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
	
	//UP07 어플리케이션이 API플랫폼에서 삭제된 케이스
	@Test
	public void UP07_removed_application() {
			
		System.out.println("UP07 Start - removed_application");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("removed_app_instance_id");
		String plan_id = prop.getProperty("test_plan_id_other");
		UpdateProvisionBody body = new UpdateProvisionBody(plan_id);
		
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());

		assertTrue(response.getBody().contains("{}"));
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		System.out.println("UP07 End - removed_application");
		}
	
	//UP08 API의 사용등록이 API플랫폼에서 해제된 케이스
	@Test
	public void UP08_removed_subscription() {
			
		System.out.println("UP08 Start - removed_subscription");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("removed_subs_instance_id");
		String plan_id = prop.getProperty("test_plan_id_other");
		UpdateProvisionBody body = new UpdateProvisionBody(plan_id);
		
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;

		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());

		assertTrue(response.getBody().contains("{}"));
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		System.out.println("UP08 End - removed_subscription");
		}

	//UP09 인스턴스 정보를 DB에서 찾을 수 없는 케이스
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
	
	//UP10 사용등록된 API의 라이프사이클이 변경된 케이스
	@Test
	public void UP10_lifecycle_changed_test() {
			
		System.out.println("UP10 Start - lifecycle_changed_test");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("lifecycle_change_instance_id");
		String plan_id = prop.getProperty("lifecycle_change_plan_id_other");
		UpdateProvisionBody body = new UpdateProvisionBody(plan_id);
		
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;
		
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		
		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		
		assertTrue(response.getBody().contains("API lifecycle changed."));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("UP10 End - lifecycle_changed_test");
	}
	
	//UP11 하나의 플랜만 사용가능하도록 설정된 케이스
	//프로퍼티 파일 변경하고 테스트
//	@Test
	public void UP11_only_one_plan_available() {
			
		System.out.println("UP11 Start - only_one_plan_available");
		
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
		
		assertTrue(response.getBody().contains("Plan update not supported."));
		assertEquals(response.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY);
		System.out.println("UP11 End - only_one_plan_available");
	}
	
	//UP13 사용등록이 해제되었고 라이프사이클도 변경된 케이스
	@Test
	public void UP13_no_subs_and_lifecycle_changed() {
			
		System.out.println("UP13 Start - no_subs_and_lifecycle_changed");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		String instance_id = prop.getProperty("lifecycle_change_and_no_subs_instance_id");
		String plan_id = prop.getProperty("lifecycle_change_plan_id_other");
		UpdateProvisionBody body = new UpdateProvisionBody(plan_id);

		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		ResponseEntity<String> response = null;
		
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		
		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		
		assertTrue(response.getBody().contains("removed subscrition and API lifecycle changed. Application"));
		assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		System.out.println("UP13 End - no_subs_and_lifecycle_changed");
	}
		
	@AfterClass
	public static void plan_recover() {
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		
		//정상 동작 케이스 플랜복구
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		UpdateProvisionBody body = new UpdateProvisionBody(plan_id);
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		ResponseEntity<String> response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println(response.getBody());
		System.out.println("====================");
		
		//유저삭제 케이스 플랜 복구
		instance_id = prop.getProperty("removed_user_instance_id");
		plan_id = prop.getProperty("test_plan_id");
		entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println("====================");
		
		//사용등록 해제 케이스 플랜 복구
		instance_id = prop.getProperty("removed_subs_instance_id");
		plan_id = prop.getProperty("test_plan_id");
		body = new UpdateProvisionBody(plan_id);
		entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println("====================");
		
		
		//어플리케이션 삭제 케이스 플랜 복구
		instance_id = prop.getProperty("removed_app_instance_id");
		plan_id = prop.getProperty("test_plan_id");
		body = new UpdateProvisionBody(plan_id);
		entity = new HttpEntity<UpdateProvisionBody>(body, headers);		
		url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id;
		HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
		System.out.println(response.getBody());
		
	}
}
