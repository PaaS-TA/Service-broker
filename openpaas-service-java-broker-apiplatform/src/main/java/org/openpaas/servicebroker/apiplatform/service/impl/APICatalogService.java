package org.openpaas.servicebroker.apiplatform.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.apiplatform.common.HttpClientUtils;
import org.openpaas.servicebroker.apiplatform.common.JsonUtils;
import org.openpaas.servicebroker.apiplatform.common.StringUtils;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
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
	
	@Override
	public Catalog getCatalog() throws ServiceBrokerException{
		
		return getCatalog(env.getProperty("CatalogLoginID"), env.getProperty("CatalogLoginPassword"));
	}

	public Catalog getCatalog(String username, String password) throws ServiceBrokerException{
		
		String cookie = "";
		
	// LoginService 클래스의  getLogin 메소드를 사용하고 리턴되는 statuscode 확인
		cookie = loginService.getLogin(username, password);
		logger.debug("Set Cookie");			

	//get published APIs
	
		HttpHeaders headers = new HttpHeaders();
		
		headers.set("Cookie", cookie);
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		String uri = env.getProperty("URI.GetAllPaginatedPublishedAPIs");
		String parameters ="action=getAllPaginatedPublishedAPIs&tenant=carbon.super&start=1&end=100";
		
		
		HttpEntity<String> entity = new HttpEntity<String>("", headers);

		ResponseEntity<String> apiListHttpResponse;
		JsonNode catalogOrg = null;
		apiListHttpResponse = HttpClientUtils.send(uri+"?"+parameters, entity, HttpMethod.GET);
		
		try {
			catalogOrg = JsonUtils.convertToJson(apiListHttpResponse);
		} catch (Exception e) {			
			throw new ServiceBrokerException(e.getMessage());
		}

		System.out.println("status:" + apiListHttpResponse.getStatusCode());
		System.out.println("result:" + apiListHttpResponse.getBody());


// ServiceDefinition initialization

		List<ServiceDefinition> services = new ArrayList<ServiceDefinition>();
		JsonNode apis = catalogOrg.get("apis");

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
			bindable = true;
			planUpdatable = true;
			plans = new ArrayList<Plan>();
			tags = new ArrayList<String>();

			metadata = new HashMap<String,Object>();
				metadata.put("displayName", api.get("name").asText());
				metadata.put("imageUrl", api.get("thumbnailurl").asText());
				metadata.put("longDescription", "longDescription");
				metadata.put("providerDisplayName", env.getProperty("Service.Provider"));
				metadata.put("documentationUrl", "documentationUrl");
				metadata.put("supportUrl", "supportUrl");
				
			requires = new ArrayList<String>();
			dashboardClient = null;
			
			// Plan Data
			// TODO : Get plan informations. (HOWTO??)
			// default : "Unlimited"
			
			// 정확하게 API 플랫폼의 플랜명과 버전의 글자수 제한을 명시
				// 플랜 변수 선언
				String pId;
				String pName;
				String pDescription;
				Map<String, Object> pMetadata;
				boolean free;
				
				//플랜 변수 정의
				pId = api.get("name").asText() + " " + api.get("version").asText() + " " + env.getProperty("PLAN.Unlimited"); 
				pName = env.getProperty("PLAN.Unlimited");
				pDescription = env.getProperty("PLAN.Unlimited") + " Plan Description";
				//플랜 메타데이터 정의
				pMetadata = new HashMap<String,Object>();
						
						List<String> bullets = new ArrayList<String>();
							bullets.add(0, "test value");
				
						List<Object> costs= new ArrayList<Object>();
							HashMap<String, Object> cost = new HashMap<String, Object>();
								HashMap<String,Float> amount = new HashMap<String,Float>();
								amount.put("KRW", (float) 0);
							cost.put("amount", amount);
							cost.put("unit", " ");
						costs.add(cost);
						
				pMetadata.put("bullets",bullets);
				pMetadata.put("costs",costs);
				pMetadata.put("displayName", env.getProperty("PLAN.Unlimited"));
				
				free = true;
				
				Plan plan = new Plan(pId, pName, pDescription, pMetadata, free);
				plans.add(plan);
				
			ServiceDefinition service = new ServiceDefinition(id, name, description, bindable, planUpdatable, plans, tags, metadata, requires, dashboardClient);
			System.out.println(service.getId());
			services.add(service);
		}
		
		Catalog catalog = new Catalog(services);
		return catalog;
		
	}

	@Override
	public ServiceDefinition getServiceDefinition(String serviceId){
		logger.info("getServiceDefinition start");
		System.out.println("API Catalog getServiceDefinition()");
		
		ServiceDefinition svc= new ServiceDefinition();
		List<ServiceDefinition> serviceList;

		Catalog catalog;
		try {
			catalog = getCatalog();
		} catch (ServiceBrokerException e) {
			throw new RuntimeException(e.getMessage());
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
		logger.warn("could not find service");
		//찾는 서비스가 없을 경우 처리 
		//이 메소드에서 리턴값을 null로 보낼경우 ServiceInstanceController에서 예외를 발생시키지만
		//적절한 예외 메시지를 보내기 위해 직접 예외를 던짐
		
		throw new RuntimeException("could not find service in API Platform");
	}

}
