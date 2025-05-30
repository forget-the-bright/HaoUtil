package org.hao.spring;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hao.config.HaoUtilProperties;
import org.springframework.scheduling.annotation.Async;

import javax.websocket.Session;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * ws操作优化工具类
 *
 * @author wanghao(helloworlwh @ 163.com)
 * @since 2025/4/8 下午2:29
 */
@Slf4j
public class WSUtil {
    private ScheduledExecutorService pushScheduler;

    public WSUtil(HaoUtilProperties haoUtilProperties) {
        int wsSchedulerPoolSize = haoUtilProperties.getWsSchedulerPoolSize();
        if (wsSchedulerPoolSize <= 0) wsSchedulerPoolSize = 1000;
        // 为每个 WebSocket 连接创建独立任务，并提交到共享线程池
        pushScheduler = Executors.newScheduledThreadPool(wsSchedulerPoolSize); // 假设支持1000并发
    }


    /**
     * 定时发送消息到指定会话
     * 该方法使用调度器按照固定间隔发送消息，消息内容由调用者提供
     * 如果会话关闭，将取消之前的调度任务
     * 有效的使用线程池,定时任务只有在执行的时候占用线程池资源。不用一直运行等待链接推出。
     *
     * @param session        WebSocket会话对象，用于发送消息
     * @param intervalSecond 发送消息的时间间隔，以秒为单位
     * @param getMessage     一个供应商函数，用于提供要发送的消息内容
     */
    public void schedulerSendMessage(Session session, Integer intervalSecond, Supplier<String> getMessage) {
        // 安排一个定时任务，按照固定的时间间隔执行
        ScheduledFuture<?> scheduledFuture = pushScheduler.scheduleAtFixedRate(() -> {
            // 检查会话是否仍然开放
            if (!session.isOpen()) {
                // 如果会话已关闭，尝试取消之前的调度任务
                ScheduledFuture<?> future = (ScheduledFuture<?>) session.getUserProperties().get("task");
                System.out.println(session);
                if (future != null) {
                    // 如果任务正在执行，不会中断它，而是让它继续执行完成；然后关闭任务。
                    future.cancel(false);
                }
                return;
            }
            try {
                // 获取供应商提供的消息内容
                String message = getMessage.get();
                // 尝试发送消息
                WSUtil.sendMessage(session, message);
            } catch (Exception e) {
                // 如果发送消息时发生异常，打印异常信息并将异常消息发送给会话
                e.printStackTrace();
                WSUtil.sendMessage(session, e.getMessage());
            }
        }, 0, intervalSecond, TimeUnit.SECONDS);
        // 将新创建的调度任务存储在会话的用户属性中，以便后续可能的取消操作
        session.getUserProperties().put("task", scheduledFuture);
    }

    /**
     * 使用线程池发送消息到WebSocket会话
     * 该方法会不断检查会话是否开启，并按照指定间隔发送消息
     * 问题是,
     * 如果会话一直开启中,线程池的线程会一直运行，占用线程池资源。
     * 如果同时会话的数量超过核心线程数量，后续的会话会一直等待前面会话关闭的资源关闭，后面的会话才会执行发送消息。
     * 此功能不可取,切换定时线程池来解决,同一个线程任务会根据执行的定时间隔来执行，只会在执行的时候占用资源。不用等待会话关闭。
     * 请转到使用 {@link #schedulerSendMessage(Session, Integer, Supplier)}
     *
     * @param session        WebSocket会话对象，用于与客户端通信
     * @param intervalSecond 消息发送间隔，单位为秒
     * @param getMessage     消息生成器，用于获取要发送的消息内容
     */
    @SneakyThrows
    @Deprecated
    @Async
    public void poolSendMessage(Session session, Integer intervalSecond, Supplier<String> getMessage) {
        while (true) {
            // 检查会话是否仍然开启，如果未开启则退出循环
            if (!session.isOpen()) break;
            try {
                // 生成并获取要发送的消息内容
                String message = getMessage.get();
                // 发送消息到指定的WebSocket会话
                WSUtil.sendMessage(session, message);
            } catch (Exception e) {
                // 打印异常信息
                e.printStackTrace();
                // 发送异常信息到指定的WebSocket会话
                WSUtil.sendMessage(session, e.getMessage());
            }
            // 按照指定的间隔暂停线程
            TimeUnit.SECONDS.sleep(intervalSecond);
        }
    }


