package org.openpaas.servicebroker.common;

public class BindingBody {

	private String service_id;
	private String plan_id;
	private String app_guid;
	
	public BindingBody(String service_id, String plan_id, String app_guid) {
		this.service_id = service_id;
		this.plan_id = plan_id;
		this.app_guid  =app_guid;
	}
	
	public String convertToJsonString() {
		String result = "";
		
		result = "{\"plan_id\":\""+plan_id+"\", "+
					"\"service_id\":\""+service_id+"\", " +
					"\"app_guid\":\""+app_guid+"\"}";
		
		return result;
	}

	public String getService_id() {
		return service_id;
	}

	public void setService_id(String service_id) {
		this.service_id = service_id;
	}

	public String getPlan_id() {
		return plan_id;
	}

	public void setPlan_id(String plan_id) {
		this.plan_id = plan_id;
	}

	public String getApp_guid() {
		return app_guid;
	}

	public void setApp_guid(String app_guid) {
		this.app_guid = app_guid;
	}
}
