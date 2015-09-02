package org.openpaas.servicebroker.cubrid.exception;

import org.openpaas.servicebroker.exception.ServiceBrokerException;


/**
 * @author 
 *
 */
public class CubridServiceException extends ServiceBrokerException {

	private static final long serialVersionUID = 8667141725171626000L;

	public CubridServiceException(String message) {
		super(message);
	}
	
}
