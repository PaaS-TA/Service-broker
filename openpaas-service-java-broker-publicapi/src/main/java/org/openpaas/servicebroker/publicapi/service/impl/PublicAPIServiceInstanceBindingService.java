package org.openpaas.servicebroker.publicapi.service.impl;


import java.util.LinkedHashMap;
import java.util.Map;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceBindingRequest;
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

	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(CreateServiceInstanceBindingRequest request)throws ServiceInstanceBindingExistsException,ServiceBrokerException {
		logger.debug("Start - PublicAPIServiceInstanceBindingService.createServiceInstanceBinding()");
		String instanceId = request.getServiceInstanceId();
		String serviceId = request.getServiceDefinitionId();
		String planId = request.getPlanId();
		String bindingId = request.getBindingId();
		String appGuid = request.getAppGuid();
		
		//요청된 서비스ID와 플랜ID의 유효성 확인
		String existServiceId;
		int sNumber=0;
		//서비스ID 유효성 확인
		do{
			//요청된 서비스명을 설정 파일에서 찾지 못한 케이스이다.
			sNumber++;
			if(env.getProperty("Service"+sNumber+".Name")==null){
				if(sNumber==1){
					logger.error("no Servicec information at Properties File: 'application-mvc-properties'");
					throw new ServiceBrokerException("There is no service information at 'application-mvc-properties'");
				}
				else{
					logger.error("Invalid ServiceID : ["+serviceId+"]");
					throw new ServiceBrokerException("Invalid ServiceID : ["+serviceId+"]");					
				}
			}
			existServiceId="Service"+sNumber+" "+env.getProperty("Service"+sNumber+".Name")+" ServiceID";
			if(existServiceId.equals(serviceId)){
				break;
			}
		}while(env.getProperty("Service"+sNumber+".Name")!=null);
		
		//플랜ID 유효성확인
		String existPlanId;
		int pNumber=0;
		do{
			pNumber++;
			//요청된 서비스의 플랜명을 설정 파일에서 찾지 못한 케이스
			if(env.getProperty("Service"+sNumber+".Plan"+pNumber+".Name")==null){
				if(pNumber==1){
					logger.error("no Plan information for Service : ["+serviceId.split(" ")[1]+"] at Properties File: 'application-mvc-properties'");
					throw new ServiceBrokerException("There is no plan information. Properties File: 'application-mvc-properties', Service: "+ serviceId.split(" ")[0]+" Plan: "+planId.split(" ")[2]);
				}
				else{
					logger.error("Invalid PlanID : ["+planId+"]");
					throw new ServiceBrokerException("Invalid PlanID : ["+planId+"]");					
				}
			}
			existPlanId= "Service"+sNumber+" "+env.getProperty("Service"+sNumber+".Name")+" Plan"+pNumber+" "+env.getProperty("Service"+sNumber+".Plan"+pNumber+".Name")+" PlanID";
			if(existPlanId.equals(planId)){
				break;
			}
		}while(env.getProperty("Service"+sNumber+".Plan"+pNumber+".Name")!=null);

		//credential 값을 넣는다.
		Map<String,Object> credentials = new LinkedHashMap<String, Object>();
		try{
			String serviceKey = (String) request.getParameters().get("serviceKey");
			if(serviceKey==null){
				//파라미터는 입력되었으나 키 값이'serviceKey'로 입력되지 않은 경우
				logger.error("Parameter 'serviceKey' not entered.'");
				throw new ServiceBrokerException("Parameter 'serviceKey' not entered. ex) cf bind-service [appName] [serviceInstanceName] -c '{\"serviceKey\":\"[your ServiceKey]\"}'");
			}else{				
				credentials.put("serviceKey", serviceKey);
			}
		}catch(NullPointerException e){
			//파라미터가 아무것도 입력되지 않았을 경우
			logger.error("no serviceKey entered");
			throw new ServiceBrokerException("Please enter your serviceKey. ex) cf bind-service [appName] [serviceInstanceName] -c '{\"serviceKey\":\"[your ServiceKey]\"}'");
		}
		
		credentials.put("url", env.getProperty("Service"+sNumber+".Endpoint"));
		credentials.put("documentUrl", env.getProperty("Service"+sNumber+".DocumentationUrl"));
		String syslogDrainUrl = null;
		
		ServiceInstanceBinding binding = new ServiceInstanceBinding(bindingId, instanceId, credentials, syslogDrainUrl, appGuid);
		logger.debug("End - PublicAPIServiceInstanceBindingService.createServiceInstanceBinding()");
		return binding;
	}

	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request)throws ServiceBrokerException {
		logger.debug("Start - PublicAPIServiceInstanceBindingService.deleteServiceInstanceBinding()");
		String bindingId =request.getBindingId();
		String servieceInstanceId = request.getInstance().getServiceInstanceId();
		String serviceId =request.getServiceId();
		String planId = request.getPlanId();
		
		Map<String,Object> credentials = new LinkedHashMap<String, Object>();
		
		String syslogDrainUrl = null;
		String appGuid = "";
		//TODO DB없이 appGuid를 갖고 있을 수 있는지?
		
		//요청된 서비스ID와 플랜ID의 유효성 확인
		String existServiceId;
		int sNumber=0;
		//서비스ID 유효성 확인
		do{
			//요청된 서비스명을 설정 파일에서 찾지 못한 케이스이다.
			sNumber++;
			if(env.getProperty("Service"+sNumber+".Name")==null){
				if(sNumber==1){
					logger.error("no Service information at Properties File: 'application-mvc-properties'");
					throw new ServiceBrokerException("There is no service information at 'application-mvc-properties'");
				}
				else{
					logger.error("Invalid ServiceID : ["+serviceId+"]");
					throw new ServiceBrokerException("Invalid ServiceID : ["+serviceId+"]");					
				}
			}
			existServiceId="Service"+sNumber+" "+env.getProperty("Service"+sNumber+".Name")+" ServiceID";
			if(existServiceId.equals(serviceId)){
				break;
			}
		}while(env.getProperty("Service"+sNumber+".Name")!=null);
		
		//플랜ID 유효성확인
		String existPlanId;
		int pNumber=0;
		do{
			pNumber++;
			//요청된 서비스의 플랜명을 설정 파일에서 찾지 못한 케이스
			if(env.getProperty("Service"+sNumber+".Plan"+pNumber+".Name")==null){
				if(pNumber==1){
					logger.error("no Plan information for Service : ["+serviceId.split(" ")[1]+"] at Properties File: 'application-mvc-properties'");
					throw new ServiceBrokerException("There is no plan information. Properties File: 'application-mvc-properties', Service: "+ serviceId.split(" ")[0]+" Plan: "+planId.split(" ")[2]);
				}
				else{
					logger.error("Invalid PlanID : ["+planId+"]");
					throw new ServiceBrokerException("Invalid PlanID : ["+planId+"]");					
				}
			}
			existPlanId= "Service"+sNumber+" "+env.getProperty("Service"+sNumber+".Name")+" Plan"+pNumber+" "+env.getProperty("Service"+sNumber+".Plan"+pNumber+".Name")+" PlanID";
			if(existPlanId.equals(planId)){
				break;
			}
		}while(env.getProperty("Service"+sNumber+".Plan"+pNumber+".Name")!=null);
		
		
		logger.debug("End - PublicAPIServiceInstanceBindingService.deleteServiceInstanceBinding()");
		return new ServiceInstanceBinding(bindingId,servieceInstanceId,credentials,syslogDrainUrl,appGuid);
	}
	
	
}