package servicebroker.model.fixture;

import org.openpaas.servicebroker.model.Plan;

import java.util.ArrayList;
import java.util.List;


public class PlanFixture {

	public static List<Plan> getAllPlans() {
		List<Plan> plans = new ArrayList<Plan>();
		plans.add(getPlanOne());
		return plans;
	}
		
	public static Plan getPlanOne() {
		return new Plan("plan-one-id", "Plan One", "Description for Plan One");
	}
	
}
