package org.openpaas.servicebroker.altibase.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.altibase.exception.AltibaseServiceException;
import org.openpaas.servicebroker.altibase.model.AltibaseServiceInstance;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.util.JSchUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * Altibase Database의 Util 클래스이다.
 * 
 * @author 
 *
 */
@Service
public class AltibaseAdminService {


	private Logger logger = LoggerFactory.getLogger(AltibaseAdminService.class);

	
	/* ssh접속을 위한 객체 */
	@Autowired
	private JSchUtil jsch;

	/* altibase database 접속을 위한 객체*/
	@Autowired 
	private JdbcTemplate jdbcTemplate;

	//private Map<String, Object> plans = null;
	
	private final RowMapper<AltibaseServiceInstance> mapper = new ServiceInstanceRowMapper();
	
	private final RowMapper<ServiceInstanceBinding> mapper2 = new ServiceInstanceBindingRowMapper();
	
	public static final String SERVICE_INSTANCES_FILDS = "instance_id, service_id, plan_id, organization_guid, space_guid";
	public static final String SERVICE_INSTANCES_FIND_BY_INSTANCE_ID = "select " + SERVICE_INSTANCES_FILDS + " from broker.service_instances where instance_id = ?";
	public static final String SERVICE_INSTANCES_ADD = "INSERT INTO broker.service_instances (" + SERVICE_INSTANCES_FILDS + ") values (?, ?, ?, ?, ?)";
	public static final String SERVICE_INSTANCES_UPDATE_FILDS = "UPDATE broker.service_instances SET plan_id = ? WHERE instance_id = ?";
	public static final String SERVICE_INSTANCES_DELETE_BY_INSTANCE_ID = "delete from broker.service_instances where instance_id = ?";
	
	public static final String SERVICE_BINDING_FILDS ="binding_id, instance_id, app_id, username, password";
	public static final String SERVICE_BINDING_ADD = "INSERT INTO broker.service_binding (" + SERVICE_BINDING_FILDS + ") values (?, ?, ?, ?, ?)";
	public static final String SERVICE_BINDING_FIND_BY_BINDING_ID = "select " + SERVICE_BINDING_FILDS + " from broker.service_binding where binding_id = ?";
	public static final String SERVICE_BINDING_FIND_USERNAME_BY_BINDING_ID = "select username from broker.service_binding where binding_id = ?";
	public static final String SERVICE_BINDING_DELETE_BY_BINDING_ID = "delete from broker.service_binding where binding_id = ?";
	public static final String SERVICE_BINDING_FIND_USERNAME_BY_INSTANCE_ID = "select username from broker.service_binding where instance_id = '%s'";
	
	@Autowired
	public AltibaseAdminService(JSchUtil jsch, JdbcTemplate jdbcTemplate) {
		this.jsch = jsch;
		this.jdbcTemplate = jdbcTemplate;
		/*		
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
*/
	}

	/**
	 * 같은이름의 Database가 존재하는지 여부를 반환.
	 * 존재하면 true
	 * 존재하지않으면 false
	 * 
	 * @param instance
	 * @return boolean
	 * @throws AltibaseServiceException
	 */
	public boolean isExistsService(AltibaseServiceInstance instance) throws AltibaseServiceException {
		return true;
		//TODO
		/*
		try {
			List<Map<String,Object>> databases = jdbcTemplate.queryForList("SELECT * FROM service_instances WHERE db_name = '"+instance.getDatabaseName()+"'");
			
			return databases.size() > 0 ? true : false;
			
		} catch (InvalidResultSetAccessException e) {
			throw handleException(e);
		} catch (DataAccessException e) {
			throw handleException(e);
		}*/
	}

