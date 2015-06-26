package org.openpaas.servicebroker.apiplatform.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openpaas.servicebroker.apiplatform.model.APIUser;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.test.jdbc.JdbcTestUtils;


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

		try {
			apiUser = (APIUser)jdbcTemplate.queryForObject(sql, mapper);
			
		} catch (EmptyResultDataAccessException e) {
			
			logger.info("Could not found User at APIPlatform Database");
		}
		
		logger.info("getAPIUserByOrgID() finished");
	return apiUser;
	}
	
	public boolean serviceInstanceDuplicationCheck(String oranization_guid,String service_id,String plan_id){
		logger.info("serviceInstanceDuplicationCheck() start");
		
		boolean duplicationCheck =true;
		
		String sql = "SELECT COUNT(*) FROM apiplatform_services "
				+ "WHERE organization_guid='"+oranization_guid+"' AND service_id='"+service_id+"' AND plan_id='"+plan_id+"'";
		int count=0;
		
		try {
			count = jdbcTemplate.queryForInt(sql);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Database Error :"+e.getMessage());
		}
		
		if(count!=0){
			duplicationCheck=false;
		}
		logger.info("serviceInstanceDuplicationCheck() start");
		return duplicationCheck;
	}
	
	public boolean insertAPIUser(String oranization_guid, String user_id, String user_password){
		
		boolean insertAPIUserResult;
		logger.info("insertAPIUser() start");
		String sql ="INSERT INTO APIPlatform_Users (organization_guid,user_id,user_password, createtimestamp) VALUES "
				+ "('"+oranization_guid+"','"+user_id+"','"+user_password+"',utc_timestamp())";
		
		try {
			jdbcTemplate.execute(sql);
		} catch (Exception e) {
			e.printStackTrace();
			insertAPIUserResult=false;
			return insertAPIUserResult;
		}
		insertAPIUserResult=true;
		logger.info("insertAPIUser() finished");
		return insertAPIUserResult;
	}
	
	public boolean insertAPIServiceInstance(String oranization_guid, String instance_id,String space_guid,String service_id,String plan_id){
		
		boolean insertAPIServiceInstanceResult;
		
		logger.info("insertAPIUser() start");
		String sql ="INSERT INTO APIPlatform_Services (organization_guid,instance_id,space_guid,service_id,plan_id,delyn,createtimestamp) VALUES "
				+ "('"+oranization_guid+"','"+instance_id+"','"+space_guid+"','"+service_id+"','"+plan_id+"','N',utc_timestamp())";
		
		try {
			jdbcTemplate.execute(sql);
		} catch (Exception e) {
			e.printStackTrace();
			insertAPIServiceInstanceResult=false;
			return insertAPIServiceInstanceResult;
		}
		
		insertAPIServiceInstanceResult=true;
		logger.info("insertAPIUser() finished");
		return insertAPIServiceInstanceResult;
	}
	
	
	
}
