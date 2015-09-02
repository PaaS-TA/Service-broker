package org.openpaas.servicebroker.naverapi.exception;

import org.openpaas.servicebroker.exception.ServiceBrokerException;



public class NaverAPICatalogException extends ServiceBrokerException{

	private static final long serialVersionUID = 139013461006947252L;

	public NaverAPICatalogException(int status, String message) {
		super(message);
				
	}
	
	public NaverAPICatalogException(String message){
		super(message);
	}

}
