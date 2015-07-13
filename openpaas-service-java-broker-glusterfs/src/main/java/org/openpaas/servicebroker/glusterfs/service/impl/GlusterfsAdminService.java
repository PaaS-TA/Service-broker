package org.openpaas.servicebroker.glusterfs.service.impl;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.common.HttpClientUtils;
import org.openpaas.servicebroker.common.JsonUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.glusterfs.common.ConstantsAuthToken;
import org.openpaas.servicebroker.glusterfs.exception.GlusterfsServiceException;
import org.openpaas.servicebroker.glusterfs.model.GlusterfsServiceInstance;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * Utility class for manipulating a Glusterfs database.
 * 
 * @author 
 *
 */
@PropertySource("classpath:datasource.properties")
@Service
public class GlusterfsAdminService {

	public static final String SERVICE_INSTANCES_FILDS = "instance_id, service_id, plan_id, organization_guid, space_guid, tenant_name, tenant_id";
	
	public static final String SERVICE_INSTANCES_FIND_BY_INSTANCE_ID = "select " + SERVICE_INSTANCES_FILDS + " from gfbroker.service_instances where instance_id = ?";
	
	public static final String SERVICE_GLUSTERFS_INSTANCES_FIND_BY_INSTANCE_ID = "select instance_id, tenant_name, tenant_id from gfbroker.service_instances where instance_id = ?";
	
	public static final String SERVICE_INSTANCES_DELETE_BY_INSTANCE_ID = "delete from gfbroker.service_instances where instance_id = ?";
	
	public static final String SERVICE_INSTANCES_UPSERT_FILDS = "ON DUPLICATE KEY UPDATE instance_id = ?, service_id = ?, plan_id = ?, organization_guid = ?, space_guid = ?, tenant_name = ?, tenant_id = ?";
	
	public static final String SERVICE_INSTANCES_ADD = "insert into gfbroker.service_instances("+SERVICE_INSTANCES_FILDS+") values(?,?,?,?,?,?,?) "+ SERVICE_INSTANCES_UPSERT_FILDS;
	
	public static final String SERVICE_INSTANCES_UPDATE_FILDS = "UPDATE gfbroker.service_instances SET service_id = ?, plan_id = ?, organization_guid = ?, space_guid = ? where instance_id = ?";
	
	public static final String SERVICE_BINDING_FILDS ="binding_id, instance_id, app_id, username, password";
	
	public static final String SERVICE_BINDING_UPSERT_FILDS = "ON DUPLICATE KEY UPDATE instance_id = ?, app_id = ? ,username = ?, password = ?";
	
	public static final String SERVICE_BINDING_ADD = "insert into gfbroker.service_binding("+SERVICE_BINDING_FILDS+") values(?,?,?,?,?) "+ SERVICE_BINDING_UPSERT_FILDS;
	
	public static final String SERVICE_BINDING_FIND_BY_BINDING_ID = "select " + SERVICE_BINDING_FILDS + " from gfbroker.service_binding where binding_id = ?";
	
	public static final String SERVICE_BINDING_FIND_BY_INSTANCE_ID = "select " + SERVICE_BINDING_FILDS + " from gfbroker.service_binding where instance_id = ?";
	
	public static final String SERVICE_BINDING_DELETE_BY_BINDING_ID = "delete from gfbroker.service_binding where binding_id = ?";
	
	public static final String SERVICE_BINDING_FIND_USERNAME_BY_BINDING_ID = "select username from gfbroker.service_binding where binding_id = ?";
	
	// Plan별 MAX_USER_CONNECTIONS 정보
	public static String planA = "ty8u76yi-b086-4a24-b041-0aeef1a819d1";
	public static int planAsize = 1024 * 1024 * 5;
	public static String planB = "sd456f21-9bc5-4a86-937f-e2c14bb9f497";
	public static int planBsize = 1024 * 1024 * 100;
	public static String planC = "koi908i7-9bc5-4a86-937f-e2c14bb9f497";
	public static int planCsize = 1024 * 1024 * 1000;
	
	static String TENANT_PREFIX = "op_";
	
	private Logger logger = LoggerFactory.getLogger(GlusterfsAdminService.class);
	
	@Autowired
	private Environment env;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	public GlusterfsAdminService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
		
	private final RowMapper<ServiceInstance> mapper = new ServiceInstanceRowMapper();
	
