package org.openpaas.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * The response from the broker sent back to the cloud controller 
 * on a successful service instance creation request
 * * 서비스 인스턴스 생성 요청이 성공적일때, 클라우드 컨트롤러로 전달 될 응답 (Provision)
 * 
 * 2015.07.17
 * 
 * @author sgreenberg@gopivotal.com
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateServiceInstanceResponse {

	private ServiceInstance instance;
	
	public CreateServiceInstanceResponse() {}
	
	public CreateServiceInstanceResponse(ServiceInstance instance) {
		this.instance = instance;
	}

	@JsonSerialize
	@JsonProperty("dashboard_url")
	public String getDashboardUrl() {
		return instance.getDashboardUrl();
	}
	
}
