package org.openpaas.servicebroker.pinpoint.model;

import com.fasterxml.jackson.annotation.*;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "instance_id",
        "binding_id"
})

public class PinpointJson {
    @JsonProperty("instance_id")
    private List<String> instance_id;

    @JsonProperty("binding_id")
    private List<BindingJson> binding_id;


    @JsonAnyGetter
    public List<String> getInstance_id() {
        return instance_id;
    }

    @JsonAnySetter
    public void setinstance_id(List<String> instanceId) {
        this.instance_id = instanceId;
    }

    @JsonAnyGetter
    public List<BindingJson> getBinding() {
        return binding_id;
    }

    @JsonAnySetter
    public void setBinding(List<BindingJson> bindingId) {
        this.binding_id = bindingId;
    }
    public String toJsonString(){

        String jsonString = "";
        Gson gson = new Gson();
        jsonString = gson.toJson(this);
        return jsonString;

    }

    public static PinpointJson createDummy(){
        PinpointJson pinpointJson = new PinpointJson();
        List<String> linstanceId = new ArrayList<String>(){};
        List<BindingJson> bindingJsons = new ArrayList<BindingJson>(){};
        pinpointJson.setinstance_id(linstanceId);
        pinpointJson.setBinding(bindingJsons);
        return pinpointJson;
    }
}