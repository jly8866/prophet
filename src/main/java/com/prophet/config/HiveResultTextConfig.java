package com.prophet.config;

public class HiveResultTextConfig {
	
	private final static String HIVE_RESULT_FILE_DIR		= "data/";
	//public final static String HIVE_RESULT_FIELD_DELIMITER 	= "##@@#";
	public final static String HIVE_RESULT_FIELD_DELIMITER 	= "\001\001";		//hive默认列分隔符^A的八进制编码，这里是两个^A
	
	/**
	 * 获取数据文件绝对路径
	 * @param username
	 * @param queryHistId
	 * @return
	 */
	public final static String getDataFileName(String username, long queryHistId) {
		return String.format("%s%s-%d.txt", HIVE_RESULT_FILE_DIR, username, queryHistId);
	}
	
	/**
	 * 获取meta文件绝对路径
	 * @param username
	 * @param queryHistId
	 * @return
	 */
	public final static String getMetaFileName(String username, long queryHistId) {
		return String.format("%s%s-%d.meta", HIVE_RESULT_FILE_DIR, username, queryHistId);
	}
}
