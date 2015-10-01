package org.openpaas.servicebroker.model;

import org.hibernate.validator.constraints.NotEmpty;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 서비스 인스턴스에 바인드를 생성할 때 필요한 Request 정보를 가지고 있는 데이터 모델 bean 클래스. 
 * Json 어노테이션을 사용해서 JSON 형태로 제공
 * 
 * @author 송창학
 * @date 2015.0629
 */

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public class CreateServiceInstanceBindingRequest {

	@NotEmpty
	@JsonSerialize
	@JsonProperty("service_id")
	private String serviceDefinitionId;
	
	@NotEmpty
	@JsonSerialize
	@JsonProperty("plan_id")
	private String planId;

	@NotEmpty
	@JsonSerialize
	@JsonProperty("app_guid")
	private String appGuid;

	
	@JsonSerialize
	@JsonProperty("parameters")
	private Map<String, Object> parameters;
	
	@JsonIgnore
	private String serviceInstanceId;

	@JsonIgnore
	private String bindingId;
	
	public CreateServiceInstanceBindingRequest() {}
	
	public CreateServiceInstanceBindingRequest(String serviceDefinitionId, String planId, String appGuid) {
		this.serviceDefinitionId = serviceDefinitionId;
		this.planId = planId;
		this.appGuid = appGuid;
	}
	
	
	public CreateServiceInstanceBindingRequest(String serviceDefinitionId, String planId, String appGuid, Map<String, Object> parameters) {
		this(serviceDefinitionId, planId, appGuid);
		this.parameters = parameters;
	}
	
	public String getServiceDefinitionId() {
		return serviceDefinitionId;
	}
	
	public void setServiceDefinitionId(String serviceDefinitionId) {
		this.serviceDefinitionId = serviceDefinitionId;
	}

	public String getPlanId() {
		return planId;
	}
	
	public void setPlanId(String planId) {
		this.planId = planId;
	}

	public String getAppGuid() {
		return appGuid;
	}

	public void setAppGuid(String appGuid) {
		this.appGuid = appGuid;
	}
	
	public String getBindingId() { 
		return bindingId;
	}
	
	
	public Map<String, Object> getParameters() {
		return parameters;
	}
	public <T> T getParameters(Class<T> cls) {
		try {
			T bean = cls.newInstance();
			BeanUtils.populate(bean, parameters);
			return bean;
			
		} catch (Exception e) {
			throw new IllegalArgumentException("Error mapping parameters to class of type " + cls.getName());
		}
	}
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	
	
	public String getServiceInstanceId() { 
		return serviceInstanceId;
	}

	public CreateServiceInstanceBindingRequest withServiceInstanceId(final String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
		return this;
	}

	public CreateServiceInstanceBindingRequest withBindingId(final String bindingId) {
		this.bindingId = bindingId;
		return this;
	}
	
	public CreateServiceInstanceBindingRequest and() {
		return this;
	}
}
