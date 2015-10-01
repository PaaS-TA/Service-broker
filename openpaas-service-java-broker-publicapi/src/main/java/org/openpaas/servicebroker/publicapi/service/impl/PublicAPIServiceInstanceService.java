package org.openpaas.servicebroker.publicapi.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.openpaas.servicebroker.exception.ServiceInstanceExistsException;
import org.openpaas.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.UpdateServiceInstanceRequest;
import org.openpaas.servicebroker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;


@Service
public class PublicAPIServiceInstanceService implements ServiceInstanceService {

	private static final Logger logger = LoggerFactory.getLogger(PublicAPIServiceInstanceService.class);
	
	@Autowired
	private Environment env;
	

	@Override
	public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request) throws ServiceInstanceExistsException, ServiceBrokerException {
		logger.debug("Start - PublicAPIServiceInstanceService.createServiceInstance()");
		String serviceId = request.getServiceDefinitionId();
		String planId = request.getPlanId();
		
		//서비스ID에 대한 유효성은 이미 확인이 완료된 상태
		//플랜ID 유효성확인
		
		String existPlanId;
		String serviceAndNumber = serviceId.split(" ")[0];
		int pNumber=0;
		do{
			pNumber++;
			//요청된 서비스의 플랜명을 설정 파일에서 찾지 못한 케이스이다.
			if(env.getProperty(serviceAndNumber+".Plan"+pNumber+".Name")==null){
				//해당 서비스에 대해서 하나의 플랜도 정의 되어있지 않은 케이스
				if(pNumber==1){
					logger.error("no Plan information for Service : ["+serviceId.split(" ")[0]+"] at Properties File: 'application-mvc-properties'");
					throw new ServiceBrokerException("There is no plan information. Properties File: 'application-mvc-properties', Service : ["+serviceId.split(" ")[0]+"], Plan : ["+planId.split(" ")[2]+"]");
				}
				else{
					logger.error("Invalid PlanID : ["+planId+"]");
					throw new ServiceBrokerException("Invalid PlanID : ["+planId+"]");			
				}
			}
			existPlanId= serviceAndNumber+" "+env.getProperty(serviceAndNumber+".Name")+" Plan"+pNumber+" "+env.getProperty(serviceAndNumber+".Plan"+pNumber+".Name")+" PlanID";
			if(existPlanId.equals(planId)){
				break;
			}
		}while(env.getProperty(serviceAndNumber+".Plan"+pNumber+".Name")!=null);
		
		ServiceInstance instance = new ServiceInstance(request);
		logger.debug("End - PublicAPIServiceInstanceService.createServiceInstance()");
		return instance.withDashboardUrl(env.getProperty("DashboardURL"));
	}

	@Override
	public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request) throws ServiceBrokerException {
		logger.debug("Start - PublicAPIServiceInstanceService.deleteServiceInstance()");
		String serviceId = request.getServiceId();
		String planId = request.getPlanId();
		
		//요청된 서비스ID와 플랜ID의 유효성 확인
		String existServiceId;
		int sNumber=0;
		//서비스ID 유효성 확인
		do{
			//요청된 서비스명을 설정 파일에서 찾지 못한 케이스이다.
			sNumber++;
			if(env.getProperty("Service"+sNumber+".Name")==null){
				//프로퍼티 파일에 서비스가 정의 되어있지 않은 케이스
				if(sNumber==1){
					logger.error("no Service information at Properties File: 'application-mvc-properties'");
					throw new ServiceBrokerException("There is no service information at 'application-mvc-properties'");
				}
				else{
					logger.error("Invalid ServiceID : ["+planId+"]");
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
					logger.error("no Plan information for Service : ["+serviceId.split(" ")[0]+"] at Properties File: 'application-mvc-properties'");
					throw new ServiceBrokerException("There is no plan information. Properties File: 'application-mvc-properties', Service : ["+serviceId.split(" ")[0]+"], Plan : ["+planId.split(" ")[1]+"]");
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

		ServiceInstance instance = new ServiceInstance(request);
		logger.debug("End - PublicAPIServiceInstanceService.deleteServiceInstance()");
		return instance;
	}

	@Override
	public ServiceInstance getServiceInstance(String instanceId) {
		logger.debug("Start - PublicAPIServiceInstanceService.getServiceInstance()");
		CreateServiceInstanceRequest request = new CreateServiceInstanceRequest();
		ServiceInstance instance = new ServiceInstance(request);
		logger.debug("End - PublicAPIServiceInstanceService.getServiceInstance()");
		return instance;
	}

	@Override
	public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest request) throws ServiceInstanceUpdateNotSupportedException,
			ServiceBrokerException, ServiceInstanceDoesNotExistException {
		logger.debug("Start - PublicAPIServiceInstanceService.updateServiceInstance()");
		String planId = request.getPlanId();
		String existPlanId;
		String serviceAndNumber = planId.split(" ")[0];
		boolean findPlan	= false;
		
		List<String> planNames = getPlanNames(serviceAndNumber);
		
		//해당 서비스에 하나의 플랜만 정의되어 있는 케이스
		if(planNames.size()==1){
			logger.error("only one plan defined for Service : ["+env.getProperty(serviceAndNumber+".Name")+"] at Properties File: 'application-mvc-properties'");
			throw new ServiceInstanceUpdateNotSupportedException("Service instance update not supported");
		}
		else if(planNames.isEmpty()){
			//해당 서비스에 대한 플랜이 정의되어 있지 않은 케이스
			logger.error("no Plan information for Service : ["+env.getProperty(serviceAndNumber+".Name")+"] at Properties File: 'application-mvc-properties'");
			throw new ServiceBrokerException("There is no plan information. Properties: 'application-mvc-properties', Service: ["+ planId.split(" ")[0]+"]");
		}
		else{
			for(int pNumber =1;pNumber<=planNames.size();pNumber++){

				existPlanId= serviceAndNumber+" "+env.getProperty(serviceAndNumber+".Name")+" Plan"+pNumber+" "+env.getProperty(serviceAndNumber+".Plan"+pNumber+".Name")+" PlanID";	
				
				if(existPlanId.equals(planId)){
					findPlan =true;
					break;
				}
			}
		}
		//해당 서비스에 대해서 2개 이상의 플랜이 정의되어 있지만 요청된 플랜ID를 정의된 플랜 목록에서 찾지 못한 케이스
		if(!findPlan){
			throw new ServiceBrokerException("Invalid PlanID : ["+planId+"]");
		}
		ServiceInstance instance = new ServiceInstance(request);
		logger.debug("End - PublicAPIServiceInstanceService.updateServiceInstance()");
		return instance;
	}
	
	
	private List<String> getPlanNames(String serviceAndNumber){
		logger.debug("Start - PublicAPIServiceInstanceService.getPlanNames()");
		List<String> planNames = new ArrayList<String>();
		int i=0;
		do{
			i++;
			if(env.getProperty(serviceAndNumber+".Plan"+i+".Name")!=null){
				planNames.add(env.getProperty(serviceAndNumber+".Plan"+i+".Name"));				
			}
		}while(env.getProperty(serviceAndNumber+".Plan"+i+".Name")!=null);
		logger.debug("End - PublicAPIServiceInstanceService.getPlanNames()");
		return planNames;
	}

}
