package com.prophet.util;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {
	/**
	 * 获取当前时间，时间格式：yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String getNow() {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(now);
	}
	
	/**
	 * 将指定时间Date对象格式化为字符串，时间格式：yyyy-MM-dd HH:mm:ss
	 * @param time
	 * @return
	 */
	public static String formatDatetime(Date time) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(time);
	}

}
