package org.openpaas.servicebroker.mysql.service.impl;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.mysql.exception.MysqlServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * Mysql 데이터베이스를 조작하기위한 유틸리티 클래스.
 * 
 * @author 김한종
 *
 */
@Service
public class MysqlAdminService {

	public static final String SERVICE_INSTANCES_FILDS = "instance_id, service_id, plan_id, organization_guid, space_guid";
	
	public static final String SERVICE_INSTANCES_FIND_BY_INSTANCE_ID = "select " + SERVICE_INSTANCES_FILDS + " from broker.service_instances where instance_id = ?";
	
	public static final String SERVICE_INSTANCES_DELETE_BY_INSTANCE_ID = "delete from broker.service_instances where instance_id = ?";
	
	public static final String SERVICE_INSTANCES_UPSERT_FILDS = "ON DUPLICATE KEY UPDATE instance_id = ?, service_id = ?, plan_id = ?, organization_guid = ?, space_guid = ?";
	
	public static final String SERVICE_INSTANCES_ADD = "insert into broker.service_instances("+SERVICE_INSTANCES_FILDS+") values(?,?,?,?,?) "+ SERVICE_INSTANCES_UPSERT_FILDS;
	
	public static final String SERVICE_INSTANCES_UPDATE_FILDS = "UPDATE broker.service_instances SET service_id = ?, plan_id = ?, organization_guid = ?, space_guid = ? where instance_id = ?";
	
	public static final String SERVICE_BINDING_FILDS ="binding_id, instance_id, app_id, username, password";
	
	public static final String SERVICE_BINDING_UPSERT_FILDS = "ON DUPLICATE KEY UPDATE instance_id = ?, app_id = ? ,username = ?, password = ?";
	
	public static final String SERVICE_BINDING_ADD = "insert into broker.service_binding("+SERVICE_BINDING_FILDS+") values(?,?,?,?,?) "+ SERVICE_BINDING_UPSERT_FILDS;
	
	public static final String SERVICE_BINDING_FIND_BY_BINDING_ID = "select " + SERVICE_BINDING_FILDS + " from broker.service_binding where binding_id = ?";
	
	public static final String SERVICE_BINDING_FIND_BY_INSTANCE_ID = "select " + SERVICE_BINDING_FILDS + " from broker.service_binding where instance_id = ?";
	
	public static final String SERVICE_BINDING_DELETE_BY_BINDING_ID = "delete from broker.service_binding where binding_id = ?";
	
	public static final String SERVICE_BINDING_FIND_USERNAME_BY_BINDING_ID = "select username from broker.service_binding where binding_id = ?";
	
	// Plan별 MAX_USER_CONNECTIONS 정보
	public static String planA = "411d0c3e-b086-4a24-b041-0aeef1a819d1";
	public static int planAconnections = 10;
	public static String planB = "4a932d9d-9bc5-4a86-937f-e2c14bb9f497";
	public static int planBconnections = 100;
	
	static String DATABASE_PREFIX = "op_";
	
