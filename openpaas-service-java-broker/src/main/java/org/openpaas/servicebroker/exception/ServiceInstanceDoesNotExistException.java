package org.openpaas.servicebroker.exception;

/**
 * id 에 해당하는 서비스 인스턴스가  존재하는 않는 경우Exception클래스 . 
 * Exception 클래스를 상속함.
 * 
 * @author 송창학
 * @date 2015.0629
 */
public class ServiceInstanceDoesNotExistException extends Exception {
	
	private static final long serialVersionUID = -1879753092397657116L;
	
	public ServiceInstanceDoesNotExistException(String serviceInstanceId) {
		super("ServiceInstance does not exist: id = " + serviceInstanceId);
	}

}
