package org.openpaas.servicebroker.glusterfs.exception;

import org.openpaas.servicebroker.exception.ServiceBrokerException;


/**
 * Glusterfs 서비스 관련 에러 Exception클래스 . ServiceBrokerException 클래스를 상속함.
 * 
 * @author 김한종
 *
 */
public class GlusterfsServiceException extends ServiceBrokerException {

	private static final long serialVersionUID = 8667141725171626000L;

	public GlusterfsServiceException(String message) {
		super(message);
	}
	
}
