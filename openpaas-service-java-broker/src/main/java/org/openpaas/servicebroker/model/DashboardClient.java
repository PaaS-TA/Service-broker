package org.openpaas.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * DashboardClient정보를 가지고 있는 데이터 모델 bean 클래스. 
 * Json 어노테이션을 사용해서 JSON 형태로 제공
 * 
 * @author 송창학
 * @date 2015.0629
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardClient {

	@JsonSerialize
	@JsonProperty("id")
	private String id;
	
	@JsonSerialize
	@JsonProperty("secret")
	private String secret;
	
	@JsonSerialize
	@JsonProperty("redirect_uri")
	private String redirectUri;

	public DashboardClient() {
	}

	public DashboardClient(String id, String secret, String redirectUri) {
		this.id = id;
		this.secret = secret;
		this.redirectUri = redirectUri;
	}

	public String getId() {
		return id;
	}

	public String getSecret() {
		return secret;
	}

	public String getRedirectUri() {
		return redirectUri;
	}
	
}
