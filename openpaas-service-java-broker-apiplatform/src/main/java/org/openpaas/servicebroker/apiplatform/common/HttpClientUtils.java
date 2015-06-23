package org.openpaas.servicebroker.apiplatform.common;

import org.openpaas.servicebroker.apiplatform.common.test.HttpClientUtilsTest;
import org.openpaas.servicebroker.apiplatform.exception.APICatalogException;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class HttpClientUtils {

	private static final Logger logger = LoggerFactory.getLogger(HttpClientUtilsTest.class);
	static public ResponseEntity<String> send(String uri, HttpEntity<String> entity,HttpMethod httpMethod){

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
	
}
