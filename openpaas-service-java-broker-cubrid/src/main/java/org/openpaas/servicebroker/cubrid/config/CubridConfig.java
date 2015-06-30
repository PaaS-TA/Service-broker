package org.openpaas.servicebroker.cubrid.config;

import javax.sql.DataSource;

import org.openpaas.servicebroker.util.JSchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
//import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@PropertySource("classpath:datasource.properties")
public class CubridConfig {

	@Autowired
	private Environment env;
	
	@Bean
	public JSchUtil jschUtil() {

		String serverUser = env.getRequiredProperty("cubrid.server.userName");
		String serverHost = env.getRequiredProperty("cubrid.server.hostName");
		int serverPort = Integer.parseInt((String)env.getRequiredProperty("cubrid.server.portNumber"));
		
		// serverPassword, serverIdentity. Only one of the two
		String serverPassword = env.getRequiredProperty("cubrid.server.password");
		String serverIdentity = env.getRequiredProperty("cubrid.server.identity");
		
		JSchUtil jsch = new JSchUtil(serverUser, serverHost, serverPort);
		
		if( !"".equals(serverPassword) && serverPassword != null) jsch.setPassword(serverPassword);
		if( !"".equals(serverIdentity) && serverIdentity != null) jsch.setIdentity(serverIdentity);
		
		return jsch;
	}
	
	@Bean
    public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(env.getRequiredProperty("jdbc.driver"));
		dataSource.setUrl(env.getRequiredProperty("cubrid.url"));
		dataSource.setUsername(env.getRequiredProperty("cubrid.userName"));
		dataSource.setPassword(env.getRequiredProperty("cubrid.password"));

		return dataSource;
        // instantiate, configure and return DataSource
    }
	
}
