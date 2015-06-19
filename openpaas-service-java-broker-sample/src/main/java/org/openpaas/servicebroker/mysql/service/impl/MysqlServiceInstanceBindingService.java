package org.openpaas.servicebroker.mysql.service.impl;

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
 * Mongo impl to bind services.  Binding a service does the following:
 * creates a new user in the database (currently uses a default pwd of "password"),
 * saves the ServiceInstanceBinding info to the Mongo repository.
 *  
 * @author sgreenberg@gopivotal.com
 *
 */
@Service
public class MysqlServiceInstanceBindingService implements ServiceInstanceBindingService {

	private static final Logger logger = LoggerFactory.getLogger(MysqlServiceInstanceBindingService.class);
	@Autowired
	private MysqlAdminService mysqlAdminService; 
	
	
	@Autowired
	public MysqlServiceInstanceBindingService(MysqlAdminService mysqlAdminService) {
		this.mysqlAdminService = mysqlAdminService;
	}
	
	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(
			CreateServiceInstanceBindingRequest request)
			throws ServiceInstanceBindingExistsException, ServiceBrokerException {
		
		logger.debug("MysqlServiceInstanceBindingService CLASS createServiceInstanceBinding");
		ServiceInstanceBinding binding = mysqlAdminService.findBindById(request.getBindingId());
		if (binding != null) {
			throw new ServiceInstanceBindingExistsException(binding);
		}
		ServiceInstance instance = mysqlAdminService.findById(request.getServiceInstanceId());
		
		String database = instance.getServiceInstanceId();
		String username = request.getBindingId();
		// TODO Password Generator
		String password = "password";
		
		if (mysqlAdminService.isExistsUser(username)) {
			// ensure the instance is empty
			mysqlAdminService.deleteUser(database, username);
		}
		

		mysqlAdminService.createUser(database, username, password);
		
		Map<String,Object> credentials = new HashMap<String,Object>();
		credentials.put("uri", mysqlAdminService.getConnectionString(database, username, password));
		credentials.put("hostname", mysqlAdminService.getConnectionString(database, username, password));
		
		binding = new ServiceInstanceBinding(request.getBindingId(), instance.getServiceInstanceId(), credentials, null, request.getAppGuid());
		mysqlAdminService.saveBind(binding);
		
		return binding;
	}

	protected ServiceInstanceBinding getServiceInstanceBinding(String id) {
		return mysqlAdminService.findBindById(id);
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException {
		String bindingId = request.getBindingId();
		ServiceInstanceBinding binding = getServiceInstanceBinding(bindingId);
		if (binding!= null) {
			mysqlAdminService.deleteUser(binding.getServiceInstanceId(), bindingId);
			mysqlAdminService.deleteBind(bindingId);
		}
		return binding;
	}

}
