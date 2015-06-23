package org.openpaas.servicebroker.apiplatform.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openpaas.servicebroker.apiplatform.common.HttpClientUtils;
import org.openpaas.servicebroker.apiplatform.common.test.HttpClientUtilsTest;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class ProvisionRestTest {

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
	 * 사용자 ID, 비밀번호가 없을 경우
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
		
		HttpEntity<CreateServiceInstanceRequest> service = new HttpEntity<CreateServiceInstanceRequest> (request , headers);
		ResponseEntity<String> response = null;
		
		boolean bException = false;
		
		try {
			
			String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path");
			
			response = HttpClientUtilsTest.sendWithEntity(url, service, HttpMethod.PUT);

		} catch (ServiceBrokerException sbe) {
			
			assertEquals("No user", sbe.getMessage(), "401 Unauthorized");
			bException = true;
			
		}
		
		if (!bException) assertFalse("Error", true);
		
		System.out.println("End - no user");
	}


}
