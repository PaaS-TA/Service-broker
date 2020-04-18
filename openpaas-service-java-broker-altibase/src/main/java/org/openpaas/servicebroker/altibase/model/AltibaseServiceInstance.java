package org.openpaas.servicebroker.altibase.model;

import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.UpdateServiceInstanceRequest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * An instance of a ServiceDefinition.
 * 
 * @author Bae Younghee
 *
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public class AltibaseServiceInstance extends ServiceInstance{

	private String mHost;
	private String mPort;
	private int mNodeId;
	
	/**
	 * Create a ServiceInstance from a create request. If fields 
	 * are not present in the request they will remain null in the  
	 * ServiceInstance.
	 * @param request containing details of ServiceInstance
	 */
	public AltibaseServiceInstance(CreateServiceInstanceRequest request) {
		super(request);
	}
	
	/**
	 * Create a ServiceInstance from a delete request. If fields 
	 * are not present in the request they will remain null in the 
	 * ServiceInstance.
	 * @param request containing details of ServiceInstance
	 */
	public AltibaseServiceInstance(DeleteServiceInstanceRequest request) { 
		super(request);
	}
	
	/**
	 * Create a service instance from a update request. If fields 
	 * are not present in the request they will remain null in the 
	 * ServiceInstance.
	 * @param request containing details of ServiceInstance
	 */
	public AltibaseServiceInstance(UpdateServiceInstanceRequest request) { 
		super(request);
	}
	
	public AltibaseServiceInstance() {
		super(new CreateServiceInstanceRequest());
	}

	public String getHost() {
		return mHost;
	}

	public void setHost(String aHost) {
		this.mHost = aHost;
	}
	
	public String getPort() {
		return mPort;
	}

	public void setPort(String aPort) {
		this.mPort = aPort;
	}

	public int getNodeId() {
		return mNodeId;
	}
	
	public void setNodeId(int aNodeId) {
		mNodeId = aNodeId;
	}
}
