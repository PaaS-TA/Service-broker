package org.openpaas.servicebroker.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openpaas.servicebroker.exception.ServiceBrokerApiVersionException;
import org.openpaas.servicebroker.model.BrokerApiVersion;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class BrokerApiVersionInterceptor extends HandlerInterceptorAdapter {

	private final BrokerApiVersion version;

	public BrokerApiVersionInterceptor() {
		this(null);
	}

	public BrokerApiVersionInterceptor(BrokerApiVersion version) {
		this.version = version;
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws ServiceBrokerApiVersionException {
		if (version != null && !anyVersionAllowed()) {
			String apiVersion = request.getHeader(version.getBrokerApiVersionHeader());
			boolean contains = false;
			for (String brokerApiVersion : version.getApiVersions().split(", ")) {
				if (brokerApiVersion.equals(apiVersion)) {
					contains = true;
					break;
				} 
			}
			if (!contains) {
				throw new ServiceBrokerApiVersionException(version.getApiVersions(), apiVersion);
			}
			 
		}
		return true;
	}

	private boolean anyVersionAllowed() {
		return BrokerApiVersion.API_VERSION_ANY.equals(version.getApiVersions());
	}

}
