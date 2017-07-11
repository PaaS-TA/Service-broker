package org.openpaas.servicebroker.altibase.exception;

import org.openpaas.servicebroker.exception.ServiceBrokerException;


/**
 * 
 * AltibaseDB 서비스 관련 에러 Exception클래스 . ServiceBrokerException 클래스를 상속함.
 * 
 * @author Bethy
 *
 */
public class AltibaseServiceException extends ServiceBrokerException {

	private static final long serialVersionUID = 8667141725171626000L;

	public AltibaseServiceException(String message) {
		super(message);
	}
	
}
