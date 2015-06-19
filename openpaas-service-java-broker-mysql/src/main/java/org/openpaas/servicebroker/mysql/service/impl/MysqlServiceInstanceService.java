package org.openpaas.servicebroker.mysql.service.impl;


import java.util.UUID;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.openpaas.servicebroker.exception.ServiceInstanceExistsException;
import org.openpaas.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.UpdateServiceInstanceRequest;
import org.openpaas.servicebroker.mysql.exception.MysqlServiceException;
import org.openpaas.servicebroker.service.CatalogService;
import org.openpaas.servicebroker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Mongo impl to manage service instances.  Creating a service does the following:
 * creates a new database,
 * saves the ServiceInstance info to the Mongo repository.
 *  
 * @author sgreenberg@gopivotal.com
 *
 */
@Service
public class MysqlServiceInstanceService implements ServiceInstanceService {

	private static final Logger logger = LoggerFactory.getLogger(MysqlServiceInstanceService.class);
	
	@Autowired
	private MysqlAdminService mysqlAdminService;
	
	@Autowired
	private CatalogService service;
	
	@Autowired
	public MysqlServiceInstanceService(MysqlAdminService mysqlAdminService) {
		this.mysqlAdminService = mysqlAdminService;
	}
	
	@Override
	public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request) 
			throws ServiceInstanceExistsException, ServiceBrokerException {
		System.out.println("MysqlServiceInstanceService CLASS createServiceInstance");
		logger.debug("MysqlServiceInstanceService CLASS createServiceInstance");

		//ServiceInstance instance = mysqlAdminService.findById(request.getServiceInstanceId());
		ServiceInstance instance = mysqlAdminService.createServiceInstanceByRequest(request);
		//if(instance == null) instance = mysqlAdminService.createServiceInstanceByRequest(request);
		
		/*System.out.println("ServiceInstanceId 	: " + instance.getServiceInstanceId());
		System.out.println("ServiceDefinitionId : " + instance.getServiceDefinitionId());
		System.out.println("PlanId 				: " + instance.getPlanId());
		System.out.println("OrganizationGuid 	: " + instance.getOrganizationGuid());
		System.out.println("SpaceGuid 			: " + instance.getSpaceGuid());
		System.out.println("DashboardUrl 		: " + instance.getDashboardUrl());
		*/
		
		if (mysqlAdminService.isExistsService(instance)) {
			// ensure the instance is empty
			mysqlAdminService.deleteDatabase(instance);
		}
		mysqlAdminService.createDatabase(instance);
		
		mysqlAdminService.save(instance);
		return instance;
	}
	

	@Override
	public ServiceInstance getServiceInstance(String id) {
		return mysqlAdminService.findById(id);
	}

	@Override
	public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request) throws ServiceBrokerException {
		ServiceInstance instance = mysqlAdminService.findById(request.getServiceInstanceId());
		mysqlAdminService.deleteDatabase(instance);
		mysqlAdminService.delete(instance.getServiceInstanceId());
		return instance;		
	}

	@Override
	public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest request)
			throws ServiceInstanceUpdateNotSupportedException, ServiceBrokerException, ServiceInstanceDoesNotExistException {
		ServiceInstance instance = mysqlAdminService.findById(request.getServiceInstanceId());
		
		ServiceInstance updatedInstance = new ServiceInstance(request);

		if (mysqlAdminService.isExistsService(instance)) {
			// ensure the instance is empty
			mysqlAdminService.deleteDatabase(instance);
		}

		mysqlAdminService.createDatabase(updatedInstance);
		
		//mysqlAdminService.delete(instance.getServiceInstanceId());
		
		mysqlAdminService.updatePlan(instance, updatedInstance);
		return updatedInstance;
	}
	
	
}