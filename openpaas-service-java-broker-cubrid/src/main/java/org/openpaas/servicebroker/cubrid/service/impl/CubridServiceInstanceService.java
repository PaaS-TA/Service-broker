package org.openpaas.servicebroker.cubrid.service.impl;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.openpaas.servicebroker.cubrid.exception.CubridServiceException;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.openpaas.servicebroker.exception.ServiceInstanceExistsException;
import org.openpaas.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.cubrid.model.CubridServiceInstance;
import org.openpaas.servicebroker.model.UpdateServiceInstanceRequest;
import org.openpaas.servicebroker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *  서비스 인스턴스 서비스가 제공해야하는 메소드를 정의한 인터페이스 클래스인 ServiceInstance를 상속하여
 *  CubridDB 서비스 인스턴스 서비스 관련 메소드를 구현한 클래스. 서비스 인스턴스 생성/삭제/수정/조회 를 구현한다.
 *  
 * @author Cho Mingu
 *
 */
@Service
public class CubridServiceInstanceService implements ServiceInstanceService {

	private static final Logger logger = LoggerFactory.getLogger(CubridServiceInstanceService.class);

	@Autowired
	private CubridAdminService cubridAdminService;

	@Autowired
	public CubridServiceInstanceService(CubridAdminService cubridAdminService) {
		this.cubridAdminService = cubridAdminService;
	}

	/**
	 * create-service
	 * ServiceInstance를 생성한다.
	 * 
	 */
	@Override
	public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request) 
			throws ServiceInstanceExistsException, ServiceBrokerException {
		logger.info("=====> CubridServiceInstanceService CLASS createServiceInstance");

		CubridServiceInstance instance = cubridAdminService.findById(request.getServiceInstanceId());

		logger.debug("instance : {}", (instance == null ? "Not exist": "Exist") );

		if (instance != null) {

			String as_is_id = instance.getServiceInstanceId();
			String as_is_plan = instance.getPlanId();
			String to_be_id = request.getServiceInstanceId();
			String to_be_plan = request.getPlanId();

			logger.debug("as-is : Instance ID = {}, Plan = {}", as_is_id, as_is_plan);
			logger.debug("to-be : Instance ID = {}, Plan = {}", to_be_id, to_be_plan);

			if( as_is_id.equals(to_be_id) && as_is_plan.equals(to_be_plan) ) {
				instance.setHttpStatusOK();
				return instance;
			} else {
				throw new ServiceInstanceExistsException(instance);
			}
		}

		instance = new CubridServiceInstance();
		instance.setServiceInstanceId(request.getServiceInstanceId());
		instance.setPlanId(request.getPlanId());

		do {
			instance.setDatabaseName(getDatabaseName());
		} while(cubridAdminService.isExistsService(instance));

		if (!cubridAdminService.createDatabase(instance)) {
			throw new ServiceBrokerException("Failed to create new DB instance.");
		}

		cubridAdminService.save(instance);

		return instance;
	}

	/**
	 * ServiceInstance를 이용하여 ServiceInstance 정보를 조회한다.
	 */
	@Override
	public ServiceInstance getServiceInstance(String id) {
		ServiceInstance instance = null;
		try {
			instance = cubridAdminService.findById(id);
		} catch (CubridServiceException e) {
			e.printStackTrace();
		}
		return instance;
	}

	/**
	 * delete-service
	 * ServiceInstance를 삭제한다.
	 * 
	 */
	@Override
	public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request) throws CubridServiceException {
		logger.info("=====> CubridServiceInstanceService CLASS deleteServiceInstance");

		CubridServiceInstance instance = cubridAdminService.findById(request.getServiceInstanceId());

		logger.debug("instance : {}", (instance == null ? "Not exist": "Exist") );

		if (instance != null) {
			cubridAdminService.deleteDatabase(instance);
			cubridAdminService.delete(instance.getServiceInstanceId());
		}

		return instance;		
	}

	/**
	 * update-service
	 * ServiceInstance의 plan을 변경한다.
	 * 
	 */
	@Override
	public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest request)
			throws ServiceInstanceUpdateNotSupportedException, ServiceBrokerException, ServiceInstanceDoesNotExistException {
		logger.info("=====> CubridServiceInstanceService CLASS updateServiceInstance");

		CubridServiceInstance instance = cubridAdminService.findById(request.getServiceInstanceId());

		logger.debug("instance : {}", (instance == null ? "Not exist": "Exist") );

		if ( instance == null ) {
			throw new ServiceInstanceDoesNotExistException(request.getServiceInstanceId());
		} else {
			throw new ServiceInstanceUpdateNotSupportedException("Not Supported.");
		}
	}

	private String getDatabaseName() {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.update(UUID.randomUUID().toString().getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		//[^a-zA-Z0-9]
		return new BigInteger(1, digest.digest()).toString(16).replaceAll("/[^a-zA-Z0-9]+/", "").substring(0, 16);

	}

}