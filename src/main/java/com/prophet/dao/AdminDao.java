package com.prophet.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminDao {
	@Autowired
	@Qualifier("prophetJdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	public List<Map<String, Object>> checkIsAdmin(String username){
		String sql = "select id,username from admins where username=?";
		Object[] args = {username};
		return this.jdbcTemplate.queryForList(sql, args);
	}
	
	public int insertOneAdmin(String username) {
		String sql = "insert into admins(username) values(?)";
		Object[] args = {username};
		return this.jdbcTemplate.update(sql, args);
	}
}
