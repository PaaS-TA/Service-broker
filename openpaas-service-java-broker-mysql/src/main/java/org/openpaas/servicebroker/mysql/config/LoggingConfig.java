package org.openpaas.servicebroker.mysql.config;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfig {
	
	public LoggingConfig() {}
	 
	@Bean
	public ConsoleAppender consoleAppender() {
	ConsoleAppender consoleAppender = new ConsoleAppender();
	consoleAppender.setThreshold(Level.ALL);
	PatternLayout patternLayout = new PatternLayout("%d %-5p [%c{1}] %m %n");
	consoleAppender.setLayout(patternLayout);
	return consoleAppender;
	}
	 
}
