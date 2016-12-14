package org.openpaas.servicebroker.pinpoint.model;

import org.openpaas.servicebroker.model.ServiceInstanceBinding;

import java.util.HashMap;
import java.util.Map;

/**
 * A binding to a service instance
 *
 * @author
 */
public class PinpointServiceInstanceBinding extends ServiceInstanceBinding {

    public PinpointServiceInstanceBinding(String id,
                                          String serviceInstanceId,
                                          Map<String, Object> credentials,
                                          String syslogDrainUrl, String appGuid) {
        super(id, serviceInstanceId, credentials, syslogDrainUrl, appGuid);

    }

    public PinpointServiceInstanceBinding() {
        super("", "", new HashMap<String, Object>(), "", "");
    }

}
