package org.openpaas.servicebroker.exception;

import org.openpaas.servicebroker.model.ServiceInstanceBinding;



/**
 * Thrown when a duplicate request to bind to a service instance is 
 * received.
 * 
 * @author sgreenberg@gopivotal.com
 */
public class ServiceInstanceBindingExistsException extends Exception {

	private static final long serialVersionUID = -914571358227517785L;
	
	public ServiceInstanceBindingExistsException(ServiceInstanceBinding binding) {
		super("ServiceInstanceBinding already exists: serviceInstanceBinding.id = "
				+ binding.getId()
				+ ", serviceInstance.id = " + binding.getServiceInstanceId());
	}

}