	/**
	 * 같은이름의 user가 존재하는지 여부를 반환.
	 * 존재하면 true
	 * 존재하지않으면 false
	 * 
	 * @param databaseName
	 * @param username
	 * @return
	 * @throws AltibaseServiceException
	 */
	public boolean isExistsUser(String databaseName, String username) throws AltibaseServiceException {
		try {
			List<Map<String,Object>> users = jdbcTemplate.queryForList("SELECT user_id "
					+ "FROM system_.sys_users_ "
					+ "WHERE user_name = '" + username + "'");

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
	 * @return AltibaseServiceInstance
	 * @throws AltibaseServiceException
	 */
	public AltibaseServiceInstance findById(String id) throws AltibaseServiceException {
		AltibaseServiceInstance serviceInstance = null;
		try {			
/*
			List<Map<String,Object>> findByIdList = jdbcTemplate.queryForList("SELECT * FROM service_instances WHERE guid = '"+id+"'");

			if ( findByIdList.size() > 0) {

				serviceInstance = new AltibaseServiceInstance();
				Map<String,Object> findById = findByIdList.get(0); 

				String serviceInstanceId = (String)findById.get("guid");
				String planId = (String)findById.get("plan_id");
				String databaseName = (String)findById.get("db_name");

				if ( !"".equals(serviceInstanceId) && serviceInstanceId !=null) serviceInstance.setServiceInstanceId(serviceInstanceId);
				if ( !"".equals(planId) && planId !=null) serviceInstance.setPlanId(planId);
				if ( !"".equals(databaseName) && databaseName !=null) serviceInstance.setDatabaseName(databaseName);
			}

			return serviceInstance;*/
			serviceInstance = jdbcTemplate.queryForObject(SERVICE_INSTANCES_FIND_BY_INSTANCE_ID, mapper, id);
			
		} catch (Exception e) {
		}
		return serviceInstance;
	}

	/**
	 * ServiceInstanceBinding의 고유식별자를 이용하여 ServiceInstanceBinding 정보를 조회
	 * 존재하지않을경우 null 반환
	 * 
	 * @param id
	 * @return AltibaseServiceInstanceBinding
	 * @throws AltibaseServiceException
	 */
	public ServiceInstanceBinding findBindById(String id) throws AltibaseServiceException {
		ServiceInstanceBinding serviceInstanceBinding = null;
		try {
			serviceInstanceBinding = jdbcTemplate.queryForObject(SERVICE_BINDING_FIND_BY_BINDING_ID, mapper2, id);
		} catch (Exception e) {
		}
		return serviceInstanceBinding;
	}

	/**
	 * ServiceInstance의 고유식별자를 이용하여 ServcieInstance 정보 및 ServiceInstanceBinding 정보를 삭제
	 * 
	 * @param id
	 * @throws AltibaseServiceException
	 */
	
	public void delete(String id) throws AltibaseServiceException{
		try {
			
			jdbcTemplate.update(SERVICE_INSTANCES_DELETE_BY_INSTANCE_ID, id);
			
			String sql = String.format(SERVICE_BINDING_FIND_USERNAME_BY_INSTANCE_ID, id);

			//List<User> = jdbcTemplate.query(SERVICE_BINDING_FIND_USERNAME_BY_INSTANCE_ID, mapper2, id);
			
			List<Map<String,Object>> rows = jdbcTemplate.queryForList(sql);
			for (Map<String,Object> row : rows) {
				deleteUser(row.get("USERNAME").toString());
			}			
			
			jdbcTemplate.update("DELETE FROM broker.service_binding WHERE instance_id = ?", id);
			
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
	 * @throws AltibaseServiceException
	 */
	public void deleteBind(String id) throws AltibaseServiceException{
		try {
			
			jdbcTemplate.update(SERVICE_BINDING_DELETE_BY_BINDING_ID, id);
			
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
	 * @throws AltibaseServiceException
	 */
	public void save(AltibaseServiceInstance serviceInstance) throws AltibaseServiceException {
		try {
			
			jdbcTemplate.update(SERVICE_INSTANCES_ADD, 
					serviceInstance.getServiceInstanceId(),
					serviceInstance.getServiceDefinitionId(),
					serviceInstance.getPlanId(),
					serviceInstance.getOrganizationGuid(),
					serviceInstance.getSpaceGuid());
			
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
	 * @throws AltibaseServiceException
	 */
	public void saveBind(ServiceInstanceBinding serviceInstanceBinding) throws AltibaseServiceException{
		try {		
			
			jdbcTemplate.update(SERVICE_BINDING_ADD,
					serviceInstanceBinding.getId(),
					serviceInstanceBinding.getServiceInstanceId(),
					serviceInstanceBinding.getAppGuid(),
					serviceInstanceBinding.getCredentials().get("username"),
					serviceInstanceBinding.getCredentials().get("password"));
			
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
	 * @throws AltibaseServiceException
	 */
	public void update(ServiceInstance instance) throws AltibaseServiceException{
		try {
			
			jdbcTemplate.update(SERVICE_INSTANCES_UPDATE_FILDS, 
					instance.getPlanId(), instance.getServiceInstanceId());
			
		} catch (InvalidResultSetAccessException e) {
			throw handleException(e);
		} catch (DataAccessException e) {
			throw handleException(e);
		}
	}

	/**
	 * ServiceInstance 정보를 이용하여 database 정지 및 삭제.
	 * ssh 이용
	 * 
	 * @param serviceInstance
	 * @throws AltibaseServiceException
	 */
	public void deleteDatabase(AltibaseServiceInstance serviceInstance) throws AltibaseServiceException {
		//TODO
		/*
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
		*/
	}

	/**
	 * ServiceInstance 정보를 이용하여 database 생성 및 구동.
	 * ssh 이용
	 * 
	 * @param serviceInstance
	 * @return
	 * @throws AltibaseServiceException
	 */
	public boolean createDatabase(AltibaseServiceInstance serviceInstance) throws AltibaseServiceException {
		return true;
		//TODO
		/*
		// database name
		String databaseName = serviceInstance.getDatabaseName();
		String filePath = "$CUBRID_DATABASES/" + databaseName;
		List<String> commands = new ArrayList<>();
		String planId = serviceInstance.getPlanId();

		@SuppressWarnings("unchecked")
		Map<String, String> plan = (Map<String, String>) plans.get(planId);

		if (plan == null) {
			throw new AltibaseServiceException("no plan");
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
		
		return "0".equals(rs.get("exitStatus").get(0)) ? true : false;*/
	}

	
	/**
	 * 특정 database에 사용자를 추가.
	 * ssh 이용.
	 * 
	 * @param database
	 * @param userId
	 * @param password
	 * @throws AltibaseServiceException
	 */
	public void createUser(String userId, String password) throws AltibaseServiceException{
		try {
			
			jdbcTemplate.execute("CREATE USER " + userId + " IDENTIFIED BY \"" + password + "\"");
			jdbcTemplate.execute("GRANT ALL PRIVILEGES TO " + userId);
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	/**
	 * 특정 database에서 사용자를 삭제
	 * 
	 * 
	 * @param database
	 * @param username
	 * @throws AltibaseServiceException
	 */
	public void deleteUser(String userName) throws AltibaseServiceException {
		try {
			jdbcTemplate.execute("DROP USER " + userName + " CASCADE");
		} catch (Exception e) {
			//throw handleException(e);
		}

	}
	
	/**
	 * connection uri 생성
	 * 
	 * @param database
	 * @param username
	 * @param password
	 * @return
	 */
	public String getConnectionString(String username, String password, String host, String port) {
		StringBuilder builder = new StringBuilder();

		builder.append("jdbc:Altibase://");
		builder.append(host);
		builder.append(":");
		builder.append(port);
		builder.append("/mydb?user=");
		builder.append(username);
		builder.append("&password=");
		builder.append(password);

		return builder.toString();
	}
	
	private AltibaseServiceException handleException(Exception e) {
		logger.warn(e.getLocalizedMessage(), e);
		return new AltibaseServiceException(e.getLocalizedMessage());
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

	private static final class ServiceInstanceRowMapper implements RowMapper<AltibaseServiceInstance> {
        @Override
        public AltibaseServiceInstance mapRow(ResultSet rs, int rowNum) throws SQLException {
            CreateServiceInstanceRequest request = new CreateServiceInstanceRequest();
            request.withServiceInstanceId(rs.getString(1));
            request.setServiceDefinitionId(rs.getString(2));
            request.setPlanId(rs.getString(3));
            request.setOrganizationGuid(rs.getString(4));
            request.setSpaceGuid(rs.getString(5));
            return new AltibaseServiceInstance(request);
        }
    }
	
	private static final class ServiceInstanceBindingRowMapper implements RowMapper<ServiceInstanceBinding> {
        @Override
        public ServiceInstanceBinding mapRow(ResultSet rs, int rowNum) throws SQLException {
        	Map<String,Object> credentials = new HashMap<String,Object>();
    		credentials.put("username", rs.getString(4));
    		credentials.put("password", rs.getString(5));    		
            return new ServiceInstanceBinding(rs.getString(1), 
            		rs.getString(2), 
            		credentials, 
            		"",
            		rs.getString(3));
        }
    }
}
