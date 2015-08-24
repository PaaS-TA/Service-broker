package org.openpaas.servicebroker.model;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * *서비스 인스턴스를 업데이트 하기 위해 클라우드 컨트롤러로부터 전달되는 요청
 * 
 * 2015.07.17
 * @author 송창학 수석
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateServiceInstanceRequest {

	@NotEmpty
	@JsonSerialize
	@JsonProperty("plan_id")
	private String planId;
	
	@JsonIgnore
	private String serviceInstanceId;

	public UpdateServiceInstanceRequest() {} 
	
	public UpdateServiceInstanceRequest(String planId) {
		this.planId = planId;
	}

	public String getPlanId() {
		return planId;
	}
	
	public String getServiceInstanceId() { 
		return serviceInstanceId;
	}

	public UpdateServiceInstanceRequest withInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId; 
		return this;
	}
}
