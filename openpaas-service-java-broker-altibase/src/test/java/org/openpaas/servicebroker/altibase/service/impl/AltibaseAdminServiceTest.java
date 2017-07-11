package org.openpaas.servicebroker.altibase.service.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openpaas.servicebroker.altibase.AltibaseConfiguration;
import org.openpaas.servicebroker.altibase.exception.AltibaseServiceException;
import org.openpaas.servicebroker.altibase.model.AltibaseServiceInstance;
import org.openpaas.servicebroker.altibase.service.impl.AltibaseAdminService;
import org.openpaas.servicebroker.util.JSchUtil;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AltibaseConfiguration.class)
//@Ignore
public class AltibaseAdminServiceTest {
	
	@Autowired
	private JSchUtil jsch;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private AltibaseAdminService altibaseAdminService;
	
	@Before
	public void initialize() {
		jsch.enableDebug();
		altibaseAdminService = new AltibaseAdminService(jsch, jdbcTemplate);
		
	}
	
	@After
	public void closer() {
		
	}
	
	@Test
	public void createDatabase() throws AltibaseServiceException {
		AltibaseServiceInstance serviceInstance = new AltibaseServiceInstance();
		serviceInstance.setServiceInstanceId("test_service_instance_id");
		serviceInstance.setNodeId(1);
		serviceInstance.setPlanId("5d8f7c3d-ce3d-4487-adbe-63aeab39cb5b");
		altibaseAdminService.getNode(serviceInstance );
	}
	
	@Test
	public void deleteDatabase() throws AltibaseServiceException {
		AltibaseServiceInstance serviceInstance = new AltibaseServiceInstance();
		serviceInstance.setNodeId(1);
		
		altibaseAdminService.freeNode(serviceInstance );
	}
	
//	@Test
//	public void createUser() throws AltibaseServiceException {
//		altibaseAdminService.createUser("testuser2", "testpass2");
//	}
//	
//	@Test
//	public void deleteUser() throws AltibaseServiceException {
//		altibaseAdminService.deleteUser("testuser2");
//	}
//	
	@Test
	public void getConnectionString() {
		String database="test_db_name";
		String username="test_user";
		String password="test_passwd";
		String host="localhost";
		String port="20030";
		
		String expected = "jdbc:Altibase://"+host+":"+port+"/mydb?user="+username+"&password="+password;
		assertEquals(expected, altibaseAdminService.getConnectionString(username, password, host, port));
	}
	
	@Test
	public void isExistsService() throws AltibaseServiceException {
		AltibaseServiceInstance instance = new AltibaseServiceInstance();
		
		//instance.setDatabaseName("test_db_name");
		assertTrue(altibaseAdminService.isExistsService(instance));
		
		//instance.setDatabaseName("test_db_name_not_exist");
		assertFalse(altibaseAdminService.isExistsService(instance));
	}
	
	@Test
	public void isExistsUser() throws AltibaseServiceException {
		
		//assertTrue(altibaseAdminService.isExistsUser("test_db_name", "test_db_user_name"));
		//assertFalse(altibaseAdminService.isExistsUser("test_db_name", "test_db_user_name_not_exist"));
	}
	
	@Test
	public void findById() throws AltibaseServiceException {
		String service_instance_id = "test_service_instance_id";
		String plan_id = "5d8f7c3d-ce3d-4487-adbe-63aeab39cb5b";
		String db_name = "test_db_name";
		
		AltibaseServiceInstance serviceInstance = altibaseAdminService.findById(service_instance_id);
		
		assertEquals(service_instance_id, serviceInstance.getServiceInstanceId());
		assertEquals(plan_id, serviceInstance.getPlanId());
		//assertEquals(db_name, serviceInstance.getDatabaseName());
		
	}
	
	@Test
	public void findBindById() throws AltibaseServiceException {
		String service_instancne_binding_id = "test_service_instance_binding_id";
		String service_instance_id = "test_service_instance_id";
		String db_user_name = "test_db_user_name";
		
		ServiceInstanceBinding serviceInstanceBinding = altibaseAdminService.findBindById(service_instancne_binding_id);
		
		assertEquals(service_instancne_binding_id, serviceInstanceBinding.getId());
		assertEquals(service_instance_id, serviceInstanceBinding.getServiceInstanceId());
		
	}
	
	@Test public void getServiceAddress() {
		StringBuilder builder = new StringBuilder();
		
		DriverManagerDataSource ds = (DriverManagerDataSource) jdbcTemplate.getDataSource();
		
		String[] split = ds.getUrl().split(":");
		
		builder.append(jsch.getHostname());
		builder.append(":");
		builder.append("30000");
		builder.append(":");
		
		System.out.println(split[3]);
	}

}
