package org.openpaas.servicebroker.test.rest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;












import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openpaas.servicebroker.common.BindingBody;
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
public class BindingRestTest {
	
	private static Properties prop = new Properties();
	
	@BeforeClass
	public static void init() {
		
		System.out.println("== Started test Binding API ==");

		// Initialization
		// Get properties information
		String propFile = "test.properties";
 
		InputStream inputStream = BindingRestTest.class.getClassLoader().getResourceAsStream(propFile);
		
		try {
			prop.load(inputStream);
	 	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	 		System.err.println(e);
	 	}
		
	}
	
	public void createProvision(){
		//System.out.println("sendProvision_Provision_create");
		//sendProvision_Provision_delete();
		//sendProvision_Provision_create();
	}
	/**
	 * POST요청
	 */
	@Test	
	public void TC001_binding_methodPOST() {
		
		System.out.println("Start - POST");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("binding_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			url = url.replace("#BINDING_ID", prop.getProperty("broker.apitest.info.bindingIdA"));
			
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
	public void TC002_binding_methodOPTIONS() {
		
		System.out.println("Start - OPTIONS");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("binding_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			url = url.replace("#BINDING_ID", prop.getProperty("broker.apitest.info.bindingIdA"));
			
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
	public void TC003_binding_methodHEAD() {
		
		System.out.println("Start - HEAD");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("binding_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			url = url.replace("#BINDING_ID", prop.getProperty("broker.apitest.info.bindingIdA"));
			
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
	 * HEAD요청
	 */
	@Test	
	public void TC004_binding_methodPATCH() {
		
		System.out.println("Start - HEAD");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;

		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("binding_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			url = url.replace("#BINDING_ID", prop.getProperty("broker.apitest.info.bindingIdA"));
			
			response = HttpClientUtils.send(url, entity, HttpMethod.PATCH);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) { 
			
			assertEquals(sbe.getMessage(), "405 Method Not Allowed");
			bException = true;
			
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - HEAD");
	}
	
	/**
	 * Binding_create
	 */
	@Test	
	public void TC005_binding_create() {
		
		System.out.println("Start - binding_create");
		
		provisionDelete();
		provisionCreate();
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		//HttpEntity<String> entity = new HttpEntity<String>("", headers);
		BindingBody bindingbody = new BindingBody(prop.getProperty("broker.apitest.info.serviceId"), 
				prop.getProperty("broker.apitest.info.planIdA"), 
				prop.getProperty("broker.apitest.info.appGuidA"));
		
		HttpEntity<BindingBody> entity = new HttpEntity<BindingBody>(bindingbody, headers);
		ResponseEntity<String> response = null;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("binding_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			url = url.replace("#BINDING_ID", prop.getProperty("broker.apitest.info.bindingIdA"));
			
			response = HttpClientUtils.sendBinding(url, entity, HttpMethod.PUT);
			
			if (response.getStatusCode() != HttpStatus.CREATED) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) { 
			
			assertFalse(sbe.getMessage(), true);
		}
		
		assertTrue("Success", true);
		
		System.out.println("End - binding_create");
	}
	
	/**
	 * Binding_create
	 */
	@Test	
	public void TC006_binding_duplication_create() {
		
		System.out.println("Start - binding_duplication_create");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		//HttpEntity<String> entity = new HttpEntity<String>("", headers);
		BindingBody bindingbody = new BindingBody(prop.getProperty("broker.apitest.info.serviceId"), 
				prop.getProperty("broker.apitest.info.planIdA"), 
				prop.getProperty("broker.apitest.info.appGuidA"));
		
		HttpEntity<BindingBody> entity = new HttpEntity<BindingBody>(bindingbody, headers);
		ResponseEntity<String> response = null;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("binding_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			url = url.replace("#BINDING_ID", prop.getProperty("broker.apitest.info.bindingIdA"));
			
			response = HttpClientUtils.sendBinding(url, entity, HttpMethod.PUT);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) { 
			
			assertFalse(sbe.getMessage(), true);
		}
		
		assertTrue("Success", true);
		
		System.out.println("End - Binding_create");
	}
	
	/**
	 * Binding_create
	 */
	@Test	
	public void TC007_binding_create_invalid_planId() {
		
		System.out.println("Start - Binding_create");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		//HttpEntity<String> entity = new HttpEntity<String>("", headers);
		BindingBody bindingbody = new BindingBody(prop.getProperty("broker.apitest.info.serviceId"), 
				prop.getProperty("broker.apitest.info.planIdC"), 
				prop.getProperty("broker.apitest.info.appGuidB"));
		
		HttpEntity<BindingBody> entity = new HttpEntity<BindingBody>(bindingbody, headers);
		ResponseEntity<String> response = null;
		boolean bException = false;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("binding_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			url = url.replace("#BINDING_ID", prop.getProperty("broker.apitest.info.bindingIdA"));
			
			response = HttpClientUtils.sendBinding(url, entity, HttpMethod.PUT);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) { 
			assertEquals(sbe.getMessage(), "409 Conflict");
			bException = true;
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - binding_duplication_create");
	}
	
	/**
	 * Binding_create
	 */
	@Test	
	public void TC008_binding_delete() {
		
		System.out.println("Start - binding_delete");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		
		ResponseEntity<String> response = null;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("binding_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			url = url.replace("#BINDING_ID", prop.getProperty("broker.apitest.info.bindingIdA"));
			url = url + "?service_id=" + prop.getProperty("broker.apitest.info.serviceId") + "&plan_id=" + prop.getProperty("broker.apitest.info.planIdA");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.DELETE);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) { 
			
			assertFalse(sbe.getMessage(), true);
		}
		
		assertTrue("Success", true);
		
		System.out.println("End - binding_delete");
	}
	
	/**
	 * Binding_create
	 */
	@Test	
	public void TC009_binding_delete_invalid_bindingId() {
		
		System.out.println("Start - binding_delete_invalid_bindingId");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		
		ResponseEntity<String> response = null;
		boolean bException = false;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("binding_path");
			
			url = url.replace("#INSTANCE_ID", prop.getProperty("broker.apitest.info.intanceId"));
			url = url.replace("#BINDING_ID", prop.getProperty("broker.apitest.info.bindingIdD"));
			url = url + "?service_id=" + prop.getProperty("broker.apitest.info.serviceId") + "&plan_id=" + prop.getProperty("broker.apitest.info.planIdA");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.DELETE);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) { 
			assertEquals(sbe.getMessage(), "410 Gone");
			bException = true;
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - binding_delete_invalid_bindingId");
	}
	
	/*private boolean checkValidCatalogJson(JsonNode json) {
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
	}*/
	
	
	private void provisionCreate() {
		
		System.out.println("Start - Provision_create");
		
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
			
		} catch (ServiceBrokerException sbe) {
		}
		
		System.out.println("End - Provision_create");
	}
	
	private void provisionDelete(){
		System.out.println("Start - Provision_delete");
		
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
			
		} catch (ServiceBrokerException sbe) {
		}
		System.out.println("End - Provision_delete");
	}
}
