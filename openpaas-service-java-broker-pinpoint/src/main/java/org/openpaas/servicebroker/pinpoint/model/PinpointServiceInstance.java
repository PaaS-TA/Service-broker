package org.openpaas.servicebroker.pinpoint.model;

import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.UpdateServiceInstanceRequest;

public class PinpointServiceInstance extends ServiceInstance{
    private String serviceInstanceId ;
    
	public PinpointServiceInstance(CreateServiceInstanceRequest request){
		super(request);
	}
    public PinpointServiceInstance(DeleteServiceInstanceRequest request) {
        super(request);
    }

    public PinpointServiceInstance(UpdateServiceInstanceRequest request) {
        super(request);
    }

    public PinpointServiceInstance() {
        super(new CreateServiceInstanceRequest());
    }
    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

}
