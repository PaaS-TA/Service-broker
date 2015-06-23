package org.openpaas.servicebroker.apiplatform.service.impl;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

@Service
public class APIServiceInstanceBindingService implements ServiceInstanceBindingService{

	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(
			CreateServiceInstanceBindingRequest arg0)
			throws ServiceInstanceBindingExistsException,
			ServiceBrokerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(
			DeleteServiceInstanceBindingRequest arg0)
			throws ServiceBrokerException {
		// TODO Auto-generated method stub
		return null;
	}

}
