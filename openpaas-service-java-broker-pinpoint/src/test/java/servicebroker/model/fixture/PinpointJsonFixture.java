package servicebroker.model.fixture;

import com.fasterxml.jackson.annotation.*;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "instance_id",
        "binding_id"
})

public class PinpointJsonFixture {
    @JsonProperty("instance_id")
    private List<String> instance_id;

    @JsonProperty("binding_id")
    private List<BindingJsonFixture> binding_id;


    @JsonAnyGetter
    public List<String> getInstance_id() {
        return instance_id;
    }

    @JsonAnySetter
    public void setinstance_id(List<String> instanceId) {
        this.instance_id = instanceId;
    }

    @JsonAnyGetter
    public List<BindingJsonFixture> getBinding() {
        return binding_id;
    }

    @JsonAnySetter
    public void setBinding(List<BindingJsonFixture> bindingId) {
        this.binding_id = bindingId;
    }
    public String toJsonString(){

        String jsonString = "";
        Gson gson = new Gson();
        jsonString = gson.toJson(this);
        return jsonString;

    }

    public static PinpointJsonFixture createDummy(){
        PinpointJsonFixture pinpointJsonFixture = new PinpointJsonFixture();
        List<String> linstanceId = new ArrayList<String>(){};
        List<BindingJsonFixture> bindingJsonFixtures = new ArrayList<BindingJsonFixture>(){};
        pinpointJsonFixture.setinstance_id(linstanceId);
        pinpointJsonFixture.setBinding(bindingJsonFixtures);
        return pinpointJsonFixture;
    }
}