package com.prophet.service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prophet.dao.HiveSecretTableDao;
import com.prophet.dao.HiveSecretUserPrivsDao;
import com.prophet.domain.HiveSecretTable;
import com.prophet.dao.HiveMetaStoreDao;

@Service
public class HiveSecretDataService extends BaseService{
	private HiveSecretTableDao hiveSecretTableDao;
	private HiveSecretUserPrivsDao hiveSecretUserPrivsDao;
	private HiveMetaStoreDao hiveMetaStoreDao;
	
	@Autowired
	public void setHiveSecretTableDao(HiveSecretTableDao hiveSecretTableDao) {
		this.hiveSecretTableDao = hiveSecretTableDao;
	}

	@Autowired
	public void setHiveSecretUserPrivsDao(HiveSecretUserPrivsDao hiveSecretUserPrivsDao) {
		this.hiveSecretUserPrivsDao = hiveSecretUserPrivsDao;
	}

	@Autowired
	public void setHiveMetaStoreDao(HiveMetaStoreDao hiveMetaStoreDao) {
		this.hiveMetaStoreDao = hiveMetaStoreDao;
	}

	/**
	 * 验证某个表是否为机密表
	 * @return
	 */
	public List<Map<String, Object>> checkIsSecretTable(String tableSchema, String tableName) {
		return this.hiveSecretTableDao.checkIsSecretTable(tableSchema, tableName);
	}
	
	/**
	 * 检查某个表某个用户是否有权限
	 * @param username
	 * @param tableSchema
	 * @param tableName
	 * @return
	 */
	public boolean checkPrivilege(String username, String tableSchema, String tableName) {
		List<Map<String, Object>> daoResult = this.hiveSecretUserPrivsDao.checkSecretPrivilege(username, tableSchema, tableName);
		if (daoResult.size() == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 所有机密表面板，顺便展示哪些是当前用户有权限的
	 * @param username
	 * @return
	 * 返回数据结构dbTableResult：{
	 *    'default':[
	 *      {'table_id':21, 'table_name':'access_path1', 'info':'...'},
	 *      {'table_id':22, 'table_name':'access_path2', 'info':null},
	 *      ...
	 *    ],
	 *    'users':[
	 *      {'table_id':24, 'table_name':'access_path5', 'info':'...'},
	 *      {'table_id':25, 'table_name':'access_path6', 'info':null},
	 *      ...
	 *    ],
	 * }
	 */
	public Map<String, Object> getAllSecretTablesByUser(String username) {
		Map<String, Object> serviceResult = this.initServiceResult();
		Map<String, ArrayList<HashMap<String, Object>>> dbTableResult = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		List<Map<String, Object>> daoResult = null;
		try {
			daoResult = this.hiveSecretTableDao.getAllSecretTablesByUser(username);
			for (Map<String, Object> line : daoResult) {
				//如果数据里的db在当前db组内
				ArrayList<HashMap<String, Object>> previousList = dbTableResult.get(line.get("table_schema"));
				if (previousList == null) {
					//如果结果集里没有该db，则需要初始化一个空ArrayList出来；否则直接加入即可
					previousList = new ArrayList<HashMap<String, Object>>();
				}
				HashMap<String, Object> currLine = new HashMap<String, Object>();
				currLine.put("table_id", line.get("table_id"));
				currLine.put("table_name", line.get("table_name"));
				currLine.put("info", line.get("info"));
				previousList.add(currLine);
				
				//覆盖性写入
				dbTableResult.put(line.get("table_schema").toString(), previousList);
			}
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
		}
		serviceResult.put("data", dbTableResult);
		return serviceResult;
	}
	
	public Map<String, Object> getAllNonSecretTables() {
		Map<String, Object> serviceResult = this.initServiceResult();
		List<Map<String, Object>> nonSecretTables = null;
		try {
			nonSecretTables = this.hiveSecretTableDao.getAllNonSecretTables();
			
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
		}
		serviceResult.put("data", nonSecretTables);
		return serviceResult;
	}
	
	/**
	 * 增加机密数据表
	 * @param targetSecretTables
	 * @return
	 */
	public Map<String, Object> addSecretTables(List<String> targetSecretTables) {
		Map<String, Object> serviceResult = this.initServiceResult();
		int data = 1;
		List<HiveSecretTable> secretTables = new ArrayList<HiveSecretTable>();
		for (String dbAndTable : targetSecretTables) {
			String[] s = dbAndTable.split("\\.");
			HiveSecretTable h = new HiveSecretTable();
			h.setTableSchema(s[0]);
			h.setTableName(s[1]);
			secretTables.add(h);
		}
		try {
			this.hiveSecretTableDao.addSecretTables(secretTables);
			
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
			data = -1;
		}
		serviceResult.put("data", data);
		return serviceResult;
	}
	
	/**
	 * 获取所有机密表
	 * @return
	 */
	public Map<String, Object> getAllSecretTables() {
		Map<String, Object> serviceResult = this.initServiceResult();
		List<Map<String, Object>> daoResult = null;
		
		try {
			daoResult = this.hiveSecretTableDao.getAllSecretTables();
			
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
		}
		serviceResult.put("data", daoResult);
		return serviceResult;
	}
	
	/**
	 * 给用户批量授权机密表权限
	 * @param username
	 * @param secretTableIds
	 * @return
	 */
	public Map<String, Object> grantSecretPrivToUser(String username, List<Integer> secretTableIds) {
		Map<String, Object> serviceResult = this.initServiceResult();
		try {
			for (Integer id : secretTableIds) {
				this.hiveSecretUserPrivsDao.insertOneUserSecretPriv(username, id);
			}
			
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
		}
		serviceResult.put("data", null);
		return serviceResult;
	}
	
}
