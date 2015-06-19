package org.openpaas.servicebroker.mysql.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.mysql.exception.MysqlServiceException;
import org.openpaas.servicebroker.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * Utility class for manipulating a Mysql database.
 * 
 * @author 
 *
 */
@Service
public class MysqlAdminService {

	public static final String SERVICE_INSTANCES_FILDS = "instance_id, service_id, plan_id, organization_guid, space_guid";
	
	public static final String SERVICE_INSTANCES_FIND_BY_INSTANCE_ID = "select " + SERVICE_INSTANCES_FILDS + " from test.service_instances where instance_id = ?";
	
	public static final String SERVICE_INSTANCES_DELETE_BY_INSTANCE_ID = "delete from test.service_instances where instance_id = ?";
	
	public static final String SERVICE_INSTANCES_UPSERT_FILDS = "ON DUPLICATE KEY UPDATE instance_id = ?, service_id = ?, plan_id = ?, organization_guid = ?, space_guid = ?";
	
	public static final String SERVICE_INSTANCES_ADD = "insert into test.service_instances("+SERVICE_INSTANCES_FILDS+") values(?,?,?,?,?) "+ SERVICE_INSTANCES_UPSERT_FILDS;
	
	public static final String SERVICE_INSTANCES_UPDATE_FILDS = "UPDATE test.service_instances SET instance_id = ?, service_id = ?, plan_id = ?, organization_guid = ?, space_guid = ?";
	
	static String DATABASE_PREFIX = "cf_";
	
	private Logger logger = LoggerFactory.getLogger(MysqlAdminService.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	public MysqlAdminService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
		
	@Autowired
	private CatalogService catalogService;
	
	private final RowMapper<ServiceInstance> mapper = new ServiceInstanceRowMapper();
	
	public boolean isExistsService(ServiceInstance instance){
		System.out.println("MysqlAdminService.isExistsService");
		List<String> databases = jdbcTemplate.queryForList("SHOW DATABASES LIKE '"+getDatabase(instance.getServiceInstanceId())+"'",String.class);
		return databases.size() > 0;
	}
	
	public boolean isExistsUser(String userId){
		System.out.println("MysqlAdminService.isExistsUser");
		return true;
	}
	
	public ServiceInstance findById(String id){
		System.out.println("MysqlAdminService.findById");
		ServiceInstance serviceInstance = jdbcTemplate.queryForObject(SERVICE_INSTANCES_FIND_BY_INSTANCE_ID, mapper, id);
		serviceInstance.withDashboardUrl(getDashboardUrl(serviceInstance.getServiceInstanceId()));
		return serviceInstance;
	}
	
	public ServiceInstance createServiceInstanceByRequest(CreateServiceInstanceRequest request){
		System.out.println("MysqlAdminService.findById");
		return new ServiceInstance(request).withDashboardUrl(getDashboardUrl(request.getServiceInstanceId()));
	}
	
	public ServiceInstanceBinding findBindById(String id){
		System.out.println("MysqlAdminService.findBindById");
		return null;
	}
	
	public void delete(String id) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.delete");
			jdbcTemplate.update(SERVICE_INSTANCES_DELETE_BY_INSTANCE_ID, id);
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void updatePlan(ServiceInstance instance, ServiceInstance request) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.delete");
			jdbcTemplate.update(SERVICE_INSTANCES_UPDATE_FILDS,
					instance.getServiceInstanceId(),
					instance.getServiceDefinitionId(),
					request.getPlanId(),
					instance.getOrganizationGuid(),
					instance.getSpaceGuid());
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void deleteBind(String id) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.deleteBind");
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
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
	
	public void saveBind(ServiceInstanceBinding serviceInstanceBinding) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.saveBind");
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void deleteDatabase(ServiceInstance serviceInstance) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.deleteDatabase");
			jdbcTemplate.execute("DROP DATABASE IF EXISTS " + getDatabase(serviceInstance.getServiceInstanceId()));
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void createDatabase(ServiceInstance serviceInstance) throws MysqlServiceException{
		/*String plan1 = "411d0c3e-b086-4a24-b041-0aeef1a819d1";
		String plan2 = "4a932d9d-9bc5-4a86-937f-e2c14bb9f497";
		String size = "";
		String connect = "";*/
		
		try{
			
			/*if(serviceInstance.getPlanId().equals(plan1)){
				size = "10";
				connect = "10";
			}	
			if(serviceInstance.getPlanId().equals(plan1)){
				size = "100";
				connect = "20";
			}*/
			
			System.out.println("MysqlAdminService.createDatabase");
			jdbcTemplate.execute("CREATE DATABASE " + getDatabase(serviceInstance.getServiceInstanceId()));
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void createUser(String database, String userId, String password) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.createUser");
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void deleteUser(String database, String userId) throws MysqlServiceException{
		try{
			System.out.println("MysqlAdminService.deleteUser");
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public String getConnectionString(String database, String username, String password) {
		StringBuilder builder = new StringBuilder();
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
	
	public String getDashboardUrl(String instanceId){
		
		return "http://www.sample.com/"+instanceId;
	}
	
	public String getDatabase(String id){
		
		String database;

		String s = id.replaceAll("-", "_");
		
	    database = DATABASE_PREFIX+s;
	    
	  return database;
	}
	
}
