package org.openpaas.servicebroker.pinpoint.model;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "dashboard_url",
        "collector_ips",
        "collector_span_port",
        "collector_stat_port",
        "collector_tcp_port",
})
public class CollectorJson {
    @JsonProperty("dashboard_url")
    private String dashboard_url;
    @JsonProperty("collector_ips")
    private List<String> collector_ips;
    @JsonProperty("collector_span_port")
    private String collector_span_port;
    @JsonProperty("collector_stat_port")
    private String collector_stat_port;
    @JsonProperty("collector_tcp_port")
    private String collector_tcp_port;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnyGetter
    public String getDashboard_url() {
        return dashboard_url;
    }

    @JsonAnySetter
    public void setDashboard_url(String dashboard_url) {
        this.dashboard_url = dashboard_url;
    }

    @JsonAnyGetter
    public List<String> getCollector_ips() {
        return collector_ips;
    }

    @JsonAnySetter
    public void setCollector_ips(List<String> collector_ips) {
        this.collector_ips = collector_ips;
    }

    @JsonAnyGetter
    public String getCollector_stat_port() {
        return collector_stat_port;
    }

    @JsonAnySetter
    public void setCollector_stat_port(String collector_stat_port) {
        this.collector_stat_port = collector_stat_port;
    }

    @JsonAnyGetter
    public String getCollector_span_port() {
        return collector_span_port;
    }

    @JsonAnySetter
    public void setCollector_span_port(String collector_span_port) {
        this.collector_span_port = collector_span_port;
    }

    @JsonAnyGetter
    public String getCollector_tcp_port() {
        return collector_tcp_port;
    }

    @JsonAnySetter
    public void setCollector_tcp_port(String collector_tcp_port) {
        this.collector_tcp_port = collector_tcp_port;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }


}