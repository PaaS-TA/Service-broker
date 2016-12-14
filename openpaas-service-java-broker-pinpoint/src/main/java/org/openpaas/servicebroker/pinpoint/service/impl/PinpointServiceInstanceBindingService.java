package org.openpaas.servicebroker.pinpoint.service.impl;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.pinpoint.exception.PinpointServiceException;
import org.openpaas.servicebroker.pinpoint.model.PinpointServiceInstanceBinding;
import org.openpaas.servicebroker.service.ServiceInstanceBindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 서비스 인스턴스 바인딩 관련 서비스가 제공해야하는 메소드를 정의한 인터페이스 클래스인 ServiceInstanceBindingService를 상속하여
 * 서비스 인스턴스 바인드 생성/삭제/조회 를 구현한다.
 */
@Service
public class PinpointServiceInstanceBindingService implements ServiceInstanceBindingService {

    private static final Logger logger = LoggerFactory.getLogger(PinpointServiceInstanceBindingService.class);

    @Autowired
    private PinpointAdminService pinpointAdminService;

    @Autowired
    public PinpointServiceInstanceBindingService(PinpointAdminService pinpointAdminService) {
        this.pinpointAdminService = pinpointAdminService;
    }

    /**
     * createServiceInstanceBinding
     *
     * @param request
     * @return ServiceInstanceBinding
     * @throws ServiceInstanceBindingExistsException
     */
    @Override
    public ServiceInstanceBinding createServiceInstanceBinding(CreateServiceInstanceBindingRequest request)
            throws PinpointServiceException, ServiceInstanceBindingExistsException {

        logger.debug("PinpointServiceInstanceBindingService CLASS createServiceInstanceBinding");

        ServiceInstanceBinding binding = pinpointAdminService.createServiceInstanceBindingByRequest(request);
        return binding;
    }

    /**
     * deleteServiceInstanceBinding
     *
     * @param request
     * @return ServiceInstanceBinding
     * @throws ServiceBrokerException
     */
    @Override
    public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request)
            throws ServiceBrokerException {
        String bindingId = request.getBindingId();
        if (!pinpointAdminService.isExistBinding(bindingId)) throw new PinpointServiceException("Binding Service is not exist.");
        ServiceInstanceBinding binding = pinpointAdminService.deleteServiceInstanceBinding(bindingId);

        return binding;
    }




//

}
