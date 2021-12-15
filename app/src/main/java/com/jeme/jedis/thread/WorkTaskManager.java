
package com.jeme.jedis.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/***
 * 线程管理类
 * @author jeme
 */
public class WorkTaskManager {
    /**
     * 创建一个可重用固定线程数的线程池
     */
    private static final int POOL_SIZE = 5;
    private static WorkTaskManager workTaskManager;
    /**
     * 创建一个可重用固定线程数的线程池
     */
    private ExecutorService mPool;

    public static synchronized WorkTaskManager getInstance() {
        try {
            if (null == workTaskManager) {
                workTaskManager = new WorkTaskManager();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workTaskManager;
    }

    private WorkTaskManager() {
        try {
            ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("j-thread-call-runner-%d").build();

            mPool = new ThreadPoolExecutor(POOL_SIZE, POOL_SIZE, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingDeque<>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addWorkEventTask(Runnable trackEvenTask) {
        try {
             mPool.execute(trackEvenTask);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Future<Object> addWorkEventTask(Callable<Object> trackEvenTask) {
        return mPool.submit(trackEvenTask);
    }

}
