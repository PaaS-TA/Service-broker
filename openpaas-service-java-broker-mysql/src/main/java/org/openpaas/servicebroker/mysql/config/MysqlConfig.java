package org.openpaas.servicebroker.mysql.config;

import java.net.UnknownHostException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
//import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Spring boot 구동시 사용하는 JdbcTemplate Bean 를 생성하는 클래스. 
 * 
 * @author 김한종
 *
 */
@Configuration
@PropertySource("classpath:datasource.properties")
public class MysqlConfig {

	@Autowired
	private Environment env;

	@Bean
	public JdbcTemplate jdbcTemplate() throws UnknownHostException {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource());
		return jdbcTemplate;
	}
	
	//@Override
	@Bean
    public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(env.getRequiredProperty("jdbc.driver"));
		dataSource.setUrl(env.getRequiredProperty("jdbc.url"));
		dataSource.setUsername(env.getRequiredProperty("jdbc.username"));
		dataSource.setPassword(env.getRequiredProperty("jdbc.pwd"));

		return dataSource;
    }
	
}
