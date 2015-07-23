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
import org.openpaas.servicebroker.common.ProvisionBody;
import org.openpaas.servicebroker.common.UpdateProvisionBody;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.org.apache.xml.internal.security.utils.Base64;

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
	

	/**
	 * POST요청
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
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("instance_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			
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
	 * OPTIONS요청
	 */
	@Test	
	public void TC002_methodOPTIONS() {
		
		System.out.println("Start - OPTIONS");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("instance_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			
			response = HttpClientUtils.send(url, entity, HttpMethod.OPTIONS);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {
			
			assertFalse(sbe.getMessage(), true);
			bException = true;
			
		}
		
		if (!bException) assertTrue("Success", true);
		
		System.out.println("End - OPTIONS");
	}

	/**
	 * HEAD요청
	 */
	@Test	
	public void TC003_methodHEAD() {
		
		System.out.println("Start - HEAD");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("instance_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			
			response = HttpClientUtils.send(url, entity, HttpMethod.HEAD);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) { 
			
			assertEquals(sbe.getMessage(), "405 Method Not Allowed");
			bException = true;
			
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - HEAD");
	}
	
	/**
	 *  필수값 누락 체크 : service_id 누락
	 */
	@Test	
	public void TC004_provision_mandatory_check() {
		
		System.out.println("Start - provision_mandatory_check");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		//HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ProvisionBody body = new ProvisionBody("", 
				prop.getProperty("broker.apitest.info.planIdA"), 
				prop.getProperty("broker.apitest.info.organizationGuid"), 
				prop.getProperty("broker.apitest.info.spaceGuid"));
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("instance_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			
			response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
			
		} catch (ServiceBrokerException sbe) {
			
			assertEquals(sbe.getMessage(), "422 Unprocessable Entity");
			bException = true;
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - provision_mandatory_check");
	}

	/**
	 * Provision_create
	 */
	@Test	
	public void TC005_provision_create() {
		
		System.out.println("Start - Provision_create");
		
		deleteProvision(prop.getProperty("broker.apitest.info.intanceId"));
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		//HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ProvisionBody body = new ProvisionBody(prop.getProperty("broker.apitest.info.serviceId"), 
				prop.getProperty("broker.apitest.info.planIdA"), 
				prop.getProperty("broker.apitest.info.organizationGuid"), 
				prop.getProperty("broker.apitest.info.spaceGuid"));
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);
		ResponseEntity<String> response = null;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("instance_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			
			response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
			
			if (response.getStatusCode() != HttpStatus.CREATED) throw new ServiceBrokerException("Response code is " + response.getStatusCode());
			
			JsonNode json = JsonUtils.convertToJson(response);
			
			if (!checkValidCatalogJson(json)) throw new ServiceBrokerException("validation check is fail.");
			
			
		} catch (ServiceBrokerException sbe) {
			sbe.printStackTrace();
			
			assertFalse(sbe.getMessage(), true);
		}
		
		assertTrue("Success", true);
		
		System.out.println("End - Provision_create");
	}
	
	/**
	 * Provision_same_info_create
	 */
	@Test	
	public void TC006_provision_duplication_create() {
		
		System.out.println("Start - provision_duplication_create");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		ProvisionBody body = new ProvisionBody(prop.getProperty("broker.apitest.info.serviceId"), 
				prop.getProperty("broker.apitest.info.planIdA"), 
				prop.getProperty("broker.apitest.info.organizationGuid"), 
				prop.getProperty("broker.apitest.info.spaceGuid"));
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);
		ResponseEntity<String> response = null;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("instance_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			
			response = HttpClientUtils.sendProvision(url, entity, HttpMethod.PUT);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());
			
			JsonNode json = JsonUtils.convertToJson(response);
			
			if (!checkValidCatalogJson(json)) throw new ServiceBrokerException("validation check is fail.");
			
			
		} catch (ServiceBrokerException sbe) {
			assertFalse(sbe.getMessage(), true);
		}
		
		assertTrue("Success", true);
		
		System.out.println("End - provision_duplication_create");
	}
	
	/**
	 * Provision_chang
	 */
	@Test	
	public void TC007_provision_change() {
		
		System.out.println("Start - provision_change");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		UpdateProvisionBody body = new UpdateProvisionBody(prop.getProperty("broker.apitest.info.planIdB"));
		
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);
		ResponseEntity<String> response = null;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("instance_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			
			response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());
			
			
		} catch (ServiceBrokerException sbe) {
			assertFalse(sbe.getMessage(), true);
		}
		
		assertTrue("Success", true);
		
		System.out.println("End - provision_change");
	}
	
	/**
	 * Provision_chang_invalid_planId
	 */
	@Test	
	public void TC008_provision_chang_invalid_planId() {
		
		System.out.println("Start - provision_chang_invalid_planId");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		UpdateProvisionBody body = new UpdateProvisionBody(prop.getProperty("broker.apitest.info.planIdC"));
		
		HttpEntity<UpdateProvisionBody> entity = new HttpEntity<UpdateProvisionBody>(body, headers);
		ResponseEntity<String> response = null;
		boolean bException = false;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("instance_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			
			response = HttpClientUtils.sendUpdateProvision(url, entity, HttpMethod.PATCH);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());
			
			
		} catch (ServiceBrokerException sbe) {
			assertEquals(sbe.getMessage(), "422 Unprocessable Entity");
			bException = true;
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - provision_chang_invalid_planId");
	}
	
	/**
	 * Provision_delete
	 */
	@Test
	public void TC009_provision_delete(){
		System.out.println("Start - provision_delete");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("instance_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			url = url + "?service_id=" + prop.getProperty("broker.apitest.info.serviceId") + "&plan_id=" + prop.getProperty("broker.apitest.info.planIdA");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.DELETE);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());
			
		} catch (ServiceBrokerException sbe) {
			
			assertFalse("Error", true);
			
		}
		
		System.out.println("End - provision_delete");
	}
	
	/**
	 * Provision_delete_non_instanceId
	 */
	@Test
	public void TC010_provision_delete_invalid_instanceId(){
		System.out.println("Start - provision_delete_invalid_instanceId");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		boolean bException = false;
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("instance_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.non.intanceId"));
			url = url + "?service_id=" + prop.getProperty("broker.apitest.info.serviceId") + "&plan_id=" + prop.getProperty("broker.apitest.info.planIdA");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.DELETE);
			
			if (response.getStatusCode() != HttpStatus.GONE) throw new ServiceBrokerException("Response code is " + response.getStatusCode());
			
		} catch (ServiceBrokerException sbe) {
			assertEquals(sbe.getMessage(), "410 Gone");
			bException = true;
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - provision_delete_invalid_instanceId");
	}
	
	private void deleteProvision(String instanceId){
		System.out.println("Start - deleteProvision");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		ProvisionBody body = new ProvisionBody(prop.getProperty("broker.apitest.info.serviceId"), 
				prop.getProperty("broker.apitest.info.planIdA"), 
				prop.getProperty("broker.apitest.info.organizationGuid"), 
				prop.getProperty("broker.apitest.info.spaceGuid"));
		
		HttpEntity<ProvisionBody> entity = new HttpEntity<ProvisionBody>(body, headers);
		ResponseEntity<String> response = null;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("instance_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			url = url + "?service_id=" + prop.getProperty("broker.apitest.info.serviceId") + "&plan_id=" + prop.getProperty("411d0c3e-b086-4a24-b041-0aeef1a819d1");
			
			response = HttpClientUtils.sendProvision(url, entity, HttpMethod.DELETE);
			
			if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.GONE) throw new ServiceBrokerException("Response code is " + response.getStatusCode());
			
		} catch (ServiceBrokerException sbe) {
			sbe.printStackTrace();
		}
		
		System.out.println("End - deleteProvision");
	}
	
	private boolean checkValidCatalogJson(JsonNode json) {
		boolean bResult = true;

		try {
			if (StringUtils.isEmpty(json.get("dashboard_url").asText())) {
				System.err.println("dashboard_url is empty.");
				bResult = false;
			}
			
		} catch (Exception ex) {
			System.err.println("Catch exception: " + ex.getMessage());
			ex.printStackTrace();
			bResult = false;
		}
		
		return bResult;
	}
}
