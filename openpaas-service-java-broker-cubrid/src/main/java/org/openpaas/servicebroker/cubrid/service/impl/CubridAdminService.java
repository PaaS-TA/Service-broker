package org.openpaas.servicebroker.cubrid.service.impl;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openpaas.servicebroker.cubrid.exception.CubridServiceException;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.util.JSchUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	public CubridAdminService(JSchUtil jsch, JdbcTemplate jdbcTemplate) {
		this.jsch = jsch;
		this.jdbcTemplate = jdbcTemplate;
	}
		
	public boolean isExistsService(ServiceInstance instance){
		
		List<Map<String,Object>> databases = jdbcTemplate.queryForList("select * from service_instances where db_name = '"+instance.getDatabaseName()+"'");
		
		return databases.size() > 0 ? true : false;
	}
	
	public boolean isExistsUser(String userId){
		
		List<Map<String,Object>> users = jdbcTemplate.queryForList("select * from service_instance_bindings where db_user_name = '"+userId+"'");
		
		return users.size() > 0 ? true : false;
	}
	
	public ServiceInstance findById(String id){
		ServiceInstance serviceInstance = new ServiceInstance();
		
		List<Map<String,Object>> findByIdList = jdbcTemplate.queryForList("select * from service_instances where guid = '"+id+"'");
		
		if ( findByIdList.size() > 0) {
			
			Map<String,Object> findById = findByIdList.get(0); 
			
			String serviceInstanceId = (String)findById.get("guid");
			String planId = (String)findById.get("plan_id");
			String databaseName = (String)findById.get("db_name");
			
			if ( !"".equals(serviceInstanceId) && serviceInstanceId !=null) serviceInstance.setServiceInstanceId(serviceInstanceId);
			if ( !"".equals(planId) && planId !=null) serviceInstance.setPlanId(planId);
			if ( !"".equals(databaseName) && databaseName !=null) serviceInstance.setDatabaseName(databaseName);
		} else {
			serviceInstance.setServiceInstanceId(id);
		}
		
		return serviceInstance;
	}
	
	public ServiceInstanceBinding findBindById(String id){
		ServiceInstanceBinding serviceInstanceBinding = new ServiceInstanceBinding();
		
		List<Map<String,Object>> findByIdList = jdbcTemplate.queryForList("select * from service_instance_bindings where guid = '"+id+"'");
		
		if ( findByIdList.size() > 0) {
			
			Map<String,Object> findById = findByIdList.get(0);
			
			String serviceInstanceBindingId = (String)findById.get("guid");
			String serviceInstanceId = (String)findById.get("service_instance_id");
			String databaseUserName = (String)findById.get("db_user_name");
			
			if ( !"".equals(serviceInstanceBindingId) && serviceInstanceBindingId !=null) serviceInstanceBinding.setId(serviceInstanceBindingId);
			if ( !"".equals(serviceInstanceId) && serviceInstanceId !=null) serviceInstanceBinding.setServiceInstanceId(serviceInstanceId);
			if ( !"".equals(databaseUserName) && databaseUserName !=null) serviceInstanceBinding.setDatabaseUserName(databaseUserName);
			
		} else {
			serviceInstanceBinding.setServiceInstanceId(id);
		}
		
		return serviceInstanceBinding;
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
			// database name
			String databaseName = serviceInstance.getDatabaseName();
			String filePath = "$CUBRID_DATABASES/" + databaseName;
			List<String> commands = new ArrayList<>();
			
			//1. create shell command(s)
			//1-1. stop database server
			commands.add("cubrid server stop " + databaseName);
			//1-2. delete database
			commands.add("cubrid deletedb " + databaseName);
			//1-3. remove database directory
			commands.add("rm -rf " + filePath);
			
			//1. execute command(s)
			jsch.shell(commands);
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void createDatabase(ServiceInstance serviceInstance) throws CubridServiceException{
		try{
			// database name
			String databaseName = serviceInstance.getDatabaseName();
			String filePath = "$CUBRID_DATABASES/" + databaseName;
			List<String> commands = new ArrayList<>();
			
			//1. create shell command(s)
			//1-1. create directory for database
			commands.add("mkdir -p " + filePath);
			//1-2. create database
			commands.add("cubrid createdb "
					+ "--db-volume-size=100M "
					+ "--log-volume-size=100M "
					+ "--file-path="+filePath+" "
					+ databaseName+" en_US");
			//1-3. start database server
			commands.add("cubrid server start " + databaseName);
			
			//2. execute command(s)
			jsch.shell(commands);
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void createUser(String database, String userId, String password) throws CubridServiceException{
		try{
			//1. create query 'create user ... '
			String q = "CREATE USER " + userId + " PASSWORD '" + password + "'" + "GROUPS DBA";
			//2. create shell command
			String command = "csql -c \""+q+"\" " + database + " -u dba";
			
			//3. execute command(s)
			jsch.shell(command);
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void deleteUser(String database, String userId) throws CubridServiceException{
		try{
			//1. create query 'drop user .... '
			String q = "DROP USER " + userId;
			//2. create shell command
			String command = "csql -c \""+q+"\" " + database + " -u dba";
			
			//3. execute command(s)
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
	/*
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
	}*/
	
}
