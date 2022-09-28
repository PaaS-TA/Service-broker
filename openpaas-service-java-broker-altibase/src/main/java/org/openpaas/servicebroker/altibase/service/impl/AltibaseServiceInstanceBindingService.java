package org.openpaas.servicebroker.altibase.service.impl;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.altibase.model.AltibaseServiceInstance;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.service.ServiceInstanceBindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * 서비스 인스턴스 바인딩 관련 서비스가 제공해야하는 메소드를 정의한 인터페이스 클래스인 ServiceInstanceBindingService를 상속하여
 * AltibaseDB 서비스 인스턴스 바인딩  관련 메소드를 구현한 클래스. 서비스 인스턴스 바인드 생성/삭제/조회 를 구현한다.
 *  
 * @author Bae Younghee
 *
 */
@Service
public class AltibaseServiceInstanceBindingService implements ServiceInstanceBindingService {

	private static final Logger logger = LoggerFactory.getLogger(AltibaseServiceInstanceBindingService.class);
	@Autowired
	private AltibaseAdminService altibaseAdminService; 

	@Autowired
	private Environment env;
		
	@Autowired
	public AltibaseServiceInstanceBindingService(AltibaseAdminService altibaseAdminService) {
		this.altibaseAdminService = altibaseAdminService;
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
		logger.info("=====> AltibaseServiceInstanceBindingService CLASS createServiceInstanceBinding");
		
		ServiceInstanceBinding binding = altibaseAdminService.findBindById(request.getBindingId());
		
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
		
		AltibaseServiceInstance instance = altibaseAdminService.findById(request.getServiceInstanceId());

		/*	2020.10.30	For Replication 	
			* App_Guid 값으로 이중화 대상인지를 체크.
			* 삭제할 때는? 
		*/
		/* 해당 node에서 새로운 사용자명이 존재하는지 검증합니다.
		 * user는 sys여야 함 */

		String user = env.getRequiredProperty("jdbc.username");
		String pwd = env.getRequiredProperty("jdbc.password");
		
		String host = instance.getHost();
		String port = instance.getPort();	
		
		String url = altibaseAdminService.getConnectionString(user, pwd, host, port);
		String password = null;
		String username = null;		
				
		/*	2020.10.30	Replication 관련 추가.		
			ServiceInstanceBinding 클래스에 NAME, PASSWORD가 없다. 어떻게 Binding 시키는지? 
		*/
		if (altibaseAdminService.isRepUser(request.getAppGuid())) {			
			
				String userNamePassWD = altibaseAdminService.getDBUsername(request.getAppGuid());
				logger.debug("userNamePassWD :  {}", userNamePassWD);
				String[] arrayIm = userNamePassWD.split("!@#%&@#%!");
				username = arrayIm[0]; 
				password = arrayIm[1]; 
			
		} else {
			
				// TODO Password Generator
				password = getPassword();
				username = null;
			
			// Username Generator (Unique)
			do {
				username = getUsername();
			} while(altibaseAdminService.isExistsUser(username));
			
			/* 해당 node에서 새로운 사용자명이 존재하는지 검증합니다.
			* user는 sys여야 함 */
			if (altibaseAdminService.isExistsUser(url, username)) {			
				// 사용자 삭제시 특정 Databas의 사용자를 삭제하지 않아 아래와 같이 사용자 명으로 삭제 처리합니다.
				altibaseAdminService.deleteUser(url, username);
			}
			
		}		

		/* String url = altibaseAdminService.getConnectionString(user, pwd, host, port);  */
		altibaseAdminService.createUser(url, username, password);
		
		String bindUrl = altibaseAdminService.getConnectionString(username, password, host, port);
		Map<String,Object> credentials = new HashMap<String,Object>();
		credentials.put("uri", bindUrl);
		credentials.put("jdbcurl", bindUrl);
		credentials.put("hostname", host);
		credentials.put("port", port);
		credentials.put("username", username);
		credentials.put("password", password);
		credentials.put("name", "mydb");
		
		binding = new ServiceInstanceBinding(request.getBindingId(),
				instance.getServiceInstanceId(),
				credentials,
				null,
				request.getAppGuid());
		
		altibaseAdminService.saveBind(binding);
		
		return binding;
	}

	/**
	 * ServiceInstanceBinding의 교유식별자를 이용하여 AltibaseServiceInstanceBinding정보를 조회.
	 * 
	 * @param id
	 * @return AltibaseServiceInstanceBinding
	 * @throws ServiceBrokerException
	 */
	protected ServiceInstanceBinding getServiceInstanceBinding(String id) throws ServiceBrokerException {
		return altibaseAdminService.findBindById(id);
	}
 
	/**
	 * unbind-serivce-broker
	 * ServiceInstanceBinding을 삭제
	 * 
	 */
	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException {
		logger.info("=====> AltibaseServiceInstanceBindingService CLASS deleteServiceInstanceBinding");
		
		String bindingId = request.getBindingId();
		ServiceInstanceBinding binding = getServiceInstanceBinding(bindingId);
		
		logger.debug("binding : {}", (binding == null ? "Not exist": "Exist") );
		
		if (binding != null) {
			AltibaseServiceInstance instance = altibaseAdminService.findById(binding.getServiceInstanceId());
			
			// instance를 위한 node 연결 url 구하기
			String url = altibaseAdminService.getConnectionString(
												env.getRequiredProperty("jdbc.username"),
												env.getRequiredProperty("jdbc.password"),
												instance.getHost(),
												instance.getPort());
			
			altibaseAdminService.deleteUser(url, binding.getCredentials().get("username").toString());
			altibaseAdminService.deleteBind(bindingId);
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
