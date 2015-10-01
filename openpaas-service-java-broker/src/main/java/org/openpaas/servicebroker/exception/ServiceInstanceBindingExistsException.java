package org.openpaas.servicebroker.exception;

import org.openpaas.servicebroker.model.ServiceInstanceBinding;



/**
 * Bindig id 에 해당하는 서비스 인스턴스가 존재하는 경우Exception클래스 . 
 * Exception 클래스를 상속함.
 * 
 * @author 송창학
 * @date 2015.0629
 */
public class ServiceInstanceBindingExistsException extends Exception {

	private static final long serialVersionUID = -914571358227517785L;
	
	public ServiceInstanceBindingExistsException(ServiceInstanceBinding binding) {
		super("ServiceInstanceBinding already exists: serviceInstanceBinding.id = "
				+ binding.getId()
				+ ", serviceInstance.id = " + binding.getServiceInstanceId());
	}

}
