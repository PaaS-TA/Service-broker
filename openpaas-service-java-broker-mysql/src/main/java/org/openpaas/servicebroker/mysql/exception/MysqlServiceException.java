package org.openpaas.servicebroker.mysql.exception;

import org.openpaas.servicebroker.exception.ServiceBrokerException;


/**
 * Mysql 서비스 관련 에러 Exception클래스 
 * 
 * @author 김한종
 *
 */
public class MysqlServiceException extends ServiceBrokerException {

	private static final long serialVersionUID = 8667141725171626000L;

	public MysqlServiceException(String message) {
		super(message);
	}
	
}
