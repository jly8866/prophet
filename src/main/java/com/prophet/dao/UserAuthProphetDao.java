package com.prophet.dao;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import com.prophet.interfaces.UserAuthDaoInterface;
import com.prophet.common.Encryptor;

/**
 * prophet内置用户认证基础类
 *
 */
public class UserAuthProphetDao implements UserAuthDaoInterface{
	private JdbcTemplate jdbcTemplate;
	
	public UserAuthProphetDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@Override
	public int authenticate(String username, String password){
		String sql = "select id,username from prophet_users where username=? and password=? and is_active=1";
		Object[] args = {username, Encryptor.encryptSHA(password)};
		return (this.jdbcTemplate.queryForList(sql, args).size() == 1) ? 0 : 1;
	}

	@Override
	public boolean hasUser(String username) {
		String sql = "select id,username from prophet_users where username=? and is_active=1";
		Object[] args = {username};
		return (this.jdbcTemplate.queryForList(sql, args).size() == 1) ? true : false;
	}
	
	/**
	 * 获取所有prophet用户的信息
	 * @return
	 */
	public List<Map<String, Object>> getAllProphetUsers() {
		String sql = "select id,username,is_active,user_type,create_time from prophet_users;";
		return this.jdbcTemplate.queryForList(sql);
	}
	
	
	public int addProphetUser(String username, String password, String isActive, String userType) {
		String sql = "insert into prophet_users (username,password,is_active,user_type)"
				+ " values(?,?,?,?)";
		int active = isActive.equals("true") ? 1 : 0;
		Object[] args = {username, Encryptor.encryptSHA(password), active, userType};
		return this.jdbcTemplate.update(sql, args);
	}
	
	public void deleteUserById(int userId) {
		String sql = "delete from prophet_users where id=?";
		Object[] args = {userId};
		this.jdbcTemplate.update(sql, args);
	}
}
