package org.openpaas.servicebroker.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * *바인드 요청이 성공적일때, 클라우드 컨트롤러로 전달되는 응답
 * 
 * 2015.07.17
 * @author 송창학 수석
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceInstanceBindingResponse {

	ServiceInstanceBinding binding;
	
	public ServiceInstanceBindingResponse() {}
	
	public ServiceInstanceBindingResponse(ServiceInstanceBinding binding) {
		this.binding = binding;
	}

	@NotEmpty
	@JsonSerialize
	@JsonProperty("credentials")
	public Map<String, Object> getCredentials() {
		return binding.getCredentials();
	}

	@JsonSerialize
	@JsonProperty("syslog_drain_url")
	public String getSyslogDrainUrl() {
		return binding.getSyslogDrainUrl();
	}
	
}
