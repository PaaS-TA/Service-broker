package org.openpaas.servicebroker.common;

import org.openpaas.servicebroker.common.ProvisionBody;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class HttpClientUtils {

	private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
	
	static public ResponseEntity<String> send(String uri, HttpEntity<String> entity,HttpMethod httpMethod) throws ServiceBrokerException {

		RestTemplate client = new RestTemplate();
		
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setConnectTimeout(0);
		requestFactory.setReadTimeout(0);
		
		client.setRequestFactory(requestFactory);
		
		ResponseEntity<String> httpResponse=null;
		try {
			httpResponse = client.exchange(uri, httpMethod, entity, String.class);		
		} catch (Exception e) {
			if(e.getMessage().equals("404 Not Found"))
			{
				throw new ServiceBrokerException("API Platform Error: API Platform Server Not Found");
			}
			logger.error(e.getMessage());
			throw new ServiceBrokerException(e.getMessage());
		}
		
		logger.info("Http Response");
		return httpResponse;
	}
	
	static public ResponseEntity<String> sendProvision(String uri, HttpEntity<ProvisionBody> entity,HttpMethod httpMethod) throws ServiceBrokerException {

		RestTemplate client = new RestTemplate();
		
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setConnectTimeout(0);
		requestFactory.setReadTimeout(0);
		
		client.setRequestFactory(requestFactory);
		
		ResponseEntity<String> httpResponse=null;
		try {
			httpResponse = client.exchange(uri, httpMethod, entity, String.class);		
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new ServiceBrokerException(e.getMessage());
		}
		
		logger.info("Http Response");
		return httpResponse;
	}

	static public ResponseEntity<String> sendUpdateProvision(String uri, HttpEntity<UpdateProvisionBody> entity,HttpMethod httpMethod) throws ServiceBrokerException {

		RestTemplate client = new RestTemplate();
		
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setConnectTimeout(0);
		requestFactory.setReadTimeout(0);
		
		client.setRequestFactory(requestFactory);
		
		ResponseEntity<String> httpResponse=null;
		try {
			httpResponse = client.exchange(uri, httpMethod, entity, String.class);		
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new ServiceBrokerException(e.getMessage());
		}
		
		logger.info("Http Response");
		return httpResponse;
	}
	
	static public ResponseEntity<String> sendBinding(String uri, HttpEntity<BindingBody> entity,HttpMethod httpMethod) throws ServiceBrokerException {

		RestTemplate client = new RestTemplate();
		
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setConnectTimeout(0);
		requestFactory.setReadTimeout(0);
		
		client.setRequestFactory(requestFactory);
		
		ResponseEntity<String> httpResponse=null;
		try {
			httpResponse = client.exchange(uri, httpMethod, entity, String.class);		
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new ServiceBrokerException(e.getMessage());
		}
		
		logger.info("Http Response");
		return httpResponse;
	}
}
