package org.openpaas.servicebroker.model;

import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.HashMap;
import java.util.Map;

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
	
	@JsonSerialize
	@JsonProperty("service_id")
	private String serviceDefinitionId;

	@JsonSerialize
	@JsonProperty("parameters")
	private Map<String, Object> parameters;

	@JsonSerialize
	@JsonProperty("previous_values")
	private PreviousValues previousValues;

	@JsonIgnore
	private String serviceInstanceId;

	public UpdateServiceInstanceRequest() {} 
	
	public UpdateServiceInstanceRequest(String planId) {
		this.planId = planId;
	}

	public UpdateServiceInstanceRequest(String planId, String serviceDefinitionId) {
		this.planId = planId;
		this.serviceDefinitionId = serviceDefinitionId;
	}

	public UpdateServiceInstanceRequest(String planId, String serviceDefinitionId, Map<String, Object> parameters, PreviousValues previousValues) {
		this(planId, serviceDefinitionId);
		this.parameters = parameters;
		this.previousValues = previousValues;
	}

	public void setPlanId(String planId) {
		this.planId = planId;
	}

	public String getPlanId() {
		return planId;
	}

	public void setServiceDefinitionId(String serviceDefinitionId) { this.serviceDefinitionId = serviceDefinitionId; }

	public String getServiceDefinitionId() { return serviceDefinitionId; }

	public Map<String, Object> getParameters() { return  parameters; }
	public <T> T getParameters(Class<T> cls) {
		try {
			T bean = cls.newInstance();
			BeanUtils.populate(bean, parameters);
			return bean;
		} catch (Exception e) {
			throw new IllegalArgumentException("Error mapping UpdateServiceInstanceRequest parameters to class of type " + cls.getName());
		}
	}
	public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

	public void setPreviousValues(PreviousValues previousValues) { this.previousValues = previousValues; }
	public PreviousValues getPreviousValues() {
		return previousValues;
	}
	
	public String getServiceInstanceId() { 
		return serviceInstanceId;
	}

	public UpdateServiceInstanceRequest withInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId; 
		return this;
	}
}
