package com.prophet.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prophet.service.UserAuthService;


@RestController
public class UserController extends BaseController{
	private UserAuthService userAuthService;
	
	@Autowired
	public void setUserAuthService(UserAuthService userAuthService) {
		this.userAuthService = userAuthService;
	}

	/**
	 * 登录接口
	 * @param request
	 * @param username
	 * @param password
	 * @return
	 */
	@RequestMapping(value = "/login.json", method = RequestMethod.POST)
	public Map<String, Object> loginController(HttpServletRequest request, @RequestParam("username") String username, @RequestParam("password") String password) {
		Map<String, Object> serviceResult = this.userAuthService.authenticate(username, password);
		
		if ((int)(serviceResult.get("data")) == 0) {
			//认证成功，则登录
			HttpSession session = request.getSession();
			//检查该用户是否为admin并设置session
			int isAdmin = -1;
			if (this.userAuthService.isAdmin(username) == true) {
				isAdmin = 1;
			} else {
				isAdmin = 0;
			}
			session.setAttribute("userAuthSystemType", this.userAuthService.getUserAuthSystemType());
			session.setAttribute("isAdmin", isAdmin);
			session.setAttribute("loginedUser", username);
			
		}
		
		return this.encodeToJsonResult(serviceResult);
	}
	
	/**
	 * 退出登录的接口
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/logout.json", method = RequestMethod.POST)
	public Map<String, Object> logoutController(HttpServletRequest request) {
		Map<String, Object> controllerResult = new HashMap<String, Object>();
		
		HttpSession session = request.getSession();
		if (session.getAttribute("loginedUser") != null) {
			session.removeAttribute("loginedUser");
		}
		if (session.getAttribute("isAdmin") != null) {
			session.removeAttribute("isAdmin");
		}
		session.invalidate();
		
		controllerResult.put("status", 0);
		controllerResult.put("message", "ok");
		controllerResult.put("data", null);
		
		return controllerResult;
	}
	
	/**
	 * 从session里获取当前登录的用户名
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/get_login_user.json", method = RequestMethod.GET)
	public Map<String, Object> currLoginUserController(HttpServletRequest request) {
		Map<String, Object> controllerResult = new HashMap<String, Object>();
		Map<String, Object> data = new HashMap<String, Object>();
		controllerResult.put("status", 0);
		controllerResult.put("message", "ok");
		if (this.getLoginUserInfo(request) != null) {
			data.put("isAdmin", this.getLoginUserInfo(request).get("isAdmin"));
			data.put("loginedUser", this.getLoginUserInfo(request).get("loginedUser"));
			data.put("userAuthSystemType", this.getLoginUserInfo(request).get("userAuthSystemType"));
			controllerResult.put("data", this.getLoginUserInfo(request));
		}
		return controllerResult;
	}
	
	@RequestMapping(value = "/get_all_prophet_users.json", method = RequestMethod.GET)
	public Map<String, Object> getAllProphetUsersController(HttpServletRequest request) {
		Map<String, Object> loginUserInfo = this.getLoginUserInfo(request);
		if (
				loginUserInfo.get("isAdmin").toString().equals("0") || 
				!loginUserInfo.get("userAuthSystemType").toString().equals("prophet")
			) {
			Map<String, Object> restfulResult = new HashMap<String, Object>();
			restfulResult.put("status", 1);
			restfulResult.put("message", "用户非管理员，或userAuthSystemType不是prophet，请求出错!");
			restfulResult.put("data", null);
			return restfulResult;
		}
		return this.encodeToJsonResult(this.userAuthService.getAllProphetUsers());
	}
	
	@RequestMapping(value = "/add_prophet_user.json", method = RequestMethod.POST)
	public Map<String, Object> addProphetUserController(HttpServletRequest request,
			@RequestParam("username") String username, 
			@RequestParam("password") String password,
			@RequestParam("isActive") String isActive,
			@RequestParam("userType") String userType
			) {
		Map<String, Object> loginUserInfo = this.getLoginUserInfo(request);
		if (
				loginUserInfo.get("isAdmin").toString().equals("0") || 
				!loginUserInfo.get("userAuthSystemType").toString().equals("prophet")
			) {
			Map<String, Object> restfulResult = new HashMap<String, Object>();
			restfulResult.put("status", 1);
			restfulResult.put("message", "用户非管理员，或userAuthSystemType不是prophet，请求出错!");
			restfulResult.put("data", null);
			return restfulResult;
		}
		return this.encodeToJsonResult(this.userAuthService.addProphetUser(username, password, isActive, userType));
	}
	
	@RequestMapping(value = "/delete_user_by_id.json", method = RequestMethod.POST)
	public Map<String, Object> deleteUserByIdController(HttpServletRequest request, @RequestParam("userId") int userId) {
		Map<String, Object> loginUserInfo = this.getLoginUserInfo(request);
		if (
				loginUserInfo.get("isAdmin").toString().equals("0") || 
				!loginUserInfo.get("userAuthSystemType").toString().equals("prophet")
			) {
			Map<String, Object> restfulResult = new HashMap<String, Object>();
			restfulResult.put("status", 1);
			restfulResult.put("message", "用户非管理员，或userAuthSystemType不是prophet，请求出错!");
			restfulResult.put("data", null);
			return restfulResult;
		}
		return this.encodeToJsonResult(this.userAuthService.deleteUserById(userId));
	}
}
