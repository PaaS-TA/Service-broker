package org.openpaas.servicebroker.mongodb.config;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openpaas.servicebroker.mongodb.service.impl.MongoAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

@Configuration
@PropertySources(value = {@PropertySource("classpath:datasource.properties")})
@EnableMongoRepositories(basePackages = "org.openpaas.servicebroker.mongodb.repository")
public class MongoConfig {
	
	@Autowired
	private Environment env;
	
	public @Bean MongoTemplate mongoTemplate(Mongo mongo) throws UnknownHostException {

		String DB_NAME = env.getRequiredProperty("mongodb.dbName");
		
		return new MongoTemplate(mongo, DB_NAME);
	}

	public @Bean MongoClient mongoClient() throws UnknownHostException {
		System.out.println(env.getRequiredProperty("mongodb.hosts"));
		String[] hosts = env.getRequiredProperty("mongodb.hosts").split(", ");
		int port = Integer.parseInt(env.getRequiredProperty("mongodb.port"));
		
		MongoCredential credential = MongoCredential.createCredential(env.getRequiredProperty("mongodb.userName"),
																	env.getRequiredProperty("mongodb.authSource"),
																	env.getRequiredProperty("mongodb.password").toCharArray());
		
		List<ServerAddress> serverAddressList = new ArrayList<ServerAddress>();
		for (String host : hosts ) {
			serverAddressList.add(new ServerAddress(host, port));
		}
		return new MongoClient(serverAddressList, Arrays.asList(credential));
		
	}

	public @Bean MongoAdminService mongoAdminService() throws UnknownHostException {
		return new MongoAdminService(mongoClient());
	}

}
