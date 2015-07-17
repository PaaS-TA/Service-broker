package org.openpaas.servicebroker.model;

/**
 *  A request sent by the cloud controller to remove a binding
 *  of a service.
 *  *서비스를 언바인드 하기 위해 클라우드 컨트롤러로부터 전달된 요청 (Unbind)
 *  
 *  2015.07.17
 * @author 송창학 수석
 *
 */
public class DeleteServiceInstanceBindingRequest {

	private final String bindingId;
	private final String serviceId;
	private final ServiceInstance instance;
	private final String planId;

	public DeleteServiceInstanceBindingRequest(String bindingId,
			ServiceInstance instance, String serviceId, String planId) {
		this.bindingId = bindingId;
		this.instance = instance;
		this.serviceId = serviceId; 
		this.planId = planId;
	}

	public String getBindingId() {
		return bindingId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public ServiceInstance getInstance() {
		return instance;
	}

	public String getPlanId() {
		return planId;
	}

}
