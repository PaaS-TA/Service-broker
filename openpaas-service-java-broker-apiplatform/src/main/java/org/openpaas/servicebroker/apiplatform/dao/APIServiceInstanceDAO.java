package org.openpaas.servicebroker.apiplatform.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openpaas.servicebroker.apiplatform.model.APIServiceInstance;
import org.openpaas.servicebroker.apiplatform.model.APIUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class APIServiceInstanceDAO {

	private static final Logger logger = LoggerFactory.getLogger(APIServiceInstanceDAO.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	public APIServiceInstanceDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@Autowired
	private Environment env;
	
	public APIUser getAPIUserByOrgID(String oranization_guid){
		logger.info("getAPIUserByOrgID() start");
		logger.debug("oranization_guid : "+oranization_guid);
		String sql = "SELECT * FROM apiplatform_users WHERE organization_guid='"+oranization_guid+"'";
		
		APIUser apiUser = new APIUser();
		
		RowMapper mapper = new RowMapper(){
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				APIUser apiUser = new APIUser();
				apiUser.setOrganization_guid(rs.getString("organization_guid"));
				apiUser.setUser_id(rs.getString("user_id"));
				apiUser.setUser_password(rs.getString("user_password"));
				
				return apiUser;
				}
			};

		apiUser = (APIUser)jdbcTemplate.queryForObject(sql, mapper);

		logger.info("getAPIUserByOrgID() finished");
		
	return apiUser;
	}
	
	public APIServiceInstance getAPIServiceByInstanceID(String instanceId){
		logger.info("getServiceByInstanceID() start");
		logger.debug("instance_id : "+instanceId);
		String sql = "SELECT * FROM apiplatform_services WHERE instance_id='"+instanceId+"'";
		
		APIServiceInstance apiService = new APIServiceInstance();
		
		RowMapper mapper = new RowMapper(){
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				APIServiceInstance apiService = new APIServiceInstance();
				apiService.setInstance_id(rs.getString("instance_id"));
				apiService.setOrganization_id(rs.getString("organization_guid"));
				apiService.setSpace_guid(rs.getString("space_guid"));
				apiService.setService_id(rs.getString("service_id"));
				apiService.setPlan_id(rs.getString("plan_id"));
				
				return apiService;
				}
			};

			apiService = (APIServiceInstance)jdbcTemplate.queryForObject(sql, mapper);

		logger.info("getAPIServiceByInstanceID() finished");
		
	return apiService;
	}
	//
	public APIUser getAPIUserByInstanceId(String instanceId){
		logger.debug("instanceId : "+instanceId);
		String sql = "SELECT * FROM apiplatform_users WHERE instance_id='"+instanceId+"'";
		
		APIUser apiUser = new APIUser();
		
		RowMapper mapper = new RowMapper(){
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				APIUser apiUser = new APIUser();
				apiUser.setOrganization_guid(rs.getString("organization_guid"));
				apiUser.setUser_id(rs.getString("user_id"));
				apiUser.setUser_password(rs.getString("user_password"));
				
				return apiUser;
				}
			};

		apiUser = (APIUser)jdbcTemplate.queryForObject(sql, mapper);
		
		return apiUser;
	}
	
	public void insertAPIUser(String oranization_guid, String user_id, String user_password){
		
		logger.info("insertAPIUser() start");
		String sql ="INSERT INTO apiplatform_users (organization_guid,user_id,user_password, createtimestamp) VALUES "
				+ "('"+oranization_guid+"','"+user_id+"','"+user_password+"',utc_timestamp())";
		
		jdbcTemplate.execute(sql);

		logger.info("insertAPIUser() finished");
	}
	
	public void insertAPIServiceInstance(String oranization_guid, String serviceInstanceId,String space_guid,String service_id,String plan_id){
		
		logger.info("insertAPIUser() start");
		String sql ="INSERT INTO apiplatform_services (organization_guid,instance_id,space_guid,service_id,plan_id,delyn,createtimestamp) VALUES "
				+ "('"+oranization_guid+"','"+serviceInstanceId+"','"+space_guid+"','"+service_id+"','"+plan_id+"','N',utc_timestamp())";
	
		jdbcTemplate.execute(sql);
		
		logger.info("insertAPIUser() finished");
	}
}
