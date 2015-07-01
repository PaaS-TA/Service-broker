package org.openpaas.servicebroker.apiplatform.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

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
				
				System.out.println(rs.getString("organization_guid"));
				
				return apiUser;
				}
			};

		apiUser = (APIUser)jdbcTemplate.queryForObject(sql, mapper);

		logger.info("getAPIUserByOrgID() finished");
		
	return apiUser;
	}
	
	public int getServiceCount(String organizationGuid,String serviceInstanceId, String service_id,String plan_id){
		logger.info("serviceInstanceDuplicationCheck() start");

		String sql = "SELECT COUNT(*) FROM apiplatform_services "
				+ "WHERE organization_guid='"+organizationGuid+"' AND instance_id='"+serviceInstanceId+"' AND service_id='"+service_id+"' AND plan_id='"+plan_id+"'";
		
		int count = jdbcTemplate.queryForInt(sql);
		
		logger.info("serviceInstanceDuplicationCheck() finished");
		return count;
	}
	
	public int getServiceInstanceIdCount(String serviceInstanceId){
		logger.info("serviceInstanceIdDuplicationCheck() start");
	
		String sql = "SELECT COUNT(*) FROM apiplatform_services "
				+ "WHERE instance_id='"+serviceInstanceId+"'";
		int count = jdbcTemplate.queryForInt(sql);
		
		logger.info("serviceInstanceIdDuplicationCheck() finished");
		return count;
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
