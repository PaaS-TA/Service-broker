package org.openpaas.servicebroker.cubrid.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.cubrid.exception.CubridServiceException;
import org.openpaas.servicebroker.cubrid.model.CubridServiceInstance;
import org.openpaas.servicebroker.cubrid.model.CubridServiceInstanceBinding;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.util.JSchUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

/**
 * Cubrid Database의 Util 클래스이다.
 * 
 * @author 
 *
 */
@Service
public class CubridAdminService {


	private Logger logger = LoggerFactory.getLogger(CubridAdminService.class);

	
	/* ssh접속을 위한 객체 */
	@Autowired
	private JSchUtil jsch;

	/* cubrid database 접속을 위한 객체*/
	@Autowired 
	private JdbcTemplate jdbcTemplate;

	private Map<String, Object> plans = null;

	@Autowired
	public CubridAdminService(JSchUtil jsch, JdbcTemplate jdbcTemplate) {
		this.jsch = jsch;
		this.jdbcTemplate = jdbcTemplate;

		this.plans = new HashMap<String, Object>();
		Map<String, String> plan = new HashMap<String, String>();

		//Plan A
		plan.put("vol", "100M");
		plan.put("charset", "ko_KR.utf8");
		this.plans.put("utf8", plan);

		plan = new HashMap<String, String>();
		//Plan C
		plan.put("vol", "100M");
		plan.put("charset", "ko_KR.euckr");
		this.plans.put("euckr", plan);

	}

	/**
	 * 같은이름의 Database가 존재하는지 여부를 반환.
	 * 존재하면 true
	 * 존재하지않으면 false
	 * 
	 * @param instance
	 * @return boolean
	 * @throws CubridServiceException
	 */
	public boolean isExistsService(CubridServiceInstance instance) throws CubridServiceException {
		try {
			List<Map<String,Object>> databases = jdbcTemplate.queryForList("SELECT * FROM service_instances WHERE db_name = '"+instance.getDatabaseName()+"'");
			
			return databases.size() > 0 ? true : false;
			
		} catch (InvalidResultSetAccessException e) {
			throw handleException(e);
		} catch (DataAccessException e) {
			throw handleException(e);
		}
	}

	/**
	 * 같은이름의 user가 존재하는지 여부를 반환.
	 * 존재하면 true
	 * 존재하지않으면 false
	 * 
	 * @param databaseName
	 * @param username
	 * @return
	 * @throws CubridServiceException
	 */
	public boolean isExistsUser(String databaseName, String username) throws CubridServiceException {
		try {
			List<Map<String,Object>> users = jdbcTemplate.queryForList("SELECT * "
					+ "FROM service_instances A, service_instance_bindings B "
					+ "WHERE A.guid = B.service_instance_id AND B.db_user_name = '"+username+"' AND A.db_name = '"+databaseName+"'");

			return users.size() > 0 ? true : false;
			
		} catch (InvalidResultSetAccessException e) {
			throw handleException(e);
		} catch (DataAccessException e) {
			throw handleException(e);
		}
	}

	/**
	 *  ServiceInstance의 고유식별자를 이용하여  ServiceInstance의 정보를 조회하여 반환.
	 * 존재하지않을경우 null반환
	 * 
	 * @param id
	 * @return CubridServiceInstance
	 * @throws CubridServiceException
	 */
	public CubridServiceInstance findById(String id) throws CubridServiceException {
		try {
			CubridServiceInstance serviceInstance = null;

			List<Map<String,Object>> findByIdList = jdbcTemplate.queryForList("SELECT * FROM service_instances WHERE guid = '"+id+"'");

			if ( findByIdList.size() > 0) {

				serviceInstance = new CubridServiceInstance();
				Map<String,Object> findById = findByIdList.get(0); 

				String serviceInstanceId = (String)findById.get("guid");
				String planId = (String)findById.get("plan_id");
				String databaseName = (String)findById.get("db_name");

				if ( !"".equals(serviceInstanceId) && serviceInstanceId !=null) serviceInstance.setServiceInstanceId(serviceInstanceId);
				if ( !"".equals(planId) && planId !=null) serviceInstance.setPlanId(planId);
				if ( !"".equals(databaseName) && databaseName !=null) serviceInstance.setDatabaseName(databaseName);
			}

			return serviceInstance;
			
		} catch (InvalidResultSetAccessException e) {
			throw handleException(e);
		} catch (DataAccessException e) {
			throw handleException(e);
		}
	}

