package org.openpaas.servicebroker.model;

/**
 * 서비스 인스턴스를 삭제하기 위해 클라우드 컨트롤러로부터 전달된 요청 (Deprovision)
 * 
 * 2015.07.17
 * @author 송창학 수석
 *
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
