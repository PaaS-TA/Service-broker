package org.openpaas.servicebroker.naverapi.model.fixture;

import org.openpaas.servicebroker.model.Catalog;


public class CatalogFixture {

	public static Catalog getCatalog() {
		return new Catalog(ServiceFixture.getAllServices());
	}
	
}
