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
		assertTrue(response.getBody().contains("Invalid PlanID : ["+plan_id+"]"));
		
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
		assertTrue(response.getBody().contains("Invalid ServiceID : ["+service_id+"]"));
		
		System.out.println("End - D002_invalid_parameters_service_id_test");
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
	
}	
