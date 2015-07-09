package org.openpaas.servicebroker.mongodb.service.impl;

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
import org.openpaas.servicebroker.mongodb.repository.MongoServiceInstanceBindingRepository;
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
 * @author 
 *
 */
@Service
public class MongoServiceInstanceBindingService implements ServiceInstanceBindingService {

	private static final Logger logger = LoggerFactory.getLogger(MongoServiceInstanceBindingService.class);

	private MongoAdminService mongo; 

	private MongoServiceInstanceBindingRepository repository;

	@Autowired
	public MongoServiceInstanceBindingService(MongoAdminService mongo, 
			MongoServiceInstanceBindingRepository repository) {
		this.mongo = mongo;
		this.repository = repository;
	}

	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(
			CreateServiceInstanceBindingRequest request)
					throws ServiceInstanceBindingExistsException, ServiceBrokerException {
		logger.info("=====> CubridServiceInstanceBindingService CLASS createServiceInstanceBinding");

		ServiceInstanceBinding binding = repository.findOne(request.getBindingId());
		if (binding != null) {
			throw new ServiceInstanceBindingExistsException(binding);
		}
		
		String database = request.getServiceInstanceId();
		String username = request.getBindingId();
		// TODO Password Generator
		String password = UUID.randomUUID().toString();
		
		// TODO check if user already exists in the DB

		mongo.createUser(database, username, password);
		
		Map<String,Object> credentials = new HashMap<String,Object>();
		credentials.put("uri", mongo.getConnectionString(database, username, password));
		
		binding = new ServiceInstanceBinding(request.getBindingId(), request.getServiceInstanceId(), credentials, null, request.getAppGuid());
		repository.save(binding);
		
		return binding;
	}

	protected ServiceInstanceBinding getServiceInstanceBinding(String id) {
		return repository.findOne(id);
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException {
		logger.info("=====> CubridServiceInstanceBindingService CLASS deleteServiceInstanceBinding");

		ServiceInstanceBinding binding = getServiceInstanceBinding(request.getBindingId());
		if (binding!= null) {
			mongo.deleteUser(binding.getServiceInstanceId(), request.getBindingId());
			repository.delete(request.getBindingId());
		}
		return binding;
	}

}
