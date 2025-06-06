package org.hao.core.ws;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.hao.core.Maps;
import org.hao.vo.Tuple;

import javax.websocket.Session;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 抽象基类，用于实现WebSocket数据推送功能，支持主动与被动模式连接。
 * 提供了会话管理、定时任务调度及消息发送等通用逻辑。
 *
 * @author Wang Hao (helloworlwh@163.com)
 * @since 2025-05-31
 */

@Slf4j
public abstract class BaseIntervalWs {
    //region 静态全局回话状态存储
    /*
     * java 子类共享父类静态变量，所以维护用类型作为key来隔离的集合
     *  1层 key为类型， value为  2层 集合 key:tagName，value 为3层 key为 intervalSecond，value 为 线程池和定时任务的元祖
     */
    private static ConcurrentHashMap<Class<?>, ConcurrentHashMap
            <String, ConcurrentHashMap
                    <Integer, Tuple<CopyOnWriteArraySet<Session>, ScheduledFuture<?>>>>> metaScheduledWSMap = new ConcurrentHashMap<>();
    /*
     * java 子类共享父类静态变量，所以维护用类型作为key来隔离的集合,不同类型子类的的回话存储数量
     */
    private static ConcurrentHashMap<Class<?>, CopyOnWriteArraySet<Session>> metaWebSockets = new ConcurrentHashMap<>();

    protected static CopyOnWriteArraySet<Session> getWebSockets(Class entityClass) {
        return metaWebSockets.computeIfAbsent(entityClass, k -> new CopyOnWriteArraySet<>());
    }

    protected static ConcurrentHashMap<String, ConcurrentHashMap
            <Integer, Tuple<CopyOnWriteArraySet<Session>, ScheduledFuture<?>>>> getScheduledWSMap(Class entityClass) {
        return metaScheduledWSMap.computeIfAbsent(entityClass, k -> new ConcurrentHashMap<>());
    }

    /**
     * 安排或更新定时任务，并清理不再使用的资源。
     *
     * <p>该方法是 WebSocket 定时任务调度的核心逻辑，执行以下两步操作：
     * <ol>
     *     <li>如果当前元组中没有已存在的定时任务（`scheduledFutureTuple.getSecond()` 为空），
     *         并且提供了有效的时间间隔和消息生成器，则创建并安排新的定时任务。</li>
     *     <li>遍历全局定时任务映射表（{@link #metaScheduledWSMap}），移除所有空闲的任务条目（即没有关联会话的任务）。</li>
     * </ol>
     *
     * <p>适用场景：
     * <ul>
     *     <li>新增定时任务：当有新的被动模式连接加入时，确保为其分配定时任务。</li>
     *     <li>清理无用资源：定期检查并移除不再使用的任务条目，优化内存占用。</li>
     * </ul>
     *
     * @param scheduledFutureTuple 包含会话集合和定时任务的元组对象
     * @param intervalSecond       推送消息的时间间隔（单位：秒）。null 表示不启用自动推送。
     * @param taskSendMessage      消息生成器，用于定义定时任务的具体行为。
     */
    protected static synchronized void computeScheduledTask(Tuple<CopyOnWriteArraySet<Session>, ScheduledFuture<?>> scheduledFutureTuple,
                                                            Integer intervalSecond,
                                                            Runnable taskSendMessage) {
        ScheduledFuture<?> second = scheduledFutureTuple.getSecond();
        if (ObjectUtil.isEmpty(second) && intervalSecond != null && taskSendMessage != null) {
            scheduledFutureTuple.setSecond(SpringUtil.getBean(WSUtil.class).defineScheduledTask(intervalSecond, taskSendMessage));
        }
        for (ConcurrentHashMap<String, ConcurrentHashMap<Integer, Tuple<CopyOnWriteArraySet<Session>, ScheduledFuture<?>>>> value : metaScheduledWSMap.values()) {
            List<String> removeKey = new ArrayList<>();
            for (Map.Entry<String, ConcurrentHashMap<Integer, Tuple<CopyOnWriteArraySet<Session>, ScheduledFuture<?>>>> entry : value.entrySet()) {
                long count = entry.getValue().values().stream().flatMap(tuple -> tuple.getFirst().stream()).count();
                if (count == 0) {
                    removeKey.add(entry.getKey());
                }
            }
            for (String key : removeKey) {
                value.remove(key);
            }
        }
    }

