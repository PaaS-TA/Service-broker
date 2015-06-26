package org.openpaas.servicebroker.apiplatform.common;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class ApiPlatformUtils {

	private static final Logger logger = LoggerFactory.getLogger(ApiPlatformUtils.class);
	
	static public void apiPlatformErrorMessageCheck(JsonNode json) throws ServiceBrokerException{
		
		if(json.has("status")&&!json.get("status").asText().equals("APPROVED")){
			throw new ServiceBrokerException("APIPlatform Add An Application Error");
		}
		
		if (json.get("error").asText().equals("true")) { 
			String apiPlatformMessage = json.get("message").asText();
			
			logger.info("API Platform Message : "+apiPlatformMessage);
			
			if(apiPlatformMessage.equals("null is not supported")){
				throw new ServiceBrokerException("Check! the HttpMethod or Media Type");
			}
			else if(apiPlatformMessage.equals("Login failed.Please recheck the username and password and try again.")){
				throw new ServiceBrokerException("Check! the Username and Password");
			}
			else if(apiPlatformMessage.equals("Error occurred while executing the action generateApplicationKey")){
				//인스턴스 아이디 중복 또는 이미 만들어진 어플리케이션의 이름과 인스턴스 아이디가 같은 경우, 
				//이미 키생성이 완료된 어플리케이션의 이름을 Generate Key API의 파라미터로 넣은 경우
				
				
			}
			else {
				throw new ServiceBrokerException(json.get("message").asText());
			}
		}
		return;
	}
}