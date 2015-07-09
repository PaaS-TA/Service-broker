package org.openpaas.servicebroker.mongodb.config;

import java.net.UnknownHostException;

import org.openpaas.servicebroker.mongodb.service.impl.MongoAdminService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

@Configuration
@EnableMongoRepositories(basePackages = "org.openpaas.servicebroker.mongodb.repository")
public class MongoConfig {

	public static final String DB_NAME = "mongo-broker";

	public @Bean MongoTemplate mongoTemplate(Mongo mongo) throws UnknownHostException {
		return new MongoTemplate(mongo, DB_NAME);
	}

	public @Bean MongoClient mongoClient() throws UnknownHostException {
		return new MongoClient("localhost");
	}

	public @Bean MongoAdminService mongoAdminService() throws UnknownHostException {
		return new MongoAdminService(mongoClient());
	}

}