    /**
     * 获取 GEWS（Global Event WebSocket Status）的状态信息。
     *
     * <p>该方法遍历 {@link #metaScheduledWSMap} 中的所有条目，统计每个类的会话数量、被动会话数量和主动会话数量，
     * 并将这些信息汇总为一个包含总览和详细信息的 {@link LinkedHashMap}。</p>
     *
     * <p>返回值结构如下：</p>
     * <ul>
     *     <li><strong>"总览"</strong>: 包含全局会话总数、全局被动会话数和全局主动会话数。</li>
     *     <li><strong>"内容"</strong>: 包含每个类的详细状态信息，包括概览和详情。</li>
     * </ul>
     *
     *
     * @return 返回一个包含 GEWS 状态信息的 {@link LinkedHashMap} 实例
     */
    public static LinkedHashMap<String, Object> getIntervalWSStatus() {

        // 初始化存储结果的 LinkedHashMap
        LinkedHashMap<String, Object> info = new LinkedHashMap<>();

        // 初始化全局统计变量
        Integer sumAllSize = 0; // 全局会话总数量
        Integer sumPassiveSessionCount = 0; // 全局被动会话数量
        Integer sumActiveSize = 0; // 全局主动会话数量

        // 遍历 metaScheduledWSMap 中的每个条目
        for (Map.Entry<
                Class<?>,
                ConcurrentHashMap<String, ConcurrentHashMap<Integer, Tuple<CopyOnWriteArraySet<Session>, ScheduledFuture<?>>>>
                >
                entry : metaScheduledWSMap.entrySet()) {

            // 获取当前类及其对应的状态映射
            Class<?> entityClass = entry.getKey();
            ConcurrentHashMap<String, ConcurrentHashMap<Integer, Tuple<CopyOnWriteArraySet<Session>, ScheduledFuture<?>>>> statusMap = entry.getValue();
            LinkedHashMap<String, Object> statusMapConvert = new LinkedHashMap<>();
            for (Map.Entry<String, ConcurrentHashMap<Integer, Tuple<CopyOnWriteArraySet<Session>, ScheduledFuture<?>>>> stateInfo : statusMap.entrySet()) {
                String paramTagKey = stateInfo.getKey();
                LinkedHashMap<String, Object> stateInfoContent = new LinkedHashMap<>();
                ConcurrentHashMap<Integer, Tuple<CopyOnWriteArraySet<Session>, ScheduledFuture<?>>> value = stateInfo.getValue();
                value.forEach((intervalSecond, tuple) -> {
                    List<String> sessionIds = tuple.getFirst().stream().map(session -> session.getId()).collect(Collectors.toList());
                    stateInfoContent.put(intervalSecond.toString(), Tuple.newTuple(sessionIds, tuple.getSecond()));
                });
                statusMapConvert.put(paramTagKey, stateInfoContent);
            }


            // 计算当前类的会话数量、被动会话数量和主动会话数量
            Integer allSize = getWebSockets(entityClass).size(); // 当前类的会话总数量
            Integer passiveSessionCount = getClassPassiveSessionCount(entityClass); // 当前类的被动会话数量
            Integer activeSize = allSize - passiveSessionCount; // 当前类的主动会话数量

            // 更新全局统计变量
            sumAllSize += allSize;
            sumPassiveSessionCount += passiveSessionCount;
            sumActiveSize += activeSize;

            // 将当前类的状态信息存入 info 映射中
            info.put(entityClass.getSimpleName(), Maps.asMap(
                    Maps.put("概览",
                            Maps.asMap(
                                    Maps.put("会话总数量", allSize),
                                    Maps.put("被动会话数量", passiveSessionCount),
                                    Maps.put("主动会话数量", activeSize)
                            )
                    ),
                    Maps.put("详情", statusMapConvert)
            ));
        }

        // 构建最终返回结果
        LinkedHashMap<String, Object> reuslt = new LinkedHashMap<>();
        reuslt.put("总览", Maps.asMap(
                Maps.put("GE全局会话总数量", sumAllSize),
                Maps.put("GE全局被动会话数量", sumPassiveSessionCount),
                Maps.put("GE全局主动会话数量", sumActiveSize)
        ));
        reuslt.put("内容", info);

        return reuslt;
    }

