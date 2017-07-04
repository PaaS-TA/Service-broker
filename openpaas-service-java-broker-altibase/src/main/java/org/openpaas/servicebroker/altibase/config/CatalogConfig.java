package org.openpaas.servicebroker.altibase.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.model.Catalog;
import org.openpaas.servicebroker.model.Plan;
import org.openpaas.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Altibase Broker의 상세정보를 정의
 * 
 * */


@Configuration
public class CatalogConfig {

	@Bean
	public Catalog catalog() {		
		return new Catalog( Arrays.asList(
				new ServiceDefinition(
						"altibase", 
						"AltibaseDB", 
						"A simple altibase implementation", 
						true, 
						false,
						getPlans(),
						Arrays.asList("altibase", "document"),
						getServiceDefinitionMetadata(),
						Arrays.asList("syslog_drain"),
						null)));
	}

	/* Used by OpenPaaS console */	

	private Map<String,Object> getServiceDefinitionMetadata() {
		Map<String,Object> sdMetadata = new HashMap<String,Object>();
		sdMetadata.put("displayName", "AltibaseDB");
		sdMetadata.put("imageUrl","http://www.openpaas.org/rs/altibase/images/AltibaseDB_Logo_Full.png");
		sdMetadata.put("longDescription","AltibaseDB Service");
		sdMetadata.put("providerDisplayName","OpenPaaS");
		sdMetadata.put("documentationUrl","http://www.openpaas.org");
		sdMetadata.put("supportUrl","http://www.openpaas.org");
		return sdMetadata;
	}

	private Map<String,Object> getPlanMetadata(String planType) {		
		Map<String,Object> planMetadata = new HashMap<String,Object>();
		planMetadata.put("costs", getCosts());
		planMetadata.put("bullets", getBullets(planType));
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

	private List<String> getBullets(String planType) {
		return Arrays.asList("Shared AltibaseDB server", 
				planType);
	}

	private List<Plan> getPlans() {
		
		List<Plan> plans = Arrays.asList(
				new Plan("5d8f7c3d-ce3d-4487-adbe-63aeab39cb5b", 
						"Altibase-Plan1-Shared-vm", 
						"This is a Altibase plan. Shared-vm.",
						getPlanMetadata("shared-vm"),
						true)/*,
				new Plan("7e361c93-70ed-4eaa-8ab5-b1b53fe8a8f4", 
						"Altibase-Plan2-Dedicated-vm", 
						"This is a Altibase plan. Dedicated-vm.",
						getPlanMetadata("dedicated-vm"),
						true)*/);
		
		return plans;
	}

}