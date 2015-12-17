package org.openpaas.servicebroker.config;

import org.openpaas.servicebroker.model.BrokerApiVersion;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
/**
 * 서비스 브로커 Auto Configuration 을 설정하는 클래스 (spring boot autoconfigure 사용)
 * 
 * @author 송창학
 * @date 2015.0629
 */

@Configuration
@ComponentScan(basePackages = {"org.openpaas.servicebroker"})
@ConditionalOnWebApplication
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class ServiceBrokerAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(BrokerApiVersion.class)
	public BrokerApiVersion brokerApiVersion() {
		//example
		//"2.5"
		//"2.5, 2.6, 2.7"
		return new BrokerApiVersion("2.5, 2.6, 2.8");
	}
}