	private final RowMapper<GlusterfsServiceInstance> mapper3 = new GlusterfsServiceInstanceRowMapper();
	
	private final RowMapper<ServiceInstanceBinding> mapper2 = new ServiceInstanceBindingRowMapper();
	
	/**
	 * ServiceInstance의 유무를 확인합니다
	 * @param instance
	 * @return
	 */
	/*public boolean isExistsService(ServiceInstance instance){
		System.out.println("GlusterfsAdminService.isExistsService");
		//List<String> databases = jdbcTemplate.queryForList("SHOW DATABASES LIKE '"+getDatabase(instance.getServiceInstanceId())+"'",String.class);
		List<String> databases = null;
		return databases.size() > 0;
	}*/
	
	/**
	 * 사용자 유무를 확인합니다.
	 * @param userId
	 * @return
	 */
	/*public boolean isExistsUser(String userId){
		System.out.println("GlusterfsAdminService.isExistsUser");
		try {
			jdbcTemplate.execute("SHOW GRANTS FOR '"+userId+"'");
		} catch (Exception e) {
			return false;
		}
		return true;
	}*/
	
	/**
	 * ServiceInstanceId로 ServiceInstance정보를 조회합니다.
	 * @param id
	 * @return
	 */
	public ServiceInstance findById(String id){
		System.out.println("GlusterfsAdminService.findById");
		ServiceInstance serviceInstance = null;;
		try {
			serviceInstance = jdbcTemplate.queryForObject(SERVICE_INSTANCES_FIND_BY_INSTANCE_ID, mapper, id);
			serviceInstance.withDashboardUrl(getDashboardUrl(serviceInstance.getServiceInstanceId()));
		} catch (Exception e) {
		}
		return serviceInstance;
	}
	
