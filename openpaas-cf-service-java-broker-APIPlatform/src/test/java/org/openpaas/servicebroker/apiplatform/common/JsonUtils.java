//package org.openpaas.servicebroker.apiplatform.common;
//
//import java.io.IOException;
//
//import org.openpaas.servicebroker.exception.ServiceBrokerException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.ResponseEntity;
//
//import com.fasterxml.jackson.core.JsonParseException;
//import com.fasterxml.jackson.databind.JsonMappingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//public class JsonUtils {
//	
//	
//	public static JsonNode convertStringToJson(String data) throws ServiceBrokerException{
//		ObjectMapper mapper = new ObjectMapper();
//		JsonNode result = null;
//		
//		try {
//			// Convert response body(string) to JSON Object
//			result = mapper.readValue(data, JsonNode.class);
//			
//		} catch (JsonParseException e) {
//			throw new ServiceBrokerException(e.getMessage());
//		} catch (JsonMappingException e) {
//			throw new ServiceBrokerException(e.getMessage());
//		} catch (IOException e) {
//			throw new ServiceBrokerException(e.getMessage());
//		}
//		
//		return result;
//		
//	}
//
//	
//	private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class) ;
//	
//	static public JsonNode convertToJson(ResponseEntity<String> httpResponse) throws Exception{
//
//		ObjectMapper mapper = new ObjectMapper();
//		JsonNode json = null;
//		try {
//			// Convert response body(string) to JSON Object
//			json = mapper.readValue(httpResponse.getBody(), JsonNode.class);
//		} catch (JsonParseException e) {
//			throw new ServiceBrokerException(getSwitchedMessage(1));
//		} catch (JsonMappingException e) {
//			throw new ServiceBrokerException(getSwitchedMessage(2));
//		} catch (IOException e) {
//			throw new ServiceBrokerException(getSwitchedMessage(3));
//		} catch (Exception e){
//			logger.warn("Json Convert: Exception");
//			throw new ServiceBrokerException("Json Convert: " + e.getMessage());
//		}
//		
//		logger.info("Json Convert: Complete");
//		
//		if (json.get("error").asBoolean() == true) { 
//			String apiPlatformMessage = json.get("message").asText();
//			System.out.println(apiPlatformMessage);
//			
//			if(apiPlatformMessage.equals("null is not supported")){
//				throw new ServiceBrokerException("Check! HttpMethod");				
//			}
//			else if(apiPlatformMessage.equals("Login failed.Please recheck the username and password and try again.")){
//				throw new ServiceBrokerException("Check! the Username and Password");
//			}
//			
//			
//			else{
//				throw new ServiceBrokerException(json.get("message").asText());
//			}
//		}
//		return json;
//	}
//	
//	static private String getSwitchedMessage(int status){
//		String switchedMessage = null;
//		switch(status){
//		case 1 : switchedMessage="Exception: JsonParseException";
//		case 2 : switchedMessage="Exception: JsonMappingException";
//		case 3 : switchedMessage="Exception: IOException";
//		}
//		
//		return switchedMessage;
//	}
//	
//
//}
//