	private Logger logger = LoggerFactory.getLogger(MysqlAdminService.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	public MysqlAdminService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
		
	private final RowMapper<ServiceInstance> mapper = new ServiceInstanceRowMapper();
	
	private final RowMapper<ServiceInstanceBinding> mapper2 = new ServiceInstanceBindingRowMapper();
	
	/**
	 * ServiceInstance의 유무를 확인합니다
	 * @param instance
	 * @return
	 */
	public boolean isExistsService(ServiceInstance instance){
		System.out.println("MysqlAdminService.isExistsService");
		List<String> databases = jdbcTemplate.queryForList("SHOW DATABASES LIKE '"+getDatabase(instance.getServiceInstanceId())+"'",String.class);
		return databases.size() > 0;
	}
	
	/**
	 * 사용자 유무를 확인합니다.
	 * @param userId
	 * @return
	 */
	public boolean isExistsUser(String userId){
		System.out.println("MysqlAdminService.isExistsUser");
		try {
			jdbcTemplate.execute("SHOW GRANTS FOR '"+userId+"'");
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * ServiceInstanceId로 ServiceInstance정보를 조회합니다.
	 * @param id
	 * @return
	 */
	public ServiceInstance findById(String id){
		System.out.println("MysqlAdminService.findById");
		ServiceInstance serviceInstance = null;;
		try {
			serviceInstance = jdbcTemplate.queryForObject(SERVICE_INSTANCES_FIND_BY_INSTANCE_ID, mapper, id);
			serviceInstance.withDashboardUrl(getDashboardUrl(serviceInstance.getServiceInstanceId()));
		} catch (Exception e) {
		}
		return serviceInstance;
	}
	
	/**
	 * 요청 정보로 부터 ServiceInstance를 생성합니다.
	 * @param request
	 * @return
	 */
	public ServiceInstance createServiceInstanceByRequest(CreateServiceInstanceRequest request){
		System.out.println("MysqlAdminService.createServiceInstanceByRequest");
		return new ServiceInstance(request).withDashboardUrl(getDashboardUrl(request.getServiceInstanceId()));
	}
	
	/**
	 * ServiceInstanceBindingId로 ServiceInstanceBinding정보를 조회합니다.
	 * @param id
	 * @return
	 */
	public ServiceInstanceBinding findBindById(String id){
		System.out.println("MysqlAdminService.findBindById");
		ServiceInstanceBinding serviceInstanceBinding = null;;
		try {
			serviceInstanceBinding = jdbcTemplate.queryForObject(SERVICE_BINDING_FIND_BY_BINDING_ID, mapper2, id);
		} catch (Exception e) {
		}
		return serviceInstanceBinding;
	}
	
	
	/**
	 * ServiceInstanceId로 ServiceInstanceBinding 목록 정보를 조회합니다.
	 * @param id
	 * @return
	 */
	public List<Map<String,Object>> findBindByInstanceId(String id){
		System.out.println("MysqlAdminService.findBindByInstanceId");
		System.out.println(SERVICE_BINDING_FIND_BY_INSTANCE_ID + "/"+id);
		//List<ServiceInstanceBinding> list = new ArrayList<ServiceInstanceBinding>();
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		try {
			list = jdbcTemplate.queryForList(SERVICE_BINDING_FIND_BY_INSTANCE_ID, id);
		} catch (Exception e) {
		}
		return list;
	}
	
	/**
	 * 요청 정보로부터 ServiceInstanceBinding 정보를 생성합니다.
	 * @param request
	 * @return
	 */
	public ServiceInstanceBinding createServiceInstanceBindingByRequest(CreateServiceInstanceBindingRequest request){
		System.out.println("MysqlAdminService.createServiceInstanceBindingByRequest");
		return new ServiceInstanceBinding(request.getBindingId(), 
				request.getServiceInstanceId(), 
				new HashMap<String, Object>(), 
				"syslogDrainUrl", 
				request.getAppGuid());
	}
	
	/**
	 * ServiceInstance 정보를 저장합니다.
	 * @param serviceInstance
	 * @throws MysqlServiceException
	 */
	public void save(ServiceInstance serviceInstance) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.save");
			jdbcTemplate.update(SERVICE_INSTANCES_ADD, 
					serviceInstance.getServiceInstanceId(),
					serviceInstance.getServiceDefinitionId(),
					serviceInstance.getPlanId(),
					serviceInstance.getOrganizationGuid(),
					serviceInstance.getSpaceGuid(),
					serviceInstance.getServiceInstanceId(),
					serviceInstance.getServiceDefinitionId(),
					serviceInstance.getPlanId(),
					serviceInstance.getOrganizationGuid(),
					serviceInstance.getSpaceGuid());
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	/**
	 * ServiceInstanceId로 ServiceInstance정보를 삭제합니다.
	 * @param id
	 * @throws MysqlServiceException
	 */
	public void delete(String id) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.delete");
			jdbcTemplate.update(SERVICE_INSTANCES_DELETE_BY_INSTANCE_ID, id);
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	/**
	 * 해당하는 Database를 삭제합니다.
	 * @param serviceInstance
	 * @throws MysqlServiceException
	 */
	public void deleteDatabase(ServiceInstance serviceInstance) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.deleteDatabase");
			jdbcTemplate.execute("DROP DATABASE IF EXISTS " + getDatabase(serviceInstance.getServiceInstanceId()));
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	/**
	 * 해당하는 Database를 생성합니다.
	 * @param serviceInstance
	 * @throws MysqlServiceException
	 */
	public void createDatabase(ServiceInstance serviceInstance) throws MysqlServiceException{
		
		try{
			
			System.out.println("MysqlAdminService.createDatabase");
			jdbcTemplate.execute("CREATE DATABASE " + getDatabase(serviceInstance.getServiceInstanceId()));
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	/**
	 * ServiceInstance의 Plan 정보를 수정합니다.
	 * @param instance
	 * @param request
	 * @throws MysqlServiceException
	 */
	public void updatePlan(ServiceInstance instance, ServiceInstance request) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.updatePlan");
			jdbcTemplate.update(SERVICE_INSTANCES_UPDATE_FILDS,
					instance.getServiceDefinitionId(),
					request.getPlanId(),
					instance.getOrganizationGuid(),
					instance.getSpaceGuid(),
					instance.getServiceInstanceId());
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	/**
	 * 사용자별 MAX_USER_CONNECTIONS정보를 수정합니다.
	 * @param database
	 * @param userId
	 * @param connections
	 * @return
	 */
	public boolean updateUserConnections(String database, String userId, String connections){
		System.out.println("MysqlAdminService.updateUserConnections");
		try {
			jdbcTemplate.execute("GRANT USAGE ON " + database + ".* TO '" + userId + "'@'%' WITH MAX_USER_CONNECTIONS " + connections);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * ServiceInstanceBinding 정보를 저장합니다.
	 * @param serviceInstanceBinding
	 * @throws MysqlServiceException
	 */
	public void saveBind(ServiceInstanceBinding serviceInstanceBinding) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.saveBind");
			jdbcTemplate.update(SERVICE_BINDING_ADD, 
					serviceInstanceBinding.getId(),
					serviceInstanceBinding.getServiceInstanceId(),
					serviceInstanceBinding.getAppGuid(),
					serviceInstanceBinding.getCredentials().get("username"),
					serviceInstanceBinding.getCredentials().get("password"),
					serviceInstanceBinding.getServiceInstanceId(),
					serviceInstanceBinding.getAppGuid(),
					serviceInstanceBinding.getCredentials().get("username"),
					serviceInstanceBinding.getCredentials().get("password"));
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	/**
	 * ServiceInstanceBindingId로 ServiceInstanceBinding 정보를 삭제합니다.
	 * @param id
	 * @throws MysqlServiceException
	 */
	public void deleteBind(String id) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.deleteBind");
			jdbcTemplate.update(SERVICE_BINDING_DELETE_BY_BINDING_ID, id);
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	/**
	 * 해당하는 User를 생성합니다.
	 * @param database
	 * @param userId
	 * @param password
	 * @throws MysqlServiceException
	 */
	public void createUser(String database, String userId, String password) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.createUser");

			jdbcTemplate.execute("CREATE USER '"+userId+"' IDENTIFIED BY '"+password+"'");
			jdbcTemplate.execute("GRANT ALL PRIVILEGES ON "+database+".* TO '"+userId+"'@'%'");
			
			//GRANT ALL ON cf_11b9e707_0e2b_47e3_a21a_fd01a8eb0454.* TO '62519bdc9523157e'@'%' WITH MAX_USER_CONNECTIONS 2;
			//jdbcTemplate.execute("FLUSH PRIVILEGES");
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	//public void deleteUser(String database, String userId) throws MysqlServiceException{
	/**
	 * 해당하는 User를 삭제합니다.
	 * @param database
	 * @param bindingId
	 * @throws MysqlServiceException
	 */
	public void deleteUser(String database, String bindingId) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.deleteUser");
			String userId = jdbcTemplate.queryForObject(SERVICE_BINDING_FIND_USERNAME_BY_BINDING_ID, String.class, bindingId);
			jdbcTemplate.execute("DROP USER '"+userId+"'@'%'");
		} catch (Exception e) {
			//throw handleException(e);
		}
	}
	
	/**
	 * 해당하는 User를 삭제합니다.
	 * @param database
	 * @param bindingId
	 * @throws MysqlServiceException
	 */
	public void deleteUser(String userId) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.deleteUser");
			jdbcTemplate.execute("DROP USER '"+userId+"'@'%'");
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	/*public String getConnectionString(String database, String username, String password) {
		//mysql://b5d435f40dd2b2:ebfc00ac@us-cdbr-east-03.cleardb.com:3306/ad_c6f4446532610ab
		StringBuilder builder = new StringBuilder();
		return builder.toString();
	}*/
	
	/**
	 * Database 접속정보를 생성합니다.
	 * @param database
	 * @param username
	 * @param password
	 * @param hostName
	 * @return
	 */
	public String getConnectionString(String database, String username, String password, String hostName) {
		//mysql://ns4VKg4Xtoy5mNCo:KyNRTVYPJyoqG1xo@10.30.40.163:3306/cf_dd2e0ffe_2bab_4308_b191_7d8814b16933
		StringBuilder builder = new StringBuilder();
		builder.append("mysql://"+username+":"+password+"@"+hostName+":3306/"+database);
		return builder.toString();
	}
	
	public String getServerAddresses() {
		StringBuilder builder = new StringBuilder();
		return builder.toString();
	}
	
	private MysqlServiceException handleException(Exception e) {
		logger.warn(e.getLocalizedMessage(), e);
		return new MysqlServiceException(e.getLocalizedMessage());
	}
	
	// DashboardUrl 생성
	public String getDashboardUrl(String instanceId){
		
		return "http://www.sample.com/"+instanceId;
	}
	
	// Database명 생성
	public String getDatabase(String id){
		
		String database;
		String s = id.replaceAll("-", "_");
	    database = DATABASE_PREFIX+s;
	    
	  return database;
	}
	
	// User명 생성
	public String getUsername(String id) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			// TODO: handle exception
		}
		digest.update(id.getBytes());
		String username = new BigInteger(1, digest.digest()).toString(16).replaceAll("/[^a-zA-Z0-9]+/", "").substring(0, 16);
		return username;
						
	}
	
	// User MAX_USER_CONNECTIONS 설정 조정
	public void setUserConnections(String planId, String id) throws ServiceBrokerException{
		
		/* Plan 정보 설정 */
		int totalConnections = 0;
		int totalUsers;
		int userPerConnections;
		int mod;
		
		if(planA.equals(planId)) totalConnections = planAconnections;
		if(planB.equals(planId)) totalConnections = planBconnections;
		if(!planA.equals(planId) && !planB.equals(planId)) throw new ServiceBrokerException("");
		
		// ServiceInstanceBinding 정보를 조회한다.
		List<Map<String,Object>> list = findBindByInstanceId(id);
		// ServiceInstance의 총 Binding 건수 확인
		totalUsers = list.size();
		if(totalUsers <= 0) return;
		if(totalConnections<totalUsers) throw new ServiceBrokerException("It may not exceed the specified plan.(Not assign Max User Connection)");

		// User당 connection 수 = Plan의 connection / 총 Binding 건수
		userPerConnections = totalConnections / totalUsers;
		// 미할당 connection = Plan의 connection % 총 Binding 건수
		mod = totalConnections % totalUsers;

		for(int i=0;i < list.size();i++){
			Map<String,Object> tmp = list.get(i);
			
			// 첫번째 사용자에게 User당 connection 수 + 미할당 connection 할당
			if(i==0){
				updateUserConnections(getDatabase((String)tmp.get("instance_id")), getUsername((String)tmp.get("binding_id")), (userPerConnections+mod)+"");
				//System.out.println(tmp.get("instance_id")+"/"+tmp.get("binding_id")+"/"+userPerConnections);
			}
			//  User당 connection 수  할당
			else{
				updateUserConnections(getDatabase((String)tmp.get("instance_id")), getUsername((String)tmp.get("binding_id")), userPerConnections+"");
				//System.out.println(tmp.get("instance_id")+"/"+tmp.get("binding_id")+"/"+userPerConnections);
			}
		}
	}
	
	// User MAX_USER_CONNECTIONS 설정 조정
	public boolean checkUserConnections(String planId, String id) throws ServiceBrokerException{
			
		/* Plan 정보 설정 */
		int totalConnections = 0;
		int totalUsers;
		
		if(planA.equals(planId)) totalConnections = planAconnections;
		if(planB.equals(planId)) totalConnections = planBconnections;
		if(!planA.equals(planId) && !planB.equals(planId)) throw new ServiceBrokerException("");
		
		// ServiceInstanceBinding 정보를 조회한다.
		List<Map<String,Object>> list = findBindByInstanceId(id);
		// ServiceInstance의 총 Binding 건수 확인
		totalUsers = list.size();
		
		if(totalConnections <= totalUsers) return true;
		
		return false;
	}
	
	private static final class ServiceInstanceRowMapper implements RowMapper<ServiceInstance> {
        @Override
        public ServiceInstance mapRow(ResultSet rs, int rowNum) throws SQLException {
            CreateServiceInstanceRequest request = new CreateServiceInstanceRequest();
            request.withServiceInstanceId(rs.getString(1));
            request.setServiceDefinitionId(rs.getString(2));
            request.setPlanId(rs.getString(3));
            request.setOrganizationGuid(rs.getString(4));
            request.setSpaceGuid(rs.getString(5));
            return new ServiceInstance(request);
        }
    }
	
	private static final class ServiceInstanceBindingRowMapper implements RowMapper<ServiceInstanceBinding> {
        @Override
        public ServiceInstanceBinding mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ServiceInstanceBinding(rs.getString(1), 
            		rs.getString(2), 
            		new HashMap<String, Object>(), 
            		"", 
            		rs.getString(3));
        }
    }
	
	
}
