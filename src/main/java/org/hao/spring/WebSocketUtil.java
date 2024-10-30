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


    public static String waitMessage(ConcurrentHashMap<String, Object> messageSessionMap) {
        return waitMessage(messageSessionMap, 3);
    }

    /**
     * param messageSessionMap
     * param MINUTES           等待分钟数
     * return 客户端发送的消息
     * description 等待客户端消息
     */
    public static String waitMessage(ConcurrentHashMap<String, Object> messageSessionMap, Integer MINUTES) {
        WebSocketUtil.sleep(() -> {
            if (StrUtil.isEmpty((String) messageSessionMap.get("message"))) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }, MINUTES);
        return (String) messageSessionMap.get("message");
    }

    /**
     * param session
     * param message
     * description 向客户端发送消息
     */
    public static void sendMessage(Session session, String message) {
        if (session.isOpen()) {
            //发送消息
            session.getAsyncRemote().sendText(message);
        }
    }


    public static void sleep(Supplier<Boolean> supplier) {
        // 1000MillSeconds * 60  * 3 =  3 MINUTES = 180000 毫秒
        // 180000 毫秒 / 100 毫秒 = 1800
        // 等于 (1000*60)/100 = 600   600*3=1800
        sleep(supplier, 3);
    }

    /**
     * param supplier 执行业务操作 返回true false,如果为true 结束休眠
     * param MINUTES  休眠分钟数
     */

    public static void sleep(Supplier<Boolean> supplier, Integer MINUTES) {
        try {
        /*Long MILLISECONDS = 1000l * 60l * MINUTES;
        Long loadMILLISECONDS = MILLISECONDS / 100;*/
            Long loadMILLISECONDS = 600l * MINUTES;
            for (int i = 0; i < loadMILLISECONDS; i++) {

                TimeUnit.MILLISECONDS.sleep(100);

                if (supplier.get()) break;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
