package org.openpaas.servicebroker.mysql.service.impl;

import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.mysql.exception.MysqlServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Utility class for manipulating a Mysql database.
 * 
 * @author 
 *
 */
@Service
public class MysqlAdminService {

	
	private Logger logger = LoggerFactory.getLogger(MysqlAdminService.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	public MysqlAdminService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
		
	public boolean isExistsService(ServiceInstance instance){
		return true;
	}
	
	public boolean isExistsUser(String userId){
		return true;
	}
	
	public ServiceInstance findById(String id){
		return null;
	}
	
	public ServiceInstanceBinding findBindById(String id){
		return null;
	}
	
	public void delete(String id) throws MysqlServiceException{
		try{
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void deleteBind(String id) throws MysqlServiceException{
		try{
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void save(ServiceInstance serviceInstance) throws MysqlServiceException{
		try{
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void saveBind(ServiceInstanceBinding serviceInstanceBinding) throws MysqlServiceException{
		try{
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void deleteDatabase(ServiceInstance serviceInstance) throws MysqlServiceException{
		try{
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void createDatabase(ServiceInstance serviceInstance) throws MysqlServiceException{
		try{
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void createUser(String database, String userId, String password) throws MysqlServiceException{
		try{
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	public void deleteUser(String database, String userId) throws MysqlServiceException{
		try{
			
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
	
}
