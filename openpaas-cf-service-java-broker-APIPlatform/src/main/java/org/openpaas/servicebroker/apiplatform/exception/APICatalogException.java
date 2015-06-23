package org.openpaas.servicebroker.apiplatform.exception;

import org.openpaas.servicebroker.exception.ServiceBrokerException;



public class APICatalogException extends ServiceBrokerException{

	private static final long serialVersionUID = 139013461006947252L;

	public APICatalogException(int status, String message) {
		super(message);
				
	}
	
	public APICatalogException(String message){
		super(message);
	}

}
