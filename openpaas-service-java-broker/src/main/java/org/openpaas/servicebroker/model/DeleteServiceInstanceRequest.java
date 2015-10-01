package org.openpaas.servicebroker.model;

/**
 * 서비스 인스턴스를 삭제 할 때 필요한 Request 정보를 가지고 있는 데이터 모델 bean 클래스. 
 * Json 어노테이션을 사용해서 JSON 형태로 제공
 * 
 * @author 송창학
 * @date 2015.0629
 */

public class DeleteServiceInstanceRequest {

	private final String serviceInstanceId;
	private final String serviceId;
	private final String planId;

	public DeleteServiceInstanceRequest(String instanceId, String serviceId,
			String planId) {
		this.serviceInstanceId = instanceId; 
		this.serviceId = serviceId;
		this.planId = planId;
	}

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getPlanId() {
		return planId;
	}

}
