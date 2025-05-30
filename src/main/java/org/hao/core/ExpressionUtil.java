package org.hao.core;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于 MVEL 表达式引擎的工具类，用于编译、执行动态表达式，并支持内置函数与全局变量管理。
 *
 * <p>该类封装了对表达式的解析、执行以及上下文管理逻辑，适用于规则引擎、动态脚本等场景，
 * 支持扩展自定义函数和变量。</p>
 *
 * @author wanghao (helloworlwh@163.com)
 * @since 2024/12/3
 */

public class ExpressionUtil {

    // 初始化规则模板
    public static final String rulesTemplate = initTemplate();
    // 内置函数映射
    private static final Map<String, Method> builtInFunc = initBuiltInFuncMap();
    // 变量解析工厂
    private static final Map<String, Object> variableResolverFactory = initVariableResolverFactory();

    /**
     * 获取变量解析工厂的副本
     *
     * @return 变量解析工厂的副本
     */
    public static Map<String, Object> getVariableResolverFactory() {
        return new HashMap<>(variableResolverFactory);
    }

    /**
     * 获取内置函数的副本
     *
     * @return 内置函数的副本
     */
    public static Map<String, Object> getBuiltInFunc() {
        return new HashMap<>(builtInFunc);
    }

    /**
     * 初始化模板
     *
     * @return 从类路径中读取模板文件并返回内容
     */
    private static String initTemplate() {
        InputStream inputByClassPath = HutoolPlus.getInputByClassPath("META-INF/rulesTemplate.js");
        return IoUtil.read(inputByClassPath, StandardCharsets.UTF_8);
    }

    /**
     * 初始化变量解析工厂
     *
     * @return 变量解析工厂
     */
    private static Map<String, Object> initVariableResolverFactory() {
        if (variableResolverFactory == null) {
            HashMap<String, Object> variableResolverFactory = new HashMap<>();
            variableResolverFactory.put(DateUtil.class.getSimpleName(), DateUtil.class);
            variableResolverFactory.put(StrUtil.class.getSimpleName(), StrUtil.class);
            variableResolverFactory.put(NumberUtil.class.getSimpleName(), NumberUtil.class);
            return variableResolverFactory;
        }
        return variableResolverFactory;
    }

    /**
     * 初始化内置函数映射
     *
     * @return 内置函数映射
     */
    private static Map<String, Method> initBuiltInFuncMap() {
        Method[] methods = ReflectUtil.getMethodsDirectly(ExpressionBuiltInFunc.class, false, false);
        return Maps.asMap(Arrays.stream(methods).map(method -> Maps.put(method.getName(), method)).collect(Collectors.toList()), Method.class);
    }

    /**
     * 构建表达式
     *
     * @param expression 原始表达式
     * @return 将规则模板和表达式合并后的结果
     */
    private static String buildExpression(String expression) {
        return rulesTemplate + "\n " + expression;
    }

    /**
     * 评估表达式
     *
     * @param expression 表达式
     * @param vars       变量
     * @return 评估结果
     */
    public static Object eval(String expression, Map<String, Object> vars) {
        return MVEL.eval(buildExpression(expression), vars);
    }

    /**
     * 评估表达式
     *
     * @param expression      表达式
     * @param resolverFactory 变量解析工厂
     * @return 评估结果
     */
    public static Object eval(String expression, VariableResolverFactory resolverFactory) {
        return MVEL.eval(buildExpression(expression), resolverFactory);
    }

    /**
     * 构建解析上下文
     *
     * @return 解析上下文
     */
    public static ParserContext buildParserContext() {
        ParserContext parserContext = ParserContext.create();
        builtInFunc.forEach((methodName, method) -> parserContext.addImport(methodName, method));
        //parserContext.setImports(builtInFunc);
        return parserContext;
    }

    /**
     * 执行表达式
     *
     * @param expression    表达式
     * @param parserContext 解析上下文
     * @return 执行结果
     */
    public static Object executeExpression(String expression, ParserContext parserContext) {
        return executeExpression(expression, parserContext, getVariableResolverFactory());
    }

    /**
     * 执行表达式
     *
     * @param expression 表达式
     * @return 执行结果
     */
    public static Object executeExpression(String expression) {
        return executeExpression(expression, buildParserContext(), getVariableResolverFactory());
    }

    /**
     * 执行表达式
     *
     * @param expression 表达式
     * @param vars       变量
     * @return 执行结果
     */
    public static Object executeExpression(String expression, Map<String, Object> vars) {
        vars.putAll(getVariableResolverFactory());
        return executeExpression(expression, buildParserContext(), vars);
    }

    /**
     * 执行表达式
     *
     * @param expression      表达式
     * @param resolverFactory 变量解析工厂
     * @return 执行结果
     */
    public static Object executeExpression(String expression, VariableResolverFactory resolverFactory) {
        return executeExpression(expression, buildParserContext(), resolverFactory);
    }

    /**
     * 执行表达式
     *
     * @param expression    表达式
     * @param parserContext 解析上下文
     * @param vars          变量
     * @return 执行结果
     */
    public static Object executeExpression(String expression, ParserContext parserContext, Map<String, Object> vars) {
        if (vars == null) {
            vars = getVariableResolverFactory();
        } else {
            vars.putAll(getVariableResolverFactory());
        }
        Serializable serializable = MVEL.compileExpression(expression, parserContext);
        Object result = MVEL.executeExpression(serializable, parserContext, vars);
        for (String key : getVariableResolverFactory().keySet()) {
            vars.remove(key);
        }
        return result;
    }

    /**
     * 执行表达式
     *
     * @param expression      表达式
     * @param parserContext   解析上下文
     * @param resolverFactory 变量解析工厂
     * @return 执行结果
     */
    public static Object executeExpression(String expression, ParserContext parserContext, VariableResolverFactory resolverFactory) {
        if (resolverFactory == null) {
            resolverFactory = new MapVariableResolverFactory(getVariableResolverFactory());
        }
        Serializable serializable = MVEL.compileExpression(expression, parserContext);
        return MVEL.executeExpression(serializable, parserContext, resolverFactory);
    }

    /**
     * 添加全局方法
     *
     * @param name   方法名
     * @param method 方法对象
     */
    public void addGlobalMethod(String name, Method method) {
        if (builtInFunc.containsKey(name)) {
            throw new RuntimeException("name:" + name + " is already exist");
        }
        if (variableResolverFactory.containsKey(name)) {
            throw new RuntimeException("name:" + name + " is already exist");
        }
        builtInFunc.put(name, method);
    }

    /**
     * 添加全局变量
     *
     * @param name   变量名
     * @param object 变量对象
     */
    public void addGlobalVariable(String name, Object object) {
        if (builtInFunc.containsKey(name)) {
            throw new RuntimeException("name:" + name + " is already exist");
        }
        if (variableResolverFactory.containsKey(name)) {
            throw new RuntimeException("name:" + name + " is already exist");
        }
        variableResolverFactory.put(name, object);
    }
}
