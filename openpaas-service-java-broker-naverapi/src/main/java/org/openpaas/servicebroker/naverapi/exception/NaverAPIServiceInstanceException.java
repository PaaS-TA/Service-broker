package org.openpaas.servicebroker.naverapi.exception;

import org.openpaas.servicebroker.exception.ServiceBrokerException;

public class NaverAPIServiceInstanceException extends ServiceBrokerException{

	private static final long serialVersionUID = 1L;
	
	private int statusCode;
	private String codeMessage;
	
	public NaverAPIServiceInstanceException(int status, String message) {
		super(message);
		
		this.statusCode = status;
		this.codeMessage = message;
	}
	
	public NaverAPIServiceInstanceException(String message) {
		super(message);
		this.codeMessage = message;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getCodeMessage() {
		return codeMessage;
	}

	public void setCodeMessage(String codeMessage) {
		this.codeMessage = codeMessage;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}