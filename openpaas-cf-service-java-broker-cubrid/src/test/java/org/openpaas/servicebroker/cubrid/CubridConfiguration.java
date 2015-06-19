package org.openpaas.servicebroker.cubrid;

import javax.sql.DataSource;




import org.openpaas.servicebroker.util.JSchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@PropertySource("classpath:datasource.properties")
public class CubridConfiguration {
	
	@Autowired
	private Environment env;
	
	public @Bean JSchUtil jschUtil() {

		String serverUser = env.getRequiredProperty("server.userName");
		String serverHost = env.getRequiredProperty("server.hostName");
		int serverPort = Integer.parseInt((String)env.getRequiredProperty("server.portNumber"));
		
		// serverPassword, serverIdentity. Only one of the two
		String serverPassword = env.getRequiredProperty("server.password");
		String serverIdentity = env.getRequiredProperty("server.identity");
		
		JSchUtil jsch = new JSchUtil(serverUser, serverHost, serverPort);
		
		if( !"".equals(serverPassword) && serverPassword != null) jsch.setPassword(serverPassword);
		if( !"".equals(serverIdentity) && serverIdentity != null) jsch.setIdentity(serverIdentity);
		
		return jsch;
	}
/*	
	public @Bean DataSource dataSource() {
		String	driver = "cubrid.jdbc.driver.CUBRIDDriver";
		String	url =  "jdbc:cubrid:54.186.246.247::testdb:::";
		String	username = "dba";
		String	password = "dba";
		DriverManagerDataSource dataSource = new DriverManagerDataSource();

		dataSource.setDriverClassName(env.getRequiredProperty("jdbc.driver"));
		dataSource.setUrl(env.getRequiredProperty("cubird.url"));
		dataSource.setUsername(env.getRequiredProperty("cubird.username"));
		dataSource.setPassword(env.getRequiredProperty("cubird.password"));
		
		return dataSource;
		// instantiate, configure and return DataSource
	}
	*/
}
