package org.openpaas.servicebroker.exception;

/**
 * ServiceDefinition 정보가 존재하지 않을 경우Exception클래스 . 
 * Exception 클래스를 상속함.
 * 
 * @author 송창학
 * @date 2015.0629
 */

public class ServiceDefinitionDoesNotExistException extends Exception {
	
	private static final long serialVersionUID = -62090827040416788L;

	public ServiceDefinitionDoesNotExistException(String serviceDefinitionId) {
		super("ServiceDefinition does not exist: id = " + serviceDefinitionId);
	}
	
}