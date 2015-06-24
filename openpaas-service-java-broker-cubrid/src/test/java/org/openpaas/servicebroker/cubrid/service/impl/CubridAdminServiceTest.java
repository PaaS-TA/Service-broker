package org.openpaas.servicebroker.cubrid.service.impl;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openpaas.servicebroker.cubrid.CubridConfiguration;
/*import org.openpaas.servicebroker.cubrid.config.Application;
import org.openpaas.servicebroker.cubrid.config.BrokerConfig;
import org.openpaas.servicebroker.cubrid.config.CatalogConfig;
import org.openpaas.servicebroker.cubrid.config.CubridConfig;*/
import org.openpaas.servicebroker.cubrid.exception.CubridServiceException;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.util.JSchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CubridConfiguration.class)
public class CubridAdminServiceTest {
	
	@Autowired
	private JSchUtil jsch;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private CubridAdminService cubridAdminService;
	
	@Before
	public void initialize() {
		jsch.enableDebug();
		cubridAdminService = new CubridAdminService(jsch, jdbcTemplate);
		
		String service_instance_id = "test_service_instance_id";
		String plan_id = "test_plan_id";
		String db_name = "test_db_name";
		String service_instancne_binding_id = "test_service_instance_binding_id";
		String db_user_name = "test_db_user_name";
		
		jdbcTemplate.update("insert into service_instances (guid, plan_id, db_name) values (?, ?, ?)", service_instance_id, plan_id, db_name);
		jdbcTemplate.update("insert into service_instances (guid, plan_id, db_name) values (?, ?, ?)", service_instance_id+"1", plan_id+"1", db_name+"1");
		jdbcTemplate.update("insert into service_instances (guid, plan_id, db_name) values (?, ?, ?)", service_instance_id+"2", plan_id+"2", db_name+"2");
		
		jdbcTemplate.update("insert into service_instance_bindings (guid, service_instance_id, db_user_name) values (?, ?, ?)", service_instancne_binding_id, service_instance_id, db_user_name);
		jdbcTemplate.update("insert into service_instance_bindings (guid, service_instance_id, db_user_name) values (?, ?, ?)", service_instancne_binding_id+"1", service_instance_id+"1", db_user_name+"1");
		jdbcTemplate.update("insert into service_instance_bindings (guid, service_instance_id, db_user_name) values (?, ?, ?)", service_instancne_binding_id+"2", service_instance_id+"2", db_user_name+"2");
		
	}
	
	@After
	public void closer() {
		jdbcTemplate.update("delete from service_instances");
		jdbcTemplate.update("delete from service_instance_bindings");
	}
	
	@Test
	public void deleteDatabase() throws CubridServiceException {
		ServiceInstance serviceInstance = new ServiceInstance();
		serviceInstance.setDatabaseName("test_db_name");
		
		cubridAdminService.deleteDatabase(serviceInstance );
	}
	
	@Test
	public void createDatabase() throws CubridServiceException {
		ServiceInstance serviceInstance = new ServiceInstance();
		serviceInstance.setDatabaseName("cubrid_test");
		
		cubridAdminService.createDatabase(serviceInstance );
	}
	
	@Test
	public void createUser() throws CubridServiceException {
		cubridAdminService.createUser("testdb", "testuser2", "testpass2");
	}
	
	@Test
	public void deleteUser() throws CubridServiceException {
		cubridAdminService.deleteUser("testdb", "testuser2");
	}
	
	@Test
	public void getConnectionString() {
		String database="test_db_name";
		String username="test_user";
		String password="test_passwd";
		
		String expected = "jdbc:cubrid:localhost:30000:"+database+":"+username+":"+password+":";
		assertEquals(expected, cubridAdminService.getConnectionString(database, username, password));
	}
	/*
	//private method test
	@Test
	public void getDBList() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method m = cubridAdminService.getClass().getDeclaredMethod("getDBList", null);
		System.out.println(m.invoke(cubridAdminService, null));
	}
	*/
	@Test
	@Transactional
	public void queryTest() {
		String service_instance_id = "test_service_instance_id";
		String plan_id = "test_plan_id";
		String db_name = "test_db_name";
		
		jdbcTemplate.update("insert into service_instances (guid, plan_id, db_name) values (?, ?, ?)", service_instance_id, plan_id, db_name);
		
		List<Map<String,Object>> findById = jdbcTemplate.queryForList("select * from service_instances where guid = '"+service_instance_id+"'");
		System.out.println("findById : " + findById);
		//List<String> databases = jdbcTemplate.queryForList("select db_name from service_instances where db_name = ?", db_name, String.class);
		List<String> databases = jdbcTemplate.queryForList("select db_name from service_instances where db_name = '"+db_name+"'", String.class);
		System.out.println("databases : " + databases);
		
		jdbcTemplate.update("delete from service_instances where guid = ?", service_instance_id);
	}
	
	@Test
	public void isExistsService() {
		ServiceInstance instance = new ServiceInstance();
		
		instance.setDatabaseName("test_db_name");
		assertTrue(cubridAdminService.isExistsService(instance));
		
		instance.setDatabaseName("test_db_name_not_exist");
		assertFalse(cubridAdminService.isExistsService(instance));
	}
	
	@Test
	public void isExistsUser() {
		
		assertTrue(cubridAdminService.isExistsUser("test_db_user_name"));
		assertFalse(cubridAdminService.isExistsUser("test_db_user_name_not_exist"));
	}
	
	@Test
	public void findById() {
		String service_instance_id = "test_service_instance_id";
		String plan_id = "test_plan_id";
		String db_name = "test_db_name";
		
		//jdbcTemplate.update("insert into service_instances (guid, plan_id, db_name) values (?, ?, ?)", service_instance_id, plan_id, db_name);
		
		ServiceInstance serviceInstance = cubridAdminService.findById(service_instance_id);
		/*
		System.out.println(serviceInstance.getServiceInstanceId());
		System.out.println(serviceInstance.getPlanId());
		System.out.println(serviceInstance.getDatabaseName());*/
		
		assertEquals(service_instance_id, serviceInstance.getServiceInstanceId());
		assertEquals(plan_id, serviceInstance.getPlanId());
		assertEquals(db_name, serviceInstance.getDatabaseName());
		
		//jdbcTemplate.update("delete from service_instances where guid = ?", service_instance_id);
		
	}
	
	@Test
	public void findBindById() {
		String service_instancne_binding_id = "test_service_instance_binding_id";
		String service_instance_id = "test_service_instance_id";
		String db_user_name = "test_db_user_name";
		
		ServiceInstanceBinding serviceInstanceBinding = cubridAdminService.findBindById(service_instancne_binding_id);
		
		assertEquals(service_instancne_binding_id, serviceInstanceBinding.getId());
		assertEquals(service_instance_id, serviceInstanceBinding.getServiceInstanceId());
		assertEquals(db_user_name, serviceInstanceBinding.getDatabaseUserName());
		
	}

}
