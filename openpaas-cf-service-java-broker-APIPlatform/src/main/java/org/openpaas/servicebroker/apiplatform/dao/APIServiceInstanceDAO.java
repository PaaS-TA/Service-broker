package org.openpaas.servicebroker.apiplatform.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.openpaas.servicebroker.apiplatform.model.APIUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
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
		logger.info("start getAPIUserByOrgID()");
		logger.debug("oranization_guid : "+oranization_guid);
		String sql = "SELECT * from APIUser where organization_guid='"+oranization_guid+"'";
		
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
			apiUser = jdbcTemplate.queryForObject(sql, mapper);
			
		} catch (EmptyResultDataAccessException e) {
			
			logger.info("no User");
		}
		
//			@Override
//			public APIUser mapRow(ResultSet rs, int rowNum) throws SQLException {
////				APIUser apiUser = new APIUser()
//				
//				apiUser.setOrganization_guid(rs.getString("organization_guid"));
//				apiUser.setUser_id(rs.getString("user_id"));
//				apiUser.setUser_password(rs.getString("user_password"));
//				
//				
//				System.out.println("APIUser get");
//				System.out.println(apiUser.getUser_id());
//				return apiUser;
//			});
//
//		if(response==null){
//			return null;
//		}
//		
//		
		System.out.println("APIUser get");
		System.out.println(apiUser.getUser_id());
		logger.info("getAPIUserByOrgID - end");
	return apiUser;
	}
	
	public boolean insertAPIUser(String oranization_guid, String User_id, String User_password){
		
		boolean result =true;
		String sql ="INSERT INTO APIUser (organization_guid,user_id,user_password) VALUES ('"+oranization_guid+"','"+User_id+"','"+User_password+"')";
		
		try {
			jdbcTemplate.execute(sql);			
		} catch (Exception e) {
			result=false;
			return result;
		}
		
		return result;
	}
}
