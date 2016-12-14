package servicebroker.model.fixture;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;


public class DataFixture {

	public static String getOrgOneGuid() {
		return "org-guid-one";
	}
	
	public static String getSpaceOneGuid() {
		return "space-guid-one";
	}
	
	public static String toJson(Object object) throws IOException {
		 ObjectMapper mapper = new ObjectMapper();
		 return mapper.writeValueAsString(object);
	}
	
}
