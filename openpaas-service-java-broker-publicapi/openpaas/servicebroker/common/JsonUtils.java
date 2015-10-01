package org.openpaas.servicebroker.common;

import java.io.IOException;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class) ;
	
	static public JsonNode convertToJson(ResponseEntity<String> httpResponse) throws ServiceBrokerException{

		ObjectMapper mapper = new ObjectMapper();
		JsonNode json = null;
		try {
			json = mapper.readValue(httpResponse.getBody(), JsonNode.class);
		} catch (JsonParseException e) {
			throw new ServiceBrokerException(getSwitchedMessage(1));
		} catch (JsonMappingException e) {
			throw new ServiceBrokerException(getSwitchedMessage(2));
		} catch (IOException e) {
			throw new ServiceBrokerException(getSwitchedMessage(3));
		} catch (Exception e){
			logger.warn("Json Convert: Exception");
			throw new ServiceBrokerException("Json Convert Exception : " + e.getMessage());
		}
		
		logger.info("Json Convert completed");
		
		return json;
	}
	
	static private String getSwitchedMessage(int status){
		String switchedMessage = null;
		switch(status){
		case 1 : switchedMessage="Exception: JsonParseException";
		case 2 : switchedMessage="Exception: JsonMappingException";
		case 3 : switchedMessage="Exception: IOException";
		}
		return switchedMessage;
	}
}
