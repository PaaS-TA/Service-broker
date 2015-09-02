package org.openpaas.servicebroker.publicapi.exception;

import org.openpaas.servicebroker.exception.ServiceBrokerException;

public class PublicAPIServiceInstanceException extends ServiceBrokerException{

	private static final long serialVersionUID = 1L;
	
	private int statusCode;
	private String codeMessage;
	
	public PublicAPIServiceInstanceException(int status, String message) {
		super(message);
		
		this.statusCode = status;
		this.codeMessage = message;
	}
	
	public PublicAPIServiceInstanceException(String message) {
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