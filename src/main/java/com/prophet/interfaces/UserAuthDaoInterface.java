/**
 * 
 */
package com.prophet.interfaces;

/**
 * 用户认证系统的接口
 */
public interface UserAuthDaoInterface {
	/**
	 * 验证用户名密码，0通过，1失败
	 * @param username
	 * @param password
	 * @return
	 */
	public int authenticate(String username, String password);
	
	/**
	 * 检查是否存在该用户
	 * @param username
	 * @return
	 */
	public boolean hasUser(String username);
}
