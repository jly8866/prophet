package com.prophet.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.prophet.domain.HiveSecretTable;

@Repository
public class HiveSecretTableDao {
	@Autowired
	@Qualifier("prophetJdbcTemplate")
	private JdbcTemplate jdbcTemplateProphet;
	
	@Autowired
	@Qualifier("hiveMetaStoreJdbcTemplate")
	private JdbcTemplate jdbcTemplateMetaStore;
	
	public List<Map<String, Object>> checkIsSecretTable(String tableSchema, String tableName) {
		String sql = "select id, table_schema, table_name from hive_secret_tables where table_schema=? and table_name=?";
		Object[] args = {tableSchema, tableName};
		return jdbcTemplateProphet.queryForList(sql, args);
	}
	
	/**
	 * 所有机密表面板，顺便展示哪些是当前用户有权限的
	 * @param username
	 * @return
	 */
	public List<Map<String, Object>> getAllSecretTablesByUser(String username) {
		/*
		 * 注意这里是b.username是在on里不是在where，这样才能在join时就把数据连接出来.
		 * 因为在使用left jion时，on和where条件的区别如下：
		 * 1、 on条件是在生成临时表时使用的条件，它不管on中的条件是否为真，都会返回左边表中的记录。
		 * 2、where条件是在临时表生成好后，再对临时表进行过滤的条件。这时已经没有left join的含义（必须返回左边表的记录）了，条件不为真的就全部过滤掉。
		 * 不管on上的条件是否为真都会返回left或right表中的记录，full则具有left和right的特性的并集。 而inner jion没这个特殊性，则条件放在on中和where中，返回的结果集是相同的。
		 * 详见 https://www.cnblogs.com/fuge/archive/2011/12/26/2342576.html
		 */
		String sql = "select a.id as table_id, a.table_schema, a.table_name, if(b.username=?,'您已具有查询权限',null) as info "
				+ "from hive_secret_tables a left join hive_secret_user_privs b on a.id=b.hive_secret_table_id and b.username=? ";
		Object[] args = {username, username};
		return jdbcTemplateProphet.queryForList(sql, args);
	}
	
	/**
	 * 从prophet数据库和metastore里获取所有的非机密数据表，需要动态拼接sql里的in值
	 * 用到了线程安全的StringBuffer，以及MySQL的where (a,b) not in ((1,2),(3,4),(5,6))语法
	 * @return
	 */
	public List<Map<String, Object>> getAllNonSecretTables() {
		String sqlSecret = "select id,table_schema,table_name from hive_secret_tables";
		List<Map<String, Object>> secretTables = jdbcTemplateProphet.queryForList(sqlSecret);
		
		StringBuffer sqlMetastore = new StringBuffer("select DBS.NAME as DB_NAME,TBLS.TBL_ID,TBLS.TBL_NAME,TBLS.TBL_TYPE from TBLS,DBS "
				+ "where TBLS.DB_ID=DBS.DB_ID and (DBS.NAME,TBLS.TBL_NAME) not in (" );

		if (secretTables.size() == 0) {
			sqlMetastore.append("('','')");
		} else {
			for (int i = 0; i < secretTables.size(); i++) {
				if (i != 0) {
					sqlMetastore.append(",");
				}
				sqlMetastore.append(String.format("('%s','%s')", secretTables.get(i).get("table_schema").toString(), secretTables.get(i).get("table_name").toString()));
			}
		}
		sqlMetastore.append(") order by DBS.NAME,TBLS.TBL_NAME");
		
		return this.jdbcTemplateMetaStore.queryForList(sqlMetastore.toString());
	}
	
	/**
	 * 通过batchUpdate批量插入机密数据表记录
	 * @param secretTables
	 */
	public void addSecretTables(List<HiveSecretTable> secretTables) {
		String sql = "insert into hive_secret_tables(table_schema,table_name) values(?,?)";
		this.jdbcTemplateProphet.batchUpdate(sql, new BatchPreparedStatementSetter(){

			@Override
			public int getBatchSize() {
				return secretTables.size();
			}

			@Override
			public void setValues(PreparedStatement ps, int index) throws SQLException {
				HiveSecretTable h = secretTables.get(index);
				ps.setString(1, h.getTableSchema());
				ps.setString(2, h.getTableName());
			}
			
		});
	}
	
	/**
	 * 获取所有机密表
	 * @return
	 */
	public List<Map<String, Object>> getAllSecretTables() {
		String sql = "select id, table_schema, table_name from hive_secret_tables order by table_schema,table_name";
		return this.jdbcTemplateProphet.queryForList(sql);
	}
}
