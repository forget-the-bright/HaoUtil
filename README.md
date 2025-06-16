# HaoUtil 使用文档

HaoUtil 是一个基于 Spring Boot 的通用工具库，旨在简化开发流程、提高代码复用性，并提供丰富的实用功能。该库支持多种场景下的通用操作，包括
WebSocket 数据推送、日志切面处理、定时任务管理、表达式解析、Excel 导出等。

---

## 🧩 功能模块总览

| 模块                                                                                                       | 主要功能                                        |
|----------------------------------------------------------------------------------------------------------|---------------------------------------------|
| `spring`                                                                                                 | 提供 Spring Boot 启动增强功能，打印服务运行信息              |
| `core`                                                                                                   | 核心工具类，包含 IP 工具、线程池、集合操作、表达式引擎、WebSocket 支持等 |
| `annotation`                                                                                             | 注解支持，用于方法级日志记录和耗时统计                         |
| `aspect`                                                                                                 | 切面逻辑实现，配合注解完成日志拦截与输出                        |
| `config`                                                                                                 | 配置属性加载与自动装配                                 |
| `vo`                                                                                                     | 通用数据结构封装，如 Tuple、Tuples 等                   |
| [ws](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\config\HaoUtilProperties.java#L13-L13) | WebSocket 抽象基类与工具类，支持定时消息推送                 |

---

## 📦 主要功能详解

### 1. **Spring Boot启动增强 - [SpringRunUtil](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\spring\SpringRunUtil.java#L24-L63)**

#### 功能描述：

- 在 Spring Boot 启动后自动打印访问地址、Swagger 文档路径、IP 地址等信息。
- 支持在启动后执行自定义逻辑（通过 Consumer 接口）。

#### 示例：

```java
public static void main(String[] args) {
    SpringRunUtil.runAfter(Application.class, args, context -> {
        // 自定义启动后逻辑
    });
}
```

---

### 2. **WebSocket定时推送 - [BaseIntervalWs](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\core\ws\BaseIntervalWs.java#L28-L510), [WSUtil](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\core\ws\WSUtil.java#L20-L331)**

#### 功能描述：

- 提供 WebSocket 主动/被动连接模式，支持定时推送消息。
- 可根据不同的请求参数(重写getSessionParamKey())和时间间隔（intervalSecond）区分推送逻辑。
- 支持会话管理和资源清理，避免内存泄漏。

#### 示例：

```java

@Component
@ServerEndpoint("/data/GeCurrentVauleWS/{intervalSecond}") //此注解相当于设置访问URL
@Slf4j
@Data
public class MyWebSocket extends BaseIntervalWs {
    private Map<String, String> paramMap;

    @OnOpen
    @SneakyThrows
    public void onOpen(Session session,
                       @PathParam(value = "intervalSecond") Integer intervalSecond) {
        super.runSendGeWsMessage(session, intervalSecond);
    }

    /**
     * 解析参数，用于获取会话隔离的参数键,和定义自己需要的全局参数
     * @param session 当前会话
     * @return 当前会话开启携带的参数
     */
    @Override
    protected Map<String, String> parseParameters(Session session) {
        Map<String, String> paramMap = super.parseParameters(session);
        this.paramMap = paramMap;
        return paramMap;
    }

    /**
     * 获取会话隔离的参数键,同参数回话,会在同一个定时任务中执行。
     * @return 会话隔离的参数键
     */
    @Override
    protected String getSessionParamKey() {
        Map<String, String> paramMap = parseParameters(session);
        paramMap.remove("token");
        if (CollUtil.isNotEmpty(paramMap)) {
            return paramMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));
        }
        // 自定义会话参数键
        return "sessionParamKey";
    }

    /**
     * 定时任务执行获取数据的方法
     * @return 返回要发送的消息
     */
    @Override
    protected String getMessage() {
        return "当前时间：" + new Date() + paramMap;
    }

    @SneakyThrows
    @OnMessage
    public void onMessage(String message) {
        log.debug("【{}消息】收到客户端消息:{}", this.getClass().getSimpleName(), message);
        try {
            session.getAsyncRemote().sendText(message);
        } catch (Exception e) {
            session.getAsyncRemote().sendText(JSONObject.toJSONString(e.getMessage()));
        }

    }

    @OnClose
    @Override
    public void onClose() {
        super.onClose();
    }
}
```

---

### 3. **表达式解析 - [ExpressionUtil](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\core\ExpressionUtil.java#L31-L251)**

#### 功能描述：

- 基于 MVEL 表达式引擎，支持动态表达式计算。
- 内置数学函数、日期处理、日志输出等功能。
- 可扩展自定义函数与变量。

#### 示例：

```java
String expression = "add(2, 3); print('Hello World');";
Object result = ExpressionUtil.executeExpression(expression);
```

---

### 4. **日志切面与性能监控 - [@LogDefine](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\annotation\LogDefine.java#L16-L21), [@PrintLnTime](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\annotation\PrintLnTime.java#L17-L20)**

#### 功能描述：

- 支持通过注解对方法进行日志记录和耗时统计。
- 结合 Swagger 注解（`@ApiOperation`）可输出接口描述信息。
- 支持彩色控制台输出，提升可读性。

#### 示例：

```java

@PrintLnTime
@ApiOperation("测试接口")
@GetMapping("/test")
public String test() {
    return "OK";
}
```

---

### 5. **文件导出与样式设置 - [HutoolPlus](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\core\HutoolPlus.java#L37-L775)**

#### 功能描述：

- 封装 Hutool 的 Excel 操作，支持合并单元格、样式设置、模板导出。
- 提供响应头设置、文件下载等 Web 场景下的便捷方法。

#### 示例：

```java
ExcelWriter writer = ExcelUtil.getWriter("output.xlsx");
writer.write(dataList);
HutoolPlus.download(writer, response);
```

---

### 6. **本地资源加载 - [NativeUtils](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\core\NativeUtils.java#L21-L142)**

#### 功能描述：

- 支持从 JAR 包中加载本地 DLL/SO 文件。
- 自动创建临时目录并确保资源释放。

#### 示例：

```java
NativeUtils.loadLibraryFromJar("/lib/native.dll");
```

---

### 7. **配置化自动装配 - [HaoUtilAutoConfig](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\config\HaoUtilAutoConfig.java#L25-L64)**

#### 功能描述：

- 支持通过 `application.yml` 控制是否启用各功能模块。
- 包括日志切面、接口打印、WebSocket 支持等开关配置。

#### 示例配置 (`application.yml`)：

```yaml
hao-util:
  enabled: true
  print-interface: true
  enable-ws: true
  ws-scheduler-pool-size: 20
```

---

### 8. **通用数据结构构建 - [Maps](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\core\Maps.java#L20-L107), [Lists](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\core\Lists.java#L16-L62), [Tuples](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\vo\Tuples.java#L9-L73)**

#### 功能描述：

- 快速构建 Map、List、Tuple 等数据结构。
- 支持类型安全的构造方式，适用于复杂参数初始化。

#### 示例：

```java
Map<String, Object> map = Maps.asMap(
        Maps.put("name", "Tom"),
        Maps.put("age", 20)
);

List<String> list = Lists.generateList(5, () -> UUID.randomUUID().toString());
```

---

### 9. **IP与网络操作 - [IPUtils](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\core\ip\IPUtils.java#L28-L114)**

#### 功能描述：

- 获取本机所有 IP 地址。
- 识别 HTTP 请求中的客户端真实 IP（支持代理头）。

#### 示例：

```java
String clientIp = IPUtils.getIpAddr(request);
List<String> allIps = IPUtils.allIP;
```

---

### 10. **线程调度与等待 - [ThreadUtil](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\core\thread\ThreadUtil.java#L21-L81)**

#### 功能描述：

- 提供线程池管理、请求上下文传递、线程等待等工具方法。
- 支持等待线程池任务完成后再继续执行主线程。

#### 示例：

```java
ThreadPoolExecutor pool = ThreadUtil.getTheadPool(5);
pool.execute(() ->System.out.println("Task Running"));
ThreadUtil.waitThreadPoolCompleted(pool, "All Task Completed");
```

---

### 11. **控制台彩色打印 - [PrintUtil](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\core\print\PrintUtil.java#L12-L99)**

#### 功能描述：

- 使用 ANSI 转义码实现在控制台输出不同颜色的日志信息。
- 支持前景色、背景色及文本样式的组合输出。

#### 示例：

```java
import org.hao.core.print.PrintUtil;
PrintUtil.RED.Println("红色错误信息");
System.out.println(PrintUtil.BLUE.getColorStr("蓝色提示信息"));
```

---

### 12. **本地资源配置 - [HaoUtilProperties](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\config\HaoUtilProperties.java#L13-L52)**

#### 功能描述：

- 映射 `application.yml` 中的 `hao-util` 配置项。
- 支持控制日志打印、WebSocket、接口调用日志等功能开关。

#### 示例：

```java

@Autowired
private HaoUtilProperties haoUtilProperties;

// 获取线程池大小
int poolSize = haoUtilProperties.getWsSchedulerPoolSize();
```

---

## 📦 总线架构图（简略）

```
+-----------------------------+
|           Application       |
|     (Controller / Service) |
+------------+--------------+
             |
   +---------v---------+
   |     HaoUtil         |
   |  (工具链 & 切面)    |
   +---------+-----------+
             |
   +---------v---------+
   |     BaseIntervalWs  |
   |   WebSocket 推送    |
   +---------+-----------+
             |
   +---------v---------+
   |    WSUtil / Timer   |
   |  线程池 / 定时器  |
   +---------+-----------+
             |
   +---------v---------+
   |   ExpressionUtil    |
   |   表达式解析引擎    |
   +---------+-----------+
             |
   +---------v---------+
   |      PrintAspect    |
   |   日志与耗时切面    |
   +---------+-----------+
             |
   +---------v---------+
   |     NativeUtils     |
   |   本地资源加载器    |
   +-------------------+
```

---

## ✅ 如何使用 HaoUtil？

### 步骤 1：引入依赖

```xml

<dependency>
    <groupId>io.github.forget-the-bright</groupId>
    <artifactId>HaoUtil</artifactId>
    <version>1.0.15.2</version>
</dependency>
```

### 步骤 2：启用组件

在你的主类或配置类上添加：

```yaml
hao-util:
  enabled: true
```

### 步骤 3：配置选项（可选）

```yaml
hao-util:
  enabled: true
  print-interface: true
  enable-ws: true
  ws-scheduler-pool-size: 20
```

---

## 🔧 适用场景

| 场景             | 对应组件                              |
|----------------|-----------------------------------|
| 微服务调试          | `@PrintLnTime`, `@LogDefine`      |
| WebSocket 实时推送 | `BaseIntervalWs`, `WSUtil`        |
| 表达式脚本解析        | `ExpressionUtil`                  |
| 日志统一格式输出       | `PrintInterfaceUtil`, `PrintUtil` |
| Excel 导出       | `HutoolPlus`                      |
| 本地资源加载         | `NativeUtils`                     |
| IP 地址识别        | `IPUtils`                         |
| 多线程调度          | `ThreadUtil`                      |

---

## 📚 更多文档参考

- [项目源码 GitHub](https://github.com/forget-the-bright/HaoUtil)
- [Maven Central 发布版本](https://search.maven.org/artifact/io.github.forget-the-bright/HaoUtil)

---

## 🛡️ 开源协议

本项目采用 [Apache License 2.0](LICENSE)，欢迎自由使用与二次开发。