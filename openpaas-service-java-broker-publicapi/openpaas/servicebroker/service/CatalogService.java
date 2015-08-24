package org.openpaas.servicebroker.service;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.model.Catalog;
import org.openpaas.servicebroker.model.ServiceDefinition;


/**
 * Handles the catalog of services made available by this 
 * broker.
 * *브로커에 의해서 사용가능하게 된 서비스 목록을 제어한다.
 * 
 * 2015.07.17
 * @author 송창학 수석
 */
public interface CatalogService {

	/**
	 * 브로커에 의해 제공되는 서비스의 목록을 가져온다.
	 * 
	 * @rerurn 서비스 목록
	 * 2015.07.17
	 * @author 송창학 수석
	 */
	Catalog getCatalog() throws ServiceBrokerException;

	/**
	 * 인자값인 서비스 아이디에 해당하는 ServiceDefinition을 가져온다.
	 * 
	 * @param serviceId  서비스 목록에 있는 서비스의 아이디
	 * @return ServiceDefinition객체, 서비스가 존재하지 않을 경우 null을 리턴
	 * 2015.07.17
	 * @author 송창학 수석
	 */
	ServiceDefinition getServiceDefinition(String serviceId) throws ServiceBrokerException;
	
}
