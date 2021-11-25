package com.prophet.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.prophet.dao.UserAuthLdapDao;
import com.prophet.dao.UserAuthProphetDao;
import com.prophet.dao.AdminDao;
import com.prophet.interfaces.UserAuthDaoInterface;

@Service
public class UserAuthService extends BaseService{
	/*用户认证系统的类型*/
	@Value("${authentication.system}")
	private String authSystemType;
	
	/*LDAP相关配置*/
	@Value("${authentication.ldap.url}")
	private String LDAP_URL;
	@Value("${authentication.ldap.base-dn}")
	private String LDAP_BASE_DN;
	@Value("${authentication.ldap.user-search-dn}")
	private String LDAP_USER_SEARCH_DN;
	@Value("${authentication.ldap.user-search-column}")
	private String LDAP_USER_SEARCH_COLUMN;
	@Value("${authentication.ldap.factory}")
	private String LDAP_FACTORY;
	@Value("${authentication.ldap.security-authentication}")
	private String LDAP_SECURITY_AUTHENTICATION;
	@Value("${authentication.ldap.security-credenticials}")
	private String LDAP_SECURITY_CREDENTIALS;
	
	private AdminDao adminDao;
	
	@Autowired
	@Qualifier("prophetJdbcTemplate")
	private JdbcTemplate jdbcTemplateProphet;
	
	@Autowired
	public void setAdminDao(AdminDao adminDao) {
		this.adminDao = adminDao;
	}

	public UserAuthService() {
		
	}
	
	/*单例，常驻内存，所有UserAuthService实例共享同一个userAuthDao*/
	private static UserAuthDaoInterface userAuthDao;
	
	/**
	 * 工厂模式：生产认证连接类DAO的工厂
	 * 设计思想：UserAuthService由spring初始化后先从配置文件里读取配置的Auth开关、LDAP等信息，然后authenticate时调用该方法
	 * 		获取具体类型的dao（如果null则初始化并常驻内存）。
	 * @return 具体类型的dao
	 * @throws Exception 
	 */
	private UserAuthDaoInterface getUserAuthDao() throws Exception {
		if (!this.validateConfig(this.authSystemType)) {
			throw new Exception(String.format("application.properties文件里%s相关参数配置不正确，请检查!", this.authSystemType));
		}
		switch (this.authSystemType.toLowerCase()) {
			case "ldap":
				if (userAuthDao == null) {
					synchronized(UserAuthService.class) {
						if (userAuthDao == null) {
							userAuthDao = new UserAuthLdapDao(LDAP_URL, LDAP_BASE_DN, LDAP_USER_SEARCH_DN, LDAP_USER_SEARCH_COLUMN, 
								LDAP_FACTORY, LDAP_SECURITY_AUTHENTICATION, LDAP_SECURITY_CREDENTIALS);
						}
					}
				}
				break;
			case "prophet":
				if (userAuthDao == null) {
					synchronized(UserAuthService.class) {
						if (userAuthDao == null) {
							userAuthDao = new UserAuthProphetDao(this.jdbcTemplateProphet);
						}
					}
				}
				break;
		}
		return userAuthDao;
	}

	/**
	 * 验证用户名密码是否正确
	 * @param uid
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> authenticate(String uid, String password) {
		Map<String, Object> serviceResult = this.initServiceResult();
		int daoResult = -1;
		try {
			daoResult = this.getUserAuthDao().authenticate(uid, password);
			
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
		}
		serviceResult.put("data", daoResult);
		return serviceResult;
	}
	
	/**
	 * 根据auth系统类型验证对应的参数是否在application.properties文件里配置正确
	 * @param authSystemType
	 * @return
	 */
	private boolean validateConfig(String authSystemType) {
		return true;
	}
	
	/**
	 * 检查某个用户是否是admin
	 * @param username
	 * @return
	 */
	public boolean isAdmin(String username) {
		return (this.adminDao.checkIsAdmin(username).size() == 0) ? false : true;
	}
	
	/**
	 * 检查用户系统里是否存在某个用户
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public boolean hasUser(String username) throws Exception {
		return this.getUserAuthDao().hasUser(username);
	}
	
	/**
	 * 获取用户认证系统的类型
	 * @return
	 */
	public String getUserAuthSystemType() {
		return this.authSystemType;
	}
	
	/**
	 * 在使用了prophet内置用户系统情况下，获取所有用户的信息
	 * @return
	 */
	public Map<String, Object> getAllProphetUsers() {
		Map<String, Object> serviceResult = this.initServiceResult();
		List<Map<String, Object>> daoResult = null;
		try {
			if (this.authSystemType.toLowerCase().equals("prophet")) {
				daoResult = ((UserAuthProphetDao)this.getUserAuthDao()).getAllProphetUsers();
			}
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
		}
		serviceResult.put("data", daoResult);
		return serviceResult;
	}
	
	/**
	 * 增加一个prophet user
	 * @param username
	 * @param password
	 * @param isActive
	 * @param userType
	 * @return
	 */
	public Map<String, Object> addProphetUser(String username, String password, String isActive, String userType) {
		Map<String, Object> serviceResult = this.initServiceResult();
		int daoResult = -1;
		try {
			//如果是admin则向admin表里插入一个
			if (userType.equals("admin")) {
				this.adminDao.insertOneAdmin(username);
			}
			
			if (this.authSystemType.toLowerCase().equals("prophet")) {
				daoResult = ((UserAuthProphetDao)this.getUserAuthDao()).addProphetUser(username, password, isActive, userType);
			}
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
		}
		serviceResult.put("data", daoResult);
		return serviceResult;
	}
	
	public Map<String, Object> deleteUserById(int userId) {
		Map<String, Object> serviceResult = this.initServiceResult();
		int daoResult = -1;
		try {
			
			if (this.authSystemType.toLowerCase().equals("prophet")) {
				((UserAuthProphetDao)this.getUserAuthDao()).deleteUserById(userId);
			}
		} catch (Exception ex) {
			serviceResult.put("msg", ex.getMessage());
		}
		serviceResult.put("data", daoResult);
		return serviceResult;
	}
}
