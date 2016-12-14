package org.openpaas.servicebroker.pinpoint.service.impl;


import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.openpaas.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.UpdateServiceInstanceRequest;
import org.openpaas.servicebroker.pinpoint.exception.PinpointServiceException;
import org.openpaas.servicebroker.pinpoint.model.PinpointServiceInstance;
import org.openpaas.servicebroker.service.CatalogService;
import org.openpaas.servicebroker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 서비스 인스턴스 서비스가 제공해야하는 메소드를 정의한 인터페이스 클래스인 ServiceInstance를 상속하여
 * Mysql 서비스 인스턴스 서비스 관련 메소드를 구현한 클래스.
 * 서비스 인스턴스 생성/삭제/수정/조회 를 구현한다.
 */
@Service
public class PinpointServiceInstanceService implements ServiceInstanceService {

    private static final Logger logger = LoggerFactory.getLogger(PinpointServiceInstanceService.class);
    @Autowired
    private PinpointAdminService pinpointAdminService;

    @Autowired
    private CatalogService service;

    @Autowired
    public PinpointServiceInstanceService(PinpointAdminService pinpointAdminService) {
        this.pinpointAdminService = pinpointAdminService;
    }

    /**
     * Provision(create Instance)
     */
    @Override
    public ServiceInstance createServiceInstance (CreateServiceInstanceRequest request) throws ServiceBrokerException {
        logger.debug(this.getClass().getName().toString() + ":createServiceInstance");
        ServiceInstance instance = null;
        if (!pinpointAdminService.isExistInstance(request.getServiceInstanceId())) {
            instance = pinpointAdminService.createServiceInstanceByRequest(request);
        }
        return instance;

    }

    @Override
    public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request) throws PinpointServiceException {

        ServiceInstance instance = getServiceInstance(request.getServiceInstanceId());
        logger.debug(this.getClass().getName().toString() + ":deleteServiceInstance");
        if (instance == null) {
            throw new PinpointServiceException("instance is null : "+request.getServiceInstanceId());
        } else {
            pinpointAdminService.deleteServiceInstance(request.getServiceInstanceId());
        }

        return instance;
    }

    @Override
    public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest request)
            throws PinpointServiceException, ServiceInstanceDoesNotExistException, ServiceInstanceUpdateNotSupportedException{

        if ( pinpointAdminService.isExistInstance(request.getServiceInstanceId()) ) {
            throw new ServiceInstanceDoesNotExistException(request.getServiceInstanceId());
        } else {
            throw new ServiceInstanceUpdateNotSupportedException("Not Supported.");
        }
    }

    @Override
    public ServiceInstance getServiceInstance(String id){
        ServiceInstance instance = new PinpointServiceInstance();
        try {
            pinpointAdminService.isExistInstance(id);
            instance.setServiceInstanceId(id);
        } catch (PinpointServiceException e) {
            e.getLocalizedMessage();
        }

        return instance;
    }
}