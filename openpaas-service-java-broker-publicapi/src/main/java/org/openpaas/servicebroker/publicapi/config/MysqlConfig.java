package org.openpaas.servicebroker.publicapi.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
//import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


@Configuration
@PropertySource("classpath:datasource.properties")
public class MysqlConfig {

//	@Value("${jdbc.driver}")
//	private String jdbcDriver;
	
	@Autowired
	private Environment env;
//	@Bean
//	public PropertyPlaceholderConfigurer properties() {
//		PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
//		Resource[] resources = new ClassPathResource[] { 
//				new ClassPathResource("mysql.properties")
//		};
//		propertyPlaceholderConfigurer.setLocations(resources);
//		propertyPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
//		return propertyPlaceholderConfigurer;
//	}
//	@Bean
//	public JdbcTemplate jdbcTemplate() throws UnknownHostException {
//		return new JdbcTemplate();
//	}
	
	//@Override
	@Bean
    public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
//		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
//		dataSource.setDriverClassName(env.getRequiredProperty("JDBC.Driver"));
//		dataSource.setUrl(env.getProperty("JDBC.Url"));
//		dataSource.setUsername(env.getProperty("JDBC.Username"));
//		dataSource.setPassword(env.getProperty("JDBC.Password"));

		return dataSource;
        // instantiate, configure and return DataSource
    }
}
