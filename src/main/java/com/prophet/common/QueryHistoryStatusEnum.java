package com.prophet.common;

/**
 * query_history表status字段的枚举值
 *
 */
public enum QueryHistoryStatusEnum {
	FINISHED(0, "已运行完毕"), RUNNING(1, "任务执行中"), ABORTED(2, "已经被取消"), ERROR(3, "运行出现错误");

	private int index;
	private String name;
	
	private QueryHistoryStatusEnum(int index, String name) {
		this.index = index;
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * 根据键值获取对应的状态名的方法
	 * @param index
	 * @return
	 */
	public static String getNameByIndex(int index) throws Exception {
		String name = "";
		for (QueryHistoryStatusEnum q : QueryHistoryStatusEnum.values()) {
			if (q.getIndex() == index) {
				name = q.getName();
			}
		}
		//如果遍历完了都没有找到对应的index，则抛出异常
		if (name.equals("")) {
			throw new Exception(String.format("根据状态值%d无法找到对应的QueryHistoryStatus枚举值描述!", index));
		}
		return name;
	}
	
	/**
	 * 根据状态值描述获取对应的状态值的方法
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static int getIndexByName(String name) throws Exception {
		int index = -1;
		for (QueryHistoryStatusEnum q : QueryHistoryStatusEnum.values()) {
			if (q.getName().equals(name)) {
				index = q.getIndex();
			}
		}
		//如果遍历完了都没有找到对应的index，则抛出异常
		if (index == -1) {
			throw new Exception(String.format("根据状态值描述%s无法找到对应的QueryHistoryStatus枚举值!", name));
		}
		return index;
	}
}