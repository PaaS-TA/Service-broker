package org.openpaas.servicebroker.cubrid.config;

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
					"cubrid", 
					"Cubrid DB", 
					"A simple cubrid implementation", 
					true, 
					false,
					Arrays.asList(
							new Plan("cubrid-plan", 
									"Default cubrid Plan", 
									"This is a default cubrid plan.  All services are created equally.",
									getPlanMetadata())),
					Arrays.asList("cubrid", "document"),
					getServiceDefinitionMetadata(),
					null,
					null)));
	}
	
/* Used by Pivotal CF console */	
	
	private Map<String,Object> getServiceDefinitionMetadata() {
		Map<String,Object> sdMetadata = new HashMap<String,Object>();
		sdMetadata.put("displayName", "CubridDB");
		sdMetadata.put("imageUrl","http://www.openpaas.org/rs/cubrid/images/CubridDB_Logo_Full.png");
		sdMetadata.put("longDescription","CubridDB Service");
		sdMetadata.put("providerDisplayName","OpenPaas");
		sdMetadata.put("documentationUrl","http://www.openpaas.org");
		sdMetadata.put("supportUrl","http://www.openpaas.org");
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
		return Arrays.asList("Shared CubridDB server", 
				"100 MB Storage (not enforced)", 
				"40 concurrent connections (not enforced)");
	}
	
}