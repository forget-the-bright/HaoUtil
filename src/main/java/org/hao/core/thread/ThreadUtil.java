package org.hao.core.thread;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Description 线程操作工具类
 * Author wanghao(helloworlwh @ 163.com)
 * Date 2024/5/30 16:51
 */
public class ThreadUtil {
    public static ThreadPoolExecutor getTheadPool() {
        return ThreadUtil.getTheadPool(5);
    }

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return request;
    }

    public static HttpServletResponse getResponse() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        return response;
    }

    public static ThreadPoolExecutor getTheadPool(Integer corePoolSize) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, //核心线程数
                20, //最大线程数
                60, TimeUnit.SECONDS, //每个线程最大运行时间 ,和时间单位
                new LinkedBlockingQueue<Runnable>(10000));//线程存放队列 ，有界队列容量1万
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(servletRequestAttributes, true);//设置子线程共享
        return threadPoolExecutor;
    }


    public static Boolean waitThreadPoolCompleted(ThreadPoolExecutor threadPoolExecutor) {
        return ThreadUtil.waitThreadPoolCompleted(threadPoolExecutor, "");
    }

    public static Boolean waitThreadPoolCompleted(ThreadPoolExecutor threadPoolExecutor, String message) {
        return ThreadUtil.waitThreadPoolCompleted(threadPoolExecutor, "", () -> {
        });
    }

    public static Boolean waitThreadPoolCompleted(ThreadPoolExecutor threadPoolExecutor, String message, ExecuteOperation operation) {
        threadPoolExecutor.shutdown();
        try {
            while (true) {
                if (threadPoolExecutor.isTerminated()) {
                    System.out.println("===========================================================================");
                    System.out.println(message + " 全部线程执行完毕 ");
                    System.out.println("===========================================================================");
                    operation.execute();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public interface ExecuteOperation {
        void execute();
    }
}
