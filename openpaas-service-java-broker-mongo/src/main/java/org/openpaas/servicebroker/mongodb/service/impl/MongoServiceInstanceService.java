package org.openpaas.servicebroker.mongodb.service.impl;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.openpaas.servicebroker.exception.ServiceInstanceExistsException;
import org.openpaas.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.UpdateServiceInstanceRequest;
import org.openpaas.servicebroker.mongodb.exception.MongoServiceException;
import org.openpaas.servicebroker.mongodb.model.MongoServiceInstance;
import org.openpaas.servicebroker.mongodb.repository.MongoServiceInstanceRepository;
import org.openpaas.servicebroker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.DB;

/**
 *  
 * @author
 *
 */
@Service
public class MongoServiceInstanceService implements ServiceInstanceService {

	private static final Logger logger = LoggerFactory.getLogger(MongoServiceInstanceService.class);

	private MongoAdminService mongo;

	private MongoServiceInstanceRepository repository;

	@Autowired
	public MongoServiceInstanceService(MongoAdminService mongo, MongoServiceInstanceRepository repository) {
		this.mongo = mongo;
		this.repository = repository;
	}

	@Override
	public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request) 
			throws ServiceInstanceExistsException, ServiceBrokerException {
		logger.info("=====> MongoServiceInstanceService CLASS createServiceInstance");

		// TODO MongoDB dashboard
		MongoServiceInstance instance = repository.findOne(request.getServiceInstanceId());
		if (instance != null) {
			String as_is_id = instance.getServiceInstanceId();
			String as_is_plan = instance.getPlanId();
			String to_be_id = request.getServiceInstanceId();
			String to_be_plan = request.getPlanId();

			logger.debug("as-is : Instance ID = {}, Plan = {}", as_is_id, as_is_plan);
			logger.debug("to-be : Instance ID = {}, Plan = {}", to_be_id, to_be_plan);

			if( as_is_id.equals(to_be_id) && as_is_plan.equals(to_be_plan) ) {
				instance.setHttpStatusOK();
				return instance;
			} else {
				throw new ServiceInstanceExistsException(instance);
			}
		}
		instance = new MongoServiceInstance(request);
		if (mongo.databaseExists(request.getServiceInstanceId())) {
			// ensure the instance is empty
			mongo.deleteDatabase(request.getServiceInstanceId());
		}
		DB db = mongo.createDatabase(instance.getServiceInstanceId());
		if (db == null) {
			throw new ServiceBrokerException("Failed to create new DB instance: " + instance.getServiceInstanceId());
		}
		repository.save(instance);
		return instance;
	}


	@Override
	public ServiceInstance getServiceInstance(String id) {
		return repository.findOne(id);
	}

	@Override
	public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request) throws MongoServiceException {
		logger.info("=====> MongoServiceInstanceService CLASS deleteServiceInstance");

		mongo.deleteDatabase(request.getServiceInstanceId());
		ServiceInstance instance = repository.findOne(request.getServiceInstanceId());
		repository.delete(request.getServiceInstanceId());
		return instance;
	}

	@Override
	public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest request)
			throws ServiceInstanceUpdateNotSupportedException, ServiceBrokerException, ServiceInstanceDoesNotExistException {
		logger.info("=====> MongoServiceInstanceService CLASS updateServiceInstance");

		ServiceInstance instance = repository.findOne(request.getServiceInstanceId());
		if (instance == null) {
			throw new ServiceInstanceDoesNotExistException(request.getServiceInstanceId());
		}
		
		throw new ServiceInstanceUpdateNotSupportedException("Not Support");
		
		/*
		repository.delete(request.getServiceInstanceId());
		MongoServiceInstance updatedInstance = new MongoServiceInstance(request);
		repository.save(updatedInstance);
		return updatedInstance;*/
	}

}