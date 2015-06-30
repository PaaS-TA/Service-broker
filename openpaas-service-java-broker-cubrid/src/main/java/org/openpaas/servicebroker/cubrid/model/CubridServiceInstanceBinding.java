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
	
	private String databaseUserName;	

	public CubridServiceInstanceBinding(String id, 
			String serviceInstanceId, 
			Map<String,Object> credentials,
			String syslogDrainUrl, String appGuid) {
		super(id, serviceInstanceId, credentials, syslogDrainUrl, appGuid);

	}

	public CubridServiceInstanceBinding() {
		super("", "", new HashMap<String, Object>() , "", "");
	}
	
	public String getDatabaseUserName() {
		return databaseUserName;
	}

	public void setDatabaseUserName(String databaseUserName) {
		this.databaseUserName = databaseUserName;
	}

}
