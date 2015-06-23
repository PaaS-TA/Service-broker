package org.openpaas.servicebroker.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class StringTest {

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	
	@Test
	public void ListDB() {
		List<String> listdb = new ArrayList<>();
		
		listdb.add("");
	}
	
	public @Test void getDBName() {
		String dbName = "  1.  testdb1";
		
		String[] s = dbName.split("  ");
		
		for ( int i = 0 ; i < s.length ; i++) 
			System.out.println(i + " :" +s[i]);
	}

}
