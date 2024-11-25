package org.hao.spring;

import cn.hutool.core.util.StrUtil;

import javax.websocket.Session;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Description TODO
 * Author wanghao(helloworlwh  163.com)
 * Date 2024/5/31 13:57
 */
public class WebSocketUtil {


    /**
     * 等待消息方法，用于从消息会话映射中获取消息
     * 此方法重载了waitMessage方法，使用默认的过期时间
     *
     * @param messageSessionMap 一个ConcurrentHashMap，键为会话标识，值为消息对象或过期时间
     * @return 返回从会话映射中获取的消息，如果没有找到或超时，则返回null
     */
    public static String waitMessage(ConcurrentHashMap<String, Object> messageSessionMap) {
        return waitMessage(messageSessionMap, 3);
    }

    /**
     * 等待客户端消息
     *
     * @param messageSessionMap 用于存储消息和会话信息的并发哈希映射
     * @param MINUTES          等待分钟数
     * @return 客户端发送的消息
     *
     * 本方法通过暂停线程的方式等待客户端发送消息，直到达到指定的等待时间或收到消息为止
     * 使用WebSocketUtil.sleep方法来实现等待逻辑，该方法会根据给定的条件定期检查消息是否已到达
     * 如果在指定时间内没有收到消息，方法将返回null，否则将返回接收到的消息字符串
     */
    public static String waitMessage(ConcurrentHashMap<String, Object> messageSessionMap, Integer MINUTES) {
        WebSocketUtil.sleep(() -> {
            // 检查消息是否已到达
            if (StrUtil.isEmpty((String) messageSessionMap.get("message"))) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }, MINUTES);
        // 返回接收到的消息，如果没有消息则返回null
        return (String) messageSessionMap.get("message");
    }


    /**
     * 向客户端发送消息
     *
     * @param session 与客户端的会话对象，用于通信
     * @param message 要发送的消息内容
     */
    public static void sendMessage(Session session, String message) {
        if (session.isOpen()) {
            // 发送消息
            session.getAsyncRemote().sendText(message);
        }
    }



    /**
     * Utility method for sleeping, allowing for conditional termination.
     * This method provides a way to pause the current thread, with the ability to check conditions for termination.
     *
     * @param supplier A supplier that determines whether to terminate the sleep.
     *                 If the supplier returns true, the sleep ends and the method returns.
     */
    public static void sleep(Supplier<Boolean> supplier) {
        // 1000MillSeconds * 60  * 3 =  3 MINUTES = 180000 毫秒
        // 180000 毫秒 / 100 毫秒 = 1800
        // 等于 (1000*60)/100 = 600   600*3=1800
        sleep(supplier, 3);
    }


    /**
     * 执行业务操作并根据结果决定是否结束休眠的方法
     * 此方法用于在执行特定业务逻辑的同时，让当前线程休眠指定的时间
     * 如果业务操作提前完成（即Supplier返回true），则提前结束休眠
     *
     * @param supplier 执行业务操作的Supplier对象，返回true或false
     * @param MINUTES  休眠的分钟数
     */
    public static void sleep(Supplier<Boolean> supplier, Integer MINUTES) {
        try {
            // 计算休眠的毫秒数，每分钟600个100毫秒的间隔
            Long loadMILLISECONDS = 600l * MINUTES;
            for (int i = 0; i < loadMILLISECONDS; i++) {

                // 使当前线程休眠100毫秒
                TimeUnit.MILLISECONDS.sleep(100);

                // 如果Supplier返回true，即业务操作完成，提前结束休眠
                if (supplier.get()) break;
            }
        } catch (InterruptedException e) {
            // 如果线程被中断，抛出运行时异常
            throw new RuntimeException(e);
        }
    }

}