    //endregion

    //region 当前会话对象参数方法
    /**
     * 当前 WebSocket 会话对象。
     */
    protected Session session;

    /**
     * 消息推送的时间间隔（单位：秒），null 表示不启用自动推送。
     */
    protected Integer intervalSecond;

    /**
     * 标识当前连接是否为主动模式。
     * 主动模式下不依赖定时任务，仅用于手动请求或状态统计。
     */
    private Boolean isActice = true;

    /**
     * 当前实例所属的具体子类类型，用于隔离不同子类的静态资源。
     */
    private final transient Class<?> entityClass = this.getClass();

    /**
     * 定时任务元组，包含：
     * - 第一个元素：关联该任务的 WebSocket 会话集合；
     * - 第二个元素：对应的定时任务调度器对象。
     */
    private Tuple<CopyOnWriteArraySet<Session>, ScheduledFuture<?>> scheduledFutureTuple;

    /**
     * 获取或创建与当前会话参数和时间间隔对应的定时任务元组。
     *
     * <p>该元组包含：
     * <ul>
     *     <li>一组关联的 WebSocket 会话集合</li>
     *     <li>一个可调度的任务对象（如定时器）</li>
     * </ul>
     *
     * @return 包含会话集合和定时任务的元组对象
     */
    private Tuple<CopyOnWriteArraySet<Session>, ScheduledFuture<?>> getScheduledFutureTuple() {

        ConcurrentHashMap<Integer, Tuple<CopyOnWriteArraySet<Session>, ScheduledFuture<?>>> scheduledMap =
                getScheduledWSMap(entityClass).computeIfAbsent(getSessionParamKey(), k -> new ConcurrentHashMap<>());

        Tuple<CopyOnWriteArraySet<Session>, ScheduledFuture<?>> scheduledFutureTuple = scheduledMap
                .computeIfAbsent(intervalSecond, k -> new Tuple<>(new CopyOnWriteArraySet<>(), null));

        return scheduledFutureTuple;
    }

    /**
     * 添加新的 WebSocket 会话到全局存储，并根据连接模式初始化相关资源。
     *
     * <p>如果为“主动模式”（intervalSecond == 0 且 tagNames == "null"），则不绑定定时任务；
     * 否则进入“被动模式”，将订阅指定数据点并启动定时推送任务。
     *
     * @param session        当前建立的 WebSocket 会话
     * @param intervalSecond 推送时间间隔（秒）；0 表示不启用自动推送
     */
    private void addSessionStorge(Session session, Integer intervalSecond) {
        //普通主动模式连接必须参数
        this.session = session;
        getWebSockets(entityClass).add(session);
        if (intervalSecond == 0) {
            Integer allSize = getWebSockets(entityClass).size();
            Integer passiveSessionCount = getPassiveSessionCount(null);
            Integer activeSize = allSize - passiveSessionCount;
            log.info("【{}消息】有新的主动连接，所有会话总数为:{},主动连接数:{},被动连接数:{}", entityClass.getSimpleName(), allSize, activeSize, passiveSessionCount);
            this.isActice = true;
            return;
        }
        //被动模式需要所有参数
        WSUtil.errorClose(session, "时间间隔不能为空", ObjectUtil.isEmpty(intervalSecond));
        this.intervalSecond = intervalSecond;
        scheduledFutureTuple = getScheduledFutureTuple();
        scheduledFutureTuple.getFirst().add(session);
        this.isActice = false;
    }

