package org.openpaas.servicebroker.cubrid.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.cubrid.exception.CubridServiceException;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.util.JSchUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Utility class for manipulating a Cubrid database.
 * 
 * @author 
 *
 */
@Service
public class CubridAdminService {

	
	private Logger logger = LoggerFactory.getLogger(CubridAdminService.class);
	
	@Autowired
	private JSchUtil jsch;
	
	@Autowired
	public CubridAdminService(JSchUtil jsch) {
		this.jsch = jsch;
	}
		
	public boolean isExistsService(ServiceInstance instance){
		
		for (String dbName : getDBList()) {
			if (instance.getServiceInstanceId().equals(dbName)) {
				return false;
			}
		}
		
		return true;
	}
	
	//always true
	public boolean isExistsUser(String userId){
		return true;
	}
	
	public ServiceInstance findById(String id){
		return null;
	}
	
	public ServiceInstanceBinding findBindById(String id){
		return null;
	}
	
	public void delete(String id) throws CubridServiceException{
		try{
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void deleteBind(String id) throws CubridServiceException{
		try{
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void save(ServiceInstance serviceInstance) throws CubridServiceException{
		try{
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void saveBind(ServiceInstanceBinding serviceInstanceBinding) throws CubridServiceException{
		try{
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void deleteDatabase(ServiceInstance serviceInstance) throws CubridServiceException{
		try{
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void createDatabase(ServiceInstance serviceInstance) throws CubridServiceException{
		try{
			List<String> commands = new ArrayList<>();
			
			jsch.shell(commands);
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void createUser(String database, String userId, String password) throws CubridServiceException{
		try{
			String q = "create user " + userId + " password '" + password + "'";
			String command = "csql -c \""+q+"\" " + database + " -u dba";
			
			jsch.shell(command);
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void deleteUser(String database, String userId) throws CubridServiceException{
		try{
			String q = "DROP USER " + userId;
			String command = "csql -c \""+q+"\" " + database + " -u dba";
			
			jsch.shell(command);
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public String getConnectionString(String database, String username, String password) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("jdbc:cubrid:");
		builder.append(getServerAddresses());
		builder.append(database);
		builder.append(":");
		builder.append(username);
		builder.append(":");
		builder.append(password);
		builder.append(":");
		
		return builder.toString();
	}
	
	public String getServerAddresses() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(jsch.getHostname());
		builder.append(":");
		builder.append("30000");
		builder.append(":");
		return builder.toString();
	}
	
	private CubridServiceException handleException(Exception e) {
		logger.warn(e.getLocalizedMessage(), e);
		return new CubridServiceException(e.getLocalizedMessage());
	}
	
	// get DB List from ResultMap
	// get DBname List  from DB List
	public List<String> getDBList() {
		String command = "cm_admin listdb";
		Map<String, List<String>> resultMap = jsch.shell(command);
		
		//value exam..
		//listDB = [  1.  DBName1,   2.  DBname2]
		//I want ... [DBName1, DBname2]
		List<String> listDB = resultMap.get(command);

		List<String> listDBName = new ArrayList<>();
		for (int i=0; i < listDB.size(); i++) {
			 listDBName.add( listDB.get(i).split("  ")[2]);
		}
		
		return listDBName;
	}
}
