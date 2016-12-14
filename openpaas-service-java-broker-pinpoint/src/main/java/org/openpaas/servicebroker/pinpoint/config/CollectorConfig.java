package org.openpaas.servicebroker.pinpoint.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.openpaas.servicebroker.pinpoint.exception.PinpointServiceException;
import org.openpaas.servicebroker.pinpoint.model.CollectorJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

//import org.springframework.context.annotation.PropertySources;

/**
 * Pinpoint 를 Binding을 할때 Collector 정보 정의
 */

public class CollectorConfig {

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(CollectorConfig.class);

    public CollectorJson collectorJson() throws PinpointServiceException {
        ObjectMapper mapper = new ObjectMapper();
        CollectorJson collectorJsons = new CollectorJson();

        InputStream is = CollectorConfig.class.getResourceAsStream("/collector.json");

        if (is != null) {
            try {
                List<CollectorJson> lstCollectorJsons = mapper.readValue(is, TypeFactory.defaultInstance().constructCollectionType(List.class, CollectorJson.class));
                collectorJsons = lstCollectorJsons.get(0);
                logger.debug(this.getClass().getName().toString() + ":Dashboard_url: " + collectorJsons.getDashboard_url());
            }catch (IOException e){
                throw new PinpointServiceException(e.getLocalizedMessage());
            }

        } else {
            collectorJsons.setDashboard_url("collector info is null");
        }
        return collectorJsons;
    }

}
