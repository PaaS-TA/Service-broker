//package org.openpaas.servicebroker.publicapi.dao;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.List;
//import java.util.Map;
//
//import org.openpaas.servicebroker.exception.ServiceBrokerException;
//import org.openpaas.servicebroker.publicapi.model.APIServiceInstance;
//import org.openpaas.servicebroker.publicapi.model.APIUser;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.env.Environment;
//import org.springframework.dao.DuplicateKeyException;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.RowMapper;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public class PublicAPIServiceInstanceDAO {
//
//	private static final Logger logger = LoggerFactory
//			.getLogger(PublicAPIServiceInstanceDAO.class);
//
//	@Autowired
//	private JdbcTemplate jdbcTemplate;
//
//	@Autowired
//	public PublicAPIServiceInstanceDAO(JdbcTemplate jdbcTemplate) {
//		this.jdbcTemplate = jdbcTemplate;
//	}
//
//	@Autowired
//	private Environment env;
//
//	public List<Map<String, Object>> getServiceInstancAtDB(String instance_id)
//			throws ServiceBrokerException {
//		List<Map<String, Object>> databases;
//		String sql = "SELECT * FROM publicapi_services WHERE instance_id='"
//				+ instance_id + "'";
//		try {
//			databases = jdbcTemplate.queryForList(sql);
//		} catch (Exception e) {
//			throw new ServiceBrokerException("Database Exception");
//		}
//		return databases;
//	}
//
//	public boolean insertServiceInstance(String instance_id, String service_id,
//			String plan_id, String organization_guid, String space_guid,
//			String serviceKey) throws ServiceBrokerException {
//		boolean serviceInstanceExsists = false;
//
//		String sql = "INSERT INTO publicapi_services (instance_id,service_id,plan_id,organization_guid,space_guid,service_key,delyn,createtimestamp) VALUES "
//				+ "('"
//				+ instance_id
//				+ "','"
//				+ service_id
//				+ "','"
//				+ plan_id
//				+ "','"
//				+ organization_guid
//				+ "','"
//				+ space_guid
//				+ "', '"
//				+ serviceKey + "','N',utc_timestamp())";
//		try {
//			jdbcTemplate.execute(sql);
//			// 인스턴스 아이디가 중복되는 경우
//		} catch (DuplicateKeyException e) {
//			serviceInstanceExsists = true;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new ServiceBrokerException(e.getMessage());
//		}
//
//		return serviceInstanceExsists;
//	}
//
//	public boolean insertBinding(String binding_id, String instance_id,
//			String app_guid) throws ServiceBrokerException {
//		boolean bindingExsists = false;
//
//		String sql = "INSERT INTO publicapi_bindings (binding_id,instance_id,app_guid, delyn, createtimestamp) VALUES "
//				+ "('"
//				+ binding_id
//				+ "','"
//				+ instance_id
//				+ "','"
//				+ app_guid
//				+ "','N',utc_timestamp())";
//		try {
//			jdbcTemplate.execute(sql);
//			// 바인드 아이디가 중복되는 경우
//		} catch (DuplicateKeyException e) {
//			bindingExsists = true;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new ServiceBrokerException(e.getMessage());
//		}
//
//		return bindingExsists;
//	}
//
//	public List<Map<String, Object>> getBindingAtDB(String binding_id)
//			throws ServiceBrokerException {
//		List<Map<String, Object>> databases;
//		String sql = "SELECT * FROM publicapi_bindings WHERE binding_id='"
//				+ binding_id + "'";
//		try {
//			databases = jdbcTemplate.queryForList(sql);
//		} catch (Exception e) {
//			throw new ServiceBrokerException("Database Exception");
//		}
//		return databases;
//
//	}
//
//	public APIServiceInstance getAPIServiceByInstance(String serviceInstanceId) {
//		logger.info("getServiceByInstanceID() start");
//		logger.debug("instance_id : " + serviceInstanceId);
//		String sql = "SELECT * FROM apiplatform_services WHERE instance_id='"
//				+ serviceInstanceId + "'";
//
//		APIServiceInstance apiServiceInstance = new APIServiceInstance();
//
//		RowMapper mapper = new RowMapper() {
//			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
//				APIServiceInstance apiService = new APIServiceInstance();
//				apiService.setInstance_id(rs.getString("instance_id"));
//				apiService.setOrganization_guid(rs
//						.getString("organization_guid"));
//				apiService.setSpace_guid(rs.getString("space_guid"));
//				apiService.setService_id(rs.getString("service_id"));
//				apiService.setPlan_id(rs.getString("plan_id"));
//				apiService.setDelyn(rs.getString("delyn"));
//				return apiService;
//			}
//		};
//
//		apiServiceInstance = (APIServiceInstance) jdbcTemplate.queryForObject(
//				sql, mapper);
//
//		logger.info("getAPIServiceByInstanceID() finished");
//
//		return apiServiceInstance;
//	}
//
//	public APIServiceInstance getAPIInfoByInstanceID(String serviceInstanceId) {
//		logger.info("getServiceByInstanceID() start");
//		logger.debug("instance_id : " + serviceInstanceId);
//		String sql = "SELECT s.organization_guid,s.instance_id,s.space_guid,s.service_id,s.plan_id,u.user_id,u.user_password,s.delyn FROM apiplatform_services s "
//				+ "INNER JOIN apiplatform_users u ON u.organization_guid = s.organization_guid WHERE instance_id= '"
//				+ serviceInstanceId + "'";
//
//		APIServiceInstance apiService = new APIServiceInstance();
//
//		RowMapper mapper = new RowMapper() {
//			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
//				APIServiceInstance apiService = new APIServiceInstance();
//				apiService.setInstance_id(rs.getString("instance_id"));
//				apiService.setOrganization_guid(rs
//						.getString("organization_guid"));
//				apiService.setSpace_guid(rs.getString("space_guid"));
//				apiService.setService_id(rs.getString("service_id"));
//				apiService.setPlan_id(rs.getString("plan_id"));
//				apiService.setDelyn(rs.getString("delyn"));
//				return apiService;
//			}
//		};
//
//		apiService = (APIServiceInstance) jdbcTemplate.queryForObject(sql,
//				mapper);
//
//		logger.info("getAPIServiceByInstanceID() finished");
//
//		return apiService;
//	}
//
//	public void insertAPIUser(String oranizationGuid, String userId,
//			String userPassword) {
//
//		logger.info("insertAPIUser() start");
//		String sql = "INSERT INTO apiplatform_users (organization_guid,user_id,user_password, createtimestamp) VALUES "
//				+ "('"
//				+ oranizationGuid
//				+ "','"
//				+ userId
//				+ "','"
//				+ userPassword + "',utc_timestamp())";
//
//		jdbcTemplate.execute(sql);
//
//		logger.info("insertAPIUser() finished");
//	}
//
//	public void insertAPIServiceInstance(String oranizationGuid,
//			String serviceInstanceId, String spaceGuid, String serviceId,
//			String planId) {
//
//		logger.info("insertAPIServiceInstance() start");
//		String sql = "INSERT INTO apiplatform_services (organization_guid,instance_id,space_guid,service_id,plan_id,delyn,createtimestamp) VALUES "
//				+ "('"
//				+ oranizationGuid
//				+ "','"
//				+ serviceInstanceId
//				+ "','"
//				+ spaceGuid
//				+ "','"
//				+ serviceId
//				+ "','"
//				+ planId
//				+ "','N',utc_timestamp())";
//
//		jdbcTemplate.execute(sql);
//
//		logger.info("insertAPIServiceInstance() finished");
//	}
//
//	public void updateAPIServiceInstance(String serviceInstanceId, String planId) {
//
//		logger.info("updateAPIServiceInstance() start");
//		String sql = "UPDATE apiplatform_services SET plan_id = '" + planId
//				+ "' WHERE instance_id = '" + serviceInstanceId + "'";
//
//		jdbcTemplate.update(sql);
//
//		logger.info("updateAPIServiceInstance() finished");
//	}
//
//	public void deleteAPIServiceInstance(String oranizationGuid,
//			String serviceInstanceId, String serviceId, String planId) {
//
//		logger.info("deleteAPIServiceInstance() start");
//		String sql = "UPDATE apiplatform_services SET delyn = 'Y', deletetimestamp = utc_timestamp() WHERE instance_id = '"
//				+ serviceInstanceId
//				+ "' AND "
//				+ "organization_guid = '"
//				+ oranizationGuid
//				+ "' AND service_id = '"
//				+ serviceId
//				+ "' AND plan_id = '" + planId + "'";
//
//		jdbcTemplate.update(sql);
//
//		logger.info("deleteAPIServiceInstance() finished");
//	}
//
//	public void test_beforeBindingTest(String serviceInstanceId) {
//
//		String sql = "UPDATE apiplatform_services SET delyn = 'N' WHERE instance_id = '"
//				+ serviceInstanceId + "'";
//		jdbcTemplate.update(sql);
//	}
//}