    /**
     * 启动或更新当前 WebSocket 会话的消息定时推送任务。
     *
     * <p>该方法仅在“被动模式”下生效（即 {@link #isActice} 为 false）。它根据指定的时间间隔启动或更新定时任务，
     * 并记录当前连接状态（包括主动和被动连接数量）用于监控与日志追踪。
     *
     * <p>执行逻辑如下：
     * <ol>
     *     <li>如果当前是主动模式（{@link #isActice} == true），直接返回，不执行后续操作。</li>
     *     <li>调用 {@link #computeScheduledTask(Tuple, Integer, Runnable)} 方法，安排或更新定时任务。</li>
     *     <li>统计所有会话总数、被动连接数和主动连接数，并记录日志信息。</li>
     * </ol>
     *
     * @param intervalSecond 推送消息的时间间隔（单位：秒）。null 表示不启用自动推送。
     */
    private void runToSendMessage(Integer intervalSecond) {
        if (isActice) return;
        computeScheduledTask(scheduledFutureTuple, intervalSecond, taskSendMessage());
        Integer allSize = getWebSockets(entityClass).size();
        Integer passiveSessionCount = getPassiveSessionCount(null);
        Integer activeSize = allSize - passiveSessionCount;
        log.info("【{}消息】有新的被动连接，所有会话总数为:{},主动连接数:{},被动连接数:{}", entityClass.getSimpleName(), allSize, activeSize, passiveSessionCount);
    }

    /**
     * 处理新的 WebSocket 连接，并根据指定的时间间隔启动消息推送任务。
     *
     * <p>该方法是 WebSocket 消息处理的核心入口，执行以下两步操作：
     * <ol>
     *     <li>调用 {@link #addSessionStorge(Session, Integer)} 方法，将当前会话添加到全局存储，
     *         并根据连接模式（主动/被动）初始化相关资源。</li>
     *     <li>调用 {@link #runToSendMessage(Integer)} 方法，启动或更新定时任务以推送消息。</li>
     * </ol>
     *
     * <p>适用场景：
     * <ul>
     *     <li>主动模式：无需时间间隔和点位信息，直接记录日志并完成连接。</li>
     *     <li>被动模式：需要绑定时间间隔和点位信息，并加入定时任务调度。</li>
     * </ul>
     *
     * @param session        当前建立的 WebSocket 会话
     * @param intervalSecond 推送消息的时间间隔（单位：秒），0 表示不启用自动推送
     */
    protected void runSendGeWsMessage(Session session, Integer intervalSecond) {
        addSessionStorge(session, intervalSecond);
        runToSendMessage(intervalSecond);
    }