	/**
	 * ServiceInstanceBinding의 고유식별자를 이용하여 ServiceInstanceBinding 정보를 조회
	 * 존재하지않을경우 null 반환
	 * 
	 * @param id
	 * @return CubridServiceInstanceBinding
	 * @throws CubridServiceException
	 */
	public CubridServiceInstanceBinding findBindById(String id) throws CubridServiceException {
		try {
			CubridServiceInstanceBinding serviceInstanceBinding = null;

			List<Map<String,Object>> findByIdList = jdbcTemplate.queryForList("SELECT * FROM service_instance_bindings WHERE guid = '"+id+"'");

			if ( findByIdList.size() > 0) {
				serviceInstanceBinding = new CubridServiceInstanceBinding();
				Map<String,Object> findById = findByIdList.get(0);

				String serviceInstanceBindingId = (String)findById.get("guid");
				String serviceInstanceId = (String)findById.get("service_instance_id");
				String databaseUserName = (String)findById.get("db_user_name");
				
				if ( !"".equals(serviceInstanceBindingId) && serviceInstanceBindingId !=null) serviceInstanceBinding.setId(serviceInstanceBindingId);
				if ( !"".equals(serviceInstanceId) && serviceInstanceId !=null) serviceInstanceBinding.setServiceInstanceId(serviceInstanceId);
				if ( !"".equals(databaseUserName) && databaseUserName !=null) serviceInstanceBinding.setDatabaseUserName(databaseUserName);

			}

			return serviceInstanceBinding;
			
		} catch (InvalidResultSetAccessException e) {
			throw handleException(e);
		} catch (DataAccessException e) {
			throw handleException(e);
		}
	}

	/**
	 * ServiceInstance의 고유식별자를 이용하여 ServcieInstance 정보 및 ServiceInstanceBinding 정보를 삭제
	 * 
	 * @param id
	 * @throws CubridServiceException
	 */
	
	public void delete(String id) throws CubridServiceException{
		try {
			
			jdbcTemplate.update("DELETE FROM service_instances WHERE guid = ?", id);
			jdbcTemplate.update("DELETE FROM service_instance_bindings WHERE service_instance_id = ?", id);
			
		} catch (InvalidResultSetAccessException e) {
			throw handleException(e);
		} catch (DataAccessException e) {
			throw handleException(e);
		}
	}

	/**
	 * ServcieInstanceBinding의 고유식별자를 이용하여 ServiceInstanceBinding 정보를 삭제
	 * 
	 * @param id
	 * @throws CubridServiceException
	 */
	public void deleteBind(String id) throws CubridServiceException{
		try {
			
			jdbcTemplate.update("DELETE FROM service_instance_bindings WHERE guid = ?", id);
			
		} catch (InvalidResultSetAccessException e) {
			throw handleException(e);
		} catch (DataAccessException e) {
			throw handleException(e);
		}
	}

	/**
	 * ServiceInstance 정보를 저장.
	 * 
	 * @param serviceInstance
	 * @throws CubridServiceException
	 */
	public void save(CubridServiceInstance serviceInstance) throws CubridServiceException {
		try {
			
			jdbcTemplate.update("INSERT INTO service_instances (guid, plan_id, db_name) values (?, ?, ?)", 
					serviceInstance.getServiceInstanceId(), serviceInstance.getPlanId(), serviceInstance.getDatabaseName());
			
		} catch (InvalidResultSetAccessException e) {
			throw handleException(e);
		} catch (DataAccessException e) {
			throw handleException(e);
		}
	}

	/**
	 * ServiceInstanceBinding 정보를 저장.
	 * 
	 * @param serviceInstanceBinding
	 * @throws CubridServiceException
	 */
	public void saveBind(CubridServiceInstanceBinding serviceInstanceBinding) throws CubridServiceException{
		try {		
			
			jdbcTemplate.update("INSERT INTO service_instance_bindings (guid, service_instance_id, db_user_name) values (?, ?, ?)",
					serviceInstanceBinding.getId(), serviceInstanceBinding.getServiceInstanceId(), serviceInstanceBinding.getDatabaseUserName());
			
		} catch (InvalidResultSetAccessException e) {
			throw handleException(e);
		} catch (DataAccessException e) {
			throw handleException(e);
		}
	}

