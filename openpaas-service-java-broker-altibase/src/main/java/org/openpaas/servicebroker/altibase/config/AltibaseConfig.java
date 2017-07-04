package org.openpaas.servicebroker.altibase.config;

import javax.sql.DataSource;

import org.openpaas.servicebroker.util.JSchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
//import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * 
 * Altibase Datasource를 정의 Altibase server에 ssh접속정의.
 * 
 * 
 * @author Cho Mingu
 *
 */

@Configuration
@PropertySource("classpath:datasource.properties")
public class AltibaseConfig {

	@Autowired
	private Environment env;
	
	@Bean
	public JSchUtil jschUtil() {

		String serverUser = env.getRequiredProperty("altibase.server.userName");
		String serverHost = env.getRequiredProperty("altibase.server.hostName");
		int serverPort = Integer.parseInt((String)env.getRequiredProperty("altibase.server.portNumber"));
		
		// serverPassword, serverIdentity. Only one of the two
		String serverPassword = env.getRequiredProperty("altibase.server.password");
		String serverIdentity = env.getRequiredProperty("altibase.server.identity");
		String serverSudoPassword = env.getRequiredProperty("altibase.server.sudo.password");
		
		JSchUtil jsch = new JSchUtil(serverUser, serverHost, serverPort);
		
		if( !"".equals(serverSudoPassword) && serverSudoPassword != null) jsch.setSudoPassword(serverSudoPassword);
		if( !"".equals(serverIdentity) && serverIdentity != null) jsch.setIdentity(serverIdentity);
		else jsch.setPassword(serverPassword);
		jsch.enableDebug();
		
		return jsch;
	}
	
	@Bean
    public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(env.getRequiredProperty("jdbc.driver"));
		dataSource.setUrl(env.getRequiredProperty("jdbc.url"));
		dataSource.setUsername(env.getRequiredProperty("jdbc.username"));
		dataSource.setPassword(env.getRequiredProperty("jdbc.password"));

		return dataSource;
        // instantiate, configure and return DataSource
    }
	
}
