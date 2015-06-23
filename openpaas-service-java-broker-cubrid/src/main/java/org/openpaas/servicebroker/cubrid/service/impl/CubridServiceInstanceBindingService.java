package org.openpaas.servicebroker.cubrid.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.service.ServiceInstanceBindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * cubrid impl to bind services.  Binding a service does the following:
 * creates a new user in the database (currently uses a default pwd of "password"),
 * saves the ServiceInstanceBinding info to the Mongo repository.
 *  
 * @author sgreenberg@gopivotal.com
 *
 */
@Service
public class CubridServiceInstanceBindingService implements ServiceInstanceBindingService {

	private static final Logger logger = LoggerFactory.getLogger(CubridServiceInstanceBindingService.class);
	@Autowired
	private CubridAdminService cubridAdminService; 
	
	
	@Autowired
	public CubridServiceInstanceBindingService(CubridAdminService cubridAdminService) {
		this.cubridAdminService = cubridAdminService;
	}
	
	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(
			CreateServiceInstanceBindingRequest request)
			throws ServiceInstanceBindingExistsException, ServiceBrokerException {
		
		logger.debug("CubridServiceInstanceBindingService CLASS createServiceInstanceBinding");
		ServiceInstanceBinding binding = cubridAdminService.findBindById(request.getBindingId());
		if (binding != null) {
			throw new ServiceInstanceBindingExistsException(binding);
		}
		ServiceInstance instance = cubridAdminService.findById(request.getServiceInstanceId());
		
		String database = instance.getServiceInstanceId();
		String username = request.getBindingId();
		// TODO Password Generator
		String password = "password";
		
		if (cubridAdminService.isExistsUser(username)) {
			// ensure the instance is empty
			cubridAdminService.deleteUser(database, username);
		}
		

		cubridAdminService.createUser(database, username, password);
		
		Map<String,Object> credentials = new HashMap<String,Object>();
		credentials.put("uri", cubridAdminService.getConnectionString(database, username, password));
		credentials.put("hostname", cubridAdminService.getConnectionString(database, username, password));
		
		binding = new ServiceInstanceBinding(request.getBindingId(), instance.getServiceInstanceId(), credentials, null, request.getAppGuid());
		cubridAdminService.saveBind(binding);
		
		return binding;
	}

	protected ServiceInstanceBinding getServiceInstanceBinding(String id) {
		return cubridAdminService.findBindById(id);
	}
 
	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException {
		String bindingId = request.getBindingId();
		ServiceInstanceBinding binding = getServiceInstanceBinding(bindingId);
		if (binding!= null) {
			cubridAdminService.deleteUser(binding.getServiceInstanceId(), bindingId);
			cubridAdminService.deleteBind(bindingId);
		}
		return binding;
	}

}