	/**
	 * ServiceInstanceId로 tenantInfo정보를 조회합니다.
	 * @param id
	 * @return
	 */
	public GlusterfsServiceInstance tenantInfofindById(String id){
		System.out.println("GlusterfsAdminService.tenantInfofindById");
		GlusterfsServiceInstance serviceInstance = null;;
		try {
			serviceInstance = jdbcTemplate.queryForObject(SERVICE_GLUSTERFS_INSTANCES_FIND_BY_INSTANCE_ID, mapper3, id);
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
		System.out.println("GlusterfsAdminService.createServiceInstanceByRequest");
		return new ServiceInstance(request).withDashboardUrl(getDashboardUrl(request.getServiceInstanceId()));
	}
	
	/**
	 * ServiceInstanceBindingId로 ServiceInstanceBinding정보를 조회합니다.
	 * @param id
	 * @return
	 */
	public ServiceInstanceBinding findBindById(String id){
		System.out.println("GlusterfsAdminService.findBindById");
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
		System.out.println("GlusterfsAdminService.findBindByInstanceId");
		
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
		System.out.println("GlusterfsAdminService.createServiceInstanceBindingByRequest");
		
		return new ServiceInstanceBinding(request.getBindingId(), 
				request.getServiceInstanceId(), 
				new HashMap<String, Object>(), 
				"syslogDrainUrl", 
				request.getAppGuid());
	}
	
	/**
	 * ServiceInstance 정보를 저장합니다.
	 * @param serviceInstance
	 * @throws GlusterfsServiceException
	 */
	public void save(ServiceInstance serviceInstance, GlusterfsServiceInstance gf) throws GlusterfsServiceException{
		try{
			System.out.println("GlusterfsAdminService.save");
			jdbcTemplate.update(SERVICE_INSTANCES_ADD, 
					serviceInstance.getServiceInstanceId(),
					serviceInstance.getServiceDefinitionId(),
					serviceInstance.getPlanId(),
					serviceInstance.getOrganizationGuid(),
					serviceInstance.getSpaceGuid(),
					gf.getTenantName(),
					gf.getTenantId(),
					serviceInstance.getServiceInstanceId(),
					serviceInstance.getServiceDefinitionId(),
					serviceInstance.getPlanId(),
					serviceInstance.getOrganizationGuid(),
					serviceInstance.getSpaceGuid(),
					gf.getTenantName(),
					gf.getTenantId());
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	/**
	 * ServiceInstanceId로 ServiceInstance정보를 삭제합니다.
	 * @param id
	 * @throws GlusterfsServiceException
	 */
	public void delete(String id) throws GlusterfsServiceException{
		try{
			System.out.println("GlusterfsAdminService.delete");
			
			jdbcTemplate.update(SERVICE_INSTANCES_DELETE_BY_INSTANCE_ID, id);
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	/**
	 * 해당하는 Database를 삭제합니다.
	 * @param serviceInstance
	 * @throws GlusterfsServiceException
	 */
	public void deleteTenant(ServiceInstance serviceInstance) throws ServiceBrokerException{
		
		
		GlusterfsServiceInstance gfInstance =  tenantInfofindById(serviceInstance.getServiceInstanceId());
		
		if(null == gfInstance) throw new ServiceBrokerException("");
		
		try{
			System.out.println("GlusterfsAdminService.deleteDatabase");
			
			HttpHeaders headers = new HttpHeaders();	
			headers.set("X-Auth-Token", ConstantsAuthToken.AUTH_TOKEN);
			String body = 	"";
			HttpEntity<String> entity = new HttpEntity<String>(body, headers);
			ResponseEntity<String> response = null;
			
			try{
					
				String url = env.getProperty("glusterfs.endpoint") + env.getProperty("glusterfs.uri.deletetenant");

				url = url.replace("#TENANT_ID", gfInstance.getTenantId());
				
				response = HttpClientUtils.send(url, entity, HttpMethod.DELETE);
				
				if (response.getStatusCode() != HttpStatus.NO_CONTENT) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

			} catch (Exception e) {
				e.printStackTrace();
				if (e.getMessage().equals("401 Unauthorized")) {
					setGlusterfsAuthToken();
					deleteTenant(serviceInstance);
				}else{ // if(e.getMessage().equals("API Platform Error: API Platform Server Not Found")){
					throw new ServiceBrokerException("Tennant exception occurred during deletion.");
				}
			}
		} catch (Exception e) {
			throw handleException(e);
		}
	}
	
	/**
	 * 해당하는 Database를 생성합니다.
	 * @param serviceInstance
	 * @throws GlusterfsServiceException
	 */
	/*public void createDatabase(ServiceInstance serviceInstance) throws GlusterfsServiceException{
		
		try{
			
			System.out.println("GlusterfsAdminService.createDatabase");
			//jdbcTemplate.execute("CREATE DATABASE " + getDatabase(serviceInstance.getServiceInstanceId()));
		} catch (Exception e) {
			throw handleException(e);
		}
	}*/
	
	public void setGlusterfsAuthToken() throws GlusterfsServiceException{
		
		System.out.println("GlusterfsAdminService.getGlusterfsAuthToken");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		String body = 	"{" + 
							"\"auth\": " +
							    "{" + 
								"\"tenantName\": \"" + env.getProperty("glusterfs.tenantname") + "\"," + 
								"\"passwordCredentials\": " + 
								"{" + 
									"\"username\": \"" + env.getProperty("glusterfs.username") + "\"," + 
									"\"password\": \"" + env.getProperty("glusterfs.password") + "\"" + 
									"}" + 
								"}" + 
						"}";
		
		HttpEntity<String> entity = new HttpEntity<String>(body, headers);
		ResponseEntity<String> response = null;
		
		try{
				
			String url = env.getProperty("glusterfs.authurl") + env.getProperty("glusterfs.uri.auth");
			
			response = HttpClientUtils.send(url, entity, HttpMethod.POST);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

			JsonNode json = JsonUtils.convertToJson(response);
			
			setAuthToken(json);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw handleException(e);
		}
	}

	private void setAuthToken(JsonNode json) {
		System.out.println("json : " + json.toString());
		
		String authToken = json.get("access").get("token").get("id").asText();
		
		System.out.println("authToken : " + authToken);
		
		ConstantsAuthToken.AUTH_TOKEN = authToken;
		
	}
	
	public void setGlusterfsQuota(String planId, String tenantId) throws GlusterfsServiceException{
		
		System.out.println("GlusterfsAdminService.getGlusterfsAuthToken");
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.set("X-Auth-Token", ConstantsAuthToken.AUTH_TOKEN);
		headers.set("X-Account-Meta-Quota-Bytes", planAsize+"");
		if(planId.equals(planB)) headers.set("X-Account-Meta-Quota-Bytes", planBsize+"");
		if(planId.equals(planC)) headers.set("X-Account-Meta-Quota-Bytes", planCsize+"");
		String body = 	"";
		
		HttpEntity<String> entity = new HttpEntity<String>(body, headers);
		ResponseEntity<String> response = null;
		System.out.println("body : " + body);
		try{
				
			String url = env.getProperty("glusterfs.swiftproxy") + env.getProperty("glusterfs.uri.account");
			System.out.println("url : " + url);
			url = url.replace("#TENANT_ID", tenantId);
			
			response = HttpClientUtils.send(url, entity, HttpMethod.POST);
			
			if (response.getStatusCode() != HttpStatus.NO_CONTENT) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage().equals("403 Forbidden")) {
				setGlusterfsAuthToken();
				setGlusterfsQuota(planId, tenantId);
			}else { 
				throw new GlusterfsServiceException("Tennant exception occurred during update.");
			}
		}
	}

	public String getGlusterfsUserIdByUserName(String username) throws GlusterfsServiceException{
		
		System.out.println("GlusterfsAdminService.getGlusterfsAuthToken");
		HttpHeaders headers = new HttpHeaders();	
		headers.set("X-Auth-Token", ConstantsAuthToken.AUTH_TOKEN);
		String body = 	"";
		String userId = "";
		HttpEntity<String> entity = new HttpEntity<String>(body, headers);
		ResponseEntity<String> response = null;
		System.out.println("body : " + body);
		
		try{
				
			String url = env.getProperty("glusterfs.endpoint") + env.getProperty("glusterfs.uri.userinfo");

			url = url.replace("#USER_NAME", username);
			
			response = HttpClientUtils.send(url, entity, HttpMethod.GET);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

			JsonNode json = JsonUtils.convertToJson(response);
			
			userId = getUserId(json);
			
		} catch (Exception e) {
			if (e.getMessage().equals("401 Unauthorized")) {
				System.out.println("setGlusterfsAuthTokenaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
				setGlusterfsAuthToken();
				userId = getGlusterfsUserIdByUserName(username);
			}else { 
				throw new GlusterfsServiceException("UserInfo exception occurred during search.");
			}
		}
		return userId;
	}

	private String getUserId(JsonNode json) {
		System.out.println("json : " + json.toString());
		
		String userId = json.get("user").get("id").asText();
		
		return userId;
		
	}
	
	public GlusterfsServiceInstance createTenant(ServiceInstance serviceInstance) throws GlusterfsServiceException{
		
		System.out.println("GlusterfsAdminService.createTenant");
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Auth-Token", ConstantsAuthToken.AUTH_TOKEN);
		String body = 	"{" + 
				"\"tenant\": " +
				    "{" + 
					"\"name\": \"" + getTenantName(serviceInstance.getServiceInstanceId()) + "\"," + 
					"\"description\": \"Glusterfs Service\"," + 
					"\"enabled\": true" + 
					"}" + 
			"}";
		HttpEntity<String> entity = new HttpEntity<String>(body, headers);
		ResponseEntity<String> response = null;
		System.out.println("body : " + body);
		GlusterfsServiceInstance glusterfsServiceInstance = null;
		
		try{
				
			String url = env.getProperty("glusterfs.endpoint") + env.getProperty("glusterfs.uri.createtenant");
			System.out.println("url : " + url);
			
			response = HttpClientUtils.send(url, entity, HttpMethod.POST);
			
			JsonNode json = JsonUtils.convertToJson(response);
			
			glusterfsServiceInstance = new GlusterfsServiceInstance(serviceInstance);
			
			setTenantInfo(json,glusterfsServiceInstance);
			
			setGlusterfsQuota(serviceInstance.getPlanId(), glusterfsServiceInstance.getTenantId());
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException e) {
			e.printStackTrace();
			if (e.getMessage().equals("401 Unauthorized")) {
				setGlusterfsAuthToken();
				glusterfsServiceInstance = createTenant(serviceInstance);
			}else if(e.getMessage().equals("409 Conflict")){
				throw new GlusterfsServiceException("Tennant exception occurred during creation.(duplicated)");
			}else { 
				throw new GlusterfsServiceException("Tennant exception occurred during creation.");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return glusterfsServiceInstance;
	}
	
	private void setTenantInfo(JsonNode json, GlusterfsServiceInstance glusterfsServiceInstance) {
		System.out.println("json : " + json.toString());
		
		glusterfsServiceInstance.setTenantId(json.get("tenant").get("id").asText());
		glusterfsServiceInstance.setTenantName(json.get("tenant").get("name").asText());
		
	}
	
	/**
	 * ServiceInstance의 Plan 정보를 수정합니다.
	 * @param instance
	 * @param request
	 * @throws GlusterfsServiceException
	 */
	public void updatePlan(ServiceInstance instance, ServiceInstance request) throws GlusterfsServiceException{
		try{
			System.out.println("GlusterfsAdminService.updatePlan");
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
	/*public boolean updateUserConnections(String database, String userId, String connections){
		System.out.println("GlusterfsAdminService.updateUserConnections");
		try {
			jdbcTemplate.execute("GRANT USAGE ON " + database + ".* TO '" + userId + "'@'%' WITH MAX_USER_CONNECTIONS " + connections);
		} catch (Exception e) {
			return false;
		}
		return true;
	}*/
	
	/**
	 * ServiceInstanceBinding 정보를 저장합니다.
	 * @param serviceInstanceBinding
	 * @throws GlusterfsServiceException
	 */
	public void saveBind(ServiceInstanceBinding serviceInstanceBinding) throws GlusterfsServiceException{
		try{
			System.out.println("GlusterfsAdminService.saveBind");
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
	 * @throws GlusterfsServiceException
	 */
	public void deleteBind(String id) throws GlusterfsServiceException{
		try{
			System.out.println("GlusterfsAdminService.deleteBind");
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
	 * @throws GlusterfsServiceException
	 */
	public void createUser(String tenantId, String userId, String password) throws GlusterfsServiceException{
		System.out.println("GlusterfsAdminService.createTenant");
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Auth-Token", ConstantsAuthToken.AUTH_TOKEN);
		String body = 	"{" +
					    	"\"user\": {" +
					    		"\"name\": \"" + userId + "\"," +
						        "\"email\": \"\"," +
						        "\"enabled\": true," +
						        "\"password\": \"" + password + "\"," +
						        "\"tenantId\" : \"" + tenantId + "\"" +
						     "}" +
						"}";
		HttpEntity<String> entity = new HttpEntity<String>(body, headers);
		ResponseEntity<String> response = null;
		System.out.println("body : " + body);
		
		try{
				
			String url = env.getProperty("glusterfs.endpoint") + env.getProperty("glusterfs.uri.createusers");
			System.out.println("url : " + url);
			
			response = HttpClientUtils.send(url, entity, HttpMethod.POST);
			
			if (response.getStatusCode() != HttpStatus.OK) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException e) {
			e.printStackTrace();
			if (e.getMessage().equals("401 Unauthorized")) {
				setGlusterfsAuthToken();
				createUser(tenantId, userId, password);
			}else if(e.getMessage().equals("409 Conflict")){
				throw new GlusterfsServiceException("Tennant exception occurred during creation.(duplicated)");
			}else { 
				throw new GlusterfsServiceException("Tennant exception occurred during creation.");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw handleException(e);
		} 
	}
	
	//public void deleteUser(String database, String userId) throws GlusterfsServiceException{
	/**
	 * 해당하는 User를 삭제합니다.
	 * @param database
	 * @param bindingId
	 * @throws GlusterfsServiceException
	 */
	public void deleteUser(String instanceId, String bindingId) throws GlusterfsServiceException{
		System.out.println("GlusterfsAdminService.createTenant");
		
		String userId = getGlusterfsUserIdByUserName(getUsername(bindingId));
		
		HttpHeaders headers = new HttpHeaders();	
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.set("X-Auth-Token", ConstantsAuthToken.AUTH_TOKEN);
		String body = 	"";
		HttpEntity<String> entity = new HttpEntity<String>(body, headers);
		ResponseEntity<String> response = null;
		System.out.println("body : " + body);
		
		try{
				
			String url = env.getProperty("glusterfs.endpoint") + env.getProperty("glusterfs.uri.deleteusers");
			System.out.println("url : " + url);
			System.out.println("userId : " + userId);
			url = url.replace("#USER_ID", userId);
			response = HttpClientUtils.send(url, entity, HttpMethod.DELETE);
			
			if (response.getStatusCode() != HttpStatus.NO_CONTENT) throw new ServiceBrokerException("Response code is " + response.getStatusCode());

		} catch (ServiceBrokerException e) {
			e.printStackTrace();
			if (e.getMessage().equals("401 Unauthorized")) {
				setGlusterfsAuthToken();
				deleteUser(instanceId, bindingId);
			}else { 
				throw new GlusterfsServiceException("Tennant exception occurred during creation.");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw handleException(e);
		} 
	}
	
	/**
	 * 해당하는 User를 삭제합니다.
	 * @param database
	 * @param bindingId
	 * @throws GlusterfsServiceException
	 */
	/*public void deleteUser(String userId) throws GlusterfsServiceException{
		try{
			System.out.println("GlusterfsAdminService.deleteUser");
			jdbcTemplate.execute("DROP USER '"+userId+"'@'%'");
		} catch (Exception e) {
			throw handleException(e);
		}
	}*/
	
	/*public String getConnectionString(String database, String username, String password) {
		//glusterfs://b5d435f40dd2b2:ebfc00ac@us-cdbr-east-03.cleardb.com:3306/ad_c6f4446532610ab
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
	/*public String getConnectionString(String database, String username, String password, String hostName) {
		//glusterfs://ns4VKg4Xtoy5mNCo:KyNRTVYPJyoqG1xo@10.30.40.163:3306/cf_dd2e0ffe_2bab_4308_b191_7d8814b16933
		StringBuilder builder = new StringBuilder();
		builder.append("glusterfs://"+username+":"+password+"@"+hostName+":3306/"+database);
		return builder.toString();
	}*/
	
	/*public String getServerAddresses() {
		StringBuilder builder = new StringBuilder();
		return builder.toString();
	}*/
	
	private GlusterfsServiceException handleException(Exception e) {
		logger.warn(e.getLocalizedMessage(), e);
		return new GlusterfsServiceException(e.getLocalizedMessage());
	}
	
	// DashboardUrl 생성
	public String getDashboardUrl(String instanceId){
		
		return "http://www.sample.com/"+instanceId;
	}
	
	// Tenant
	public String getTenantName(String id){
		
		String database;
		String s = id.replaceAll("-", "_");
	    database = TENANT_PREFIX+getUsername(s);
	    
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
	/*public void setUserConnections(String planId, String id) throws ServiceBrokerException{
		
		 Plan 정보 설정 
		int totalConnections = 0;
		int totalUsers;
		int userPerConnections;
		int mod;
		
		//if(planA.equals(planId)) totalConnections = planAconnections;
		//if(planB.equals(planId)) totalConnections = planBconnections;
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
				//updateUserConnections(getDatabase((String)tmp.get("instance_id")), getUsername((String)tmp.get("binding_id")), (userPerConnections+mod)+"");
				//System.out.println(tmp.get("instance_id")+"/"+tmp.get("binding_id")+"/"+userPerConnections);
			}
			//  User당 connection 수  할당
			else{
				//updateUserConnections(getDatabase((String)tmp.get("instance_id")), getUsername((String)tmp.get("binding_id")), userPerConnections+"");
				//System.out.println(tmp.get("instance_id")+"/"+tmp.get("binding_id")+"/"+userPerConnections);
			}
		}
	}*/
	
	// User MAX_USER_CONNECTIONS 설정 조정
	/*public boolean checkUserConnections(String planId, String id) throws ServiceBrokerException{
			
		 Plan 정보 설정 
		int totalConnections = 0;
		int totalUsers;
		
		//if(planA.equals(planId)) totalConnections = planAconnections;
		//if(planB.equals(planId)) totalConnections = planBconnections;
		if(!planA.equals(planId) && !planB.equals(planId)) throw new ServiceBrokerException("");
		
		// ServiceInstanceBinding 정보를 조회한다.
		List<Map<String,Object>> list = findBindByInstanceId(id);
		// ServiceInstance의 총 Binding 건수 확인
		totalUsers = list.size();
		
		if(totalConnections <= totalUsers) return true;
		
		return false;
	}*/
	
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
	
	private static final class GlusterfsServiceInstanceRowMapper implements RowMapper<GlusterfsServiceInstance> {
        @Override
        public GlusterfsServiceInstance mapRow(ResultSet rs, int rowNum) throws SQLException {
        	GlusterfsServiceInstance instance = new GlusterfsServiceInstance();
        	instance.setServiceInstanceId(rs.getString(1));
        	instance.setTenantName(rs.getString(2));
        	instance.setTenantId(rs.getString(3));
            return instance;
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
