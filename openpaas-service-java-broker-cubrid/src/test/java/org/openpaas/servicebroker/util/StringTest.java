package org.openpaas.servicebroker.util;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class StringTest {

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
	
	@Test
	public void get16UUID() {
		
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.update(UUID.randomUUID().toString().getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		//[^a-zA-Z0-9]
		
		System.out.println(new BigInteger(1, digest.digest()).toString(16));
		return ;
	}
	
	@Test
	public void testReplace() {
		System.out.println("b22sdasd098098".matches("^[a-zA-Z][a-zA-Z0-9]+"));
		System.out.println("00b--2a6363c74c1ee1286097d8ebb0b65e0".replaceAll("/[^a-zA-Z]+/", ""));
		
	}
	
}
