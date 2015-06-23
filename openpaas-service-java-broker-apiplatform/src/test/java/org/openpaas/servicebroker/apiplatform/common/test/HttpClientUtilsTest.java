package org.openpaas.servicebroker.apiplatform.common.test;

import org.openpaas.servicebroker.apiplatform.common.HttpClientUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class HttpClientUtilsTest {
	
	public static ResponseEntity<String> client(String url, HttpEntity<String> entity, HttpMethod method) throws ServiceBrokerException {
		
		ResponseEntity<String> response = null;
		
		try {
			
			RestTemplate client = new RestTemplate();
			
			response = client.exchange(url, method, entity, String.class);		
			
			System.out.println("status:"+response.getStatusCode());
			System.out.println("result:"+response.getBody());

		} catch (Exception ex) {
			System.err.println(ex);
			throw new ServiceBrokerException(ex.getMessage());
		}
		
		return response;		
	}
	
	private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
	static public ResponseEntity<String> send(String uri, HttpEntity<String> entity,HttpMethod httpMethod) throws ServiceBrokerException{

		RestTemplate client = new RestTemplate();
		ResponseEntity<String> httpResponse=null;
		//HttpException은 uri가 잘못 되었을 때 발생
		try {
			httpResponse = client.exchange(uri, httpMethod, entity, String.class);		
		} catch (Exception e) {
			logger.error("Exception: HttpClientUtils");
			throw new RuntimeException("API Platfrom Server Not Found. Check! the URI");
		}
		
		logger.info("Http Response");
		return httpResponse;
	}

	static public ResponseEntity<String> sendWithEntity(String uri ,HttpEntity<CreateServiceInstanceRequest> provisionRequest,  HttpMethod httpMethod) throws ServiceBrokerException{

		RestTemplate client = new RestTemplate();
		ResponseEntity<String> httpResponse=null;
		//HttpException은 uri가 잘못 되었을 때 발생
		try {
			httpResponse = client.exchange(uri, httpMethod, provisionRequest, String.class);		
		} catch (Exception e) {
			logger.error("Exception: HttpClientUtils");
			throw new RuntimeException("API Platfrom Server Not Found. Check! the URI");
		}
		
		logger.info("Http Response");
		return httpResponse;
	}

	
}
