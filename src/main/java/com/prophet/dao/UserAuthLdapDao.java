package com.prophet.dao;

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import com.prophet.interfaces.UserAuthDaoInterface;

/**
 * LDAP服务连接基础类
 *
 */
public class UserAuthLdapDao implements UserAuthDaoInterface{
	private String URL;
	private String BASE_DN;
	private String USER_SEARCH_DN;
	private String USER_SEARCH_COLUMN;
	private String FACTORY;
	private String SECURITY_AUTHENTICATION;
	private String SECURITY_CREDENTIALS;
	
	private LdapContext ctx = null;				//LDAP连接上下文，后续都操作这个
	private final Control[] connCtls = null;	//控制连接的一些属性

	public UserAuthLdapDao(){
		
	}
	
	public UserAuthLdapDao(String URL, String BASE_DN, String USER_SEARCH_DN, String USER_SEARCH_COLUMN, String FACTORY, String SECURITY_AUTHENTICATION, String SECURITY_CREDENTIALS) {
		this.URL = URL;
		this.BASE_DN = BASE_DN;
		this.USER_SEARCH_DN = USER_SEARCH_DN;
		this.USER_SEARCH_COLUMN = USER_SEARCH_COLUMN;
		this.FACTORY = FACTORY;
		this.SECURITY_AUTHENTICATION = SECURITY_AUTHENTICATION;
		this.SECURITY_CREDENTIALS = SECURITY_CREDENTIALS;
	}
	
	/**
	 * 连接LDAP
	 */
	private void connect() {
		//设置一些env参数
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, this.FACTORY);
		env.put(Context.PROVIDER_URL, this.URL);
		env.put(Context.SECURITY_AUTHENTICATION, this.SECURITY_AUTHENTICATION);
		
		env.put(Context.SECURITY_PRINCIPAL, this.BASE_DN); 
		env.put(Context.SECURITY_CREDENTIALS, this.SECURITY_CREDENTIALS);
		//env.put("java.naming.ldap.attributes.binary", "objectSid objectGUID");
	
		try {
			ctx = new InitialLdapContext(env, connCtls);
			//logger.info("连接LDAP成功!");
		} catch (javax.naming.AuthenticationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * 断开连接
	 */
	private void closeContext(){
		if (ctx != null) {
			try {
				ctx.close();
			}
			catch (NamingException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 获取用户的完整DN
	 * @param uid
	 * @return
	 */
	private String getUserDN(String uid) {
		connect();
		String userDN = "";
		
		try {
			SearchControls constraints = new SearchControls();
			String filter = String.format("(%s=%s)",this.USER_SEARCH_COLUMN, uid);
			String[] attrPersonArray = { "uid", "displayName", "cn" };
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
			constraints.setReturningAttributes(attrPersonArray); 
			
			NamingEnumeration<SearchResult> en = ctx.search(this.USER_SEARCH_DN, filter, constraints);
		
			if (en == null || !en.hasMoreElements()) {
				//logger.info(String.format("LDAP里未找到该用户的信息! user:%s", uid));
				return "";
			}
			while (en != null && en.hasMoreElements()) {
				Object obj = en.nextElement();
				if (obj instanceof SearchResult) {
					SearchResult si = (SearchResult) obj;
					userDN += si.getName();
					userDN += "," + this.USER_SEARCH_DN;
				} else {
					//logger.info(obj.toString());
				}
			}
			
		} catch (Exception e) {
			//logger.info(String.format("查找用户时产生异常! user:%s 报错信息如下：", uid));
			//logger.error(e.getMessage());
		}
		
		return userDN;
	}

	/**
	 * 认证用户的用户名和密码
	 */
	@Override
	public int authenticate(String UID, String password) {
		int result = -1;
		String userDN = getUserDN(UID);
		
		//没有该用户信息
		if (userDN.equals("")) {
			result = 1;
			return result;
		}
		
		try {
			ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, userDN);
			ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
			ctx.reconnect(connCtls);
			//logger.info(String.format("LDAP用户验证通过! user:%s", UID));
			result = 0;
		} catch (AuthenticationException e) {
			//logger.info(String.format("LDAP用户验证失败! user:%s，报错信息如下：", UID));
			//logger.warn(e.getMessage());
			result = 1;
		} catch (NamingException e) {
			//logger.info(String.format("LDAP用户验证失败! user:%s，报错信息如下：", UID));
			result = 1;
		}
		//一定要关闭LDAP连接
		closeContext();
		return result;
	}
	
	@Override
	public boolean hasUser(String UID) {
		return this.getUserDN(UID).equals("") == true ? false : true;
	}

	/**
	 * 向LDAP增加用户，暂未用到
	 */
	public boolean addUser(String usr, String pwd,String uid,String description) {
		try {
			BasicAttributes attrsbu = new BasicAttributes();
			BasicAttribute objclassSet = new BasicAttribute("objectclass");
			objclassSet.add("inetOrgPerson");
			attrsbu.put(objclassSet);
			attrsbu.put("sn", usr);
			attrsbu.put("cn", usr);
			attrsbu.put("uid", uid);
			attrsbu.put("userPassword", pwd);
			attrsbu.put("description", description);
			ctx.createSubcontext("uid="+uid+"", attrsbu);
		
			return true;
		} catch (NamingException ex) {
			ex.printStackTrace();
		}
		closeContext();
		return false;
	} 
	
}
