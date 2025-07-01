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
### 13. **动态编译工具**

- 支持动态编译 Java 代码，并执行结果。
- 支持动态加载类，并执行结果。
- 支持springboot jar 环境,解压jar中引用的库到 临时文件目录的 tempCompilerDir

#### 注意事项：
jdk版本大于8的时候,本地解析classpath 会用到反射获取jdk内部类，
但是8之后的jdk做了处理，需要添加 vm配置 开启 ` --add-opens java.base/jdk.internal.loader=ALL-UNNAMED` 才能运行。

本地项目如果使用的注解处理器,编译时动态生成代码的,例如lombok, 
如果jdk版本是8 需要jdk运行环境中或者环境变量中 classpath 添加 tools.jar, idea中开发在项目sdk中添加 tools.jar 也可以。
版本大于jdk8 ,tools.jar 功能默认基础在jre 中,并且也jdk移除了 tools.jar, 无需过多配置

#### 示例：
```java
    @Test
    public void testHaoCompliler() throws Exception {
        long start = System.currentTimeMillis();
        String className = "com.example.demo.Greeter";
        String javaCode = "package com.example.demo;\n" +
                "\n" +
                "import org.hao.core.print.PrintUtil;\n" +
                "import org.hao.spring.SpringRunUtil;\n" +
                "import org.hao.annotation.LogDefine;\n" +
                "\n" +
                "public class Greeter {\n" +
                "    @LogDefine(\"123\")        " +
                "    public void sayHello(String name) {\n" +
                "        System.out.println(\"Hello, \" + name + \"!\");\n" +
                "        PrintUtil.BLUE.Println(\"name = \" + name);\n" +
                "        SpringRunUtil.printRunInfo();\n" +
                "    }\n" +
                "}";
        String currentWorkingDirectory = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentWorkingDirectory);
        // 使用工具类编译并加载类
        Class<?> clazz = CompilerUtil.compileAndLoadClass(className, javaCode);
        long end = System.currentTimeMillis();
        log.info("testHaoCompliler 编译耗时：{}ms", end - start);
        Method sayHello = clazz.getMethod("sayHello", String.class);
        LogDefine annotation = sayHello.getAnnotation(LogDefine.class);

        // 创建类实例并调用方法
        Object obj = clazz.getDeclaredConstructor().newInstance();
        sayHello.invoke(obj, "World");
    }
```
以下是关于 **FailSafe 功能** 的使用说明，可将其补充到 [README.md](file://D:\Project\private\Java\HaoUtil\README.md) 中的 **“失败保障工具”** 模块。

---

### 14. **失败保障工具 - FailSafe**

#### 功能描述：

基于 [Failsafe](https://failsafe.dev/) 提供灵活的失败处理机制（如重试、熔断、超时等），支持自定义策略和执行回调。适用于网络请求、数据库操作、第三方接口调用等易失败场景。

通过封装 [FailSafeHandler](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\core\failsafe\FailSafeHandler.java#L17-L100) 接口和 `FailSafeHandlerExecutor`，可以快速构建具有失败恢复能力的任务，并监控任务执行状态（成功、失败、重试、完成）。

#### 示例代码：

##### 自定义 FailSafeHandler 实现类

```java
@Slf4j
public class DemoFailSafeHandler<Object> implements FailSafeHandler<Object> {

    @Override
    public Policy<Object> initFailSafe() {
        return RetryPolicy.<Object>builder()
                .handle(Exception.class)
                .withMaxRetries(3)
                .withDelay(Duration.ofSeconds(1))
                .onRetry(event -> log.info("重试中... 尝试第 {} 次", event.getAttemptCount()))
                .onSuccess(event -> log.info("操作成功，尝试次数：{}", event.getAttemptCount()))
                .onFailure(event -> log.error("操作失败，尝试次数：{}", event.getAttemptCount()))
                .build();
    }

    @Override
    public void onComplete(ExecutionCompletedEvent<Object> event) {
        log.info("操作已完成，结果为: {}", event.getResult());
    }
}
```

##### 使用 FailSafeHandler 执行任务

```java
@Test
public void testFailsafeHandler() throws Exception {
    FailSafeHandler<Integer> handler = new DemoFailSafeHandler<>();
    Integer result = FailSafeHandlerExecuteor.execute(handler, () -> {
        int i = RandomUtil.randomInt(0, 3);
        if (i != 2) throw new Exception("模拟失败");
        return i;
    });
    System.out.println("最终结果：" + result);
}
```

#### 特性支持：

- ✅ **重试策略**：配置最大重试次数、延迟时间、指数退避等。
- ✅ **熔断机制**：自动切断故障链路，防止雪崩效应（需自行实现熔断逻辑）。
- ✅ **超时控制**：设置任务最大执行时间。
- ✅ **降级处理**：失败后返回默认值或备用方案（可扩展实现）。
- ✅ **事件监听**：提供 onRetry / onSuccess / onFailure / onComplete 回调方法。

---

#### 注解方式集成 AOP 切面（可选）

你可以结合 [@FailSafeRule](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\annotation\FailSafeRule.java#L11-L15) 注解与切面 [FailSafeAspect](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\aspect\FailSafeAspect.java#L22-L81) 实现方法级别的 FailSafe 管理。


##### 在方法上使用注解

```java
@FailSafeRule(handler = DemoFailSafeHandler.class)
public String retryableMethod() {
    // 可能会抛异常的方法
    return "success";
}
```


---

#### 配置启用 FailSafe 支持

在 `application.yml` 中开启 FailSafe 相关功能：

```yaml
hao-util:
  enable-failsafe: true # 启用 FailSafe 支持
```


---

#### 适用场景

| 场景                 | 说明 |
|----------------------|------|
| 网络请求失败重试       | HTTP 调用、RPC 调用等 |
| 数据库连接/操作容错     | 连接中断、事务失败等情况 |
| 第三方 API 接口调用    | 外部服务不稳定时进行重试 |
| 关键业务流程保障       | 如支付、订单提交等 |

---

#### 依赖引入

确保项目中已引入 Failsafe 依赖（HaoUtil 已内置）：

```xml
<dependency>
    <groupId>dev.failsafe</groupId>
    <artifactId>failsafe</artifactId>
</dependency>
```


---

#### 补充建议

- 可根据实际需求继承 [FailSafeHandler](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\core\failsafe\FailSafeHandler.java#L17-L100) 并覆盖 [initFailSafe()](file://D:\Project\private\Java\HaoUtil\src\main\java\org\hao\core\failsafe\FailSafeHandler.java#L33-L47) 方法来自定义策略。
- 对于高并发场景，建议配合线程池使用，避免阻塞主线程。
- 日志输出建议详细记录每次重试信息，便于排查问题。


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
    <version>1.0.15.9</version>
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