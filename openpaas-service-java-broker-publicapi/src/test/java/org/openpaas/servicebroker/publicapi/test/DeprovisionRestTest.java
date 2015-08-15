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
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.annotation.Rollback;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeprovisionRestTest {

	private static Properties prop = new Properties();
	
	private static String instance_id ="deprovision-test";
	@BeforeClass
	public static void init() {
		
		System.out.println("== Started test Deprovision API ==");

		// Initialization
		// Get properties information
		String propFile = "test.properties";
 
		InputStream inputStream = DeprovisionRestTest.class.getClassLoader().getResourceAsStream(propFile);
		
		try {
			prop.load(inputStream);
	 	} catch (IOException e) {
	 		
			e.printStackTrace();
	 		System.err.println(e);
	 	}
	}
	
	
	//D001 잘못된 플랜아이디로 요청이 들어온 케이스
	@Test
	public void D001_invalid_parameters_plan_id_test() {
		System.out.println("Start - D001_invalid_parameters_plan_id_test");
		
		String plan_id = prop.getProperty("test_plan_id_fail");
		String service_id= prop.getProperty("test_service_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+prop.getProperty("test_instance_id")+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid PlanID :["+plan_id+"]"));
		
		System.out.println("End - D001_invalid_parameters_plan_id_test");
	}
	
	//D002 잘못된 서비스 아이디로 요청이 들어온 케이스
	@Test
	public void D002_invalid_parameters_service_id_test() {
		System.out.println("Start - D002_invalid_parameters_service_id_test");
		
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id_fail");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+prop.getProperty("test_instance_id")+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("Invalid ServiceID :["+service_id+"]"));
		
		System.out.println("End - D002_invalid_parameters_service_id_test");
	}
	
	//D003 존재하지 않는 인스턴스 아이디로 요청이 들어온 케이스 410 GONE
	@Test
	public void D003_invalid_parameters_instance_id_test() {
		System.out.println("Start - D003_invalid_parameters_instance_id_test");
		
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" 
				+prop.getProperty("test_instance_id_fail")+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.GONE, response.getStatusCode());
		assertTrue(response.getBody().equals("{}"));
		
		System.out.println("End - D003_invalid_parameters_instance_id_test");
	}
	
	//D004 요청된 인스턴스 아이디에 해당하는 어플리케이션을 가지고 있는 유저가 API플랫폼에서 삭제된 케이스
	//API플랫폼 관리 콘솔에서 직접 유저를 삭제하고 테스트
 	//	@Test
	public void D004_removed_user_test() {
		System.out.println("Start - D004_removed_user_test");
		
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+prop.getProperty("removed_user_instance_id")+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody().equals("{}"));
		
		System.out.println("End - D004_removed_user_test");
	}
	
	//D005 요청된 인스턴스 아이디에 해당하는 어플리케이션이 API플랫폼에서 삭제된 케이스
	@Test
	public void D005_removed_application_test() {
		System.out.println("Start - D005_removed_application_test");
		
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+prop.getProperty("removed_app_instance_id")+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody().equals("{}"));
		
		System.out.println("End - D005_removed_application_test");
	}
	
	//D006 요청된 인스턴스 아이디에 해당하는 어플리케이션에 사용등록 된 API가 API플랫폼에서 이미 사용등록이 해제된 케이스
	@Test
	public void D006_removed_subscription_test() {
		System.out.println("D006 == Start - removed_subscription_test");
		
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+prop.getProperty("removed_subs_instance_id")+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody().equals("{}"));
		
		System.out.println("D006 == End - removed_subscription_test");
	}
	

	//D007 요청된 인스턴스 아이디에 해당하는 어플리케이션에 사용등록 된 API의 라이프사이클이 변경된 케이스
	@Test
	public void D007_changed_lifecycle_test() {
		System.out.println("D007 == Start - changed_lifecycle_test");
		
		String plan_id = prop.getProperty("lifecycle_change_plan_id");
		String service_id= prop.getProperty("lifecycle_change_service_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+prop.getProperty("lifecycle_change_instance_id")+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody().equals("{}"));
		
		System.out.println("D007 == End - changed_lifecycle_test");
	}
	
	//인스턴스 아이디에 해당하는 어플리케이션을 가진 유저가 DB에서 삭제된 케이스
	@Test
	public void D008_removed_user_DB_test() {
		System.out.println("D008 == Start - removed_user_DB_test");
		
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+prop.getProperty("removed_user_instance_id_DB")+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		
		System.out.println(response.getBody());

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("APIPlatform user information not found."));
		
		System.out.println("D008 == End - removed_user_DB_test");
	}
	
	//D010 적절한 데이터로 요청이 들어온 케이스 - 200 OK
	@Test
	@Rollback(true)
	public void D010_valid_parameters_test() {
		System.out.println("Start - D010_valid_parameters_test");
		
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+instance_id+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody().equals("{}"));
		
		System.out.println("End - D010_valid_parameters_test");
	}
	
	//D011 이미 제거된 인스턴스 아이디로 요청이 들어온 케이스 - 500 INTERNAL_SERVER_ERROR
	@Test
	public void D011_valid_parameters_removed_test() {
		System.out.println("Start - D011_valid_parameters_removed_test");
		
		String plan_id = prop.getProperty("test_plan_id");
		String service_id= prop.getProperty("test_service_id");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/"
					+prop.getProperty("removed_instance_id_DB")+"?service_id="+service_id+"&plan_id="+plan_id;
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.DELETE);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().contains("already removed Service Instance : ["+prop.getProperty("removed_instance_id_DB")+"]"));
		
		System.out.println("End - D011_valid_parameters_removed_test");
	}
}	