	/**
	 * ServiceInstance 정보를 변경.
	 * 
	 * @param instance
	 * @throws CubridServiceException
	 */
	public void update(ServiceInstance instance) throws CubridServiceException{
		try {
			
			jdbcTemplate.update("UPDATE service_instances SET plan_id = ? WHERE guid = ?", 
					instance.getPlanId(), instance.getServiceInstanceId());
			
		} catch (InvalidResultSetAccessException e) {
			throw handleException(e);
		} catch (DataAccessException e) {
			throw handleException(e);
		}
	}

	/**
	 * ServiceInstance에 볼륨을 추가 
	 * 
	 * !사용되지않음!
	 * 
	 * @param databaseName
	 * @throws CubridServiceException
	 */
	public void addVolume(String databaseName) throws CubridServiceException{
		
		String command = "cubrid addvoldb -p data --db-volume-size=100MB " + databaseName;
		jsch.shell(command);

	}

	/**
	 * ServiceInstance 정보를 이용하여 database 정지 및 삭제.
	 * ssh 이용
	 * 
	 * @param serviceInstance
	 * @throws CubridServiceException
	 */
	public void deleteDatabase(CubridServiceInstance serviceInstance) throws CubridServiceException{
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
	}

	/**
	 * ServiceInstance 정보를 이용하여 database 생성 및 구동.
	 * ssh 이용
	 * 
	 * @param serviceInstance
	 * @return
	 * @throws CubridServiceException
	 */
	public boolean createDatabase(CubridServiceInstance serviceInstance) throws CubridServiceException{
		// database name
		String databaseName = serviceInstance.getDatabaseName();
		String filePath = "$CUBRID_DATABASES/" + databaseName;
		List<String> commands = new ArrayList<>();
		String planId = serviceInstance.getPlanId();

		@SuppressWarnings("unchecked")
		Map<String, String> plan = (Map<String, String>) plans.get(planId);

		if (plan == null) {
			throw new CubridServiceException("no plan");
		}

		//1. create shell command(s)
		//1-1. create directory for database
		commands.add("mkdir -p " + filePath);
		//1-2. create database
		commands.add("cubrid createdb "
				+ "--db-volume-size="+plan.get("vol")+" "
				+ "--log-volume-size=100M "
				+ "--file-path="+filePath+" "
				+ databaseName+" "+plan.get("charset"));
		//1-3. start database server
		commands.add("cubrid server start " + databaseName);

		//2. execute command(s)
		//return ==> map key: command, val: result
		Map<String, List<String>> rs = jsch.shell(commands);
		
		return "0".equals(rs.get("exitStatus").get(0)) ? true : false;
	}

	
	/**
	 * 특정 database에 사용자를 추가.
	 * ssh 이용.
	 * 
	 * @param database
	 * @param userId
	 * @param password
	 * @throws CubridServiceException
	 */
	public void createUser(String database, String userId, String password) throws CubridServiceException{
		//1. create query 'create user ... '
		String q = "CREATE USER " + userId + " PASSWORD '" + password + "'" + "GROUPS DBA";
		//2. create shell command
		String command = "csql -c \""+q+"\" " + database + " -u dba";

		//3. execute command(s)
		jsch.shell(command);
	}

	/**
	 * 특정 database에서 사용자를 삭제
	 * 
	 * 
	 * @param database
	 * @param username
	 * @throws CubridServiceException
	 */
	public void deleteUser(String database, String username) throws CubridServiceException{
		//1. create query 'drop user .... '
		String q = "DROP USER " + username;
		//2. create shell command
		String command = "csql -c \""+q+"\" " + database + " -u dba";

		//3. execute command(s)
		jsch.shell(command);

	}

	
	/**
	 * connection uri 생성
	 * 
	 * @param database
	 * @param username
	 * @param password
	 * @return
	 */
	public String getConnectionString(String database, String username, String password) {
		StringBuilder builder = new StringBuilder();

		builder.append("cubrid:");
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

		DriverManagerDataSource ds = (DriverManagerDataSource) jdbcTemplate.getDataSource();
		String[] url = ds.getUrl().split(":");

		builder.append(url[2]);
		builder.append(":");
		builder.append(url[3]);
		builder.append(":");
		return builder.toString();
	}

	private CubridServiceException handleException(Exception e) {
		logger.warn(e.getLocalizedMessage(), e);
		return new CubridServiceException(e.getLocalizedMessage());
	}
	
	/*
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
