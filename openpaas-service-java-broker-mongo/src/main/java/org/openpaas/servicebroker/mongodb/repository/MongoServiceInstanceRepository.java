package org.openpaas.servicebroker.mongodb.repository;

import org.openpaas.servicebroker.mongodb.model.MongoServiceInstance;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for ServiceInstance objects
 * 
 * @author 
 *
 */
public interface MongoServiceInstanceRepository extends MongoRepository<MongoServiceInstance, String> {

}