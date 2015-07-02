package org.openpaas.servicebroker.test;

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
		
		System.out.println("== Started test Catalog API ==");

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
	public void sendProvision_A_methodPOST() {
		
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
	public void sendProvision_B_methodOPTIONS() {
		
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
			
			sbe.printStackTrace();
			
		}
		
		if (!bException) assertTrue("Success", true);
		
		System.out.println("End - OPTIONS");
	}

	/**
	 * HEAD요청
	 */
	@Test	
	public void sendProvision_C_methodHEAD() {
		
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
			
			sbe.printStackTrace();
			
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - HEAD");
	}
	
	/**
	 * Provision
	 * Mandatory 
	 */
	@Test	
	public void sendProvision_D_Provision_validData() {
		
		System.out.println("Start - valid catalog");
		
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
			
			if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.CREATED) throw new ServiceBrokerException("Response code is " + response.getStatusCode());
			
			JsonNode json = JsonUtils.convertToJson(response);
			
			if (!checkValidCatalogJson(json)) throw new ServiceBrokerException("validation check is fail.");
			
			
		} catch (ServiceBrokerException sbe) {
			
			assertFalse(sbe.getMessage(), true);
		}
		
		assertTrue("Success", true);
		
		deleteProvision(prop.getProperty("broker.apitest.info.intanceId"));
		
		System.out.println("End - valid catalog");
	}

	private boolean checkValidCatalogJson(JsonNode json) {
		boolean bResult = true;

		try {
			if (StringUtils.isEmpty(json.get("dashboard_url").asText())) {
				System.err.println("dashboard_url is empty.");
				bResult = false;
			}
			/*for(JsonNode item: json.get("services")) {
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
			}*/
			
		} catch (Exception ex) {
			System.err.println("Catch exception: " + ex.getMessage());
			ex.printStackTrace();
			bResult = false;
		}
		
		return bResult;
	}
	
	private void deleteProvision(String instanceId){
		System.out.println("Start - delete Provision");
		
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
			url = url + "?service_id=" + prop.getProperty("broker.apitest.info.serviceId") + "&plan_id=" + prop.getProperty("411d0c3e-b086-4a24-b041-0aeef1a819d1");
			
			response = HttpClientUtils.sendProvision(url, entity, HttpMethod.DELETE);
			
			if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.GONE) throw new ServiceBrokerException("Response code is " + response.getStatusCode());
			
			JsonNode json = JsonUtils.convertToJson(response);
			
		} catch (ServiceBrokerException sbe) {
			sbe.printStackTrace();
		}
		
		System.out.println("End - delete Provision");
	}
	
}
