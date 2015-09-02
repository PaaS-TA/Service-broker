package org.openpaas.servicebroker.mongodb.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/** 
 * Force the base spring boot packages to be searched for dependencies.
 * 
 * @author 
 *
 */

@Configuration
@ComponentScan(basePackages = "org.openpaas.servicebroker")
public class BrokerConfig {
	
}
