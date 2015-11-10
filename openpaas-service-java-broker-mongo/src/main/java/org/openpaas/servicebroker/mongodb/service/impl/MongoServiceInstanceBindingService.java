package org.openpaas.servicebroker.mongodb.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.mongodb.repository.MongoServiceInstanceBindingRepository;
import org.openpaas.servicebroker.service.ServiceInstanceBindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClientURI;

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
		logger.info("=====> MongoServiceInstanceBindingService CLASS createServiceInstanceBinding");

		ServiceInstanceBinding binding = repository.findOne(request.getBindingId());
		if (binding != null) {
			String as_is_bindId = binding.getId();
			String as_is_instanceId = binding.getServiceInstanceId();
			String to_be_bindId = request.getBindingId();
			String to_be_instanceId = request.getServiceInstanceId();
			
			logger.debug("as-is : Binding ID = {}, Instance ID = {}", as_is_bindId, as_is_instanceId);
			logger.debug("to-be : Binding ID = {}, Instance ID = {}", to_be_bindId, to_be_instanceId);
			
			if( as_is_bindId.equals(to_be_bindId) && as_is_instanceId.equals(to_be_instanceId) ) {
				binding.setHttpStatusOK();
				return binding;
			} else {
				throw new ServiceInstanceBindingExistsException(binding);
			}
		}
		
		String database = request.getServiceInstanceId();
		String username = request.getBindingId();
		// TODO Password Generator
		String password = UUID.randomUUID().toString();
		
		// TODO check if user already exists in the DB

		mongo.createUser(database, username, password);
		
		Map<String,Object> credentials = new HashMap<String,Object>();
		MongoClientURI uri = new MongoClientURI(mongo.getConnectionString(database, username, password));
		
		credentials.put("uri", uri.getURI());
		credentials.put("name", uri.getDatabase());
		credentials.put("hosts", uri.getHosts());
		credentials.put("username", uri.getUsername());
		credentials.put("password", new String(uri.getPassword()));
	
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
		logger.info("=====> MongoServiceInstanceBindingService CLASS deleteServiceInstanceBinding");

		ServiceInstanceBinding binding = getServiceInstanceBinding(request.getBindingId());
		if (binding!= null) {
			mongo.deleteUser(binding.getServiceInstanceId(), request.getBindingId());
			repository.delete(request.getBindingId());
		}
		return binding;
	}

}
