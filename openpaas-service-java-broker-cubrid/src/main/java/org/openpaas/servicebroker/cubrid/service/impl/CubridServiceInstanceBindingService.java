package org.openpaas.servicebroker.cubrid.service.impl;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.cubrid.model.CubridServiceInstance;
import org.openpaas.servicebroker.cubrid.model.CubridServiceInstanceBinding;
import org.openpaas.servicebroker.service.ServiceInstanceBindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 서비스 인스턴스 바인딩 관련 서비스가 제공해야하는 메소드를 정의한 인터페이스 클래스인 ServiceInstanceBindingService를 상속하여
 * CubridDB 서비스 인스턴스 바인딩  관련 메소드를 구현한 클래스. 서비스 인스턴스 바인드 생성/삭제/조회 를 구현한다.
 *  
 * @author Cho Mingu
 *
 */
@Service
public class CubridServiceInstanceBindingService implements ServiceInstanceBindingService {

	private static final Logger logger = LoggerFactory.getLogger(CubridServiceInstanceBindingService.class);
	@Autowired
	private CubridAdminService cubridAdminService; 
	
	
	@Autowired
	public CubridServiceInstanceBindingService(CubridAdminService cubridAdminService) {
		this.cubridAdminService = cubridAdminService;
	}
	
	/**
	 * bind-service-broker
	 * ServiceInstanceBinding 생성
	 * 
	 */
	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(
			CreateServiceInstanceBindingRequest request)
			throws ServiceInstanceBindingExistsException, ServiceBrokerException {
		logger.info("=====> CubridServiceInstanceBindingService CLASS createServiceInstanceBinding");
		
		CubridServiceInstanceBinding binding = cubridAdminService.findBindById(request.getBindingId());
		
		logger.debug("binding : {} ", (binding == null ? "Not exist": "Exist") );
		
		if (binding != null) {
			
			String as_is_bindId = binding.getId();
			String as_is_instanceId = binding.getServiceInstanceId();
			String to_be_bindId = request.getBindingId();
			String to_be_instanceId = request.getServiceInstanceId();
			
			logger.debug("as-is : Binding ID = {}, Instance ID = {}", as_is_bindId, as_is_instanceId);
			logger.debug("to-be : Binding ID = {}, Instance ID = {}", to_be_bindId, to_be_instanceId);
			
			if( as_is_bindId.equals(to_be_bindId) && as_is_instanceId.equals(to_be_instanceId) ) {
				binding.setHttpStatusOK();
				return binding;
			} else {
				throw new ServiceInstanceBindingExistsException(binding);
			}
		}
		
		CubridServiceInstance instance = cubridAdminService.findById(request.getServiceInstanceId());
		String database = instance.getDatabaseName();
		
		// TODO Password Generator
		String password = getPassword();
		String username = null;

		// Username Generator (Unique)
		do {
			username = getUsername();
		} while(cubridAdminService.isExistsUser(database, username));
		
		cubridAdminService.createUser(database, username, password);

		String[] host = cubridAdminService.getServerAddresses().split(":");
		
		Map<String,Object> credentials = new HashMap<String,Object>();
		credentials.put("uri", cubridAdminService.getConnectionString(database, username, password));
		credentials.put("jdbcurl", "jdbc:" + cubridAdminService.getConnectionString(database, username, password));
		credentials.put("username", username);
		credentials.put("password", password);
		credentials.put("name", database);
		credentials.put("host", cubridAdminService.getServerAddresses());
		credentials.put("hostname", host[0]);
		if ( host.length == 1) credentials.put("port", "");
		else credentials.put("port", host[1]);
	
		binding = new CubridServiceInstanceBinding(request.getBindingId(), instance.getServiceInstanceId(), credentials, null, request.getAppGuid());
		binding.setDatabaseUserName(username);
		cubridAdminService.saveBind(binding);
		
		return binding;
	}

	/**
	 * ServiceInstanceBinding의 교유식별자를 이용하여 CubridServiceInstanceBinding정보를 조회.
	 * 
	 * @param id
	 * @return CubridServiceInstanceBinding
	 * @throws ServiceBrokerException
	 */
	protected CubridServiceInstanceBinding getServiceInstanceBinding(String id) throws ServiceBrokerException {
		return cubridAdminService.findBindById(id);
	}
 
	/**
	 * unbind-serivce-broker
	 * ServiceInstanceBinding을 삭제
	 * 
	 */
	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException {
		logger.info("=====> CubridServiceInstanceBindingService CLASS deleteServiceInstanceBinding");
		
		String bindingId = request.getBindingId();
		CubridServiceInstanceBinding binding = getServiceInstanceBinding(bindingId);
		
		logger.debug("binding : {}", (binding == null ? "Not exist": "Exist") );
		
		if (binding != null) {
			CubridServiceInstance instance = cubridAdminService.findById(binding.getServiceInstanceId());
			cubridAdminService.deleteUser(instance.getDatabaseName(), binding.getDatabaseUserName());
			cubridAdminService.deleteBind(bindingId);
		}
		
		return binding;
	}
	
	private String getUsername() {
		String uuid16 = null;
		MessageDigest digest = null;
		try {
			do {
				digest = MessageDigest.getInstance("MD5");
				digest.update(UUID.randomUUID().toString().getBytes());
				uuid16 = new BigInteger(1, digest.digest()).toString(16).replaceAll("/[^a-zA-Z]+/", "").substring(0, 16);
			} while(!uuid16.matches("^[a-zA-Z][a-zA-Z0-9]+"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return uuid16;
	}
	
	private String getPassword() {
		MessageDigest digest = null;
		try {
				digest = MessageDigest.getInstance("MD5");
				digest.update(UUID.randomUUID().toString().getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return new BigInteger(1, digest.digest()).toString(16).replaceAll("/[^a-zA-Z]+/", "").substring(0, 16);
	}

}
