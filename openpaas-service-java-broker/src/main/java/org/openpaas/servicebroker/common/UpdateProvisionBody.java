package org.openpaas.servicebroker.common;

public class UpdateProvisionBody {

	private String plan_id;
	
	public UpdateProvisionBody(String plan_id) {
		this.plan_id = plan_id;
	}
	
	public String convertToJsonString() {
		return "{\"plan_id\":\""+this.plan_id+"\"}";
	}

	public String getPlan_id() {
		return plan_id;
	}

	public void setPlan_id(String plan_id) {
		this.plan_id = plan_id;
	}
}
