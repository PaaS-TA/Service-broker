package servicebroker.model.fixture;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonPropertyOrder({
        "instance_id",
        "binding_id",
        "instance_url",
        "agent_id",
        "application_name",
        "collector_ip",
})
public class BindingJsonFixture {

    @JsonSerialize
    @JsonProperty("instance_id")
    private String instance_id;

    @JsonSerialize
    @JsonProperty("binding_id")
    private String binding_id;

    @JsonSerialize
    @JsonProperty("instance_url")
    private String instance_url;

    @JsonSerialize
    @JsonProperty("agent_id")
    private String agent_id;

    @JsonSerialize
    @JsonProperty("application_name")
    private String application_name;

    @JsonSerialize
    @JsonProperty("collector_ip")
    private String collector_ip;

    public String getBinding_id() {
        return this.binding_id;
    }

    public void setBinding_id(String bindingId) {
        this.binding_id = bindingId;
    }

    public String getInstance_id() {
        return this.instance_id;
    }

    public void setInstance_id(String instance_id) {
        this.instance_id = instance_id;
    }

    public String getInstance_url() {
        return this.instance_url;
    }

    public void setInstance_url(String instance_url) {
        this.instance_url = instance_url;
    }

    public String getAgent_id() {
        return this.agent_id;
    }

    public String setAgentId(String agent_id) { return this.agent_id = agent_id;}

    public String getApplication_name() {
        return this.application_name;
    }

    public void setApplication_name(String application_name) {
        this.application_name = application_name;
    }

    public String getCollector_ip() {
        return collector_ip;
    }

    public void setCollector_ip(String collector_ip) {
        this.collector_ip = collector_ip;
    }

}