package sampleApp.common;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.AbstractCloudConnector;
import org.springframework.cloud.CloudException;
import org.springframework.cloud.FallbackServiceInfoCreator;
import org.springframework.cloud.app.ApplicationInstanceInfo;
import org.springframework.cloud.service.BaseServiceInfo;
import org.springframework.cloud.util.EnvironmentAccessor;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 개방형 클라우드 플랫폼에서 환경설정 정보가져오기
 * 
 * @author 안찬영
 *
 * History
 * 2015.7.9 최초개발
 */
public class CloudFoundryConnector extends AbstractCloudConnector<Map<String,Object>> {

	private ObjectMapper objectMapper = new ObjectMapper();
	private EnvironmentAccessor environment = new EnvironmentAccessor();
	
	private ApplicationInstanceInfoCreator applicationInstanceInfoCreator = new ApplicationInstanceInfoCreator();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CloudFoundryConnector() {
		super((Class) CloudFoundryServiceInfoCreator.class);
	}

	@Override
	public boolean isInMatchingCloud() {
		return environment.getEnvValue("VCAP_APPLICATION") != null;
	}
	
	/**
	 * Application의 정보를 가져오기
	 */
	@Override
	public ApplicationInstanceInfo getApplicationInstanceInfo() {
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> rawApplicationInstanceInfo 
				= objectMapper.readValue(environment.getEnvValue("VCAP_APPLICATION"), Map.class);
			return applicationInstanceInfoCreator.createApplicationInstanceInfo(rawApplicationInstanceInfo);
		} catch (Exception e) {
			throw new CloudException(e);
		} 
	}
	
	/* package for testing purpose */
	void setCloudEnvironment(EnvironmentAccessor environment) {
		this.environment = environment;
	}

	
	/**
	 * 개방형 클라우드 서비스의 VCAP_SERIVCES 환경설정 정보를 가져오기
	 * 
	 * @return parsed service data
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> getServicesData() {
		String servicesString = environment.getEnvValue("VCAP_SERVICES");
		Map<String, List<Map<String,Object>>> rawServices = new HashMap<String, List<Map<String,Object>>>();
		
		if (servicesString == null || servicesString.length() == 0) {
			rawServices = new HashMap<String, List<Map<String,Object>>>();
		}
		try {
			rawServices = objectMapper.readValue(servicesString, Map.class);
		} catch (Exception e) {
			throw new CloudException(e);
		} 
		
		List<Map<String,Object>> flatServices = new ArrayList<Map<String,Object>>();
		for (Map.Entry<String, List<Map<String,Object>>> entry : rawServices.entrySet()) {
			flatServices.addAll(entry.getValue());
		}
		return flatServices;
	}
	

	@Override
	protected FallbackServiceInfoCreator<BaseServiceInfo,Map<String,Object>> getFallbackServiceInfoCreator() {
		return new CloudFoundryFallbackServiceInfoCreator();
	}
}

class CloudFoundryFallbackServiceInfoCreator extends FallbackServiceInfoCreator<BaseServiceInfo,Map<String,Object>> {
	@Override
	public BaseServiceInfo createServiceInfo(Map<String,Object> serviceData) {
		String id = (String) serviceData.get("name");
		return new BaseServiceInfo(id);
	}
}