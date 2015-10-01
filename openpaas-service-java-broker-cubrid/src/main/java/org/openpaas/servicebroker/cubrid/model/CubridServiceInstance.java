package org.openpaas.servicebroker.cubrid.model;

import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.UpdateServiceInstanceRequest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * An instance of a ServiceDefinition.
 * 
 * @author Cho Mingu
 *
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public class CubridServiceInstance extends ServiceInstance{

	private String databaseName;
	
	/**
	 * Create a ServiceInstance from a create request. If fields 
	 * are not present in the request they will remain null in the  
	 * ServiceInstance.
	 * @param request containing details of ServiceInstance
	 */
	public CubridServiceInstance(CreateServiceInstanceRequest request) {
		super(request);
	}
	
	/**
	 * Create a ServiceInstance from a delete request. If fields 
	 * are not present in the request they will remain null in the 
	 * ServiceInstance.
	 * @param request containing details of ServiceInstance
	 */
	public CubridServiceInstance(DeleteServiceInstanceRequest request) { 
		super(request);
	}
	
	/**
	 * Create a service instance from a update request. If fields 
	 * are not present in the request they will remain null in the 
	 * ServiceInstance.
	 * @param request containing details of ServiceInstance
	 */
	public CubridServiceInstance(UpdateServiceInstanceRequest request) { 
		super(request);
	}
	
	public CubridServiceInstance() {
		super(new CreateServiceInstanceRequest());
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	
}
