package org.openpaas.servicebroker.altibase;

import javax.sql.DataSource;

import org.openpaas.servicebroker.util.JSchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@PropertySource("classpath:datasource.properties")
public class AltibaseConfiguration {
	
	@Autowired
	private Environment env;
	
	@Autowired
	private DataSource dataSource;
	
	public @Bean JSchUtil jschUtil() {

		String serverUser = env.getRequiredProperty("altibase.server.userName");
		String serverHost = env.getRequiredProperty("altibase.server.hostName");
		int serverPort = Integer.parseInt((String)env.getRequiredProperty("altibase.server.portNumber"));
		
		// serverPassword, serverIdentity. Only one of the two
		String serverPassword = env.getRequiredProperty("altibase.server.password");
		String serverIdentity = env.getRequiredProperty("altibase.server.identity");
		
		JSchUtil jsch = new JSchUtil(serverUser, serverHost, serverPort);
		
		if( !"".equals(serverPassword) && serverPassword != null) jsch.setPassword(serverPassword);
		if( !"".equals(serverIdentity) && serverIdentity != null) jsch.setIdentity(serverIdentity);
		
		return jsch;
	}
	

	@Bean
    public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		//dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setDriverClassName("Altibase.jdbc.driver.AltibaseDriver");
		dataSource.setUrl("jdbc:Altibase://192.168.1.94:20020/mydb");
		dataSource.setUsername("sys");
		dataSource.setPassword("manager");

		return dataSource;
        // instantiate, configure and return DataSource
    }
	
	@Bean
	public JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource);
	} 
}
