package com.prophet.common;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ConcurrentHashMap;

import com.prophet.dao.task.HiveQueryAsyncTask;

/**
 * 线程池基础类
 */
public class ThreadPool {
	private final static int POOL_SIZE = 64;
    private static ExecutorService executorService;
    public static Map<String, Future<?>> activeThreadsMap = new ConcurrentHashMap<String,Future<?>>();	//String为线程名，Future为线程结果
    
    public final static String HIVE_QUERY_THREAD_NAME = "HiveQueryAsyncTaskThread-";
    public final static String HIVE_EMAIL_THREAD_NAME = "HiveResultMailThread-";

    private ThreadPool(){}
    
    private static ExecutorService getExecutor() {
        if (executorService == null || executorService.isShutdown()) {
            synchronized (ThreadFactory.class) {
                if (executorService == null || executorService.isShutdown()) {
                    executorService = Executors.newFixedThreadPool(POOL_SIZE);
                    activeThreadsMap = new ConcurrentHashMap<String,Future<?>>();
                }
            }
        }
        return executorService;
    }

    /**
     * 手动终止某个线程，并将Future从活跃列表移除
     * @param queryHistId
     */
    public static void stopThread(long queryHistId) {
		if (activeThreadsMap != null) {
			Future<?> result = activeThreadsMap.get(HIVE_QUERY_THREAD_NAME + queryHistId);
			if (result != null) {
				result.cancel(true);
			}
			activeThreadsMap.remove(HIVE_QUERY_THREAD_NAME + queryHistId);
		}
    }
    
    /**
     * 不返回结果的execute方法
     * @param <T extends Runnable> thread
     */
    public static <T extends Runnable> void execute(T thread) {
        getExecutor().execute(thread);
    }
    
    /**
     * 提交hive query任务
     * @param hiveTask
     * @return Future
     */
    public static void executeHiveQuery(HiveQueryAsyncTask hiveTask) {
    	Future<?> result = getExecutor().submit(hiveTask);
    	
    	//加入活跃线程列表，方便后面取消任务
        if (hiveTask != null) {
    		activeThreadsMap.put(HIVE_QUERY_THREAD_NAME + hiveTask.getQueryHistId(), result);
			
        }
    }

    /**
     * 可以返回结果的提交方法
     * @param Callable<T> task
     * @return 一个Future对象
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return getExecutor().submit(task);
    }

    /**
     * 不再使用线程池时，调用该方法关闭线程池即可
     */
    public static final void shutdown() {
        getExecutor().shutdown();
        activeThreadsMap = null;
    }
}
