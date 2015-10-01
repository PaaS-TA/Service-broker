package org.openpaas.servicebroker.mysql.service.impl;


import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.openpaas.servicebroker.exception.ServiceInstanceExistsException;
import org.openpaas.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.UpdateServiceInstanceRequest;
import org.openpaas.servicebroker.service.CatalogService;
import org.openpaas.servicebroker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 서비스 인스턴스 서비스가 제공해야하는 메소드를 정의한 인터페이스 클래스인 ServiceInstance를 상속하여
 *  Mysql 서비스 인스턴스 서비스 관련 메소드를 구현한 클래스. 
 *  서비스 인스턴스 생성/삭제/수정/조회 를 구현한다.
 *  
 * @author 김한종
 *
 */
@Service
public class MysqlServiceInstanceService implements ServiceInstanceService {

	private static final Logger logger = LoggerFactory.getLogger(MysqlServiceInstanceService.class);

	@Autowired
	private MysqlAdminService mysqlAdminService;
	
	@Autowired
	private CatalogService service;
	
	@Autowired
	public MysqlServiceInstanceService(MysqlAdminService mysqlAdminService) {
		this.mysqlAdminService = mysqlAdminService;
	}
	
	/**
	 * Provision(create)
	 */
	@Override
	public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request) 
			throws ServiceInstanceExistsException, ServiceBrokerException {
		System.out.println("MysqlServiceInstanceService CLASS createServiceInstance");
		logger.debug("loggerMysqlServiceInstanceService CLASS createServiceInstance");
		
		/* 최초 ServiceInstance 생성 요청시에는 해당 ServiceInstance가 존재하지 않아 해당 메소드를 주석처리 하였습니다.*/
		//ServiceInstance instance = mysqlAdminService.findById(request.getServiceInstanceId());
		ServiceInstance findInstance = mysqlAdminService.findById(request.getServiceInstanceId());
		
		// 요청 정보로부터 ServiceInstance정보를 생성합니다.
		ServiceInstance instance = mysqlAdminService.createServiceInstanceByRequest(request);
		
		if(findInstance != null){
			if(findInstance.getServiceInstanceId().equals(instance.getServiceInstanceId()) &&
					findInstance.getPlanId().equals(instance.getPlanId()) &&
					findInstance.getServiceDefinitionId().equals(instance.getServiceDefinitionId())){
				findInstance.setHttpStatusOK();
				return findInstance;
			}else{
				throw new ServiceInstanceExistsException(instance);
			}
		}
		
		// 해당 요청 정보로부터 생성된 Database가 존재하는지 확인합니다.
		// 존재 할 경우 Database를 삭제합니다.
		if (mysqlAdminService.isExistsService(instance)) {
			// ensure the instance is empty
			mysqlAdminService.deleteDatabase(instance);
			//throw new ServiceInstanceExistsException(instance);
		}
		
		// Database를 생성합니다.
		mysqlAdminService.createDatabase(instance);
		
		// ServiceInstance 정보를 저장합니다.
		mysqlAdminService.save(instance);
		
		return instance;
	}
	
	/**
	 * Provision(delete)
	 */
	@Override
	public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request) throws ServiceBrokerException {
		
		// ServiceInstanceId로 ServiceInstance 정보를 조회합니다.
		ServiceInstance instance = mysqlAdminService.findById(request.getServiceInstanceId());
		
		// 조회된 ServiceInstance가 없을경우 예외처리
		if(instance == null) return null;
		
		// 조회된 ServiceInstance정보로 해당 Database를 삭제합니다
		mysqlAdminService.deleteDatabase(instance);
		// 조회된 ServiceInstance정보로 해당 ServiceInstance정보를 삭제합니다
		mysqlAdminService.delete(instance.getServiceInstanceId());
		
		/* 조회된 ServiceInstance정보로 해당 ServiceInstanceBinding 정보를 삭제합니다.*/
		// ServiceInstanceBinding정보를 조회합니다.
		List<Map<String,Object>> list = mysqlAdminService.findBindByInstanceId(instance.getServiceInstanceId());
		for(Map<String,Object> tmp : list){
			// ServiceInstance에 Binding 된 사용자 정보를 삭제합니다.
			mysqlAdminService.deleteUser(instance.getServiceInstanceId(), (String)tmp.get("binding_id"));
			
			// ServiceInstance에 Binding정보를 삭제합니다.
			mysqlAdminService.deleteBind((String)tmp.get("binding_id"));
		}
		
		return instance;		
	}

	/**
	 * Provision(update)
	 */
	@Override
	public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest request)
			throws ServiceInstanceUpdateNotSupportedException, ServiceBrokerException, ServiceInstanceDoesNotExistException {
		
		// ServiceInstanceId로 ServiceInstance 정보를 조회합니다.
		ServiceInstance instance = mysqlAdminService.findById(request.getServiceInstanceId());
		
		// ServiceInstance가 없을경우 예외처리
		if(instance == null) throw new ServiceInstanceDoesNotExistException(request.getServiceInstanceId());
		
		// 요청 정보로부터 새로운 ServiceInstance정보를 생성합니다.
		ServiceInstance updatedInstance = new ServiceInstance(request);

		/* Provision(update)시 Database에 직접적인 변경사항이 존재하지 않아 주석처리 합니다.
		 * 
		if (mysqlAdminService.isExistsService(instance)) {
			// ensure the instance is empty
			mysqlAdminService.deleteDatabase(instance);
		}

		mysqlAdminService.createDatabase(updatedInstance);
			
		mysqlAdminService.delete(instance.getServiceInstanceId());
		*/
		
		/* 기존 ServiceInstance의 Plan에 변견될경우 다음 처리를 수행합니다. */
		if(!instance.getPlanId().equals(updatedInstance.getPlanId())){
			// Plan 정보에 따라 해당 Database 사용자의 MAX_USER_CONNECTIONS 정보를 조정합니다.
			try {
				mysqlAdminService.setUserConnections(updatedInstance.getPlanId(), instance.getServiceInstanceId());
			} catch (Exception e) {
				throw new ServiceInstanceUpdateNotSupportedException(e.getMessage());
			}
			
			// ServiceInstance의 Plan 정보를 수정합니다.
			mysqlAdminService.updatePlan(instance, updatedInstance);
		}
		return updatedInstance;
	}
	
	/**
	 * Provision Info
	 */
	@Override
	public ServiceInstance getServiceInstance(String id) {
		return mysqlAdminService.findById(id);
	}
	
}