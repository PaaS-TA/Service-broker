package org.openpaas.servicebroker.exception;

/**
 * 서비스 브로커 GeneralException클래스 . Exception 클래스를 상속함.
 * 
 * 
 * @author 송창학
 * @date 2015.0629
 */
public class ServiceBrokerException extends Exception {

	private static final long serialVersionUID = -5544859893499349135L;

	public ServiceBrokerException(String message) {
		super(message);
	}

	public ServiceBrokerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceBrokerException(Throwable cause) {
		super(cause);
	}

}