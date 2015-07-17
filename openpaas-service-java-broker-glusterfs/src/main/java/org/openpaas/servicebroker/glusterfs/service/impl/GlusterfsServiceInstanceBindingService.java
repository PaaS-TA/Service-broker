package org.openpaas.servicebroker.glusterfs.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.openpaas.servicebroker.glusterfs.model.GlusterfsServiceInstance;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
import org.openpaas.servicebroker.service.ServiceInstanceBindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Glusterfs impl to bind services.  Binding a service does the following:
 * creates a new user in the database (password is Ramdom String value),
 * saves the ServiceInstanceBinding info to the Glusterfs repository.
 *  
 * @author kkanzo@bluedigm.com
 *
 */
@PropertySource("classpath:application-mvc.properties")
@Service
public class GlusterfsServiceInstanceBindingService implements ServiceInstanceBindingService {

	private static final Logger logger = LoggerFactory.getLogger(GlusterfsServiceInstanceBindingService.class);
	
	@Autowired
	private Environment env;
	
	@Autowired
	private GlusterfsAdminService glusterfsAdminService; 
	
	
	@Autowired
	public GlusterfsServiceInstanceBindingService(GlusterfsAdminService glusterfsAdminService) {
		this.glusterfsAdminService = glusterfsAdminService;
	}
	
	/**
	 * Binding(create)
	 */
	@Override
	public ServiceInstanceBinding createServiceInstanceBinding(
			CreateServiceInstanceBindingRequest request)
			throws ServiceInstanceBindingExistsException, ServiceBrokerException {
		
		logger.debug("GlusterfsServiceInstanceBindingService CLASS createServiceInstanceBinding");
		
		/* 최초 ServiceInstanceBinding 생성 요청시에는 해당 ServiceInstanceBinding가 존재하지 않아 해당 메소드를 주석처리 하였습니다.*/
		ServiceInstanceBinding findBinding = glusterfsAdminService.findBindById(request.getBindingId());
		
		// 요청 정보로부터 ServiceInstanceBinding 정보를 생성합니다.
		ServiceInstanceBinding binding = glusterfsAdminService.createServiceInstanceBindingByRequest(request);
		
		if(findBinding != null){
			if(findBinding.getServiceInstanceId().equals(binding.getServiceInstanceId()) &&
					findBinding.getId().equals(binding.getId())  &&
					findBinding.getAppGuid().equals(binding.getAppGuid())){
				findBinding = getBindingInfo(request, findBinding);
				findBinding.setHttpStatusOK();
				return findBinding;
			}else{
				throw new ServiceInstanceBindingExistsException(binding);
			}
			
		}
		
		// 요청 정보로부터 ServiceInstance정보를 조회합니다.
		ServiceInstance instance = glusterfsAdminService.findById(request.getServiceInstanceId());
		GlusterfsServiceInstance gf = glusterfsAdminService.tenantInfofindById(instance.getServiceInstanceId());
					
		// ServiceInstance정보가 엇을경우 예외처리
		if(instance == null) throw new ServiceBrokerException("Not Exists ServiceInstance");
		
		// 사용자 아이디를 생성합니다.
		String username = glusterfsAdminService.getUsername(request.getBindingId());
		
		// 사용자 비밀번호를 생성합니다.
		String password = glusterfsAdminService.getUsername(request.getServiceInstanceId());
		
		// 새로운 사용자를 생성합니다.
		glusterfsAdminService.createUser(gf.getTenantId(), username, password);

		// 반환될 credentials 정보를 생성합니다.
		Map<String,Object> credentials = new HashMap<String,Object>();
		credentials.put("tenantname", gf.getTenantName());
		credentials.put("provider", "swift-keystone");
		credentials.put("username", username);
		credentials.put("password", password);
		credentials.put("auth_url", env.getRequiredProperty("glusterfs.authurl")+"/v2.0");
		binding = new ServiceInstanceBinding(request.getBindingId(), instance.getServiceInstanceId(), credentials, null, request.getAppGuid());
		
		// Binding 정보를 저장합니다.
		glusterfsAdminService.saveBind(binding);
		
		return binding;
	}

	/**
	 * Binding(delete)
	 */
	@Override
	public ServiceInstanceBinding deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request)
			throws ServiceBrokerException {
		
		String bindingId = request.getBindingId();
		
		// ServiceInstanceBinding 정보를 조회합니다.
		ServiceInstanceBinding binding = glusterfsAdminService.findBindById(bindingId);
		if(binding ==  null) return null;
		
		// ServiceInstance 정보를 조회합니다.
		ServiceInstance instance = glusterfsAdminService.findById(binding.getServiceInstanceId());
		if(instance ==  null) return null;
		
		// bindingId로 사용자를 삭제합니다.
		if(!binding.getServiceInstanceId().equals(instance.getServiceInstanceId())) return null;
		
		glusterfsAdminService.deleteUser(binding.getServiceInstanceId(), bindingId);
		
		// bindingId로 Binding 정보를 삭제합니다.
		glusterfsAdminService.deleteBind(bindingId);
		
		return binding;
	}
	
	/**
	 * Binding Info
	 */
	protected ServiceInstanceBinding getServiceInstanceBinding(String id) {
		return glusterfsAdminService.findBindById(id);
	}
	
	/**
	 * Binding Info
	 * @param request
	 * @param instance
	 * @return
	 */
	public ServiceInstanceBinding getBindingInfo(CreateServiceInstanceBindingRequest request, ServiceInstanceBinding instance){
		
		GlusterfsServiceInstance gf = glusterfsAdminService.tenantInfofindById(instance.getServiceInstanceId());
		
		// 사용자 아이디를 생성합니다.
		String username = glusterfsAdminService.getUsername(request.getBindingId());
		// 사용자 비밀번호를 생성합니다.
		String password = glusterfsAdminService.getUsername(request.getServiceInstanceId());
		
		// 반환될 credentials 정보를 생성합니다.
		Map<String,Object> credentials = new HashMap<String,Object>();
		credentials.put("tenantname", gf.getTenantName());
		credentials.put("provider", "swift-keystone");
		credentials.put("username", username);
		credentials.put("password", password);
		credentials.put("auth_url", env.getRequiredProperty("glusterfs.authurl")+"/v2.0");
		
		return new ServiceInstanceBinding(request.getBindingId(), instance.getServiceInstanceId(), credentials, null, request.getAppGuid());
		
	}
}
