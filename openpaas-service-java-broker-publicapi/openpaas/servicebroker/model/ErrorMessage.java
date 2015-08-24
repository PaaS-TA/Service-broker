package org.openpaas.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used to send errors back to the cloud controller.
 * *클라우드 컨트롤러로 에러 메시지를 보내기 위해 사용됨
 * 
 * 2015.07.17
 * @author 송창학 수석
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