    /**
     * 生成当前会话的参数键，用于唯一标识 WebSocket 连接的上下文。
     *
     * <p>该方法根据以下信息生成参数键：</p>
     * <ul>
     *     <li>{@link #entityClass}：当前类名用于隔离不同子类的连接上下文。</li>
     *     <li>查询参数（去除 token 后的剩余部分）：通过解析当前会话的 URI 查询字符串获取。</li>
     * </ul>
     *
     * <p>若存在查询参数，则返回格式为：</p>
     * <pre>{@code tagNames&key1=value1&key2=value2...}</pre>
     * 若不存在查询参数，则仅返回 {@link #entityClass}。
     *
     * @return 当前会话的参数键，用于区分不同的连接上下文。
     */
    protected String getSessionParamKey() {

        Map<String, String> paramMap = parseParameters(session);
        paramMap.remove("token");
        if (CollUtil.isNotEmpty(paramMap)) {
            return paramMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));
        }
        return entityClass.getSimpleName();
    }

    /**
     * 定义一个消息发送任务，用于定时推送消息给关联的 WebSocket 会话。
     *
     * <p>该方法返回一个 {@link Supplier} 实现，执行以下操作：
     * <ol>
     *     <li>获取当前关联的 WebSocket 会话集合。</li>
     *     <li>调用 {@link #getMessage()} 获取需要推送的消息内容。</li>
     *     <li>遍历会话集合，向每个活动的会话发送消息。</li>
     *     <li>清理已关闭或无效的会话，并取消空闲的定时任务。</li>
     * </ol>
     *
     * <p>适用场景：
     * <ul>
     *     <li>作为定时任务的核心逻辑，定期生成并推送消息。</li>
     *     <li>支持异常处理，确保在消息生成或发送失败时不会中断任务。</li>
     * </ul>
     *
     * @return 消息发送任务的实现，每次调用将生成并推送消息。
     */
    protected Runnable taskSendMessage() {
        return () -> {
            //当前WS同类型请求,同时间间隔,回话集合
            CopyOnWriteArraySet<Session> geCurrentVauleWS = scheduledFutureTuple.getFirst();
            //自持当前任务对象
            ScheduledFuture<?> scheduledFuture = scheduledFutureTuple.getSecond();
            if (CollUtil.isEmpty(geCurrentVauleWS)) {
                scheduledFutureTuple.setSecond(null);
                scheduledFuture.cancel(true);
                return ;
            }
            String message = "";
            try {
                message = getMessage();
            } catch (Exception e) {
                message = e.getMessage();
            }
            List<Session> badWS = new ArrayList<>();
            for (Session session : geCurrentVauleWS) {
                if (!session.isOpen()) {
                    badWS.add(session);
                    continue;
                }
                try {
                    WSUtil.sendMessage(session, message);
                } catch (Exception e) {
                    e.printStackTrace();
                    WSUtil.sendMessage(session, e.getMessage());
                }
            }
            if (CollUtil.isNotEmpty(badWS)){
                geCurrentVauleWS.removeAll(badWS);
            }
        };
    }

    /**
     * 统计当前类型下所有被动模式连接的数量。
     *
     * <p>该方法通过遍历全局定时任务映射表（{@link #metaScheduledWSMap}），筛选出所有处于被动模式的活动会话，
     * 并返回其总数。如果指定了特定会话（BaseIntervalWs.java L60-L60 参数不为空），则在统计时排除该会话。
     *
     * <p>执行逻辑如下：
     * <ol>
     *     <li>从 {@link #metaScheduledWSMap} 中提取所有关联的会话集合。</li>
     *     <li>过滤出处于打开状态的会话（即 `Session.isOpen()` 返回 `true`）。</li>
     *     <li>如果提供了特定会话，则从结果中排除该会话。</li>
     *     <li>返回符合条件的会话总数。</li>
     * </ol>
     *
     * @param session 需要排除的特定会话对象，可为空。如果不为空，则在统计时排除该会话。
     * @return 当前类型下所有被动模式连接的数量。
     */
    private Integer getPassiveSessionCount(Session session) {
        ConcurrentHashMap<String, ConcurrentHashMap<Integer, Tuple<CopyOnWriteArraySet<Session>, ScheduledFuture<?>>>> scheduledWSMap = getScheduledWSMap(entityClass);
        Stream<Session> sessionStream = scheduledWSMap.values()
                .stream()
                .flatMap(map -> map.values().stream())
                .map(Tuple::getFirst)
                .flatMap(set -> set.stream())
                .filter(Session::isOpen);
        if (ObjectUtil.isNotEmpty(session)) {
            sessionStream = sessionStream.filter(openSession -> !openSession.equals(session));
        }
        Long count = sessionStream.count();
        return count.intValue();
    }

    private static Integer getClassPassiveSessionCount(Class entityClass) {
        ConcurrentHashMap<String, ConcurrentHashMap<Integer, Tuple<CopyOnWriteArraySet<Session>, ScheduledFuture<?>>>> scheduledWSMap = getScheduledWSMap(entityClass);
        Stream<Session> sessionStream = scheduledWSMap.values()
                .stream()
                .flatMap(map -> map.values().stream())
                .map(Tuple::getFirst)
                .flatMap(set -> set.stream())
                .filter(Session::isOpen);
        Long count = sessionStream.count();
        return count.intValue();
    }
    //endregion

    //region 抽象的方法和需要重写实现的功能

    /**
     * 获取需要推送的消息内容。
     *
     * <p>该方法由子类实现，用于定义具体的业务逻辑以生成消息内容。
     * 返回的消息将通过 WebSocket 推送给客户端。
     *
     * @return 消息内容字符串。如果发生异常，建议返回异常信息以便调试。
     */
    protected abstract String getMessage();

    /**
     * 解析当前 WebSocket 会话的查询参数。
     *
     * <p>该方法从当前会话的 URI 查询字符串中提取键值对，并将其解析为一个 Map。
     * 使用 UTF-8 编码对查询字符串进行解码，确保参数值的正确性。
     *
     * <p>适用场景：
     * <ul>
     *     <li>解析客户端传递的连接参数（如 token、用户标识等）。</li>
     *     <li>为生成唯一标识符（如 {@link #getSessionParamKey()}）提供基础数据。</li>
     * </ul>
     *
     * @param session 当前 WebSocket 会话对象
     * @return 包含查询参数的键值对 Map。如果查询字符串为空，则返回空 Map。
     */
    protected Map<String, String> parseParameters(Session session) {
        String query = session.getRequestURI().getQuery();
        // 将查询字符串解析为 Map
        Map<String, String> paramMap = HttpUtil.decodeParamMap(query, StandardCharsets.UTF_8);
        return paramMap;
    }

    /**
     * 关闭当前 WebSocket 会话，并清理相关资源。
     *
     * <p>该方法执行以下操作：
     * <ol>
     *     <li>从全局会话集合中移除当前会话。</li>
     *     <li>取消与当前会话关联的定时任务（如果存在）。</li>
     *     <li>统计所有会话总数、主动连接数和被动连接数，并记录日志信息。</li>
     * </ol>
     *
     * <p>适用场景：
     * <ul>
     *     <li>当客户端断开连接时，确保资源被正确释放。</li>
     *     <li>避免内存泄漏或无效任务继续运行。</li>
     * </ul>
     */
    public void onClose() {
        CopyOnWriteArraySet<Session> webSockets = getWebSockets(entityClass);
        webSockets.remove(session);
        // 获取与当前会话关联的任务
        ScheduledFuture<?> future = (ScheduledFuture<?>) session.getUserProperties().get("task");
        // 检查任务是否存在且未被取消
        if (ObjectUtil.isNotEmpty(future) && !future.isCancelled()) {
            // 如果任务正在执行中，尝试中断该线程（通过调用线程的 interrupt() 方法）。
            // 尽可能快地中止任务，哪怕它已经在运行。
            boolean cancel = future.cancel(true);
        }
        Integer allSize = webSockets.size();
        Integer passiveSessionCount = getPassiveSessionCount(session);
        Integer activeSize = allSize - passiveSessionCount;
        // 记录关闭连接的消息和当前连接的总数
        log.info("【{}消息】连接断开，所有会话总数为:{},主动连接数:{},被动连接数:{}", this.getClass().getSimpleName(), allSize, activeSize, passiveSessionCount);
    }


    //endregion
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseIntervalWs that = (BaseIntervalWs) o;
        return Objects.equals(session, that.session) && Objects.equals(intervalSecond, that.intervalSecond);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session, intervalSecond);
    }


}
