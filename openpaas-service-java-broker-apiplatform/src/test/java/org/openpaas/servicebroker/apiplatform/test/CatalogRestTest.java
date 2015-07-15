package org.openpaas.servicebroker.apiplatform.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openpaas.servicebroker.common.JsonUtils;
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 시작전에 Spring Boot로 Service Broker를 띄워 놓구 진행합니다.
 * 향후에 Spring Configuration?으로 서비스를 시작하게 만들 예정
 * 
 * @author ahnchan
 */
public class CatalogRestTest {
	
	private static Properties prop = new Properties();
	
	@BeforeClass
	public static void init() {
		
		System.out.println("== Started test Catalog API ==");

		// Initialization
		// Get properties information
		String propFile = "test.properties";
 
		InputStream inputStream = CatalogRestTest.class.getClassLoader().getResourceAsStream(propFile);
		
		try {
			prop.load(inputStream);
	 	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	 		System.err.println(e);
	 	}
		
	}
	
	/**
	 * C001 사용자 ID, 비밀번호가 없을 경우 
	 */
	
	@Test	
	public void C001_getCatalogTest_noUser() {
		
		System.out.println("Start - no user");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		boolean bException = false;
		try{
			response = HttpClientUtils.send(url, entity, HttpMethod.PUT);			
		}
		catch(Exception e){
			assertTrue(e.getMessage().contains(":cannot retry due to server authentication"));
			bException = true;
		}
		
		assertTrue(bException);
		System.out.println("End - no user");
	}

	/**
	 * C002 사용자 ID, 비밀번호가 잘못 입력되었을 경우
	 */
	@Test	
	public void C002_getCatalogTest_invalidUser() {
		
		System.out.println("Start - invalid user");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id_fail") +":" + prop.getProperty("auth_password_fail")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
			
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
			
		response = HttpClientUtils.send(url, entity, HttpMethod.GET);
			
		System.out.println(response.getBody());

		JsonNode json = null;
		try {
			json=JsonUtils.convertToJson(response);
		} catch (ServiceBrokerException e) {
			assertTrue(false);
		}
		
		assertTrue(json.get("message").asText().equals("Bad credentials"));
		assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);

