package org.openpaas.servicebroker.publicapi.exception;

import org.openpaas.servicebroker.exception.ServiceBrokerException;



public class PublicAPICatalogException extends ServiceBrokerException{

	private static final long serialVersionUID = 139013461006947252L;

	public PublicAPICatalogException(int status, String message) {
		super(message);
				
	}
	
	public PublicAPICatalogException(String message){
		super(message);
	}

}
