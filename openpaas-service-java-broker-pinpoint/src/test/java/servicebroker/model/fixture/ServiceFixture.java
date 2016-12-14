package servicebroker.model.fixture;

import org.openpaas.servicebroker.model.ServiceDefinition;

import java.util.ArrayList;
import java.util.List;


public class ServiceFixture {

	public static List<ServiceDefinition> getAllServices() {
		List<ServiceDefinition> services = new ArrayList<ServiceDefinition>();
		services.add(ServiceFixture.getService());
		return services;
	} 
	
	public static ServiceDefinition getService() {
		return new ServiceDefinition(
				"service-one-id", 
				"Service One", 
				"Description for Service One", 
				true,
				PlanFixture.getAllPlans());
	}
	
}
