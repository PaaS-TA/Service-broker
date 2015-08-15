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
		
		String serviceId = request.getServiceDefinitionId();
		String planId = request.getPlanId();
		
//		String serviceKey = null;
//		try{
//			serviceKey = request.getParameters().get("serviceKey").toString();			
//		} catch(NullPointerException e){
//			serviceKey = null;
//		}
//		boolean instanceExsists = dao.insertServiceInstance(instanceId, serviceId, planId, organizationGuid, spaceGuid, serviceKey);
//	
//		ServiceInstance instance = new ServiceInstance(request);
//		if(instanceExsists){
//			List<Map<String,Object>> databases = dao.getServiceInstancAtDB(instanceId);
//			//인스턴스 아이디가 이미 존재할 경우 나머지 값들을 검증, 모두같으면 200 OK, 하나라도 다르면 exception
//			if(
//				databases.get(0).get("service_id").equals(serviceId)&&
//				databases.get(0).get("plan_id").equals(planId)&&
//				databases.get(0).get("organization_guid").equals(organizationGuid)&&
//				databases.get(0).get("space_guid").equals(spaceGuid))
//			{
//				instance.setHttpStatusOK();
//				return instance.withDashboardUrl(env.getProperty("DashboardURL"));
//			}
//			else{
//				throw new ServiceInstanceExistsException(instance);
//			}
//		}
		//서비스ID에 대한 유효성은 이미 확인이 완료된 상태이다.
		//플랜ID 유효성확인
		String existPlanId;
		String serviceAndNumber = serviceId.split(" ")[0];
		int pNumber=0;
		do{
			pNumber++;
			//요청된 서비스의 플랜명을 설정 파일에서 찾지 못한 케이스이다.
			if(env.getProperty(serviceAndNumber+".Plan"+pNumber+".Name")==null){
				//해당 서비스에 대해서 한개의 플랜도 정의 되어있지 않은 케이스
				if(pNumber==1){
					throw new ServiceBrokerException("There is no plan information. Properties File: 'application-mvc-properties', Service: "+ serviceId.split(" ")[0]+" Plan: "+planId.split(" ")[1]);
				}
				else{
					throw new ServiceBrokerException("Invalid PlanID : "+planId);			
				}
			}
			existPlanId= serviceAndNumber+" Plan"+pNumber+" "+env.getProperty(serviceAndNumber+".Plan"+pNumber+".Name")+" PlanID";
			if(existPlanId.equals(planId)){
				break;
			}
		}while(env.getProperty(serviceAndNumber+".Plan"+pNumber+".Name")!=null);
		
		ServiceInstance instance = new ServiceInstance(request);
		return instance.withDashboardUrl(env.getProperty("DashboardURL"));
	}

	@Override
	public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request) throws ServiceBrokerException {

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
				if(sNumber==1){
					throw new ServiceBrokerException("There is no service information at 'application-mvc-properties'");
				}
				else{
					throw new ServiceBrokerException("Invalid ServiceID : "+serviceId);					
				}
			}
			existServiceId="Service"+sNumber+" "+env.getProperty("Service"+sNumber+".Name")+" ServiceID";
			if(existServiceId.equals(serviceId)){
				break;
			}
		}while(env.getProperty(sNumber+".Name")!=null);
		//플랜ID 유효성확인
		String existPlanId;
		int pNumber=0;
		do{
			pNumber++;
			//요청된 서비스의 플랜명을 설정 파일에서 찾지 못한 케이스
			if(env.getProperty("Service"+sNumber+".Plan"+pNumber+".Name")==null){
				if(pNumber==1){
					throw new ServiceBrokerException("There is no plan information. Properties File: 'application-mvc-properties', Service: "+ serviceId.split(" ")[0]+" Plan: "+planId.split(" ")[1]);
				}
				else{
					throw new ServiceBrokerException("Invalid PlanID : "+planId);					
				}
			}
			existPlanId= "Service"+sNumber+" Plan"+pNumber+" "+env.getProperty("Service"+sNumber+".Plan"+pNumber+".Name")+" PlanID";
			if(existPlanId.equals(planId)){
				break;
			}
		}while(env.getProperty("Service"+sNumber+".Plan"+pNumber+".Name")!=null);
	
		
		
		
		
		
		
		ServiceInstance instance = new ServiceInstance(request);
		return instance;
	}

	@Override
	public ServiceInstance getServiceInstance(String instanceId) throws ServiceBrokerException {
		//체크해서 예외처리
		
		//List<Map<String,Object>> databases =dao.getServiceInstancAtDB(instanceId);
		
		CreateServiceInstanceRequest request = new CreateServiceInstanceRequest();
//		request.setServiceDefinitionId(databases.get(0).get("service_id").toString());
//		request.setPlanId(databases.get(0).get("plan_id").toString());
//		request.setOrganizationGuid(databases.get(0).get("organization_guid").toString());
//		request.setSpaceGuid(databases.get(0).get("space_guid").toString());
		ServiceInstance instance = new ServiceInstance(request);
		return instance;
	}

	@Override
	public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest request) throws ServiceInstanceUpdateNotSupportedException,
			ServiceBrokerException, ServiceInstanceDoesNotExistException {
		
		String planId = request.getPlanId();
		String existPlanId;
		String serviceAndNumber = planId.split(" ")[0];
		boolean findPlan	= false;

		List<String> planNames = getPlanNames(serviceAndNumber);
		
		//해당 서비스에 하나의 플랜만 정의되어 있는 케이스
		if(planNames.size()==1){
			throw new ServiceInstanceUpdateNotSupportedException("Service instance update not supported");
		}
		else if(planNames.isEmpty()){
			//프로비전 이후에 서비스 브로커를 수정하며 설정파일에 플랜정보가 사라진 케이스
			throw new ServiceBrokerException("There is no plan information. Properties File: 'application-mvc-properties', Service: ["+ planId.split(" ")[0]+"] Plan: ["+planId.split(" ")[1]+"]");
		}
		else{
			for(int pNumber =1;pNumber<planNames.size();pNumber++){
				existPlanId= serviceAndNumber+" "+"Plan"+pNumber+" "+env.getProperty(serviceAndNumber+".Plan"+pNumber+".Name")+" PlanID";	
				if(existPlanId.equals(planId)){
					findPlan =true;
				}
			}
		}
		//해당 서비스에 대해서 2개 이상의 플랜이 정의되어 있지만 요청된 플랜ID를 정의된 플랜 목록에서 찾지 못한 케이스
		if(!findPlan){
			throw new ServiceBrokerException("Invalid planID : "+planId);		
		}
		ServiceInstance instance = new ServiceInstance(request);
		return instance;
	}
	
	
	
	
	private List<String> getPlanNames(String serviceAndNumber){
		List<String> planNames = new ArrayList<String>();
		int pNumber=1;
		do{
			planNames.add(env.getProperty(serviceAndNumber+".Plan"+pNumber+".Name"));
			pNumber++;
		}while(env.getProperty(serviceAndNumber+".Plan"+pNumber+".Name")!=null);
		
		return planNames;
	}

}
