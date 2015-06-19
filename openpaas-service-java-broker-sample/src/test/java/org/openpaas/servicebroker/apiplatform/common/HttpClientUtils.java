package org.openpaas.servicebroker.apiplatform.common;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class HttpClientUtils {
	
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

}
