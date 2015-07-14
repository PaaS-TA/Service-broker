package org.openpaas.servicebroker.apiplatform.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpdateRestTest {
	
private static Properties prop = new Properties();
	
	@BeforeClass
	public static void init() {

		System.out.println("== Started test Update API ==");

		// Initialization
		// Get properties information
		String propFile = "test.properties";
 
		InputStream inputStream = UpdateRestTest.class.getClassLoader().getResourceAsStream(propFile);
		
		try {
			prop.load(inputStream);
	 	} catch (IOException e) {
	 		
			e.printStackTrace();
	 		System.err.println(e);
	 	}
		
	}
}
