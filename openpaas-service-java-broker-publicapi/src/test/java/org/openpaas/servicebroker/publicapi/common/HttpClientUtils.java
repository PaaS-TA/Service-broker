package org.openpaas.servicebroker.publicapi.common;

import org.openpaas.servicebroker.common.BindingBody;
import org.openpaas.servicebroker.common.ProvisionBody;
import org.openpaas.servicebroker.common.UpdateProvisionBody;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

public class HttpClientUtils {

	private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
	
	static public ResponseEntity<String> testSend(String uri, HttpEntity<String> entity,HttpMethod httpMethod) {

		RestTemplate client = new RestTemplate();
		
		client.setErrorHandler(new DefaultResponseErrorHandler(){
		    protected boolean hasError(HttpStatus statusCode) {
		        return false;
		    }});
		
		ResponseEntity<String> httpResponse=null;

		httpResponse = client.exchange(uri, httpMethod, entity, String.class);					

		logger.info("Get APIPlatform API Http Response");
		return httpResponse;
	}
	
	static public ResponseEntity<String> send(String uri, HttpEntity<String> entity,HttpMethod httpMethod) throws ServiceBrokerException {

		RestTemplate client = new RestTemplate();
		ResponseEntity<String> httpResponse=null;
		try {
			httpResponse = client.exchange(uri, httpMethod, entity, String.class);		
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
		return httpResponse;
	}
	
	static public ResponseEntity<String> sendProvision(String uri, HttpEntity<ProvisionBody> entity,HttpMethod httpMethod) {

		RestTemplate client = new RestTemplate();
		
		client.setErrorHandler(new DefaultResponseErrorHandler(){
		    protected boolean hasError(HttpStatus statusCode) {
		        return false;
		    }});
		
		ResponseEntity<String> httpResponse=null;
		
		httpResponse = client.exchange(uri, httpMethod, entity, String.class);		

		return httpResponse;
	}
	
	static public ResponseEntity<String> sendBinding(String uri, HttpEntity<BindingBody> entity,HttpMethod httpMethod) {

		RestTemplate client = new RestTemplate();
		
		client.setErrorHandler(new DefaultResponseErrorHandler(){
		    protected boolean hasError(HttpStatus statusCode) {
		        return false;
		    }});
		
		ResponseEntity<String> httpResponse=null;
		
		httpResponse = client.exchange(uri, httpMethod, entity, String.class);		

		return httpResponse;
	}
	
	static public ResponseEntity<String> sendUnbinding(String uri, HttpEntity<String> entity,HttpMethod httpMethod) {

		RestTemplate client = new RestTemplate();
		
		client.setErrorHandler(new DefaultResponseErrorHandler(){
		    protected boolean hasError(HttpStatus statusCode) {
		        return false;
		    }});
		
		ResponseEntity<String> httpResponse=null;
		
		httpResponse = client.exchange(uri, httpMethod, entity, String.class);		

		return httpResponse;
	}
	
	static public ResponseEntity<String> sendUpdateProvision(String uri, HttpEntity<UpdateProvisionBody> entity,HttpMethod httpMethod) {

		RestTemplate client = new RestTemplate();
		
		client.setErrorHandler(new DefaultResponseErrorHandler(){
		    protected boolean hasError(HttpStatus statusCode) {
		        return false;
		    }});
		
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setConnectTimeout(0);
		requestFactory.setReadTimeout(0);
		
		client.setRequestFactory(requestFactory);
		
		ResponseEntity<String> httpResponse=null;
		
		httpResponse = client.exchange(uri, httpMethod, entity, String.class);		

		return httpResponse;
	}

}

