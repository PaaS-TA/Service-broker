package org.openpaas.servicebroker.service;


import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.openpaas.servicebroker.exception.ServiceInstanceExistsException;
import org.openpaas.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.UpdateServiceInstanceRequest;

/**
 * Handles instances of service definitions.
 * 
 * 2015.07.17
 * @author 송창학 수석
 */
public interface ServiceInstanceService {

	/**
	 * 새로운 서비스 인스턴스를 생성한다.
	 * 
	 * @param createServiceInstanceRequest변수가 클라우드 컨트롤러로부터 전달되는 인자값을 포함하고 있다.
	 * @return 새로 생성된 ServiceInstance객체
	 * @throws ServiceInstanceExistsException - 서비스 인스턴스가 이미 존재하는 경우
	 * @throws ServiceBrokerException - 브로커 내부적인 오류가 발생한 경우
	 * 2015.07.17
	 * @author 송창학 수석
	 */
	ServiceInstance createServiceInstance(CreateServiceInstanceRequest createServiceInstanceRequest) 
			throws ServiceInstanceExistsException, ServiceBrokerException;
	
	/**
	 * 인자값으로 주어진 서비스 인스턴스 아이디에 해당하는 서비스 인스턴스 객체를 가져온다.
	 * 
	 * @param serviceInstanceId 서비스 인스턴스의 아이디
	 * @return 인자값으로 주어진 아이디에 해당하는 ServiceInstance객체를 리턴하거나 해당하는 ServiceInstance가 존재하지 않는 경우 null을 리턴한다.
	 * 2015.07.17
	 * @author 송창학 수석
	 */
	ServiceInstance getServiceInstance(String serviceInstanceId) throws ServiceBrokerException;
	
	/**
	 * 해당하는 서비스 인스턴스가 존재할 경우, 서비스 인스턴스를 삭제하고 리턴한다.
	 * 
	 * @param deleteServiceInstanceRequest변수가 서비스 인스턴스 삭제를 위한 정보를 포함하고 있다.
	 * @return 삭제된 ServiceInstance객체, ServiceInstance객체가 존재하지 않을 경우 null을 리턴
	 * @throws ServiceBrokerException - 브로커 내부적인 오류가 발생한 경우
	 * 2015.07.17
	 * @author 송창학 수석
	 */
	ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest deleteServiceInstanceRequest) 
			throws ServiceBrokerException;

	/**
	 * 서비스 인스턴스의 서비스 플랜에 한하여, 업데이트를 수행한다.
	 * 
	 * @param updateServiceInstanceRequest변수가 서비스 인스턴스 업데이트를 위한 정보를 포함하고 있다.
	 * @return 서비스 플랜이 업데이트 된 ServiceInstance객체
	 * @throws ServiceInstanceUpdateNotSupportedException - 서비스 플랜의 변경을 지원하지 않는 경우, 서비스 인스턴스의 상태 때문에 요청을 수행할 수 없는 경우
	 * @throws ServiceInstanceDoesNotExistException - 서비스 인스턴스가 존재하지 않는 경우
	 * @throws ServiceBrokerException - 브로커 내부적인 오류가 발생한 경우
	 * 2015.07.17
	 * @author 송창학 수석
	 */
	ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest updateServiceInstanceRequest)
			throws ServiceInstanceUpdateNotSupportedException, ServiceBrokerException,
			ServiceInstanceDoesNotExistException;

}
