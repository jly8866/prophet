package com.prophet.domain;

/**
 * hive查询语句历史模型类
 * table: query_history
 */
public class QueryHistory {
	private long id;
	private String queryTime;
	private String queryContent;
	private int status;				//status和strStatus是枚举里index和name的关系，在此冗余查询方便而已
	private String strStatus;
	private String username;
	private int emailNotify;
	private int resultSize;
	private String message;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getQueryTime() {
		return queryTime;
	}
	public void setQueryTime(String queryTime) {
		this.queryTime = queryTime;
	}
	public String getQueryContent() {
		return queryContent;
	}
	public void setQueryContent(String queryContent) {
		this.queryContent = queryContent;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getStrStatus() {
		return strStatus;
	}
	public void setStrStatus(String strStatus) {
		this.strStatus = strStatus;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public int getEmailNotify() {
		return emailNotify;
	}
	public void setEmailNotify(int emailNotify) {
		this.emailNotify = emailNotify;
	}
	public int getResultSize() {
		return resultSize;
	}
	public void setResultSize(int resultSize) {
		this.resultSize = resultSize;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
