package org.openpaas.servicebroker.publicapi.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openpaas.servicebroker.common.JsonUtils;
import org.openpaas.servicebroker.publicapi.common.HttpClientUtils;
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
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
		
		System.out.println("Start - C001_getCatalogTest_noUser");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		try{
			response = HttpClientUtils.testSend(url, entity, HttpMethod.GET);			
		}
		catch(Exception e){
			assertTrue(e.getMessage().contains("Full authentication is required to access this resource"));
			System.out.println(e.getMessage());
		}
		System.out.println(response.getBody());
		assertTrue(response.getBody().contains("Full authentication is required to access this resource"));
		System.out.println("End - C001_getCatalogTest_noUser");
	}

	/**
	 * C002 사용자 ID, 비밀번호가 잘못 입력되었을 경우
	 */
	@Test	
	public void C002_getCatalogTest_invalidUser() {
		
		System.out.println("Start - C002_getCatalogTest_invalidUser");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id_fail") +":" + prop.getProperty("auth_password_fail")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
			
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
			
		response = HttpClientUtils.testSend(url, entity, HttpMethod.GET);
			
		System.out.println(response.getBody());

		JsonNode json = null;
		try {
			json=JsonUtils.convertToJson(response);
		} catch (ServiceBrokerException e) {
			assertTrue(false);
		}
		
		assertTrue(json.get("message").asText().equals("Bad credentials"));
		assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);

		System.out.println("End - C002_getCatalogTest_invalidUser");
	}

	/**
	 * C003 사용자 ID, 비밀번호가 정상적으로 입력되었을 경우
	 */
	@Test	
	public void C003_getCatalogTest_correctUser() {
		
		System.out.println("Start - C003_getCatalogTest_correctUser");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		response = HttpClientUtils.testSend(url, entity, HttpMethod.GET);
		
		System.out.println(response.getBody());

		JsonNode json = null;
		try {
			json=JsonUtils.convertToJson(response);
		} catch (ServiceBrokerException e) {
			assertTrue(false);
		}
		
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		
		System.out.println("End - C003_getCatalogTest_correctUser");
	}
	/**
	 * C004 사용자 password가 유효하지 않은 경우
	 */
	@Test	
	public void C004_getCatalogTest_invalidPassword() {
		
		System.out.println("Start - C004_getCatalogTest_invalidPassword");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password_fail")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;

			
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.GET);
		
		System.out.println(response.getBody());

		JsonNode json = null;
		try {
			json=JsonUtils.convertToJson(response);
		} catch (ServiceBrokerException e) {
			assertTrue(false);
		}
		
		assertTrue(json.get("message").asText().equals("Bad credentials"));
		assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);
		
		System.out.println("End - C004_getCatalogTest_invalidPassword");
	}


	/**
	 * C005 Version이 잘못 입력되었을 경우
	 */
	@Test	
	public void C005_getCatalogTest_incorrectAPIVersion() {
		
		System.out.println("Start - C005_getCatalogTest_incorrectAPIVersion");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version_fail"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.GET);
		
		System.out.println(response.getBody());

		JsonNode json = null;
		try {
			json=JsonUtils.convertToJson(response);
		} catch (ServiceBrokerException e) {
			assertTrue(false);
		}
		
		assertEquals(response.getStatusCode(), HttpStatus.PRECONDITION_FAILED);
		assertTrue(json.get("description").asText().equals("The provided service broker API version is not supported: Expected Version = 2.5, Provided Version = "+prop.getProperty("api_version_fail")));
		
		System.out.println("End - C005_getCatalogTest_incorrectAPIVersion");
	}

	/**
	 * C006 Catalog 정보를 정상적으로 가져옴
	 * Mandatory 부분이 다 들어가 있는지 확인
	 */
	@Test	
	public void C006_getCatalogTest_validCatalog() {
		
		System.out.println("Start - getCatalogTest_validCatalog");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.GET);
	
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
		
		System.out.println("End - getCatalogTest_validCatalog");
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
		
		System.out.println("Start - C007_POST");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.POST);
		
		System.out.println(response.getBody());

		JsonNode json = null;
		try {
			json=JsonUtils.convertToJson(response);
		} catch (ServiceBrokerException e) {
			assertTrue(false);
		}
			
		assertEquals(response.getStatusCode(), HttpStatus.METHOD_NOT_ALLOWED);
		assertTrue(json.get("message").asText().equals("Request method 'POST' not supported"));

		
		System.out.println("End - C007_POST");
	}


	/**
	 * C008 PUT으로 요청
	 */
	@Test	
	public void C008_getCatalogTest_methodPUT() {
		
		System.out.println("Start - C008_PUT");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;
			
		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.PUT);
		System.out.println(response.getBody());

		JsonNode json = null;
		try {
			json=JsonUtils.convertToJson(response);
		} catch (ServiceBrokerException e) {
			assertTrue(false);
		}
			
		assertEquals(response.getStatusCode(), HttpStatus.METHOD_NOT_ALLOWED);
		assertTrue(json.get("message").asText().equals("Request method 'PUT' not supported"));
		
		System.out.println("End - C008_PUT");
	}

	/**
	 * C009 DELETE로 요청
	 */
	@Test	
	public void C009_getCatalogTest_methodDELETE() {
		
		System.out.println("Start - C009_DELETE");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		
		response = HttpClientUtils.testSend(url, entity, HttpMethod.DELETE);
		
		System.out.println(response.getBody());

		JsonNode json = null;
		try {
			json=JsonUtils.convertToJson(response);
		} catch (ServiceBrokerException e) {
			assertTrue(false);
		}
			
		assertEquals(response.getStatusCode(), HttpStatus.METHOD_NOT_ALLOWED);
		assertTrue(json.get("message").asText().equals("Request method 'DELETE' not supported"));
		
		System.out.println("End - C009_DELETE");
	}

	/**
	 * C010 PATCH로 요청
	 */
	@Test	
	public void C010_getCatalogTest_methodPATCH() {
		
		System.out.println("Start - C010_PATCH");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>(" ", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		try {
			response = HttpClientUtils.testSend(url, entity, HttpMethod.PATCH);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(e.getMessage().contains("I/O error on PATCH request for"));
		
			bException = true;
		}
		
		assertTrue(bException);
		System.out.println("End - C010_PATCH");
	}
	
	
	/**
	 * C011 OPTIONS로 요청
	 */
	@Test	
	public void C011_getCatalogTest_methodOPTIONS() {
		
		System.out.println("Start - C11_OPTIONS");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>(" ", headers);
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");

		response = HttpClientUtils.testSend(url, entity, HttpMethod.OPTIONS);
		System.out.println(response.getHeaders());
		System.out.println(response.getBody());
		System.out.println(response.getStatusCode());

		
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		
		
		System.out.println("End - C11_OPTIONS");
	}

	/**
	 * C012 HEAD로 요청
	 */
	@Test	
	public void C012_getCatalogTest_methodHEAD() {
		
		System.out.println("Start - C012_HEAD");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
		try {
			response = HttpClientUtils.testSend(url, entity, HttpMethod.HEAD);
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(e.getMessage().contains("I/O error on HEAD request for"));
			
			bException = true;
		}
		
		assertTrue(bException);
		
		System.out.println("End - C012_HEAD");
	}
}
