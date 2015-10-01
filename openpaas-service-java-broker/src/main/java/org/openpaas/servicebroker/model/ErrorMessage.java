package org.openpaas.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Error  메세지를 가지고 있는 데이터 모델 bean 클래스
 * Json 어노테이션을 사용해서 JSON 형태로 제공
 * 
 * @author 송창학
 * @date 2015.0629
 */

public class ErrorMessage {

	@JsonProperty("description")
	private String message;

	public ErrorMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
