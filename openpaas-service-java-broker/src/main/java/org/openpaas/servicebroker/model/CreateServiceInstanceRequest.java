package org.openpaas.servicebroker.model;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 서비스 인스턴스를 생성할 때 필요한 Request 정보를 가지고 있는 데이터 모델 bean 클래스. 
 * Json 어노테이션을 사용해서 JSON 형태로 제공
 * 
 * @author 송창학
 * @date 2015.0629
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public class CreateServiceInstanceRequest {

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
	@JsonProperty("organization_guid")
	private String organizationGuid;
	
	@NotEmpty
	@JsonSerialize
	@JsonProperty("space_guid")
	private String spaceGuid;

	//Cloud Controller dosen't send the definition, it's populated later
	@JsonIgnore
	private ServiceDefinition serviceDefinition;

	//Cloud Controller dosen't send instanceId in the body
	@JsonIgnore
	private String serviceInstanceId;
	
	public CreateServiceInstanceRequest() {}
	

	public CreateServiceInstanceRequest(String serviceDefinitionId, String planId, String organizationGuid, String spaceGuid) {
		this.serviceDefinitionId = serviceDefinitionId;
		this.planId = planId;
		this.organizationGuid = organizationGuid;
		this.spaceGuid = spaceGuid;
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

	public String getOrganizationGuid() {
		return organizationGuid;
	}

	public ServiceDefinition getServiceDefinition() {
		return serviceDefinition;
	}

	public void setOrganizationGuid(String organizationGuid) {
		this.organizationGuid = organizationGuid;
	}

	public String getSpaceGuid() {
		return spaceGuid;
	}

	public void setSpaceGuid(String spaceGuid) {
		this.spaceGuid = spaceGuid;
	}
	
	public String getServiceInstanceId() { 
		return serviceInstanceId;
	}
	
	public CreateServiceInstanceRequest withServiceDefinition(ServiceDefinition svc) {
		this.serviceDefinition = svc;
		return this;
	}

	public CreateServiceInstanceRequest withServiceInstanceId(
			final String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
		return this;
	}
	public CreateServiceInstanceRequest and() {
		return this;
	}
	
}
