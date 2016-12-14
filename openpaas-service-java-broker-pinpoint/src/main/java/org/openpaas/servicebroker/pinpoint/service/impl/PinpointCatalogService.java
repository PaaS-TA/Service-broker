package org.openpaas.servicebroker.pinpoint.service.impl;

import org.openpaas.servicebroker.model.Catalog;
import org.openpaas.servicebroker.model.ServiceDefinition;
import org.openpaas.servicebroker.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Catalog 서비스가 제공해야하는 메소드를 정의한 클래스
 *
 * @author 김한종
 */
@Service
public class PinpointCatalogService implements CatalogService {

    private static final Logger logger = LoggerFactory.getLogger(PinpointCatalogService.class);
    private Catalog catalog;
    private Map<String, ServiceDefinition> serviceDefs = new HashMap<String, ServiceDefinition>();

    @Autowired
    public PinpointCatalogService(Catalog catalog) {
        this.catalog = catalog;
        initializeMap();
    }

    private void initializeMap() {
        for (ServiceDefinition def : catalog.getServiceDefinitions()) {
            serviceDefs.put(def.getId(), def);
        }
    }

    @Override
    public Catalog getCatalog() {
        return catalog;
    }

    @Override
    public ServiceDefinition getServiceDefinition(String serviceId) {
        return serviceDefs.get(serviceId);
    }

}
