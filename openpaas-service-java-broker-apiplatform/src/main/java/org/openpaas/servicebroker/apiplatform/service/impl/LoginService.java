package org.openpaas.servicebroker.apiplatform.service.impl;

import org.openpaas.servicebroker.apiplatform.exception.APICatalogException;
import org.openpaas.servicebroker.common.HttpClientUtils;
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
@PropertySource("application-mvc.properties")
public class LoginService {

	private static final Logger logger = LoggerFactory.getLogger(LoginService.class);
	
	
	@Autowired
	private Environment env;
	
	public String getLogin(String username, String password) throws ServiceBrokerException{
		
		String cookie = "";
		String loginUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.Login");
		String loginParameters = "action=login&username="+username+"&password="+password;
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		ResponseEntity<String> loginResponseHttp = null;
		
		
		//POST 방식
		HttpEntity<String> httpEntity = new HttpEntity<String>(loginParameters, headers);
		loginResponseHttp = HttpClientUtils.send(loginUri, httpEntity, HttpMethod.POST);
		try {
		//API 플랫폼의 응답을 확인하기 위해 Json으로 변환
			JsonNode loginResponseJson = JsonUtils.convertToJson(loginResponseHttp);
			
			if (loginResponseJson.get("error").asText() == "true") { 
				
				throw new APICatalogException(" " + loginResponseJson.get("message").asText());
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());		
		}
		
		cookie = loginResponseHttp.getHeaders().getFirst("Set-Cookie");
		
		logger.info(" Login 성공");
		return cookie;
	}
	

}
