package com.prophet.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.prophet.domain.QueryHistory;
import com.prophet.common.QueryHistoryStatusEnum;

@Repository
public class QueryHistoryDao {
	@Autowired
	@Qualifier("prophetJdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	/**
	 * 插入一条查询历史
	 * @param queryTime
	 * @param queryContent
	 * @param status
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public long insertQueryHistory(String queryTime, String queryContent, int status, String username, int emailNotify) throws Exception {
		String sql = "insert into query_history(query_time, query_content, status, username, email_notify) "
				+ "values(?, ?, ?, ?, ?)";
		
		//获取数据库自动生成的主键值
		KeyHolder keyHolder = new GeneratedKeyHolder();
		this.jdbcTemplate.update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
				PreparedStatement ps = (PreparedStatement)conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, queryTime);
				ps.setString(2, queryContent);
				ps.setInt(3, status);
				ps.setString(4, username);
				ps.setInt(5, emailNotify);
				return ps;
			}
			
		}, keyHolder);
		return keyHolder.getKey().longValue();
	
	}
	
	/**
	 * 根据用户名获取某个人的查询历史
	 * @param username
	 * @return
	 */
	public List<QueryHistory> getAllQueryHistory(String username) throws SQLException {
		List<QueryHistory> listHistory = new ArrayList<QueryHistory>();
		
		String sql = "select id, query_time, query_content, status, username from query_history "
				+ "where username=? order by query_time desc limit 20";
		Object[] args = {username};
		this.jdbcTemplate.query(sql, args, new RowCallbackHandler(){
			
			public void processRow(ResultSet rs) throws SQLException {
				QueryHistory q = new QueryHistory();
				q.setId(rs.getInt("id"));
				q.setQueryTime(rs.getString("query_time"));
				q.setQueryContent(rs.getString("query_content"));
				q.setStatus(rs.getInt("status"));
				try {
					q.setStrStatus(QueryHistoryStatusEnum.getNameByIndex(rs.getInt("status")));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				q.setUsername(rs.getString("username"));
				listHistory.add(q);
			}
			
		});
		
		return listHistory;
	}
	
	/**
	 * 更改查询历史记录状态值和消息
	 * @param id
	 * @param status
	 * @param message
	 * @return
	 */
	public int updateQueryHistoryStatusAndMsg(long id, int status, String message) {
		String sql = "update query_history set status=?,message=? where id=?";
		Object[] args = {status, message,id};
		return this.jdbcTemplate.update(sql, args);
	}
	
	/**
	 * 根据id获取QueryHistory对象，可以进一步获得状态、SQL、查询时间等信息
	 * @param id
	 * @return
	 */
	public QueryHistory getQueryHistoryById(long id) {
		String sql = "select id, query_time, query_content, status, username, email_notify, result_size, message from query_history where id=?";
		Object[] args = {id};
		QueryHistory q = new QueryHistory();
		this.jdbcTemplate.query(sql, args, new RowCallbackHandler(){
			
			public void processRow(ResultSet rs) throws SQLException {
				q.setId(rs.getLong("id"));
				q.setQueryTime(rs.getString("query_time"));
				q.setQueryContent(rs.getString("query_content"));
				q.setStatus(rs.getInt("status"));
				try {
					q.setStrStatus(QueryHistoryStatusEnum.getNameByIndex(rs.getInt("status")));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				q.setUsername(rs.getString("username"));
				q.setEmailNotify(rs.getInt("email_notify"));
				q.setResultSize(rs.getInt("result_size"));
				q.setMessage(rs.getString("message"));
			}
			
		});
		return q;
	}
	
	/**
	 * 保存结果集行数
	 * @param queryHistId
	 * @param resultSize
	 * @return
	 */
	public int saveResultSizeById(long queryHistId, int resultSize) {
		String sql = "update query_history set result_size=? where id=?";
		Object[] args = new Object[]{resultSize, queryHistId};
		return this.jdbcTemplate.update(sql, args);
	}
}
