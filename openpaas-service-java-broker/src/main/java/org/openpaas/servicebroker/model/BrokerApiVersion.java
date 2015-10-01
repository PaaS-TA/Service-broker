package org.openpaas.servicebroker.model;


public class BrokerApiVersion {

	public final static String DEFAULT_API_VERSION_HEADER = "X-Broker-Api-Version";
	public final static String API_VERSION_ANY = "*";

	private String brokerApiVersionHeader;
	private String apiVersions;

	public BrokerApiVersion(String brokerApiVersionHeader, String apiVersions) {
		this.brokerApiVersionHeader = brokerApiVersionHeader;
		this.apiVersions = apiVersions;
	}

	public BrokerApiVersion(String apiVersions) {
		this(DEFAULT_API_VERSION_HEADER, apiVersions);
	}

	public BrokerApiVersion() {
		this(DEFAULT_API_VERSION_HEADER, API_VERSION_ANY);
	}

	public String getBrokerApiVersionHeader() {
		return brokerApiVersionHeader;
	}

	public String getApiVersions() {
		return apiVersions;
	}

}
