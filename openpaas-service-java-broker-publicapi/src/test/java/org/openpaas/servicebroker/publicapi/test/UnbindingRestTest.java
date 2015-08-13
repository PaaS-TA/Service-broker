package org.openpaas.servicebroker.publicapi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.openpaas.servicebroker.publicapi.common.APIPlatformAPI;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UnbindingRestTest {

	private static Properties prop = new Properties();
	
	private static String uri;
	private static String parameters;
	private static APIPlatformAPI api = new APIPlatformAPI();
	private static String cookie;

	@BeforeClass
	public static void init() {
		
		System.out.println("== Started test Unbinding API ==");

		// Initialization
		// Get properties information
		String propFile = "test.properties";
 
		InputStream inputStream = UnbindingRestTest.class.getClassLoader().getResourceAsStream(propFile);
		
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
	
	//U001 정상적인 파라미터가 입력된경우 - 200 OK
	@Test
	public void U001_valid_parameters_test() {
		System.out.println("U001==Start - valid_parameters_test");
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		String binding_id = UUID.randomUUID().toString();
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.sendUnbinding(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody().equals("{}"));
		
		System.out.println("U001 === End - valid_parameters_test");
	}
	
	//U002 존재하지 않는 인스턴스 아이디가 요청된 경우
	@Test
	public void U002_invalid_parameters_instance_id_test() {
		System.out.println("U002 === Start - invalid_parameters_instance_id_test start");
		
		String instance_id = prop.getProperty("test_instance_id_fail");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		String binding_id = UUID.randomUUID().toString();
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.sendUnbinding(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
		assertTrue(response.getBody().contains("ServiceInstance does not exist: id = " + instance_id));
		
		System.out.println("U002 === End - invalid_parameters_instance_id_test");
	}
	
	//U003 잘못된 서비스 아이디가 요청된 경우
	@Test
	public void U003_invalid_parameters_service_id_test() {
		System.out.println("U003 === Start - invalid_parameters_service_id_test");
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id_fail");
		String binding_id = UUID.randomUUID().toString();
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.sendUnbinding(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid ServiceID :["+service_id+"] Valid ServiceID :"));
		
		System.out.println("U003 === End - invalid_parameters_service_id_test");
	}
	
	//U004 잘못된 플랜 아이디가 요청된 경우
	@Test
	public void U004_invalid_parameters_plan_id_test() {
		System.out.println("U004 === Start - invalid_parameters_plan_id_test");
		
		String instance_id = prop.getProperty("test_instance_id");
		String plan_id = prop.getProperty("test_plan_id_fail");
		String service_id= prop.getProperty("test_service_id");
		String binding_id = UUID.randomUUID().toString();
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.sendUnbinding(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid PlanID :["+plan_id+"] Valid PlanID :"));
		
		System.out.println("U004 === End - invalid_parameters_plan_id_test");
	}
	
	//U005 요청된 인스턴스 아이디에 해당하는 어플리케이션을 가지고 있는 유저가 API플랫폼에서 삭제된 케이스
	@Test
	public void U005_removed_user_test() {
		System.out.println("U005 === Start - removed_user_test");
		
		String instance_id = prop.getProperty("removed_user_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		String binding_id = UUID.randomUUID().toString();
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.sendUnbinding(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody().equals("{}"));
		
		System.out.println("U005 === End - removed_user_test");
	}
	
	//U006 요청된 인스턴스 아이디에 해당하는 어플리케이션이 API플랫폼에서 삭제된 케이스
	@Test
	public void U006_removed_application_test() {
		System.out.println("U006 === Start - removed_application_test");
		
		String instance_id = prop.getProperty("removed_app_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		String binding_id = UUID.randomUUID().toString();
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.sendUnbinding(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody().contains("{}"));
		
		System.out.println("U006 === End - removed_application_test");
	}
	
	//U007 요청된 인스턴스 아이디에 해당하는 어플리케이션에 사용등록 된 API가 API플랫폼에서 이미 사용등록이 해제된 케이스
	@Test
	public void U007_removed_subscription_test() {
		System.out.println("U007 === Start - removed_subscription_test");
		
		String instance_id = prop.getProperty("removed_subs_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		String binding_id = UUID.randomUUID().toString();
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.sendUnbinding(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody().equals("{}"));
		
		System.out.println("U007 === End - removed_subscription_test");
	}
	
	//U008 요청된 인스턴스 아이디에 해당하는 어플리케이션에 사용등록 된 API의 라이프 사이클이 변경된 케이스
	@Test
	public void U008_lifecycle_changed_test() {
		System.out.println("U008 === Start - lifecycle_changed_test");
		
		String instance_id = prop.getProperty("lifecycle_change_instance_id");
		String plan_id = prop.getProperty("lifecycle_change_plan_id");
		String service_id= prop.getProperty("lifecycle_change_service_id");
		String binding_id = UUID.randomUUID().toString();
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.sendUnbinding(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody().equals("{}"));
		
		System.out.println("U008 === End - lifecycle_changed_test");
	}
	
	//U009 DB에서 이미 삭제된 인스턴스 아이디로 요청이 들어온 케이스
	@Test
	public void U009_removed_instance_id_DB_test() {
		System.out.println("U009 === Start - removed_instance_id_DB_test");
		
		String instance_id = prop.getProperty("removed_instance_id");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		String binding_id = UUID.randomUUID().toString();
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.sendUnbinding(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("already removed Service Instance"));
		
		System.out.println("U009 === End - removed_instance_id_DB_test");
	}
	//U010 DB에서 요청된 인스턴스 아이디에 해당하는 유저정보가 삭제된 케이스
	@Test
	public void U010_removed_user_DB_test() {
		System.out.println("U010 === Start - removed_user_DB_test");
		
		String instance_id = prop.getProperty("removed_user_instance_id_DB");
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		String binding_id = UUID.randomUUID().toString();
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.sendUnbinding(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("not found APIPlatform user information. Instance ID : ["+instance_id+"]"));
		
		System.out.println("U010 === End - removed_user_DB_test");
	}
	
	@AfterClass
	public static void subscription_recover() {
	//라이프사이클 변경 케이스 사용등록 복구
		//퍼블리셔로그인
		String cookie2 =null;
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.PublisherLogin");
		parameters = "action=login&username=apipublisher&password=qwer1234";
		cookie2 = api.login(uri, parameters);
		
		//라이프사이클 변경 PUBLISHED -> CREATED
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.LifecycleChange");
		parameters ="action=updateStatus&name=PhoneVerification&version=2.0.0&provider=apicreator&status=PUBLISHED&publishToGateway=true&requireResubscription=true";
		api.lifecycleChange(uri, parameters, cookie2);
		
		//사용등록
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.AddSubscription");
		parameters = 
				"action=addAPISubscription&name=PhoneVerification&version=2.0.0&provider=apicreator&tier=Unlimited&applicationName="+prop.getProperty("lifecycle_change_instance_id");
		api.addSubscription(uri, parameters, cookie);
		
		//라이프사이클 변경 PUBLISHED -> CREATED
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.LifecycleChange");
		parameters ="action=updateStatus&name=PhoneVerification&version=2.0.0&provider=apicreator&status=CREATED&publishToGateway=true&requireResubscription=true";
		api.lifecycleChange(uri, parameters, cookie2);
		
	//정상작동 케이스 복구
		//어플리케이션 생성
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.AddAnApplication");
		parameters = 
				"action=addApplication&application="+prop.getProperty("test_instance_id")+"&tier=Unlimited&description=&callbackUrl=";	
		api.addApplication(uri, parameters, cookie);
		//사용등록
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.AddSubscription");
		parameters = 
				"action=addAPISubscription&name=Naver&version=1.0.0&provider=apicreator&tier=Unlimited&applicationName="+prop.getProperty("test_instance_id");
		api.addSubscription(uri, parameters, cookie);
	//이미 해제된 사용등록 복구
		//사용등록
		uri = prop.getProperty("APIPlatformServer")+":"+prop.getProperty("APIPlatformPort")+prop.getProperty("URI.AddSubscription");
		parameters = 
				"action=addAPISubscription&name=Naver&version=1.0.0&provider=apicreator&tier=Unlimited&applicationName="+prop.getProperty("removed_subs_instance_id");
		api.addSubscription(uri, parameters, cookie);
	}

}
