package org.openpaas.servicebroker.publicapi.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.openpaas.servicebroker.model.Catalog;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.Plan;
import org.openpaas.servicebroker.model.ServiceDefinition;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.service.ServiceInstanceBindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;





@Service
public class PublicAPIServiceInstanceBindingService implements ServiceInstanceBindingService{
	
	private static final Logger logger = LoggerFactory.getLogger(PublicAPIServiceInstanceBindingService.class);
	
	@Autowired
	private Environment env;
	
	@Autowired
	private PublicAPIServiceInstanceService serviceInstanceService;

	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(CreateServiceInstanceBindingRequest request)throws ServiceInstanceBindingExistsException,ServiceBrokerException {
		
		String instanceId = request.getServiceInstanceId();
		String serviceId = request.getServiceDefinitionId();
		String planId = request.getPlanId();
		String bindingId = request.getBindingId();
		String appGuid = request.getAppGuid();

//		for(int i =0;i<catalog.getServiceDefinitions().size();i++){
//			
//			ServiceDefinition service = catalog.getServiceDefinitions().get(i);
//			if(service.getId().equals(serviceId)){
//				for(int j =0;j<service.getPlans().size();j++){
//					Plan plan = service.getPlans().get(j);
//					if(plan.getId().equals(planId)){
//						break;
//					}
//					//서비스 브로커를 생성한 시점을 기준으로 요청된 서비스는 카탈로그 되었지만, 플랜이 존재하지 않는 케이스
//					//서비스 브로커 생성이후에 프로퍼티 파일을 변경했다면, 서비스 브로커를 다시 생성하여야 한다.
//					else{
//						throw new ServiceBrokerException("Invalid PlanID : "+planId);
//					}
//				}		
//			}
//			//서비스 브로커를 생성한 시점을 기준으로 요청된 서비스가 카탈로그 되지 않은 케이스
//			//서비스 브로커 생성이후에 프로퍼티 파일을 변경했다면, 서비스 브로커를 다시 생성하여야 한다.
//			else{
//				throw new ServiceBrokerException("Invalid ServiceID : "+serviceId);
//			}
//		}

	//요청된 서비스ID와 플랜ID의 유효성 확인
		String existServiceId;
		int serviceNumber=0;
		//서비스ID 유효성 확인
		do{
			//요청된 서비스명을 설정 파일에서 찾지 못한 케이스이다.
			serviceNumber++;
			if(env.getProperty("Service"+serviceNumber+".Name")==null){
				if(serviceNumber==1){
					throw new ServiceBrokerException("There is no service information at 'application-mvc-properties'");
				}
				else{
					throw new ServiceBrokerException("Invalid ServiceID : "+serviceId);					
				}
			}
			existServiceId="Service"+serviceNumber+" "+env.getProperty("Service"+serviceNumber+".Name")+" ServiceID";
			if(existServiceId.equals(serviceId)){
				break;
			}
		}while(env.getProperty("Service"+serviceNumber+".Name")!=null);
		//플랜ID 유효성확인
		String existPlanId;
		int planNumber=0;
		do{
			planNumber++;
			//요청된 서비스의 플랜명을 설정 파일에서 찾지 못한 케이스이다.
			if(env.getProperty("Service"+serviceNumber+".Plan"+planNumber+".Name")==null){
				if(planNumber==1){
					throw new ServiceBrokerException("There is no plan information. Properties File: 'application-mvc-properties', Service: "+ serviceId.split(" ")[0]+" Plan: "+planId.split(" ")[0]);
				}
				else{
					throw new ServiceBrokerException("Invalid planID : "+planId);					
				}
			}
			existPlanId= "Plan"+planNumber+" "+env.getProperty("Service"+serviceNumber+".Plan"+planNumber+".Name")+" PlanID";
			if(existPlanId.equals(planId)){
				break;
			}
		}while(env.getProperty("Service"+serviceNumber+".Plan"+planNumber+".Name")!=null);

		//credential 값을 넣는다.
		Map<String,Object> credentials = new LinkedHashMap<String, Object>();
		credentials.put("url", env.getProperty("Service"+serviceNumber+".Endpoint"));
		credentials.put("serviceKey", env.getProperty("Service"+serviceNumber+".ServiceKey"));
		credentials.put("supportUrl", env.getProperty("Service"+serviceNumber+".SupportUrl"));
		String syslogDrainUrl = null;
		
		ServiceInstanceBinding binding = new ServiceInstanceBinding(bindingId, instanceId, credentials, syslogDrainUrl, appGuid);

		return binding;
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest arg0)throws ServiceBrokerException {

		
		
		
		
		
		return null;
	}
	
	
}
