//package org.openpaas.servicebroker.apiplatform.test;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Properties;
//
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpUtils;
//import javax.validation.Valid;
//
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.openpaas.servicebroker.apiplatform.common.JsonUtils;
//import org.openpaas.servicebroker.exception.ServiceBrokerException;
//import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
//import org.openpaas.servicebroker.model.CreateServiceInstanceResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.env.Environment;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.client.ResponseExtractor;
//import org.springframework.web.client.RestClientException;
//import org.springframework.web.client.RestTemplate;
//
//import sun.net.www.http.HttpClient;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.sun.org.apache.xml.internal.security.utils.Base64;
//
//
//public class ProvisionRestTest {
//
//	private static Properties prop = new Properties();
//	@Autowired
//	private Environment env;
//	
//	@Autowired
//	private CreateServiceInstanceRequest request;
//	
//	@BeforeClass
//	public static void init() {
//		
//		System.out.println("== Started test Provision API ==");
//
//		// Initialization
//		// Get properties information
//		String propFile = "test.properties";
// 
//		InputStream inputStream = CatalogRestTest.class.getClassLoader().getResourceAsStream(propFile);
//		
//		try {
//			prop.load(inputStream);
//	 	} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//	 		System.err.println(e);
//	 	}
//		
//	}
//	
//	/**
//	 * 사
//	 * @param responseExtractor 
//	 */
//	@Test	
//	public void getProvisionTest() {
//		
//		System.out.println("Start - getProvisionTest");
//		
//		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
//		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
//		headers.set("Authorization", "Basic " + new String(Base64.encode((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));
//		
//		String serviceInstanceId = "serviceInstanceId";
//		String serviceId = "serviceId";
//		String planId = "planId";
//		String organizationGuid = "organizationGuid";
//		String spaceGuid = "spaceGuid";
//
////		String stringRequest ="\"service_id\"=service_id&plan_id=planId&organization_guid=organizationGuid&space_guid=spaceGuid";
////		String stringRequestString ="{\"service_id\" : \"service-guid-here\", \"plan_id\" : \"plan-guid-here\", \"organization_guid\" : \"orgguidhere\", \"space_guid\" : \"space-guid-here\"}";
//		String url = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url") + prop.getProperty("provision_path");
//		JsonNode json = null;
////		System.out.println(stringRequestString);
//		request = new CreateServiceInstanceRequest(serviceId,planId,organizationGuid,spaceGuid);
////		request.setOrganizationGuid(organizationGuid);
////		request.setPlanId(planId);
////		request.setServiceDefinitionId(serviceId);
////		request.setSpaceGuid(spaceGuid);
//		
////		try {
////			json = JsonUtils.convertStringToJson(stringRequestString);
////			System.out.println(json.get("service_id").asText());
////			System.out.println(json.asText());
////			
////		} catch (ServiceBrokerException e1) {
////			
////			e1.printStackTrace();
////		}
//		
//		System.out.println("json변환");
//		
////		request= new CreateServiceInstanceRequest(serviceId, planId,organizationGuid, spaceGuid);
//
//		ResponseEntity<String> response =null;
//		
////		HttpEntity<JsonNode> entity = new HttpEntity<JsonNode>(json, headers);
//		
////		HttpEntity<String> entity = new HttpEntity<String>(stringRequest, headers);
//		HttpEntity<CreateServiceInstanceRequest> entity = new HttpEntity<CreateServiceInstanceRequest>(request,headers);
//
//		System.out.println("entity 정의");
//		boolean bException = false;
//		
//		try {
//			
//			RestTemplate client = new RestTemplate();
//			
////			client.execute(url, HttpMethod.PUT, "", request);
//			
//			response= client.exchange(url, HttpMethod.PUT,entity, String.class);
//			System.out.println("client.exchange 전달");
////			response = sendWithEntity(url, entity, HttpMethod.PUT, request);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		if (!bException) assertFalse("Error", true);
//		
//		System.out.println("getProvisionTest");
//	}
//	
////	public ResponseEntity<String> sendWithEntity(String url ,HttpEntity<JsonNode> entity, HttpMethod httpMethod) throws ServiceBrokerException{
////
////		RestTemplate client = new RestTemplate();
////		ResponseEntity<String> httpResponse=null;
////		//HttpException은 uri가 잘못 되었을 때 발생
////		try {
////			
////			httpResponse = client.exchange(url, httpMethod, entity, String.class);	
////		} catch (Exception e) {
////			throw new RuntimeException("API Platfrom Server Not Found. Check! the URI");
////		}
////		
////		return httpResponse;
////
////	}
//	
//	public @ResponseBody CreateServiceInstanceRequest sendEntity(CreateServiceInstanceRequest request){
//		
//		
//		return request;
//	}
//}
