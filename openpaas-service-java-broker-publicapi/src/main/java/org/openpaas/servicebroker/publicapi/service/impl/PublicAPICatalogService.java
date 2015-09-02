package org.openpaas.servicebroker.publicapi.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.model.Catalog;
import org.openpaas.servicebroker.model.ServiceDefinition;
import org.openpaas.servicebroker.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;


@Service
@PropertySource("application-mvc.properties")
public class PublicAPICatalogService implements CatalogService {

	private static final Logger logger = LoggerFactory.getLogger(PublicAPICatalogService.class);
	
	@Autowired
	private Environment env;
	
	private Catalog catalog;
	
	private Map<String,ServiceDefinition> serviceDefs = new HashMap<String,ServiceDefinition>();
	
	@Autowired
	public PublicAPICatalogService(Catalog catalog) {
		this.catalog = catalog;
		initializeMap();
	}
	
	private void initializeMap() {
		for (ServiceDefinition def: catalog.getServiceDefinitions()) {
			serviceDefs.put(def.getId(), def);
		}
	}
	
	@Override
	public Catalog getCatalog() throws ServiceBrokerException {	
		return catalog;
	}

	@Override
	public ServiceDefinition getServiceDefinition(String serviceId) throws ServiceBrokerException {
		logger.info(serviceId);
		return serviceDefs.get(serviceId);
	}
	
}
