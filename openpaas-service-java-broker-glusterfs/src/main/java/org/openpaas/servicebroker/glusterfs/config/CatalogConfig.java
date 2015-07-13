package org.openpaas.servicebroker.glusterfs.config;

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
					"e5rt43e6-0e2b-47e3-a21a-fd01a8eb0452", 
					"Glusterfs", 
					"A simple glusterfs implementation", 
					true, 
					true,
					Arrays.asList(
							new Plan("ty8u76yi-b086-4a24-b041-0aeef1a819d1", 
									"Glusterfs-Plan1-5Mb", 
									"This is a glusterfs plan1.  5Mb Storage",
									getPlanMetadata("A"),true),
							new Plan("sd456f21-9bc5-4a86-937f-e2c14bb9f497", 
									"Glusterfs-Plan2-100Mb", 
									"This is a glusterfs plan2.  100Mb Storage",
									getPlanMetadata("B"),false),
							new Plan("koi908i7-9bc5-4a86-937f-e2c14bb9f497", 
									"Glusterfs-Plan3-1000Mb", 
									"This is a glusterfs plan3.  1000Mb Storage",
									getPlanMetadata("C"),false)),
					Arrays.asList("glusterfs", "document"),
					getServiceDefinitionMetadata(),
					null,
					null)));
	}
	
/* Used by Pivotal CF console */	
	
	private Map<String,Object> getServiceDefinitionMetadata() {
		Map<String,Object> sdMetadata = new HashMap<String,Object>();
		sdMetadata.put("displayName", "Glusterfs");
		sdMetadata.put("imageUrl","http://www.openpaas.org/rs/glusterfs/images/Glusterfs_Logo_Full.png");
		sdMetadata.put("longDescription","Glusterfs Service");
		sdMetadata.put("providerDisplayName","OpenPaas");
		sdMetadata.put("documentationUrl","http://www.openpaas.org");
		sdMetadata.put("supportUrl","http://www.openpaas.org");
		return sdMetadata;
	}
	
	private Map<String,Object> getPlanMetadata(String planType) {		
		Map<String,Object> planMetadata = new HashMap<String,Object>();
		planMetadata.put("costs", getCosts(planType));
		planMetadata.put("bullets", getBullets(planType));
		return planMetadata;
	}
	
	private List<Map<String,Object>> getCosts(String planType) {
		Map<String,Object> costsMap = new HashMap<String,Object>();
		
		Map<String,Object> amount = new HashMap<String,Object>();
		
		if(planType.equals("A")){
			amount.put("usd", new Double(0.0));
			costsMap.put("amount", amount);
			costsMap.put("unit", "MONTHLY");
			
		}else if(planType.equals("B")){
			amount.put("usd", new Double(10.0));
			costsMap.put("amount", amount);
			costsMap.put("unit", "MONTHLY");
			
		}else if(planType.equals("C")){
			amount.put("usd", new Double(100.0));
			costsMap.put("amount", amount);
			costsMap.put("unit", "MONTHLY");
			
		}else {
			amount.put("usd", new Double(0.0));
			costsMap.put("amount", amount);
			costsMap.put("unit", "MONTHLY");
		}
	
		
		return Arrays.asList(costsMap);
	}
	
	private List<String> getBullets(String planType) {
		if(planType.equals("A")){
			return Arrays.asList("Shared Glusterfs 5Mb", 
					"5Mb Storage Size");
		}else if(planType.equals("B")){
			return Arrays.asList("Shared Glusterfs 100Mb", 
					"100Mb Storage Size");
		}else if(planType.equals("C")){
			return Arrays.asList("Shared Glusterfs 1000Mb", 
					"1000Mb Storage Size");
		}
		return Arrays.asList("Shared Glusterfs 5Mb server", 
				"5Mb Storage Size");
	}
	
}