    /**
     * 在单独的线程中循环向WebSocket会话发送消息
     * 此方法用于在不阻塞主线程的情况下，定期向客户端发送消息
     * 受限于jvm资源限制，此方法可能会导致JVM内存泄漏。一般的情况4g内存，jvm默认配置。
     * 开始线程数据量在2000-3000个左右，如果超过数据量就会 默认线程池限制导致堆栈溢出。
     * 临时解决问题使用。不推荐
     *
     * @param session        WebSocket会话对象，用于与客户端通信
     * @param intervalSecond 消息发送间隔时间（秒）
     * @param getMessage     消息生成器，用于获取要发送的消息内容
     */
    @SneakyThrows
    @Deprecated
    public void threadSendMessageThread(Session session, Integer intervalSecond, Supplier<String> getMessage) {
        // 启动一个新的线程执行消息发送任务
        new Thread(() -> {
            // 无限循环，直到会话关闭
            while (true) {
                // 检查会话是否仍然开放，如果关闭则退出循环
                if (!session.isOpen()) break;
                try {
                    // 从消息生成器获取消息内容
                    String message = getMessage.get();
                    // 使用WebSocket工具类发送消息
                    WSUtil.sendMessage(session, message);
                } catch (Exception e) {
                    // 如果发送消息时发生异常，打印异常信息并将异常消息发送给客户端
                    e.printStackTrace();
                    WSUtil.sendMessage(session, e.getMessage());
                }
                try {
                    // 按指定间隔暂停线程，准备下一次发送
                    TimeUnit.SECONDS.sleep(intervalSecond);
                } catch (InterruptedException e) {
                    // 如果睡眠中断，抛出运行时异常
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }


    /**
     * 使用SneakyThrows注解来悄无声息地处理可能抛出的检查型异常
     * 这个方法主要用于根据错误标志来决定是否关闭会话，并在关闭前发送错误信息
     * 如果是错误情况，它会发送错误信息，关闭会话，并抛出自定义异常
     *
     * @param session 会话对象，用于发送消息和关闭会话
     * @param message 要发送的错误信息
     * @param isError 标志是否为错误情况
     */
    @SneakyThrows
    public static void errorClose(Session session, String message, Boolean isError) {
        // 当存在错误时，执行关闭会话和发送错误信息的操作
        if (isError) {
            // 发送错误信息
            session.getAsyncRemote().sendText(message);
            // 关闭会话
            session.close();
            // 抛出包含错误信息的自定义异常
            throw new RuntimeException(message);
        }
    }


    /**
     * 关闭WebSocket连接的方法
     *
     * @param webSockets   一个CopyOnWriteArraySet集合，用于存储WebSocket对象
     * @param WebsocketObj 要关闭连接的WebSocket对象
     * @param session      当前WebSocket的会话
     *                     <p>
     *                     此方法首先从webSockets集合中移除指定的WebSocket对象，然后获取并取消与当前会话关联的任务
     *                     如果任务存在且未被取消，它将尝试中断执行任务的线程最后，它记录关闭连接的消息和当前连接的总数
     */
    public static void close(CopyOnWriteArraySet<?> webSockets, Object WebsocketObj, Session session) {
        // 从集合中移除WebSocket对象
        webSockets.remove(WebsocketObj);

        // 获取与当前会话关联的任务
        ScheduledFuture<?> future = (ScheduledFuture<?>) session.getUserProperties().get("task");

        // 检查任务是否存在且未被取消
        if (ObjectUtil.isNotEmpty(future) && !future.isCancelled()) {
            // 如果任务正在执行中，尝试中断该线程（通过调用线程的 interrupt() 方法）。
            // 尽可能快地中止任务，哪怕它已经在运行。
            boolean cancel = future.cancel(true);
        }

        // 记录关闭连接的消息和当前连接的总数
        log.info("【{}消息】连接断开，总数为:{}", WebsocketObj.getClass().getSimpleName(), webSockets.size());
    }


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
     * @param MINUTES           等待分钟数
     * @return 客户端发送的消息
     * <p>
     * 本方法通过暂停线程的方式等待客户端发送消息，直到达到指定的等待时间或收到消息为止
     * 使用WebSocketUtil.sleep方法来实现等待逻辑，该方法会根据给定的条件定期检查消息是否已到达
     * 如果在指定时间内没有收到消息，方法将返回null，否则将返回接收到的消息字符串
     */
    public static String waitMessage(ConcurrentHashMap<String, Object> messageSessionMap, Integer MINUTES) {
        WSUtil.sleep(() -> {
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
