package com.prophet.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HiveMetaStoreDao {
	@Autowired
	@Qualifier("hiveMetaStoreJdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	/**
	 * 获取所有metastore里的hive表
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getAllDbAndTablesInMetaStore() throws Exception{
		String sql = "select DBS.NAME as DB_NAME,TBLS.TBL_ID,TBLS.TBL_NAME,TBLS.TBL_TYPE from TBLS,DBS where TBLS.DB_ID=DBS.DB_ID;";
		return jdbcTemplate.queryForList(sql);
	}
	
}
