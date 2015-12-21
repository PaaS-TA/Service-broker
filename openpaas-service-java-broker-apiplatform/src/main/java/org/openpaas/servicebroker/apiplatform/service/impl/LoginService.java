package org.openpaas.servicebroker.apiplatform.service.impl;

import org.openpaas.servicebroker.apiplatform.common.ApiPlatformUtils;
import org.openpaas.servicebroker.apiplatform.common.HttpClientUtils;
import org.openpaas.servicebroker.common.JsonUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;


@Service
@PropertySource("classpath:application-mvc.properties")
public class LoginService {

	private static final Logger logger = LoggerFactory.getLogger(LoginService.class);
	
	
	@Autowired
	private Environment env;
	
	public String getLogin(String userId, String userPassword) throws ServiceBrokerException{
		
		String cookie = "";
		String loginUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.Login");
		String loginParameters = "action=login&username="+userId+"&password="+userPassword;
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		ResponseEntity<String> loginResponseHttp = null;
		
		
		//POST 방식
		HttpEntity<String> httpEntity = new HttpEntity<String>(loginParameters, headers);
		loginResponseHttp = HttpClientUtils.send(loginUri, httpEntity, HttpMethod.POST);
		try {
		//API 플랫폼의 응답을 확인하기 위해 Json으로 변환하고 응답받은 메시지를 확인한다.
			JsonNode loginResponseJson = JsonUtils.convertToJson(loginResponseHttp);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(loginResponseJson);
		} catch (Exception e) {
			throw new ServiceBrokerException(e.getMessage());
		}
		
		cookie = loginResponseHttp.getHeaders().getFirst("Set-Cookie");
		
		logger.info("Login Success");
		logger.debug("Username : ["+userId+"], Password : ["+userPassword+"]");
		return cookie;
	}
	

}
