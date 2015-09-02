package org.openpaas.servicebroker.mongodb.repository;

import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for ServiceInstanceBinding objects
 * 
 * @author 
 *
 */
public interface MongoServiceInstanceBindingRepository extends MongoRepository<ServiceInstanceBinding, String> {

}
