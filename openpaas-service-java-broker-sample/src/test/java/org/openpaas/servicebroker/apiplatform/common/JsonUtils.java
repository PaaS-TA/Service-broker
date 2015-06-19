package org.openpaas.servicebroker.apiplatform.common;

import java.io.IOException;

import org.openpaas.servicebroker.exception.ServiceBrokerException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
	
	
	public static JsonNode convertStringToJson(String data) throws ServiceBrokerException{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode result = null;
		
		try {
			// Convert response body(string) to JSON Object
			result = mapper.readValue(data, JsonNode.class);
			
		} catch (JsonParseException e) {
			throw new ServiceBrokerException(e.getMessage());
		} catch (JsonMappingException e) {
			throw new ServiceBrokerException(e.getMessage());
		} catch (IOException e) {
			throw new ServiceBrokerException(e.getMessage());
		}
		
		return result;
		
	}

}
