package org.openpaas.servicebroker.glusterfs.model;

import org.openpaas.servicebroker.model.ServiceInstance;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * An instance of a GlusterfsServiceInstance.
 * 
 * @author kkanzo
 *
 */
public class GlusterfsServiceInstance {

	private String serviceInstanceId;
	
	private String tenantName;
	
	private String tenantId;

	public GlusterfsServiceInstance(ServiceInstance serviceInstance){
		this.serviceInstanceId = serviceInstance.getServiceInstanceId();
	}
	
	public GlusterfsServiceInstance(){}
	
	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	
	
}
