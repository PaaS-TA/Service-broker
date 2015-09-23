package org.openpaas.servicebroker.mongodb.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.model.Catalog;
import org.openpaas.servicebroker.model.Plan;
import org.openpaas.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogConfig {

	@Bean
	public Catalog catalog() {		
		return new Catalog( Arrays.asList(
				new ServiceDefinition(
					"Mongo-DB", 
					"Mongo-DB", 
					"A simple mongo implementation", 
					true, 
					false,
					Arrays.asList(
							new Plan("default-plan", 
									"default-plan", 
									"This is a default mongo plan.  All services are created equally.",
									getPlanMetadata(),
									true)),
					Arrays.asList("mongodb", "document"),
					getServiceDefinitionMetadata(),
					Arrays.asList("syslog_drain"),
					null)));
	}
	
/* Used by Pivotal CF console */	
	
	private Map<String,Object> getServiceDefinitionMetadata() {
		Map<String,Object> sdMetadata = new HashMap<String,Object>();
		sdMetadata.put("displayName", "MongoDB");
		sdMetadata.put("imageUrl","http://info.mongodb.com/rs/mongodb/images/MongoDB_Logo_Full.png");
		sdMetadata.put("longDescription","MongodDB Service");
		sdMetadata.put("providerDisplayName","OpanPaaS");
		sdMetadata.put("documentationUrl","http://www.mongodb.org");
		sdMetadata.put("supportUrl","http://www.mongodb.org");
		return sdMetadata;
	}
	
	private Map<String,Object> getPlanMetadata() {		
		Map<String,Object> planMetadata = new HashMap<String,Object>();
		planMetadata.put("costs", getCosts());
		planMetadata.put("bullets", getBullets());
		return planMetadata;
	}
	
	private List<Map<String,Object>> getCosts() {
		Map<String,Object> costsMap = new HashMap<String,Object>();
		
		Map<String,Object> amount = new HashMap<String,Object>();
		amount.put("usd", new Double(0.0));
	
		costsMap.put("amount", amount);
		costsMap.put("unit", "MONTHLY");
		
		return Arrays.asList(costsMap);
	}
	
	private List<String> getBullets() {
		return Arrays.asList("Shared MongoDB server");
	}

}