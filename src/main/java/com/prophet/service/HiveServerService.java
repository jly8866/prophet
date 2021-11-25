package com.prophet.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prophet.dao.HiveServerDao;
import com.prophet.dao.QueryHistoryDao;
import com.prophet.common.QueryHistoryStatusEnum;
import com.prophet.common.ThreadPool;


@Service
public class HiveServerService extends BaseService{
	private HiveServerDao hiveServerDao;
	private QueryHistoryDao queryHistoryDao;

	@Autowired
	public void setHiveServerDao(HiveServerDao hiveServerDao) {
		this.hiveServerDao = hiveServerDao;
	}

	@Autowired
	public void setQueryHistoryDao(QueryHistoryDao queryHistoryDao) {
		this.queryHistoryDao = queryHistoryDao;
	}

	/**
	 * 向metastore数据字典查询表结构信息
	 * @return
	 */
	public Map<String, Object> descTable(String tableNameWithDb) {
		Map<String, Object> serviceResult = this.initServiceResult();
		Map<String, Object> colsAndData = new HashMap<String, Object>();
		
		Map<String, Object> dataWithType = new HashMap<String, Object>();
		dataWithType.put("type", "desc_table");
		dataWithType.put("data", colsAndData);
		List<Map<String, Object>> daoResult = null;
		try {
			daoResult = this.hiveServerDao.descTableInfo(tableNameWithDb);
			Set<String> columnSet = null;
			if (!daoResult.isEmpty()) {
				columnSet = daoResult.get(0).keySet();
			} else {
				serviceResult.put("msg", String.format("从hiveserver里获取的%s表结构信息错误!请检查hiveserver...", tableNameWithDb));
			}
			colsAndData.put("result_cols", columnSet);
			colsAndData.put("result_data", daoResult);
			
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
		}
		//更新data应放在return前一句，防止单次session里在下次更新data之前出现了异常而没来得及更新data，从而返回了之前的data.
		serviceResult.put("data", dataWithType);
		return serviceResult;
	}
	
	
	/**
	 * 利用线程池开启线程, 异步查询hive
	 * @param queryContent
	 * @param username
	 * @return
	 */
	public Map<String, Object> executeHiveSqlQuery(String queryContent, String username, long queryHistId, int emailNotify) {
		Map<String, Object> serviceResult = this.initServiceResult();
		try {
			//向hiveserver发送query
			this.hiveServerDao.sendHiveQuery(queryContent, username, queryHistId, emailNotify);
		} catch (Exception ex) {
			//这里仅会获取提交线程失败之类的异常，底层异常已被子线程捕获和处理了
			serviceResult.put("msg", ex.getMessage());
		}
		
		return serviceResult;
	}
	
	/**
	 * 从磁盘上获取某个查询的历史结果
	 * @param username
	 * @param queryHistId
	 * @return serviceResult：
	 * 	"msg":"ok",
	 *  "data":
	 * 		{
	 * 		"type":"sql_query",
	 * 		"data":{
	 * 			"result_cols":[],
	 * 			"result_data":[]
	 * 			},
	 * 		"size":300
	 * 		}
	 */
	public Map<String, Object> getHistoryResultFromDiskById(String username, long queryHistId, int pageNo) {
		Map<String, Object> serviceResult = this.initServiceResult();
		Map<String, Object> daoResult = null;
		try {
			daoResult = this.hiveServerDao.getResultFromDiskByIdByPage(username, queryHistId, pageNo, HiveServerDao.PAGE_ROWS);
			int resultSize = this.queryHistoryDao.getQueryHistoryById(queryHistId).getResultSize();
			daoResult.put("size", resultSize);
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
		}
		
		serviceResult.put("data", daoResult);
		return serviceResult;
	}
	
	/**
	 * 手动取消任务
	 * @param queryHistId
	 * @return
	 */
	public Map<String, Object> cancelTaskById (String username, long queryHistId) {
		Map<String, Object> serviceResult = this.initServiceResult();
		try {
			//服务端验证：不能随意取消别人的任务
			if (this.queryHistoryDao.getQueryHistoryById(queryHistId).getUsername().equals(username)) {
				//更新任务状态
				this.queryHistoryDao.updateQueryHistoryStatusAndMsg(queryHistId, 
						QueryHistoryStatusEnum.ABORTED.getIndex(), QueryHistoryStatusEnum.ABORTED.getName());
				
				//终止线程
				ThreadPool.stopThread(queryHistId);
			} else {
				serviceResult.put("msg", "该语句id不属于你，无法取消");
			}
			
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
		}
		
		serviceResult.put("data", null);
		return serviceResult;
	}
}
