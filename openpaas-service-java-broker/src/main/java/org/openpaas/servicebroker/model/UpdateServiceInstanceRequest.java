package org.openpaas.servicebroker.model;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 서비스 인스턴스 Plan 정보를 업데이트 할 때 필요한 Request 정보를 가지고 있는 데이터 모델 bean 클래스. 
 * Json 어노테이션을 사용해서 JSON 형태로 제공
 * 
 * @author 송창학
 * @date 2015.0629
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
