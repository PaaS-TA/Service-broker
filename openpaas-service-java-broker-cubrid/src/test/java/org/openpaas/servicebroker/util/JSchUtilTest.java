package org.openpaas.servicebroker.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openpaas.servicebroker.cubrid.CubridConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CubridConfiguration.class)
public class JSchUtilTest {
	
	@Autowired
	private JSchUtil jsch;
	
	@Before public void initialize() {
		/*
		String serverUser = "cubrid";
		String serverHost = "localhost";
		int serverPort = 50000;
		
		// serverPassword, serverIdentity. Only one of the two
		String serverPassword = "cubrid";
		String serverIdentity = null;
		
		jsch = new JSchUtil(serverUser, serverHost, serverPort);
		
		if( !"".equals(serverPassword) && serverPassword != null) jsch.setPassword(serverPassword);
		if( !"".equals(serverIdentity) && serverIdentity != null) jsch.setIdentity(serverIdentity);*/
		
	}

	@Test
	public void shellTest() {
		
		
		List<String> commands = new ArrayList<String>();
		commands.add("cm_admin listdb");
		

//				". /home/cubrid/CUBRID/.cubrid.sh"
//				, "cd $CUBRID_DATABASES"
//				, "mkdir testdb"
//				, "cd testdb"
//				, "cubrid createdb --db-volume-size=100M --log-volume-size=100M testdb en_US"
//				, "cubrid service stop"
//				, "cubrid server stop testdb"
//				, "cubrid service start"
//				, "cubrid server start testdb"
//				 "ls -al"
//				"cm_admin listdb"
		
		System.out.println(jsch.shell(commands).toString());
		
		return;
	}
	
	@Test
	public void execTest() {
		
		String command = "ls -al";
		jsch.enableDebug();
		
		for ( String str : jsch.exec(command)) {
			System.out.println(str);
		}	
		
		return ;
	}

}
