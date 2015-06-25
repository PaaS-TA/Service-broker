package org.openpaas.servicebroker.cubrid.service.impl;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.openpaas.servicebroker.cubrid.exception.CubridServiceException;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.openpaas.servicebroker.exception.ServiceInstanceExistsException;
import org.openpaas.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.UpdateServiceInstanceRequest;
import org.openpaas.servicebroker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Cubrid impl to manage service instances.  Creating a service does the following:
 * creates a new database,
 * saves the ServiceInstance info to the Cubrid repository.
 *  
 * @author
 *
 */
@Service
public class CubridServiceInstanceService implements ServiceInstanceService {

	private static final Logger logger = LoggerFactory.getLogger(CubridServiceInstanceService.class);
	
	@Autowired
	private CubridAdminService cubridAdminService;
	
	@Autowired
	public CubridServiceInstanceService(CubridAdminService cubridAdminService) {
		this.cubridAdminService = cubridAdminService;
	}
	
	@Override
	public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request) 
			throws ServiceInstanceExistsException, ServiceBrokerException {
		System.out.println("CubridServiceInstanceService CLASS createServiceInstance");
		logger.debug("CubridServiceInstanceService CLASS createServiceInstance");
		// TODO Cubrid dashboard
		//ServiceInstance instance = cubridAdminService.findById(request.getServiceInstanceId());
		
		ServiceInstance instance = new ServiceInstance();
		instance.setServiceInstanceId(request.getServiceInstanceId());
		instance.setPlanId(request.getPlanId());
		do {
			instance.setDatabaseName(getDatabaseName());
		} while(cubridAdminService.isExistsService(instance));
		cubridAdminService.createDatabase(instance);
		cubridAdminService.save(instance);
		
		return instance;
	}
	

	@Override
	public ServiceInstance getServiceInstance(String id) {
		return cubridAdminService.findById(id);
	}

	@Override
	public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request) throws CubridServiceException {
		ServiceInstance instance = cubridAdminService.findById(request.getServiceInstanceId());
		cubridAdminService.deleteDatabase(instance);
		cubridAdminService.delete(instance.getServiceInstanceId());
		return instance;		
	}

	@Override
	public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest request)
			throws ServiceInstanceUpdateNotSupportedException, ServiceBrokerException, ServiceInstanceDoesNotExistException {
		ServiceInstance instance = cubridAdminService.findById(request.getServiceInstanceId());
		cubridAdminService.delete(instance.getServiceInstanceId());
		ServiceInstance updatedInstance = new ServiceInstance(request);
		cubridAdminService.save(updatedInstance);
		return updatedInstance;
	}
	
	private String getDatabaseName() {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.update(UUID.randomUUID().toString().getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		//[^a-zA-Z0-9]
		return new BigInteger(1, digest.digest()).toString(16).replaceAll("/[^a-zA-Z0-9]+/", "").substring(0, 16);
		
	}
	
}