package org.openpaas.servicebroker.apiplatform.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.apiplatform.common.ApiPlatformUtils;
import org.openpaas.servicebroker.apiplatform.common.StringUtils;
import org.openpaas.servicebroker.common.HttpClientUtils;
import org.openpaas.servicebroker.common.JsonUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceDefinitionDoesNotExistException;
import org.openpaas.servicebroker.model.Catalog;
import org.openpaas.servicebroker.model.DashboardClient;
import org.openpaas.servicebroker.model.Plan;
import org.openpaas.servicebroker.model.ServiceDefinition;
import org.openpaas.servicebroker.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;





import com.fasterxml.jackson.databind.JsonNode;

@Service
@PropertySource("application-mvc.properties")
public class APICatalogService implements CatalogService {

	private static final Logger logger = LoggerFactory.getLogger(APICatalogService.class);
	
	@Autowired
	private Environment env;
	
	@Autowired
	private LoginService loginService;
	
	public ServiceDefinition svc;
	
	@Override
	public Catalog getCatalog() throws ServiceBrokerException{
		
		return getCatalog(env.getProperty("CatalogLoginID"), env.getProperty("CatalogLoginPassword"));
	}

	public Catalog getCatalog(String userId, String userPassword) throws ServiceBrokerException{
		
		String cookie = "";
		//로그인을 통해 쿠키값을 가져온다.
		cookie = loginService.getLogin(userId, userPassword);
		logger.debug("Set Cookie");			

	//get published APIs
	
		HttpHeaders headers = new HttpHeaders();
		
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		String getApisUri = env.getProperty("APIPlatformServer")+":"+env.getProperty("APIPlatformPort")+env.getProperty("URI.GetAllPaginatedPublishedAPIs");
		String getApisParameters ="action=getAllPaginatedPublishedAPIs&tenant=carbon.super&start=1&end=100";
		
		HttpEntity<String> entity = new HttpEntity<String>("", headers);

		ResponseEntity<String> getApisResponseHttp;
		
		JsonNode getApisResponseJson = null;
		
		getApisResponseHttp = HttpClientUtils.send(getApisUri+"?"+getApisParameters, entity, HttpMethod.GET);
		
		try {
			getApisResponseJson = JsonUtils.convertToJson(getApisResponseHttp);
			ApiPlatformUtils.apiPlatformErrorMessageCheck(getApisResponseJson);
		} catch (Exception e) {			
			throw new ServiceBrokerException(e.getMessage());
		}

// ServiceDefinition initialization

		List<ServiceDefinition> services = new ArrayList<ServiceDefinition>();
		JsonNode apis = getApisResponseJson.get("apis");

		
		List<String> planList = new ArrayList<String>();
		String planAvailable;
		int i = 1;
		do{
			planAvailable = env.getProperty("AvailablePlan"+i);
			planList.add(planAvailable);
			i++;
		} while((env.getProperty("AvailablePlan"+i)!=null));
		
		
// Make ServiceDefinitions

		for (JsonNode api : apis) {
			//서비스 변수 선언
			String id;
			String name;
			String description;
			boolean bindable;
			boolean planUpdatable;
			List<Plan> plans;
			List<String> tags;
			Map<String,Object> metadata;
			List<String> requires;
			DashboardClient dashboardClient;
			
			//서비스 변수 정의
			id = api.get("name").asText() + " " + api.get("version").asText();
			name = api.get("name").asText();
			description = StringUtils.nullToBlank(api.get("description").asText());
			if(description.equals("")){
				description="no description";
			}
			bindable = true;
			planUpdatable = true;
			plans = new ArrayList<Plan>();
			tags = new ArrayList<String>();

			metadata = new LinkedHashMap<String,Object>();
				metadata.put("displayName", api.get("name").asText());
				metadata.put("imageUrl", api.get("thumbnailurl").asText());
				metadata.put("longDescription", "longDescription");
				metadata.put("providerDisplayName", api.get("provider").asText());
				metadata.put("documentationUrl", "documentationUrl");
				metadata.put("supportUrl", "supportUrl");
				
			requires = new ArrayList<String>();
			dashboardClient = null;
			
			// Plan Data
			// TODO : Get plan informations. (HOWTO??)
			// default : "Unlimited"

			
			// 정확하게 API 플랫폼의 플랜명과 버전의 글자수 제한을 명시
				// 플랜 변수 선언
			for(i=1;i<=planList.size();i++) {
				String pId;
				String pName;
				String pDescription;
				Map<String, Object> pMetadata;
				boolean free;
				
				//플랜 변수 정의
				pId = api.get("name").asText() + " " + api.get("version").asText() + " " + env.getProperty("AvailablePlan"+i); 
				pName = env.getProperty("AvailablePlan"+i);
				pDescription = env.getProperty("AvailablePlan"+i) + " Plan Description";
				//플랜 메타데이터 정의
				pMetadata = new LinkedHashMap<String,Object>();
						
						List<String> bullets = new ArrayList<String>();
						bullets.add(0, "test value");
				
						List<Object> costs= new ArrayList<Object>();
							Map<String, Object> cost = new HashMap<String, Object>();
								Map<String,Float> amount = new HashMap<String,Float>();
								amount.put("KRW", (float) 0);
							cost.put("amount", amount);
							cost.put("unit", " ");
						costs.add(cost);
						
				pMetadata.put("bullets",bullets);
				pMetadata.put("costs",costs);
				pMetadata.put("displayName", env.getProperty("AvailablePlan"+i));
				
				free = true;
				
				Plan plan = new Plan(pId, pName, pDescription, pMetadata, free);
				plans.add(plan);
			}	
			ServiceDefinition service = new ServiceDefinition(id, name, description, bindable, planUpdatable, plans, tags, metadata, requires, dashboardClient);
			services.add(service);
		}
		
		Catalog catalog = new Catalog(services);
		return catalog;
		
	}

	@Override
	public ServiceDefinition getServiceDefinition(String serviceId) throws ServiceBrokerException{
		logger.info("getServiceDefinition start");

		List<ServiceDefinition> serviceList;

		Catalog catalog;
		try {
			catalog = getCatalog();
		} catch (ServiceBrokerException e) {
			throw new ServiceBrokerException(e.getMessage());
		}
		
		serviceList = catalog.getServiceDefinitions();
		int n = serviceList.size();
		
		for(int i=0;i<n;i++){
		ServiceDefinition findService = new ServiceDefinition();
		findService = serviceList.get(i);
		String serviceFindId = findService.getId();
			if (serviceFindId.equals(serviceId)){
				svc = findService;
				return svc;
			}
		}
		//서비스ID에 포함된 서비스명이 잘못된 케이스 - P003, P007
		//ServiceDefinitionDoesNotExistException 422 UNPROCESSABLE_ENTITY
		logger.warn("could not find service");
		return null;
	}
}
