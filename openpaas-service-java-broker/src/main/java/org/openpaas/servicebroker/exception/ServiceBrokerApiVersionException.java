package org.openpaas.servicebroker.exception;

/**
 * 서비스 브로커 Api 버전 호환 Exception클래스 . Exception 클래스를 상속함.
 * 
 * @author 송창학
 * @date 2015.0629
 */
public class ServiceBrokerApiVersionException extends Exception {

	private static final long serialVersionUID = -6792404679608443775L;

	public ServiceBrokerApiVersionException(String expectedVersion, String providedVersion) {
		super("The provided service broker API version is not supported: "
				+ "Expected Version = " + expectedVersion + ", "
				+ "Provided Version = " + providedVersion);
	}
	
}
