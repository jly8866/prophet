package com.prophet.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prophet.dao.HiveMetaStoreDao;

@Service
public class HiveMetaStoreService extends BaseService{
	private HiveMetaStoreDao hiveMetaStoreDao;

	public HiveMetaStoreDao getHiveMetaStoreDao() {
		return hiveMetaStoreDao;
	}

	@Autowired
	public void setHiveMetaStoreDao(HiveMetaStoreDao hiveMetaStoreDao) {
		this.hiveMetaStoreDao = hiveMetaStoreDao;
	}

	/**
	 * 查询metastore中所有的库名和对应包含的表名列表
	 * 返回数据结构dbTableResult：{
	 *    'default':[
	 *      {'TBL_ID':21, 'TBL_NAME':'access_path1', 'TBL_TYPE':'EXTERNAL_TABLE'},
	 *      {'TBL_ID':22, 'TBL_NAME':'access_path2', 'TBL_TYPE':'EXTERNAL_TABLE'},
	 *      {'TBL_ID':23, 'TBL_NAME':'access_path3', 'TBL_TYPE':'MANAGED_TABLE'}
	 *      ...
	 *    ],
	 *    'formatter':[
	 *      {'TBL_ID':24, 'TBL_NAME':'access_path4', 'TBL_TYPE':'EXTERNAL_TABLE'},
	 *      {'TBL_ID':25, 'TBL_NAME':'access_path5', 'TBL_TYPE':'EXTERNAL_TABLE'}
	 *    ]
	 * }
	 * */
	public Map<String, Object> getAllDbAndTablesInMetaStore() {
		Map<String, Object> serviceResult = this.initServiceResult();
		HashMap<String, ArrayList<HashMap<String, Object>>> dbTableResult = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		List<Map<String, Object>> daoResult = null;
		try {
			daoResult = this.hiveMetaStoreDao.getAllDbAndTablesInMetaStore();
			for (Map<String, Object> line : daoResult) {
				//如果数据里的db在当前db组内
				ArrayList<HashMap<String, Object>> previousList = dbTableResult.get(line.get("DB_NAME"));
				if (previousList == null) {
					//如果结果集里没有该db，则需要初始化一个空ArrayList出来；否则直接加入即可
					previousList = new ArrayList<HashMap<String, Object>>();
				}
				HashMap<String, Object> currLine = new HashMap<String, Object>();
				currLine.put("TBL_ID", line.get("TBL_ID"));
				currLine.put("TBL_NAME", line.get("TBL_NAME"));
				currLine.put("TBL_TYPE", line.get("TBL_TYPE"));
				previousList.add(currLine);
				
				//覆盖性写入
				dbTableResult.put(line.get("DB_NAME").toString(), previousList);
			}
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
		}
		serviceResult.put("data", dbTableResult);
		return serviceResult;
	}
}
