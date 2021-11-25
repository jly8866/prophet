package com.prophet.service;

import java.util.HashMap;
import java.util.Map;

/**
 * 所有Service类的基类
 * 背景：所有DAO层（除多线程包装的之外）会直接向service层返回数据和抛出异常，需要在service层进行捕获并封装成一个Map返回给controller层
 * Map结构：{"msg":"ok", "data":null}
 */
public class BaseService {
	/*protected ThreadLocal<Map<String, Object>> serviceResult = new ThreadLocal<Map<String, Object>>(){
		@Override 
		protected Map<String, Object> initialValue() {
			Map<String, Object> serviceResult = new HashMap<String, Object>();
			serviceResult.put("msg", "ok");
			serviceResult.put("data", null);
			return serviceResult;
		}
	};*/
	
	protected Map<String, Object> initServiceResult() {
		Map<String, Object> serviceResult = new HashMap<String, Object>();
		serviceResult.put("msg", "ok");
		serviceResult.put("data", null);
		return serviceResult;
	}
}
