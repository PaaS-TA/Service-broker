package org.openpaas.servicebroker.altibase.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.altibase.exception.AltibaseServiceException;
import org.openpaas.servicebroker.altibase.model.AltibaseServiceInstance;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * Altibase Database의 Util 클래스이다.
 * 
 * @author Bae Younghee
 *
 */
@Service
public class AltibaseAdminService {


	private Logger logger = LoggerFactory.getLogger(AltibaseAdminService.class);

	/* altibase database 접속을 위한 객체*/
	@Autowired 
	private JdbcTemplate jdbcTemplate;

	//private Map<String, Object> plans = null;
	
	private final RowMapper<AltibaseServiceInstance> mapper = new ServiceInstanceRowMapper();
	
	private final RowMapper<ServiceInstanceBinding> mapper2 = new ServiceInstanceBindingRowMapper();
	
	public static final String SERVICE_INSTANCES_SELECT_FILDS = "instance_id, plan_id, organization_guid, space_guid, a.node_id, host, port";
	public static final String SERVICE_INSTANCES_FIND_BY_INSTANCE_ID = 
			"select " + SERVICE_INSTANCES_SELECT_FILDS + 
			" from broker.service_instances a, broker.dedicated_nodes b" +
			" where instance_id = ? and a.node_id = b.node_id";	
	public static final String SERVICE_INSTANCES_FILDS = "instance_id, plan_id, organization_guid, space_guid, node_id";
	public static final String SERVICE_INSTANCES_ADD = "INSERT INTO broker.service_instances (" + SERVICE_INSTANCES_FILDS + ") values (?, ?, ?, ?, ?)";
	public static final String SERVICE_INSTANCES_UPDATE_FILDS = "UPDATE broker.service_instances SET plan_id = ? WHERE instance_id = ?";
	public static final String SERVICE_INSTANCES_DELETE_BY_INSTANCE_ID = "delete from broker.service_instances where instance_id = ?";
	
	public static final String SERVICE_BINDING_FILDS ="binding_id, instance_id, app_id, username, password";
	public static final String SERVICE_BINDING_ADD = "INSERT INTO broker.service_binding (" + SERVICE_BINDING_FILDS + ") values (?, ?, ?, ?, ?)";
	public static final String SERVICE_BINDING_FIND_BY_BINDING_ID = "select " + SERVICE_BINDING_FILDS + " from broker.service_binding where binding_id = ?";
	public static final String SERVICE_BINDING_FIND_USERNAME_BY_BINDING_ID = "select username from broker.service_binding where binding_id = ?";
	public static final String SERVICE_BINDING_DELETE_BY_BINDING_ID = "delete from broker.service_binding where binding_id = ?";
	public static final String SERVICE_BINDING_DELETE_BY_INSTANCE_ID = "DELETE FROM broker.service_binding WHERE instance_id = ?";
	public static final String SERVICE_BINDING_FIND_USERNAME_BY_INSTANCE_ID = "select username from broker.service_binding where instance_id = '%s'";
	
	public static final String DEDICATED_NODES_FIND_FREE_NODE = "SELECT node_id FROM broker.dedicated_nodes WHERE inuse='0' LIMIT 1";
	public static final String DEDICATED_NODES_UPDATE_FIELD = "UPDATE broker.dedicated_nodes SET inuse=? WHERE node_id=?";
	
