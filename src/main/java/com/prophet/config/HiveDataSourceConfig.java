package com.prophet.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class HiveDataSourceConfig {
	@Bean(name="hiveServerDS")
	@ConfigurationProperties(prefix="spring.ds_hive_server")
	public DataSource prophetHiveServerDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean(name="hiveServerJdbcTemplate")
	public JdbcTemplate getHiveServerJdbcTemplate(@Qualifier("hiveServerDS") DataSource dsHiveServer) {
		return new JdbcTemplate(dsHiveServer);
	}
	
}
