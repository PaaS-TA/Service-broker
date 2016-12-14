package servicebroker.pinpoiont.restTest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.springframework.http.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BindingRestTest {

	private static Properties prop = new Properties();
	private static String instance_id = "103cb323-7400-4a87-9a2a-1e245e284c41";
	private static String conflict_instance_id = "aeb24ef2-9826-4628-9dc9-c919b29f6b58";

	private static String bind_id = "f30eff77-d23b-469a-860c-db3f8518c71c";
	private static String duplicate_bind_id = "450e44a5-fbe2-4386-a58d-32b072e7555c";

	@BeforeClass
	static public void init() {

		System.out.println("== Started test Binding API ==");

		String propFile = "test.properties";
		InputStream inputStream = CatalogRestTest.class.getClassLoader().getResourceAsStream(propFile);

		try {
			prop.load(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println(e);
		}

		//Provision
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encodeBase64((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		String organization_guid = UUID.randomUUID().toString();
		String space_guid = UUID.randomUUID().toString();

		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("service_id", "pinpoiont");
		jsonObject.addProperty("plan_id", "utf8");
		jsonObject.addProperty("organization_guid", organization_guid);
		jsonObject.addProperty("space_guid", space_guid);

		Map<String, String> bodyMap = new HashMap<String,String>();

		bodyMap.put("service_id", "pinpoiont");
		bodyMap.put("plan_id", "utf8");
		bodyMap.put("organization_guid", organization_guid);
		bodyMap.put("space_guid", space_guid);

		Gson gson = new Gson();
		String	sJson = gson.toJson(jsonObject);
		HttpEntity<String> entity = new HttpEntity<>(sJson, headers);
		ResponseEntity<String> response = null;

		try {
			String APIendpoint = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url");
			String url = APIendpoint + "/v2/service_instances/" + instance_id;

			response = HttpClientUtils.send(url, entity, HttpMethod.PUT);

			if (response.getStatusCode() != HttpStatus.CREATED) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {

		}

		bodyMap.remove("plan_id");
		bodyMap.put("plan_id", "euckr");
		entity = new HttpEntity<String>(gson.toJson(bodyMap), headers);
		response = null;

		try {
			String APIendpoint = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url");
			String url = APIendpoint + "/v2/service_instances/" + conflict_instance_id;

			response = HttpClientUtils.send(url, entity, HttpMethod.PUT);

			if (response.getStatusCode() != HttpStatus.CREATED) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {

		}

		//binding
		bodyMap.clear();
		bodyMap.put("plan_id", "utf8");
		bodyMap.put("service_id", "pinpoiont");
		bodyMap.put("app_guid", "app-guid-here");

		entity = new HttpEntity<String>(new Gson().toJson(bodyMap), headers);
		response = null;

		try {
			String APIendpoint = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url");
			String url = APIendpoint + "/v2/service_instances/" + instance_id + "/service_bindings/" + duplicate_bind_id;

			response = HttpClientUtils.send(url, entity, HttpMethod.PUT);

			if (response.getStatusCode() != HttpStatus.CREATED) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {

		}

	}

	@AfterClass
	static public void closer() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encodeBase64((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		ResponseEntity<String> response = null;

		try {
			String APIendpoint = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url");
			String url = APIendpoint + "/v2/service_instances/" + instance_id + "?service_id=&plan_id=";

			response = HttpClientUtils.send(url, entity, HttpMethod.DELETE);

			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {

		}

		try {
			String APIendpoint = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url");
			String url = APIendpoint + "/v2/service_instances/" + conflict_instance_id + "?service_id=&plan_id=";

			response = HttpClientUtils.send(url, entity, HttpMethod.DELETE);

			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {

		}

		System.out.println("== End test Binding API ==");

	}

	@Test
	public void binding_201Created() {
		
		System.out.println("Start - binding");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encodeBase64((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		Map<String, String> bodyMap = new HashMap<String,String>();

		bodyMap.put("plan_id", "utf8");
		bodyMap.put("service_id", "pinpoiont");
		bodyMap.put("app_guid", "app-guid-here");

		HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(bodyMap), headers);
		ResponseEntity<String> response = null;

		try {
			String APIendpoint = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url");
			String url = APIendpoint + "/v2/service_instances/" + instance_id + "/service_bindings/" + bind_id;

			response = HttpClientUtils.send(url, entity, HttpMethod.PUT);

			if (response.getStatusCode() != HttpStatus.CREATED) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {

			System.out.println(sbe.getMessage());
			assertFalse(sbe.getMessage(), true);

		}

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		System.out.println("End - binding");
		
	}
	
	@Test
	public void binding_200OK() {

		System.out.println("Start - duplicate bind id, same binding");
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encodeBase64((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		Map<String, String> bodyMap = new HashMap<String,String>();

		bodyMap.put("plan_id", "utf8");
		bodyMap.put("service_id", "pinpoiont");
		bodyMap.put("app_guid", "app-guid-here");

		HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(bodyMap), headers);
		ResponseEntity<String> response = null;

		try {
			String APIendpoint = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url");
			String url = APIendpoint + "/v2/service_instances/" + instance_id + "/service_bindings/" + duplicate_bind_id;

			response = HttpClientUtils.send(url, entity, HttpMethod.PUT);

			if (response.getStatusCode() != HttpStatus.CREATED) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {

			System.out.println(sbe.getMessage());

		}

		assertEquals(HttpStatus.OK, response.getStatusCode());
		
		System.out.println("End - duplicate instance id, same binding");

	}
	
	@Test
	public void binding_409Conflict() {

		System.out.println("Start - duplicate bind id, annother binding");
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Broker-Api-Version", prop.getProperty("api_version"));
		headers.set("Authorization", "Basic " + new String(Base64.encodeBase64((prop.getProperty("auth_id") +":" + prop.getProperty("auth_password")).getBytes())));

		Map<String, String> bodyMap = new HashMap<String,String>();

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("application_name","application_name");
		bodyMap.put("plan_id", "utf8");
		bodyMap.put("service_id", "pinpoiont");
		bodyMap.put("app_guid", "app-guid-here");
		bodyMap.put("parameters", new Gson().toJson(jsonObject));

		HttpEntity<String> entity = new HttpEntity<String>(new Gson().toJson(bodyMap), headers);
		ResponseEntity<String> response = null;

		boolean bException = false;
		
		try {
			String APIendpoint = prop.getProperty("test_base_protocol") + prop.getProperty("test_base_url");
			String url = APIendpoint + "/v2/service_instances/" + conflict_instance_id + "/service_bindings/" + duplicate_bind_id;

			response = HttpClientUtils.send(url, entity, HttpMethod.PUT);

			if (response.getStatusCode() != HttpStatus.CREATED) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException sbe) {

			System.out.println(sbe.getMessage());
			assertEquals("409 Conflict", sbe.getMessage());
			bException = true;

		}

		if (!bException) assertFalse("Success", true);
		
		System.out.println("End - duplicate instance id, annother binding");

	}
}
