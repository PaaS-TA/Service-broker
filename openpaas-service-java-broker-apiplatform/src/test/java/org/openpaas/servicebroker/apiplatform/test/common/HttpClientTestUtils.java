package org.openpaas.servicebroker.apiplatform.test.common;

import org.openpaas.servicebroker.apiplatform.common.HttpClientUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class HttpClientTestUtils extends HttpClientUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(HttpClientTestUtils.class);
	
	static public ResponseEntity<String> sendProvision(String uri, HttpEntity<ProvisionBody> entity,HttpMethod httpMethod) throws ServiceBrokerException {

		RestTemplate client = new RestTemplate();
		ResponseEntity<String> httpResponse=null;
		//HttpException은 uri가 잘못 되었을 때 발생
		try {
			httpResponse = client.exchange(uri, httpMethod, entity, String.class);		
		} catch (Exception e) {
			logger.error("Exception: HttpClientUtils");
			throw new ServiceBrokerException("API Platfrom Server Not Found. Check! the URI");
		}
		
		logger.info("Http Response");
		return httpResponse;
	}
}