	@Autowired
	public AltibaseAdminService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * 같은이름의 service instance가 존재하는지 여부를 반환.
	 * 존재하면 true
	 * 존재하지않으면 false
	 * 
	 * @param instance
	 * @return boolean
	 * @throws AltibaseServiceException
	 */
	public boolean isExistsService(AltibaseServiceInstance instance) throws AltibaseServiceException {
		try {
			List<Map<String,Object>> databases = jdbcTemplate.queryForList(
					"SELECT service_id FROM broker.service_instances WHERE instance_id = '"+instance.getServiceInstanceId()+"'");
			
			return databases.size() > 0 ? true : false;
			
		} catch (InvalidResultSetAccessException e) {
			throw handleException(e);
		} catch (DataAccessException e) {
			throw handleException(e);
		}
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
	 * 같은 이름의 user가 존재하는지 여부를 반환.
	 * 존재하면 true
	 * 존재하지않으면 false
	 * 
	 * @param username
	 * @return
	 * @throws AltibaseServiceException
	 */
	public boolean isExistsUser(String username) throws AltibaseServiceException {
		try {

			List<Map<String,Object>> users = jdbcTemplate.queryForList("SELECT binding_id "
					+ "FROM broker.service_binding "
					+ "WHERE username = '" + username + "'");

			return users.size() > 0 ? true : false;
			
		} catch (InvalidResultSetAccessException e) {
			throw handleException(e);
		} catch (DataAccessException e) {
			throw handleException(e);
		}
	}

	/**
	 * 특정 node에 같은 이름의 user가 존재하는지 여부를 반환.
	 * 존재하면 true
	 * 존재하지않으면 false
	 * 
	 * @param url
	 * @param username
	 * @return
	 * @throws AltibaseServiceException
	 */
	public boolean isExistsUser(String url, String username) throws AltibaseServiceException {
		Connection con = null;
		Statement stmt = null;
		ResultSet res = null;
		try {
			Class.forName("Altibase.jdbc.driver.AltibaseDriver");
			con = DriverManager.getConnection( url );
			stmt = con.createStatement();
			res = stmt.executeQuery("SELECT user_id "
					+ "FROM system_.sys_users_ "
					+ "WHERE user_name = '" + username + "'");
			
			if (res.next()) return true;
			else return false;
			
		} catch (Exception e) {
			throw handleException(e);
		} finally {
			try {
				if (res != null) res.close();
				if (stmt != null) stmt.close();
				if (con != null) con.close();
			} catch (SQLException e) { }
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
			jdbcTemplate.update(SERVICE_BINDING_DELETE_BY_INSTANCE_ID, id);
			
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
					serviceInstance.getPlanId(),
					serviceInstance.getOrganizationGuid(),
					serviceInstance.getSpaceGuid(),
					serviceInstance.getNodeId());
			
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
	 * Dedicated_nodes 중 미사용중인 node 하나를 찾아서 ServiceInstance 에 할당
	 * ssh 이용
	 * 
	 * @param serviceInstance
	 * @return
	 * @throws AltibaseServiceException
	 */
	public int getNode(AltibaseServiceInstance serviceInstance) throws AltibaseServiceException {
		int nodeId = 0;
		try {
			Integer tmpNodeId = jdbcTemplate.queryForObject(DEDICATED_NODES_FIND_FREE_NODE, Integer.class);
			
			if (tmpNodeId == null) {
				
			}
			nodeId = tmpNodeId.intValue();

			jdbcTemplate.update(DEDICATED_NODES_UPDATE_FIELD, "1", nodeId);
			
		} catch (EmptyResultDataAccessException e) {
			throw new AltibaseServiceException("No more free node for a new service");
		} catch (InvalidResultSetAccessException e) {
			throw handleException(e);
		} catch (DataAccessException e) {
			throw handleException(e);
		}
		return nodeId;
	}

	/**
	 * ServiceInstance 정보를 이용하여 node 반환.
	 * ssh 이용
	 * 
	 * @param serviceInstance
	 * @throws AltibaseServiceException
	 */
	public void freeNode(AltibaseServiceInstance serviceInstance) throws AltibaseServiceException {
		try {
			jdbcTemplate.update(DEDICATED_NODES_UPDATE_FIELD, "0", serviceInstance.getNodeId());
		} catch (InvalidResultSetAccessException e) {
			throw handleException(e);
		} catch (DataAccessException e) {
			throw handleException(e);
		}
	}
	
	/**
	 * 특정 node에 사용자를 추가.
	 * ssh 이용.
	 * 
	 * @param url
	 * @param username
	 * @param password
	 * @throws AltibaseServiceException
	 */
	public void createUser(String url, String username, String password) throws AltibaseServiceException{
		Connection con = null;
		Statement stmt = null;
		try {
			Class.forName("Altibase.jdbc.driver.AltibaseDriver");
			con = DriverManager.getConnection( url );
			stmt = con.createStatement();
			stmt.execute("CREATE USER " + username + " IDENTIFIED BY \"" + password + "\"");
			stmt.execute("GRANT ALL PRIVILEGES TO " + username);
			
		} catch (Exception e) {
			throw handleException(e);
		} finally {
			try {
				if (stmt != null) stmt.close();
				if (con != null) con.close();
			} catch (SQLException e) { }
		}
	}

	/**
	 * 특정 node에서 사용자를 삭제
	 * 
	 * 
	 * @param url
	 * @param username
	 * @throws AltibaseServiceException
	 */
	public void deleteUser(String url, String username) throws AltibaseServiceException {
//		try {
//			jdbcTemplate.execute("DROP USER " + userName + " CASCADE");
//		} catch (Exception e) {
//			//throw handleException(e);
//		}
		Connection con = null;
		Statement stmt = null;
		try {
			Class.forName("Altibase.jdbc.driver.AltibaseDriver");
			con = DriverManager.getConnection( url );
			stmt = con.createStatement();
			stmt.execute("DROP USER " + username + " CASCADE");
			
		} catch (Exception e) {
			throw handleException(e);
		} finally {
			try {
				if (stmt != null) stmt.close();
				if (con != null) con.close();
			} catch (SQLException e) { }
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
	
	private static final class ServiceInstanceRowMapper implements RowMapper<AltibaseServiceInstance> {
        @Override
        public AltibaseServiceInstance mapRow(ResultSet rs, int rowNum) throws SQLException {
            CreateServiceInstanceRequest request = new CreateServiceInstanceRequest();
            request.withServiceInstanceId(rs.getString(1));
            request.setPlanId(rs.getString(2));
            request.setOrganizationGuid(rs.getString(3));
            request.setSpaceGuid(rs.getString(4));
            AltibaseServiceInstance instance = new AltibaseServiceInstance(request);
            instance.setNodeId(rs.getInt(5));
            instance.setHost(rs.getString(6));
            instance.setPort(rs.getString(7));
            return instance;
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
