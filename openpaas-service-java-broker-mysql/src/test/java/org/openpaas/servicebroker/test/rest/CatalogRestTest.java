package org.openpaas.servicebroker.test.rest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.openpaas.servicebroker.common.JsonUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.org.apache.xml.internal.security.utils.Base64;

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
	 * POST로 요청
	 */
	@Test	
	public void TC001_methodPOST() {
		
		System.out.println("Start - POST");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.POST);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {
			
			assertEquals(sbe.getMessage(), "405 Method Not Allowed");
			bException = true;
			
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - POST");
	}


	/**
	 * PUT으로 요청
	 */
	@Test	
	public void TC002_methodPUT() {
		
		System.out.println("Start - POST");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.PUT);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {
			
			assertEquals(sbe.getMessage(), "405 Method Not Allowed");
			bException = true;
			
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - PUT");
	}

	/**
	 * DELETE로 요청
	 */
	@Test	
	public void TC003_methodDELETE() {
		
		System.out.println("Start - POST");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.DELETE);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {
			
			assertEquals(sbe.getMessage(), "405 Method Not Allowed");
			bException = true;
			
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - DELETE");
	}

	/**
	 * PATCH로 요청
	 */
	@Test	
	public void TC004_methodPATCH() {
		
		System.out.println("Start - PATCH");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.PATCH);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {
			
			assertEquals(sbe.getMessage(), "405 Method Not Allowed");
			bException = true;
			
			sbe.printStackTrace();
			
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - PATCH");
	}
	
	
	/**
	 * OPTIONS로 요청
	 */
	@Test	
	public void TC005_methodOPTIONS() {
		
		System.out.println("Start - OPTIONS");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.OPTIONS);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {
			
			assertFalse(sbe.getMessage(), true);
			bException = true;
			
			sbe.printStackTrace();
			
		}
		
		if (!bException) assertTrue("Success", true);
		
		System.out.println("End - OPTIONS");
	}

	/**
	 * HEAD로 요청
	 */
	@Test	
	public void TC006_methodHEAD() {
		
		System.out.println("Start - HEAD");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.HEAD);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {
			
			assertEquals(sbe.getMessage(), "405 Method Not Allowed");
			bException = true;
			
			sbe.printStackTrace();
			
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - HEAD");
	}
	
	/**
	 * 사용자 ID, 비밀번호가 없을 경우
	 */
	@Test	
	public void TC007_noUser() {
		
		System.out.println("Start - no user");
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		
		String serviceInstanceId = "serviceInstanceId";
		String serviceId = "serviceId";
		String planId = "planId";
		String organizationGuid = "organizationGuid";
		String spaceGuid = "spaceGuid";
		
		CreateServiceInstanceRequest request;
		request= new CreateServiceInstanceRequest(serviceId, planId,organizationGuid, spaceGuid);
		
		HttpEntity<CreateServiceInstanceRequest> service = new HttpEntity<CreateServiceInstanceRequest> (request);
		ResponseEntity<String> response = null;
		
		boolean bException = false;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.PUT);
			
		} catch (ServiceBrokerException sbe) {
			
			assertEquals("No user", sbe.getMessage(), "401 Unauthorized");
			bException = true;
			
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - no user");
	}

	/**
	 * 사용자 ID, 비밀번호가 잘못 입력되었을 경우
	 */
	@Test	
	public void TC008_invalidUser() {
		
		System.out.println("Start - invalid user");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id_fail") +":" + prop.getProperty("auth_password_fail")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;

		boolean bException = false;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.GET);
			
		} catch (ServiceBrokerException sbe) {
			
			assertEquals(sbe.getMessage(), "401 Unauthorized");
			bException = true;
			
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - invalid user");
	}

	/**
	 * 사용자 ID, 비밀번호가 정상적으로 입력되었을 경우
	 */
	@Test	
	public void TC009_correctUser() {
		
		System.out.println("Start - correct user");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.GET);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {
			
			assertFalse(sbe.getMessage(), true);
			
		}
		
		assertTrue("Success", true);
		
		System.out.println("End - correct user");
	}

	/**
	 * Version이 잘못 입력되었을 경우
	 */
	@Test	
	public void TC010_incorrectAPIVersion() {
		
		System.out.println("Start - incorrect API Version");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version_fail"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;

		boolean bException = false;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.GET);
			
		} catch (ServiceBrokerException sbe) {
			
			assertEquals(sbe.getMessage(), "412 Precondition Failed");
			bException = true;
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - incorrect API Version");
	}

	/**
	 * Catalog 정보를 정상적으로 가져옴
	 * Mandatory 부분이 다 들어가 있는지 확인
	 */
	@Test	
	public void TC011_validCatalog() {
		
		System.out.println("Start - valid catalog");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("catalog_path");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.GET);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());
			
			JsonNode json = JsonUtils.convertToJson(response);
			
			if (!checkValidCatalogJson(json)) throw new ServiceBrokerException("validation check is fail.");
			
			
		} catch (ServiceBrokerException sbe) {
			
			assertFalse(sbe.getMessage(), true);
			
		}
		
		assertTrue("Success", true);
		
		System.out.println("End - valid catalog");
	}

	
	private boolean checkValidCatalogJson(JsonNode json) {
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
		
		return bResult;
	}
}
