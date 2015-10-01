package org.openpaas.servicebroker.exception;

/**
 * 서비스 인스턴스 Plan 변경 기능이 제공되지 않을 때의 Exception클래스 . Exception 클래스를 상속함.
 * 
 * @author 송창학
 * @date 2015.0629
 */

public class ServiceInstanceUpdateNotSupportedException extends Exception {

	private static final long serialVersionUID = 4719676639792071582L;

	public ServiceInstanceUpdateNotSupportedException(String message) {
		super(message);
	}

	public ServiceInstanceUpdateNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceInstanceUpdateNotSupportedException(Throwable cause) {
		super(cause);
	}

}
