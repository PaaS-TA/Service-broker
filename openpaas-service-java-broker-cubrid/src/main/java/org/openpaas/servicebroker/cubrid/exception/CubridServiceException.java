package org.openpaas.servicebroker.cubrid.exception;

import org.openpaas.servicebroker.exception.ServiceBrokerException;


/**
 * 
 * CubridDB 서비스 관련 에러 Exception클래스 . ServiceBrokerException 클래스를 상속함.
 * 
 * @author Cho mingu
 *
 */
public class CubridServiceException extends ServiceBrokerException {

	private static final long serialVersionUID = 8667141725171626000L;

	public CubridServiceException(String message) {
		super(message);
	}
	
}
