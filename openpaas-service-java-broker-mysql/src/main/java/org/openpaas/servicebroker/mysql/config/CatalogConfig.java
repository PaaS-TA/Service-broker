package org.openpaas.servicebroker.mysql.config;

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
 * Spring boot 구동시 Catalog API 에서 사용하는 Catalog Bean 를 생성하는 클래스
 * 
 * @author 김한종
 *
 */
@Configuration
public class CatalogConfig {
	
	@Bean
	public Catalog catalog() {		
		return new Catalog( Arrays.asList(
				new ServiceDefinition(
					"96b9e707-0e2b-47e3-a21a-fd01a8eb0452", 
					"Mysql-DB", 
					"A simple mysql implementation", 
					true, 
					true,
					Arrays.asList(
							new Plan("411d0c3e-b086-4a24-b041-0aeef1a819d1", 
									"Mysql-Plan1-10con", 
									"This is a mysql plan1.  10 user connections",
									getPlanMetadata("A"),true),
							new Plan("4a932d9d-9bc5-4a86-937f-e2c14bb9f497", 
									"Mysql-Plan2-100con", 
									"This is a mysql plan2.  100 user connections",
									getPlanMetadata("B"),false)),
					Arrays.asList("mysql", "document"),
					getServiceDefinitionMetadata(),
					getRequires(),
					null)));
	}
	
/* Used by Pivotal CF console */	
	
	private Map<String,Object> getServiceDefinitionMetadata() {
		Map<String,Object> sdMetadata = new HashMap<String,Object>();
		sdMetadata.put("displayName", "MysqlDB");
		sdMetadata.put("imageUrl","http://www.openpaas.org/rs/mysql/images/MysqlDB_Logo_Full.png");
		sdMetadata.put("longDescription","MysqlDB Service");
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
			return Arrays.asList("Shared MysqlDB server", 
					"10 concurrent connections (not enforced)");
		}else if(planType.equals("B")){
			return Arrays.asList("Shared MysqlDB server", 
					"100 concurrent connections (not enforced)");
		}
		return Arrays.asList("Shared MysqlDB server", 
				"10 concurrent connections (not enforced)");
	}
	
	private List<String> getRequires() {
		
		return Arrays.asList("syslog_drain");
	}
	
	
}