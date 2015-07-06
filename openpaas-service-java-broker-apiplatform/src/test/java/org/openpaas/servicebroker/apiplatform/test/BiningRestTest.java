package org.openpaas.servicebroker.apiplatform.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openpaas.servicebroker.common.BindingBody;
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.sun.org.apache.xml.internal.security.utils.Base64;


public class BiningRestTest {

private static Properties prop = new Properties();
	
	@BeforeClass
	public static void init() {
		
		System.out.println("== Started test Binding API ==");

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
	

	
	@Test
	public void test() {
		
		String instance_id = "instance_id";
		String plan_id = prop.getProperty("provision_plan_id");
		String service_id= prop.getProperty("provision_service_id");
		String app_guid = UUID.randomUUID().toString();
		String binding_id = UUID.randomUUID().toString();
		
		BindingBody body =new BindingBody(service_id, plan_id, app_guid);
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<BindingBody> entity = new HttpEntity<BindingBody>(body, headers);		
		ResponseEntity<String> response = null;

		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path") + "/" + instance_id+"/"+prop.getProperty("binding_path")+"/"+binding_id;
		
		response = HttpClientUtils.sendBinding(url, entity, HttpMethod.PUT);
		System.out.println(response.getStatusCode());
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertTrue(response.getBody().contains("credentials"));
	}

}
