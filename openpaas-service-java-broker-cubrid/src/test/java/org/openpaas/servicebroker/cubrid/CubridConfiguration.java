package org.openpaas.servicebroker.cubrid;

import org.openpaas.servicebroker.util.JSchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:datasource.properties")
public class CubridConfiguration {
	
	@Autowired
	private Environment env;
	
	public @Bean JSchUtil jschUtil() {

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
}
