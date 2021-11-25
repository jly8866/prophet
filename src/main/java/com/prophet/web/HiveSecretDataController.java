package com.prophet.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prophet.service.HiveSecretDataService;
import com.prophet.service.UserAuthService;

@RestController
public class HiveSecretDataController extends BaseController{
	private HiveSecretDataService hiveSecretDataService;
	private UserAuthService userAuthService;
	
	@Autowired
	public void setHiveSecretDataService(HiveSecretDataService hiveSecretDataService) {
		this.hiveSecretDataService = hiveSecretDataService;
	}
	
	@Autowired
	public void setUserAuthService(UserAuthService userAuthService) {
		this.userAuthService = userAuthService;
	}

	@RequestMapping(value = "/hive_secret/get_all_secrets.json", method = RequestMethod.GET)
	public Map<String, Object> getAllSecretsByUser(HttpServletRequest request) {
		Map<String, Object> serviceResult = this.hiveSecretDataService.getAllSecretTablesByUser(this.getLoginUserInfo(request).get("loginedUser").toString());
		return this.encodeToJsonResult(serviceResult);
	}
	
	@RequestMapping(value = "/hive_secret/get_all_non_secrets.json", method = RequestMethod.GET)
	public Map<String, Object> getAllNonSecrets(HttpServletRequest request) {
		Map<String, Object> serviceResult = this.hiveSecretDataService.getAllNonSecretTables();
		return this.encodeToJsonResult(serviceResult);
	}
	
	@RequestMapping(value = "/hive_secret/add_secret_tables.json", method = RequestMethod.POST)
	public Map<String, Object> addSecretTables(HttpServletRequest request, @RequestParam("targetSecretTables") List<String> targetSecretTables) {
		Map<String, Object> serviceResult = this.hiveSecretDataService.addSecretTables(targetSecretTables);
		return this.encodeToJsonResult(serviceResult);
	}
	
	@RequestMapping(value = "/hive_secret/get_all_secret_tables.json", method = RequestMethod.GET)
	public Map<String, Object> getAllSecretTables(HttpServletRequest request) {
		Map<String, Object> serviceResult = this.hiveSecretDataService.getAllSecretTables();
		return this.encodeToJsonResult(serviceResult);
	}
	
	@RequestMapping(value = "/hive_secret/grant_user_priv.json", method = RequestMethod.POST)
	public Map<String, Object> grantSecretPriv(HttpServletRequest request, @RequestParam("targetSecretTables") List<Integer> targetSecretTables, 
																			@RequestParam("username") String username) {
		//先检查用户是否存在
		boolean hasUser = false;
		try {
			hasUser = this.userAuthService.hasUser(username);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (hasUser == false) {
			Map<String, Object> restfulResult = new HashMap<String, Object>();
			restfulResult.put("status", 1);
			restfulResult.put("message", String.format("%s用户在系统里不存在, 请重新输入!", username));
			restfulResult.put("data", null);
			return restfulResult;
		} else {
			//授权
			return this.encodeToJsonResult(this.hiveSecretDataService.grantSecretPrivToUser(username, targetSecretTables));
		}
		
	}
}