		System.out.println("End - invalid user");
	}

	/**
	 * C003 사용자 ID, 비밀번호가 정상적으로 입력되었을 경우
	 */
	@Test	
	public void C003_getCatalogTest_correctUser() {
		
		System.out.println("Start - correct user");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		response = HttpClientUtils.send(url, entity, HttpMethod.GET);
		
		System.out.println(response.getBody());

		JsonNode json = null;
		try {
			json=JsonUtils.convertToJson(response);
		} catch (ServiceBrokerException e) {
			assertTrue(false);
		}
		
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		
		System.out.println("End - correct user");
	}
	/**
	 * C004 사용자 password가 유효하지 않은 경우
	 */
	@Test	
	public void C004_getCatalogTest_invalidPassword() {
		
		System.out.println("Start - invalid Password");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password_fail")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;

			
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		
		response = HttpClientUtils.send(url, entity, HttpMethod.GET);
		
		System.out.println(response.getBody());

		JsonNode json = null;
		try {
			json=JsonUtils.convertToJson(response);
		} catch (ServiceBrokerException e) {
			assertTrue(false);
		}
		
		assertTrue(json.get("message").asText().equals("Bad credentials"));
		assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);
		
		System.out.println("End - invalid Password");
	}


	/**
	 * C005 Version이 잘못 입력되었을 경우
	 */
	@Test	
	public void C005_getCatalogTest_incorrectAPIVersion() {
		
		System.out.println("Start - incorrect API Version");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version_fail"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		
		response = HttpClientUtils.send(url, entity, HttpMethod.GET);
		
		System.out.println(response.getBody());

		JsonNode json = null;
		try {
			json=JsonUtils.convertToJson(response);
		} catch (ServiceBrokerException e) {
			assertTrue(false);
		}
		
		assertEquals(response.getStatusCode(), HttpStatus.PRECONDITION_FAILED);
		assertTrue(json.get("description").asText().equals("The provided service broker API version is not supported: Expected Version = 2.4, Provided Version = "+prop.getProperty("api_version_fail")));
	}

	/**
	 * C006 Catalog 정보를 정상적으로 가져옴
	 * Mandatory 부분이 다 들어가 있는지 확인
	 */
	@Test	
	public void C006_getCatalogTest_validCatalog() {
		
		System.out.println("Start - valid catalog");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		
		response = HttpClientUtils.send(url, entity, HttpMethod.GET);
	
		JsonNode json = null;
		
		try {
			json = JsonUtils.convertToJson(response);			
	
			if (!checkValidCatalogJson(json)) throw new ServiceBrokerException("validation check is fail.");
		} catch (Exception e) {
			System.out.println(e.getMessage());			
			assertTrue(false);
		}

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertTrue("Success", true);
		
		System.out.println("End - valid catalog");
	}

	private boolean checkValidCatalogJson(JsonNode json) {
		System.out.println("Start - valid catalog json");
		boolean bResult = true;

		try {
			for(JsonNode item: json.get("services")) {
				if (StringUtils.isEmpty(item.get("id").asText())) {
					System.err.println("service ID is empty.");
					bResult = false;
				}
				if (StringUtils.isEmpty(item.get("name").asText())) {
					System.err.println("service NAME is empty.");
					bResult = false;
				}
				if (StringUtils.isEmpty(item.get("description").asText())) {
					System.err.println("service DESCRIPTION is empty.");
					bResult = false;
				}
				if (!item.get("bindable").isBoolean()) {
					System.err.println("service BINABLE is not a boolean.");
					bResult = false;
				}
				
				if (item.get("plans").isArray() && item.get("plans").size() > 0) {
					
					for(JsonNode plan: item.get("plans")) {
						if (StringUtils.isEmpty(plan.get("id").asText())) {
							System.err.println("plan ID is empty.");
							bResult = false;
						}
						if (StringUtils.isEmpty(plan.get("name").asText())) {
							System.err.println("plan NAME is empty.");
							bResult = false;
						}
						if (StringUtils.isEmpty(plan.get("description").asText())) {
							System.err.println("plan DESCRIPTION is empty.");
							bResult = false;
						}
					}				
				} else {
					System.err.println("service PLANS is empty.");
					bResult = false;
				}
			}
			
		} catch (Exception ex) {
			System.err.println("Catch exception: " + ex.getMessage());
			ex.printStackTrace();
			bResult = false;
		}
		System.out.println("End - valid catalog json");
		return bResult;
	}
	
	
	/**
	 * C007 POST로 요청
	 */
	@Test	
	public void C007_getCatalogTest_methodPOST() {
		
		System.out.println("Start - POST");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		
		response = HttpClientUtils.send(url, entity, HttpMethod.POST);
		
		System.out.println(response.getBody());

		JsonNode json = null;
		try {
			json=JsonUtils.convertToJson(response);
		} catch (ServiceBrokerException e) {
			assertTrue(false);
		}
			
		assertEquals(response.getStatusCode(), HttpStatus.METHOD_NOT_ALLOWED);
		assertTrue(json.get("message").asText().equals("Request method 'POST' not supported"));

		
		System.out.println("End - POST");
	}


	/**
	 * C008 PUT으로 요청
	 */
	@Test	
	public void C008_getCatalogTest_methodPUT() {
		
		System.out.println("Start - PUT");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;
			
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		
		response = HttpClientUtils.send(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());

		JsonNode json = null;
		try {
			json=JsonUtils.convertToJson(response);
		} catch (ServiceBrokerException e) {
			assertTrue(false);
		}
			
		assertEquals(response.getStatusCode(), HttpStatus.METHOD_NOT_ALLOWED);
		assertTrue(json.get("message").asText().equals("Request method 'PUT' not supported"));
		
		System.out.println("End - PUT");
	}

	/**
	 * C009 DELETE로 요청
	 */
	@Test	
	public void C009_getCatalogTest_methodDELETE() {
		
		System.out.println("Start - DELETE");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		
		response = HttpClientUtils.send(url, entity, HttpMethod.DELETE);
		
		System.out.println(response.getBody());

		JsonNode json = null;
		try {
			json=JsonUtils.convertToJson(response);
		} catch (ServiceBrokerException e) {
			assertTrue(false);
		}
			
		assertEquals(response.getStatusCode(), HttpStatus.METHOD_NOT_ALLOWED);
		assertTrue(json.get("message").asText().equals("Request method 'DELETE' not supported"));
		
		System.out.println("End - DELETE");
	}

	/**
	 * C010 PATCH로 요청
	 */
	@Test	
	public void C010_getCatalogTest_methodPATCH() {
		
		System.out.println("Start - PATCH");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>(" ", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		try {
			response = HttpClientUtils.send(url, entity, HttpMethod.PATCH);
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(e.getMessage().contains("I/O error on PATCH request for"));

			bException = true;
		}
		
		assertTrue(bException);
		System.out.println("End - PATCH");
	}
	
	
	/**
	 * C011 OPTIONS로 요청
	 */
	@Test	
	public void C011_getCatalogTest_methodOPTIONS() {
		
		System.out.println("Start - OPTIONS");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>(" ", headers);
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");

		response = HttpClientUtils.send(url, entity, HttpMethod.OPTIONS);
		System.out.println(response.getHeaders());
		System.out.println(response.getBody());
		System.out.println(response.getStatusCode());

		
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		
		
		System.out.println("End - OPTIONS");
	}

	/**
	 * C012 HEAD로 요청
	 */
	@Test	
	public void C012_getCatalogTest_methodHEAD() {
		
		System.out.println("Start - HEAD");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		try {
			response = HttpClientUtils.send(url, entity, HttpMethod.HEAD);
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(e.getMessage().contains("I/O error on HEAD request for"));
			
			bException = true;
		}
		
		assertTrue(bException);
		
		System.out.println("End - HEAD");
	}
}
