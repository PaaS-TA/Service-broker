package org.openpaas.servicebroker.cubrid.service.impl;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openpaas.servicebroker.cubrid.CubridConfiguration;
/*import org.openpaas.servicebroker.cubrid.config.Application;
import org.openpaas.servicebroker.cubrid.config.BrokerConfig;
import org.openpaas.servicebroker.cubrid.config.CatalogConfig;
import org.openpaas.servicebroker.cubrid.config.CubridConfig;*/
import org.openpaas.servicebroker.cubrid.exception.CubridServiceException;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.util.JSchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CubridConfiguration.class)
public class CubridAdminServiceTest {
	
	@Autowired
	private JSchUtil jsch;

	private CubridAdminService cubridAdminService;

	private String database;
	private String username;
	private String password;
	

	@Before
	public void initialize() {
		jsch.enableDebug();
		cubridAdminService = new CubridAdminService(jsch);
		
		database = "testdb";
		username = "testuser";
		password = "testpass";
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
		String expected = "jdbc:cubrid:localhost:30000:"+database+":"+username+":"+password+":";
		
		assertEquals(expected, cubridAdminService.getConnectionString(database, username, password));
		
		
		
	}
	
	//private method test
	public @Test void getDBList() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		Method m = cubridAdminService.getClass().getDeclaredMethod("getDBList", null);
		
		System.out.println(m.invoke(cubridAdminService, null));
		
	}

}
