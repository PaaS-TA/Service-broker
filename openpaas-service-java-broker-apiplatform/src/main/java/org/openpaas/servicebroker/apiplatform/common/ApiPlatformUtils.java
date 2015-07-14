package org.openpaas.servicebroker.apiplatform.common;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class ApiPlatformUtils {
	
	
	private static final Logger logger = LoggerFactory.getLogger(ApiPlatformUtils.class);
	
	static public void apiPlatformErrorMessageCheck(JsonNode json) throws ServiceBrokerException{
		
		//API 플랫폼이 error : true를 응답한 경우
		if (json.has("error")&&json.get("error").asText().equals("true")) { 
			
			String apiPlatformMessage = json.get("message").asText();
			
			logger.info("API Platform Message : "+apiPlatformMessage);
			
			//모든 API
			if(apiPlatformMessage.equals("null is not supported")){
				throw new ServiceBrokerException("Check the HttpMethod or Media Type");
			}
			
			//Login API
			else if(apiPlatformMessage.equals("Login failed.Please recheck the username and password and try again.")){
				throw new ServiceBrokerException("No User");
			}
			
			//Add an Application API
			else if(json.get("message").asText().contains("A duplicate application already exists by the name")){			
				
				//API플랫폼 Add an Application 에러
				//어플리케이션 이름이 중복될때, "error": false, "message":" A duplicate application already exists ..."			
				throw new ServiceBrokerException("Duplication");	
			}
			
			//Generate Key API
			//이미 키생성이 완료된 어플리케이션의 이름을 Generate Key API의 파라미터로 넣은 경우	
			else if(apiPlatformMessage.equals("Error occurred while executing the action generateApplicationKey")){

				throw new ServiceBrokerException("Key already generated");
			}
			
			//Add a Subscription
			//이미 사용등록이 완료된 API를 사용등록 할 경우
			else if(apiPlatformMessage.contains("Error while adding the subscription for user")){

				logger.info("API already subscribed");
				return;
			}
			
			//Remove an Application
			else if(apiPlatformMessage.equals("Error occurred while executing the action removeApplication")){
				throw new ServiceBrokerException("No Application");
			}
			
			else {
				throw new ServiceBrokerException(json.get("message").asText());
			}
		}
		//API플랫폼이 true도 false도 아닌 에러값을 보내는 경우
		else if (json.has("error")&&!json.get("error").asText().equals("false")){
			//remove a subscription
			if(json.get("message").equals("Error occurred while executing the action removeSubscription")){
				throw new ServiceBrokerException("invalid parameters");
			}
		}
		//API플랫폼에서 응답하는 에러값은 false인데, 예외를 발생시켜야 하는 경우
		else if(json.has("error")&&json.get("error").asText().equals("false")){
			//Get published APIs by application
			//API의 사용등록이 되어있지 않은 경우
			if(json.has("apis")&&!json.get("apis").has(0)){
				throw new ServiceBrokerException("No subscribed API");
			}
			//Add an Application API
			//파라미터명(키값)이 잘못 되었을 때, "error": false, "status": null
			else if(json.has("status")&&!json.get("status").asText().equals("APPROVED")){
				//어플리케이션 추가가 정상적으로 이루어지지 않은 경우이다.
				throw new ServiceBrokerException("Add an Application Error");
			}
		}	
	}
}