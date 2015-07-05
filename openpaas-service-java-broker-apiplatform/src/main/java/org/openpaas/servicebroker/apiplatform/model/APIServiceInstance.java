package org.openpaas.servicebroker.apiplatform.model;

import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;

public class APIServiceInstance {

	private String instance_id;
	
	private String organization_id;
	
	private String plan_id;
	
	private String service_id;
	
	private String space_guid;

	private String user_id;
	
	private String user_password;
	
	public String getInstance_id() {
		return instance_id;
	}

	public void setInstance_id(String instance_id) {
		this.instance_id = instance_id;
	}

	public String getOrganization_id() {
		return organization_id;
	}

	public void setOrganization_id(String organization_id) {
		this.organization_id = organization_id;
	}

	public String getPlan_id() {
		return plan_id;
	}

	public void setPlan_id(String plan_id) {
		this.plan_id = plan_id;
	}

	public String getService_id() {
		return service_id;
	}

	public void setService_id(String service_id) {
		this.service_id = service_id;
	}

	public String getSpace_guid() {
		return space_guid;
	}

	public void setSpace_guid(String space_guid) {
		this.space_guid = space_guid;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getUser_password() {
		return user_password;
	}

	public void setUser_password(String user_password) {
		this.user_password = user_password;
	}

}
