package org.openpaas.servicebroker.mongodb.restTest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
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
import org.apache.commons.codec.binary.Base64;

/**
 * 
 * @author 
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
	 * 
	 */
	@Test	
	public void getCatalogTest_noUser() {
		
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
	 * 
	 */
	@Test	
	public void getCatalogTest_invalidUser() {
		
		System.out.println("Start - invalid user");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Basic " + new String(Base64.encodeBase64((prop.getProperty("auth_id_fail") +":" + prop.getProperty("auth_password_fail")).getBytes())));
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
	 * ?Ç¨?ö©?ûê ID, ÎπÑÎ?Î≤àÌò∏Í∞? ?†ï?ÉÅ?†Å?úºÎ°? ?ûÖ?†•?êò?óà?ùÑ Í≤ΩÏö∞
	 */
	@Test	
	public void getCatalogTest_correctUser() {
		
		System.out.println("Start - correct user");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encodeBase64((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
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
	 * Version?ù¥ ?ûòÎ™? ?ûÖ?†•?êò?óà?ùÑ Í≤ΩÏö∞
	 */
	@Test	
	public void getCatalogTest_incorrectAPIVersion() {
		
		System.out.println("Start - incorrect API Version");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version_fail"));
		headers.set("Authorization", "Basic " + new String(Base64.encodeBase64((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
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
	 * Catalog ?†ïÎ≥¥Î?? ?†ï?ÉÅ?†Å?úºÎ°? Í∞??†∏?ò¥
	 * Mandatory Î∂?Î∂ÑÏù¥ ?ã§ ?ì§?ñ¥Í∞? ?ûà?äîÏß? ?ôï?ù∏
	 */
	@Test	
	public void getCatalogTest_validCatalog() {
		
		System.out.println("Start - valid catalog");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encodeBase64((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
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
	
	
	/**
	 * POSTÎ°? ?öîÏ≤?
	 */
	@Test	
	public void getCatalogTest_methodPOST() {
		
		System.out.println("Start - POST");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encodeBase64((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
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
	 * PUT?úºÎ°? ?öîÏ≤?
	 */
	@Test	
	public void getCatalogTest_methodPUT() {
		
		System.out.println("Start - PUT");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encodeBase64((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
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
	 * DELETEÎ°? ?öîÏ≤?
	 */
	@Test	
	public void getCatalogTest_methodDELETE() {
		
		System.out.println("Start - DELETE");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encodeBase64((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
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
	 * PATCHÎ°? ?öîÏ≤?
	 */
	@Test	
	public void getCatalogTest_methodPATCH() {
		
		System.out.println("Start - PATCH");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encodeBase64((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
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
	 * OPTIONSÎ°? ?öîÏ≤?
	 */
	@Test	
	public void getCatalogTest_methodOPTIONS() {
		
		System.out.println("Start - OPTIONS");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encodeBase64((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
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
	 * HEADÎ°? ?öîÏ≤?
	 */
	@Test	
	public void getCatalogTest_methodHEAD() {
		
		System.out.println("Start - HEAD");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encodeBase64((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
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
}

