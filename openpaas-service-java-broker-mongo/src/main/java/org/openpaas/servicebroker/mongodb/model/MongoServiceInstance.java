package org.openpaas.servicebroker.mongodb.model;

import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonAutoDetect;


/**
 * An instance of a ServiceDefinition.
 * 
 * @author 
 *
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@Document(collection="serviceInstance")
public class MongoServiceInstance extends ServiceInstance{

	private String id;
	
	/**
	 * Create a ServiceInstance from a create request. If fields 
	 * are not present in the request they will remain null in the  
	 * ServiceInstance.
	 * @param request containing details of ServiceInstance
	 */
	public MongoServiceInstance(CreateServiceInstanceRequest request) {
		super(request);
		this.id = request.getServiceInstanceId();
	}
	
	/**
	 * Create a ServiceInstance from a delete request. If fields 
	 * are not present in the request they will remain null in the 
	 * ServiceInstance.
	 * @param request containing details of ServiceInstance
	 */
	public MongoServiceInstance(DeleteServiceInstanceRequest request) { 
		super(request);
		this.id = request.getServiceInstanceId();
	}
	
	/**
	 * Create a service instance from a delete request. If fields 
	 * are not present in the request they will remain null in the 
	 * ServiceInstance.
	 * @param request containing details of ServiceInstance
	 */
	public MongoServiceInstance(UpdateServiceInstanceRequest request) { 
		super(request);
		this.id = request.getServiceInstanceId();
	}
	
	public MongoServiceInstance() {
		super(new CreateServiceInstanceRequest());
	}
	
	public MongoServiceInstance(String id, String serviceDefinitionId, String planId, String organizationGuid,
			String spaceGuid) {
		super(new CreateServiceInstanceRequest(
				serviceDefinitionId, 
				planId, 
				organizationGuid, 
				spaceGuid).and().
				withServiceInstanceId(id));
		this.id = id;
	}
	
	public String getId() {
		return this.id;
	}
}
