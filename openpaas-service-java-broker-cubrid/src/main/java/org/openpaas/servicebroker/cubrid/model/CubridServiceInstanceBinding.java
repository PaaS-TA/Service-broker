package org.openpaas.servicebroker.cubrid.model;

import java.util.HashMap;
import java.util.Map;

import org.openpaas.servicebroker.model.ServiceInstanceBinding;

/**
 * A binding to a service instance
 * 
 * @author 
 *
 */
public class CubridServiceInstanceBinding extends ServiceInstanceBinding{

	private String id;
	private String serviceInstanceId;
	private String databaseUserName;	
	private Map<String,Object> credentials = new HashMap<String,Object>();
	private String syslogDrainUrl;
	private String appGuid;

	public CubridServiceInstanceBinding(String id, 
			String serviceInstanceId, 
			Map<String,Object> credentials,
			String syslogDrainUrl, String appGuid) {
		super(id, serviceInstanceId, credentials, syslogDrainUrl, appGuid);
		
	}

	public CubridServiceInstanceBinding() {
		super("", "", new HashMap<String, Object>() , "", "");
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}
	
	public String getDatabaseUserName() {
		return databaseUserName;
	}

	public void setDatabaseUserName(String databaseUserName) {
		this.databaseUserName = databaseUserName;
	}

	public Map<String, Object> getCredentials() {
		return credentials;
	}

	private void setCredentials(Map<String, Object> credentials) {
		if (credentials == null) {
			credentials = new HashMap<String,Object>();
		} else {
			this.credentials = credentials;
		}
	}

	public String getSyslogDrainUrl() {
		return syslogDrainUrl;
	}
	
	public String getAppGuid() {
		return appGuid;
	}
	
}
