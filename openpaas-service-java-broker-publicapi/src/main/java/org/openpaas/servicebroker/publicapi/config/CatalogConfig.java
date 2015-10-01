package org.openpaas.servicebroker.publicapi.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.model.Catalog;
import org.openpaas.servicebroker.model.Plan;
import org.openpaas.servicebroker.model.ServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


@Configuration
public class CatalogConfig {
	
	@Autowired
	private Environment env;
	
	public int sNumber =1;
	public int pNumber =1;
	
	@Bean
	public Catalog catalog() {
		
		List<String> serviceNames = getServiceNames();
		List<ServiceDefinition> serviceDefs = new ArrayList<ServiceDefinition>();	
		List<String> requires =new ArrayList<String>();
		requires.add("syslog_drain");
		for(sNumber=1;sNumber<=serviceNames.size();sNumber++){
			serviceDefs.add(
					new ServiceDefinition(
						"Service"+sNumber+" "+env.getProperty("Service"+sNumber+".Name")+" ServiceID",
						env.getProperty("Service"+sNumber+".Name"),
						getServiceDescription(),
						true, 
						false,
						getPlans(),
						Arrays.asList("Public API Service"),
						getServiceDefinitionMetadata(),
						requires,
						null
					)
				);		
			}
		return new Catalog(serviceDefs);
	}

	private Map<String,Object> getServiceDefinitionMetadata() {
		Map<String,Object> sdMetadata = new HashMap<String,Object>();
		sdMetadata.put("displayName", env.getProperty("Service"+sNumber+".Name"));
		sdMetadata.put("imageUrl","no image");
		sdMetadata.put("longDescription",env.getProperty("Service"+sNumber+".Description"));
		sdMetadata.put("providerDisplayName",env.getProperty("Service"+sNumber+".Provider"));
		sdMetadata.put("documentationUrl",env.getProperty("Service"+sNumber+".DocumentationUrl"));
		sdMetadata.put("supportUrl",env.getProperty("SupportUrl"));
		return sdMetadata;
	}

	private Map<String,Object> getPlanMetadata(String planName) {		
		Map<String,Object> planMetadata = new HashMap<String,Object>();
		planMetadata.put("costs", getCosts());
		planMetadata.put("bullets", getBullets());
		return planMetadata;
	}

	private List<Map<String,Object>> getCosts() {
		Map<String,Object> costsMap = new HashMap<String,Object>();
		Map<String,Object> amount = new HashMap<String,Object>();
		amount.put("KRW", new Double(0.0));
		costsMap.put("amount", amount);
		costsMap.put("unit", env.getProperty("Service"+sNumber+".Plan"+pNumber+".Unit"));

		return Arrays.asList(costsMap);
	}

	private List<String> getBullets() {
		return Arrays.asList(env.getProperty("Service"+sNumber+".Plan"+pNumber+".Bullet"));
	}

	private List<Plan> getPlans() {

		List<Plan> plans = new ArrayList<Plan>();
		List<String> planNames = getPlanNames();

		for(pNumber=1;pNumber<=planNames.size();pNumber++){
			Map<String,Object> planMetadata = getPlanMetadata(env.getProperty("Service"+sNumber+".Plan"+pNumber+".Name"));
			plans.add(
				new Plan(
					"Service"+sNumber+" "+env.getProperty("Service"+sNumber+".Name")+" "+"Plan"+pNumber+" "+env.getProperty("Service"+sNumber+".Plan"+pNumber+".Name")+" PlanID",
					env.getProperty("Service"+sNumber+".Plan"+pNumber+".Name"),
					getPlanDescription(),
					planMetadata,
					isItFree(planMetadata)
				)			
			);
		}
		return plans;
	}
	
	private boolean isItFree(Map<String,Object> planMetadata){
		boolean isItFree = false;
		Map<String, Object> map = new HashMap<>();
		map =(Map<String, Object>)((List<Map>)planMetadata.get("costs")).get(0).get("amount");
		if(map.get("KRW").equals((double)0.0)){
			isItFree = true;
		};
		return isItFree;
	}
	
	private List<String> getServiceNames(){
		List<String> serviceNames = new ArrayList<String>();
		int i=0;
		do{
			i++;
			if(env.getProperty("Service"+i+".Name")!=null){
				serviceNames.add(env.getProperty("Service"+i+".Name"));				
			}
		}while(env.getProperty("Service"+i+".Name")!=null);
		
		return serviceNames;
	}

	private List<String> getPlanNames(){
		List<String> planNames = new ArrayList<String>();
		int i=0;
		do{
			i++;
			if(env.getProperty("Service"+sNumber+".Plan"+i+".Name")!=null){
				planNames.add(env.getProperty("Service"+sNumber+".Plan"+i+".Name"));
			}
		}while(env.getProperty("Service"+sNumber+".Plan"+i+".Name")!=null);
		
		return planNames;
	}
	
	private String getPlanDescription(){
		
		String planDescription ="no plan description";
		if(env.getProperty("Service"+sNumber+".Plan"+pNumber+".Description")!=null){
			planDescription = env.getProperty("Service"+sNumber+".Plan"+pNumber+".Description");
		}
		return planDescription;
	}
	
	private String getServiceDescription(){
		
		String serviceDescription ="no service description";
		if(env.getProperty("Service"+sNumber+".Description")!=null){
			serviceDescription = env.getProperty("Service"+sNumber+".Description");
		}
		return serviceDescription;
	}
}