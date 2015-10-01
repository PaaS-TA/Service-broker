package org.openpaas.servicebroker.model;

/**
 * 바인드된 서비스 인스턴스를 바인드 삭제 할 때 필요한 Request 정보를 가지고 있는 데이터 모델 bean 클래스. 
 * Json 어노테이션을 사용해서 JSON 형태로 제공
 * 
 * @author 송창학
 * @date 2015.0629
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
