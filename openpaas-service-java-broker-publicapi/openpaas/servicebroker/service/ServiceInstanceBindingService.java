package org.openpaas.servicebroker.service;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;

/**
 * 바인딩 객체를 제어한다.
 * 
 * 2015.07.17
 * @author 송창학 수석
 */
public interface ServiceInstanceBindingService {

	/**
	 * 바인딩 객체를 생성한다.
	 * 
	 * @param createServiceInstanceBindingRequest변수가 클라우드 컨트롤러로부터 전달되는 인자 값들을 포함하고 있다.
	 * @return 새로 생성된 ServiceInstanceBinding객체
	 * @throws ServiceInstanceBindingExistsException - 이미 같은 바인딩이 존재하는 경우
	 * @throws ServiceBrokerException - 브로커 내부적인 오류가 발생한 경우
	 * 2015.07.17
	 * @author 송창학 수석
	 */
	ServiceInstanceBinding createServiceInstanceBinding(
			CreateServiceInstanceBindingRequest createServiceInstanceBindingRequest)
			throws ServiceInstanceBindingExistsException, ServiceBrokerException;

	/**
	 * 서비스 인스턴스 바인딩을 삭제하고, 바인딩이 존재하지 않는 경우 null을 리턴한다.
	 *
	 * @param deleteServiceInstanceBindingRequest변수가 클라우드 컨트롤러로부터 전달되는 인자 값들을 포함하고 있다.
     * @return 삭제된 ServiceInstanceBinding객체를 리턴하거나 ServiceInstanceBinding이 이미 존재하지 않는 경우 null을 리턴한다.
     * @throws ServiceBrokerException - 브로커 내부적인 오류가 발생한 경우
     * 2015.07.17
	 * @author 송창학 수석
	 */
	ServiceInstanceBinding deleteServiceInstanceBinding(
			DeleteServiceInstanceBindingRequest deleteServiceInstanceBindingRequest) 
	        throws ServiceBrokerException;
	
}
