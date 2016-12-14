package org.openpaas.servicebroker.pinpoint.config;

import org.openpaas.servicebroker.model.Catalog;
import org.openpaas.servicebroker.model.Plan;
import org.openpaas.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring boot 구동시 Catalog API 에서 사용하는 Catalog Bean 를 생성하는 클래스
 */
@Configuration
public class CatalogConfig {

    @Bean
    public Catalog catalog() {
        return new Catalog(Arrays.asList(
                new ServiceDefinition(
                        "801bd048-c570-4629-8539-301e68fe0611",  //WEB_UI host_name
                        "Pinpoint",                                //name
                        "A simple pinpoint implementation",            //description
                        true,                                            //bindable
                        false,                                            //planUpdatable
                        //setMetadata
                        Arrays.asList(
                                new Plan("pinpoint_plan",
                                        "Pinpoint_standard",
                                        "This is a pinpoint instance defalut",
                                        new HashMap<String, Object>(), true)),
                        Arrays.asList("pinpoint"),
                        getServiceDefinitionMetadata(),
                        Arrays.asList("syslog_drain"),
                        null)));
    }

/* Used by Pivotal CF console */

    private Map<String, Object> getServiceDefinitionMetadata() {
        Map<String, Object> sdMetadata = new HashMap<String, Object>();
        sdMetadata.put("displayName", "Pinpoint Java APM");
        sdMetadata.put("imageUrl", "http://www.openpaas.org/rs/pinpoint/images/MysqlDB_Logo_Full.png");
        sdMetadata.put("longDescription", "Pinpoint Service");
        sdMetadata.put("providerDisplayName", "OpenPaas");
        sdMetadata.put("documentationUrl", "http://www.openpaas.org");
        sdMetadata.put("supportUrl", "http://www.openpaas.org");
        return sdMetadata;
    }

}