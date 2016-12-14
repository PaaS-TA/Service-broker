package org.openpaas.servicebroker.pinpoint.config;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
@ComponentScan(basePackages = "org.openpaas.servicebroker")
public class BrokerConfig {
    @Bean
    public PropertyPlaceholderConfigurer properties() {
        PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
        Resource[] resources = new ClassPathResource[]{
                new ClassPathResource("application-mvc.properties")
        };
        propertyPlaceholderConfigurer.setLocations(resources);
        propertyPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
        return propertyPlaceholderConfigurer;
    }

}
