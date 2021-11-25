package com.prophet.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HiveSecretUserPrivsDao {
	@Autowired
	@Qualifier("prophetJdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	public List<Map<String, Object>> checkSecretPrivilege(String username, String tableSchema, String tableName) {
		String sql = "select a.id from hive_secret_user_privs a, hive_secret_tables b"
				+ " where a.hive_secret_table_id=b.id and a.username=? and b.table_schema=? and b.table_name=?";
		Object[] args = {username, tableSchema, tableName};
		return jdbcTemplate.queryForList(sql, args);
	}
	
	public int insertOneUserSecretPriv(String username, int hiveSecretTableId) {
		String sql = "insert into hive_secret_user_privs(hive_secret_table_id,username) values(?,?)";
		Object[] args = {hiveSecretTableId, username};
		return this.jdbcTemplate.update(sql, args);
	}
}
