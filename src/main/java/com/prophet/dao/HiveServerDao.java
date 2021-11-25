package com.prophet.dao;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.prophet.dao.EmailUtil;
import com.prophet.common.ThreadPool;
import com.prophet.dao.task.HiveQueryAsyncTask;

@Repository
public class HiveServerDao {
	@Autowired
	@Qualifier("hiveServerJdbcTemplate")
	private JdbcTemplate jdbcTemplateHiveServer;
	
	@Autowired
	@Qualifier("prophetJdbcTemplate")
	private JdbcTemplate jdbcTemplateProphet;
	
	private EmailUtil emailUtil;
	
	@Autowired
	public void setEmailUtil(EmailUtil emailUtil) {
		this.emailUtil = emailUtil;
	}
	
	public final static int PAGE_ROWS = 20;
	public final static int COL_MAX_CHARS = 100;

	/**
	 * 开启线程向hive发送查询
	 * @param sqlContent
	 * @return 
	 */
	public void sendHiveQuery(String queryContent, String username, long queryHistId, int emailNotify) {
		//开启新的线程去连接hive执行任务
		HiveQueryAsyncTask queryTask = new HiveQueryAsyncTask();
		queryTask.setJdbcTemplateProphet(this.jdbcTemplateProphet);
		queryTask.setJdbcTemplateHiveServer(this.jdbcTemplateHiveServer);
		queryTask.setQueryContent(queryContent);
		queryTask.setUsername(username);
		queryTask.setQueryHistId(queryHistId);
		queryTask.setEmailNotify(emailNotify);
		queryTask.setEmailUtil(this.emailUtil);
		
		//Future<Map<String, Object>> taskFuture = ThreadExecutor.submit(queryTask);
		ThreadPool.executeHiveQuery(queryTask);
		
	}
	
	/**
	 * 查询表结构信息，同步方法
	 * @param tableNameWithDb
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> descTableInfo(String tableNameWithDb) throws Exception {
		List<Map<String, Object>> daoResult = new ArrayList<Map<String, Object>>();
		String sql = "describe `" + tableNameWithDb + "`";
		List<Map<String, Object>> result = jdbcTemplateHiveServer.queryForList(sql);
		
		for (Map<String, Object> line : result) {
			
			//处理comment
			if (line.containsKey("comment") && line.get("comment") != null) {
				String comment = line.get("comment").toString();
				if (comment.contains("%")) {
					// 将application/x-www-from-urlencoded字符串转换成普通字符串    
			        line.put("comment", URLDecoder.decode(comment, "UTF-8"));
				} else {
					//正常字符编码不用修改
				}
			}
			daoResult.add(line);
		}
		return daoResult;
	}
	
	/**
	 * 从磁盘上获取历史查询结果，分页获取
	 * @param username
	 * @param queryHistId
	 * @return 数据结构：
	 * 		{
	 * 		"type":"sql_query",
	 * 		"data":{
	 * 			"result_cols":[{"col_name":"col1", "col_width":40}],
	 * 			"result_data":[]
	 * 		}
	 * （size会在service层加上）
	 * @throws IOException
	 */
	public Map<String, Object> getResultFromDiskByIdByPage(String username, long queryHistId, int pageNo, int pageRows) throws Exception{
		Map<String, Object> resultWithType = new HashMap<String, Object>();
		Map<String, Object> result = new HashMap<String, Object>();
		
		List<Map<String, Object>> columns = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		
		File dataFile = new File(com.prophet.config.HiveResultTextConfig.getDataFileName(username, queryHistId));
		File metaFile = new File(com.prophet.config.HiveResultTextConfig.getMetaFileName(username, queryHistId));
		if (dataFile.isFile() && metaFile.isFile()) {
			
			List<String> listColumns = FileUtils.readLines(metaFile, "UTF-8");
			if (listColumns != null && listColumns.size() >= 1) {
				//先拼装列
				//理论上meta文件只有一行
				String[] splitCols = listColumns.get(0).split(com.prophet.config.HiveResultTextConfig.HIVE_RESULT_FIELD_DELIMITER);
				for (String col : splitCols) {
					Map<String, Object> colInfo = new HashMap<String, Object>();
					colInfo.put("col_name", col);
					colInfo.put("col_width", -1);
					columns.add(colInfo);
				}
						
				//再拼装数据
				List<String> listData = new ArrayList<String>();
				LineIterator it = null;
				try {
					it = FileUtils.lineIterator(dataFile, "UTF-8");
					int startLineNo = 0;				//闭区间

					//先移动startLineNo的指针
					while (it.hasNext() && startLineNo < (pageNo - 1) * pageRows) {
						it.nextLine();
						startLineNo++;
					}
					int endLineNo = startLineNo;		//闭区间
					//再移动endLineNo的指针
					while (it.hasNext() && endLineNo < (pageNo) * pageRows) {
						String line = it.nextLine();
						listData.add(line);
						endLineNo++;
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				} finally {
					LineIterator.closeQuietly(it);
				}
				
				//组装成前端需要的json数据
				if (listData != null && listData.size() >= 1) {
					for (String line : listData) {
						String[] fields = line.split(com.prophet.config.HiveResultTextConfig.HIVE_RESULT_FIELD_DELIMITER);
						//遍历每一个列的数据，加入HashMap
						Map<String, Object> lineMap = new HashMap<String, Object>();
						for (int i=0; i<fields.length; i++) {
							lineMap.put(columns.get(i).get("col_name").toString(), fields[i]);
							//判断该列最长长度并更新，每1个字符10个px
							int colChars = fields[i].length() > columns.get(i).get("col_name").toString().length() ? 
									fields[i].length()  : columns.get(i).get("col_name").toString().length();
							//还要拿每一行里该列的宽度和列头以及最大列宽对比下，取最大
							if (colChars > COL_MAX_CHARS) {
								colChars = COL_MAX_CHARS;
							}
							Map<String, Object> newColInfo = new HashMap<String, Object>();
							newColInfo.put("col_name", columns.get(i).get("col_name").toString());
							newColInfo.put("col_width", colChars * 8);
							columns.set(i, newColInfo);
						}

						data.add(lineMap);
					}
				}
			} else {
				//否则columns继续保持空
			}
		} else {
			throw new Exception(String.format("历史查询结果文件已经不存在，请重新发起查询！查询语句id：%d", queryHistId));
		}
		result.put("result_cols", columns);
		result.put("result_data", data);
		resultWithType.put("type", "sql_query");
		resultWithType.put("data", result);
		return resultWithType;
	}
	
}
