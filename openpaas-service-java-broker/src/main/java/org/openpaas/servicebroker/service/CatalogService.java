package org.openpaas.servicebroker.service;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.model.Catalog;
import org.openpaas.servicebroker.model.ServiceDefinition;

/**
 * Catalog 서비스가 제공해야하는 메소드를 정의한 인터페이스 클래스
 * 
 * @author 송창학
 * @date 2015.0629
 */

public interface CatalogService {

	/**
	 * @return The catalog of services provided by this broker.
	 */
	Catalog getCatalog() throws ServiceBrokerException;

	/**
	 * @param serviceId  The id of the service in the catalog
	 * @return The service definition or null if it doesn't exist
	 */
	ServiceDefinition getServiceDefinition(String serviceId) throws ServiceBrokerException;
	
}
