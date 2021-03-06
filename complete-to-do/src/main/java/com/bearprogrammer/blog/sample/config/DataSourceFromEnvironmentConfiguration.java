package com.bearprogrammer.blog.sample.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class DataSourceFromEnvironmentConfiguration {
	
	Logger logger = LoggerFactory.getLogger(DataSourceFromEnvironmentConfiguration.class);
	
	@Autowired
	Environment env;
	
	@Bean
	public DataSource dataSource() {
		logger.debug("Initializing database: {}", env.getProperty("jdbc.url"));
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		
		dataSource.setDriverClassName(env.getProperty("jdbc.driver"));
		dataSource.setUrl(env.getProperty("jdbc.url"));
		dataSource.setUsername(env.getProperty("jdbc.username"));
		dataSource.setPassword(env.getProperty("jdbc.password"));
		
		return dataSource;
	}

}
