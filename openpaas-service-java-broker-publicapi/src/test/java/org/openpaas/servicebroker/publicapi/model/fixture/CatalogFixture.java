package org.openpaas.servicebroker.publicapi.model.fixture;

import org.openpaas.servicebroker.model.Catalog;


public class CatalogFixture {

	public static Catalog getCatalog() {
		return new Catalog(ServiceFixture.getAllServices());
	}
	
}
