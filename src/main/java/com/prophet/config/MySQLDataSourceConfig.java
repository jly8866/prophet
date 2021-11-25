package com.prophet.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class MySQLDataSourceConfig {
	@Bean(name="prophetDS")
	@Primary 
	@ConfigurationProperties(prefix="spring.ds_prophet")
	public DataSource prophetMysqlDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean(name="prophetJdbcTemplate")
	public JdbcTemplate getProphetJdbcTemplate(@Qualifier("prophetDS") DataSource dsProphet) {
		return new JdbcTemplate(dsProphet);
	}
	
	@Bean(name="hiveMetaStoreDS")
	@ConfigurationProperties(prefix="spring.ds_hive_metastore")
	public DataSource hiveMetaStoreMysqlDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean(name="hiveMetaStoreJdbcTemplate")
	public JdbcTemplate getHiveMetaStoreJdbcTemplate(@Qualifier("hiveMetaStoreDS") DataSource dsHive) {
		return new JdbcTemplate(dsHive);
	}
	
}
