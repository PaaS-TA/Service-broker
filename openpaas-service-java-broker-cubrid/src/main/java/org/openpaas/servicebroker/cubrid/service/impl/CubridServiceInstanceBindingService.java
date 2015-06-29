package org.openpaas.servicebroker.cubrid.service.impl;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.cubrid.model.CubridServiceInstance;
import org.openpaas.servicebroker.cubrid.model.CubridServiceInstanceBinding;
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
		CubridServiceInstanceBinding binding = cubridAdminService.findBindById(request.getBindingId());
		if (binding != null) {
			throw new ServiceInstanceBindingExistsException(binding);
		}
		CubridServiceInstance instance = cubridAdminService.findById(request.getServiceInstanceId());
		
		String database = instance.getDatabaseName();
		// TODO Password Generator
		String password = getPassword();
		String username = null;
		do {
			username = getUsername();
		} while(cubridAdminService.isExistsUser(database, username));
		
		cubridAdminService.createUser(database, username, password);
		
		Map<String,Object> credentials = new HashMap<String,Object>();
		credentials.put("uri", cubridAdminService.getConnectionString(database, username, password));
		credentials.put("hostname", cubridAdminService.getConnectionString(database, username, password));
		credentials.put("username", username);
		credentials.put("password", password);
		
		binding = new CubridServiceInstanceBinding(request.getBindingId(), instance.getServiceInstanceId(), credentials, null, request.getAppGuid());
		binding.setDatabaseUserName(username);
		cubridAdminService.saveBind(binding);
		
		return binding;
	}

	protected CubridServiceInstanceBinding getServiceInstanceBinding(String id) {
		return cubridAdminService.findBindById(id);
	}
 
	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException {
		String bindingId = request.getBindingId();
		CubridServiceInstanceBinding binding = getServiceInstanceBinding(bindingId);
		CubridServiceInstance instance = cubridAdminService.findById(binding.getServiceInstanceId());
		if (binding!= null) {
			cubridAdminService.deleteUser(instance.getDatabaseName(), binding.getDatabaseUserName());
			cubridAdminService.deleteBind(bindingId);
		}
		return binding;
	}
	
	private String getUsername() {
		String uuid16 = null;
		MessageDigest digest = null;
		try {
			do {
				digest = MessageDigest.getInstance("MD5");
				digest.update(UUID.randomUUID().toString().getBytes());
				uuid16 = new BigInteger(1, digest.digest()).toString(16).replaceAll("/[^a-zA-Z]+/", "").substring(0, 16);
			} while(!uuid16.matches("^[a-zA-Z][a-zA-Z0-9]+"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return uuid16;
	}
	
	private String getPassword() {
		MessageDigest digest = null;
		try {
				digest = MessageDigest.getInstance("MD5");
				digest.update(UUID.randomUUID().toString().getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return new BigInteger(1, digest.digest()).toString(16).replaceAll("/[^a-zA-Z]+/", "").substring(0, 16);
	}

}
