package org.openpaas.servicebroker.publicapi.service.impl;

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
		int planNumber=0;
		do{
			planNumber++;
			//요청된 서비스의 플랜명을 설정 파일에서 찾지 못한 케이스이다.
			if(env.getProperty("Service"+serviceAndNumber+".Plan"+planNumber+".Name")==null){
				//해당 서비스에 대해서 한개의 플랜도 정의 되어있지 않은 케이스
				if(planNumber==1){
					throw new ServiceBrokerException("There is no plan information. Properties File: 'application-mvc-properties', Service: "+ serviceId.split(" ")[0]+" Plan: "+planId.split(" ")[0]);
				}
				else{
					throw new ServiceBrokerException("Invalid planID : "+planId);					
				}
			}
			existPlanId= "Plan"+planNumber+" "+env.getProperty("Service"+serviceAndNumber+".Plan"+planNumber+".Name")+" PlanID";
			if(existPlanId.equals(planId)){
				break;
			}
		}while(env.getProperty("Service"+serviceAndNumber+".Plan"+planNumber+".Name")!=null);
		
		ServiceInstance instance = new ServiceInstance(request);
		return instance.withDashboardUrl(env.getProperty("DashboardURL"));
	}

	@Override
	public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest arg0) throws ServiceBrokerException {


		
		
		
		
		return null;
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
		
		
		
		
		
		
		return null;
	}


}
