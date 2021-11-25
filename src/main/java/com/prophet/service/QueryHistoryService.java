package com.prophet.service;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prophet.dao.QueryHistoryDao;
import com.prophet.util.DateTimeUtil;
import com.prophet.domain.QueryHistory;
import com.prophet.common.QueryHistoryStatusEnum;

@Service
public class QueryHistoryService extends BaseService{
	private QueryHistoryDao queryHistoryDao;

	public QueryHistoryDao getQueryHistoryDao() {
		return queryHistoryDao;
	}

	@Autowired
	public void setQueryHistoryDao(QueryHistoryDao queryHistoryDao) {
		this.queryHistoryDao = queryHistoryDao;
	}
	
	/**
	 * 向prophet的数据库中插入一条查询历史
	 */
	public Map<String, Object> insertOneQueryHistory(String queryContent, String username, int emailNotify) {
		Map<String, Object> serviceResult = this.initServiceResult();
		long insertId = -1;
		try {
			insertId = this.queryHistoryDao.insertQueryHistory(DateTimeUtil.getNow(), queryContent, QueryHistoryStatusEnum.RUNNING.getIndex(), username, emailNotify);
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
		}
		//更新data应放在return前一句，防止单次session里在下次更新data之前出现了异常而没来得及更新data，从而返回了之前的data.
		serviceResult.put("data", insertId);
		return serviceResult;
	}
	
	/**
	 * 获取该用户查询历史
	 * @param username
	 * @return
	 */
	public Map<String, Object> getAllQueryHistoryByUser(String username) {
		Map<String, Object> serviceResult = this.initServiceResult();
		List<QueryHistory> daoResult = null;
		try {
			daoResult = this.queryHistoryDao.getAllQueryHistory(username);
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
		}
		serviceResult.put("data", daoResult);
		return serviceResult;
	}
	
	public Map<String, Object> getQueryHistoryById(long id) {
		Map<String, Object> serviceResult = this.initServiceResult();
		QueryHistory qh = null;
		try {
			qh = this.queryHistoryDao.getQueryHistoryById(id);
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
		}
		serviceResult.put("data", qh);
		return serviceResult;
	}
	
	/**
	 * 更新某个查询任务历史的状态
	 * @param queryHistId
	 * @param status
	 */
	public void updateQueryHistoryStatusAndMsg(long queryHistId, QueryHistoryStatusEnum status, String message) {
		this.queryHistoryDao.updateQueryHistoryStatusAndMsg(queryHistId, 
				status.getIndex(), message);
	}
	
}
