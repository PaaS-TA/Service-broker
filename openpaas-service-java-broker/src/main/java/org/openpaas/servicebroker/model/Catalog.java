package org.openpaas.servicebroker.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * *브로커가 제공하는 서비스들의 목록
 * 
 * 2015.07.17
 * @author 송창학 수석
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Catalog {

	@NotEmpty
	@JsonSerialize
	@JsonProperty("services")
	private List<ServiceDefinition> serviceDefinitions = new ArrayList<ServiceDefinition>();

	public Catalog() {
	}
	
	
	public Catalog(List<ServiceDefinition> serviceDefinitions) {
		this.setServiceDefinitions(serviceDefinitions); 
	}
	
	public List<ServiceDefinition> getServiceDefinitions() {
		return serviceDefinitions;
	}

	private void setServiceDefinitions(List<ServiceDefinition> serviceDefinitions) {
		if ( serviceDefinitions == null ) {
			// ensure serialization as an empty array, not null
			this.serviceDefinitions = new ArrayList<ServiceDefinition>();
		} else {
			this.serviceDefinitions = serviceDefinitions;
		} 
	}
	
}
