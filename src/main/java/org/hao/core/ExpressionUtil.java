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
 * ExpressionUtil 是一个用于处理表达式的工具类
 *
 * @author wanghao(helloworlwh @ 163.com)
 * date 2024/12/3 下午5:45
 */
public class ExpressionUtil {


    public static final String rulesTemplate = initTemplate();
    private static final Map<String, Method> builtInFunc = initBuiltInFuncMap();
    private static final Map<String, Object> variableResolverFactory = initVariableResolverFactory();

    public static Map<String, Object> getVariableResolverFactory() {
        return new HashMap<>(variableResolverFactory);
    }

    public static Map<String, Object> getBuiltInFunc() {
        return new HashMap<>(builtInFunc);
    }

    private static String initTemplate() {
        InputStream inputByClassPath = HutoolPlus.getInputByClassPath("META-INF/rulesTemplate.js");
        return IoUtil.read(inputByClassPath, StandardCharsets.UTF_8);
    }

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

    private static Map<String, Method> initBuiltInFuncMap() {
        Method[] methods = ReflectUtil.getMethodsDirectly(ExpressionBuiltInFunc.class, false, false);
        return Maps.asMap(Arrays.stream(methods).map(method -> Maps.put(method.getName(), method)).collect(Collectors.toList()), Method.class);
    }

    private static String buildExpression(String expression) {
        return rulesTemplate + "\n " + expression;
    }

    public static Object eval(String expression, Map<String, Object> vars) {
        return MVEL.eval(buildExpression(expression), vars);
    }

    public static Object eval(String expression, VariableResolverFactory resolverFactory) {
        return MVEL.eval(buildExpression(expression), resolverFactory);
    }

    public static ParserContext buildParserContext() {
        ParserContext parserContext = ParserContext.create();
        builtInFunc.forEach((methodName, method) -> parserContext.addImport(methodName, method));
        //parserContext.setImports(builtInFunc);
        return parserContext;
    }

    public static Object executeExpression(String expression, ParserContext parserContext) {
        return executeExpression(expression, parserContext, getVariableResolverFactory());
    }

    public static Object executeExpression(String expression) {
        return executeExpression(expression, buildParserContext(), getVariableResolverFactory());
    }

    public static Object executeExpression(String expression, Map<String, Object> vars) {
        return executeExpression(expression, buildParserContext(), vars);
    }

    public static Object executeExpression(String expression, VariableResolverFactory resolverFactory) {
        return executeExpression(expression, buildParserContext(), resolverFactory);
    }

    public static Object executeExpression(String expression, ParserContext parserContext, Map<String, Object> vars) {
        if (vars == null) {
            vars = getVariableResolverFactory();
        }
        Serializable serializable = MVEL.compileExpression(expression, parserContext);
        return MVEL.executeExpression(serializable, parserContext, vars);
    }

    public static Object executeExpression(String expression, ParserContext parserContext, VariableResolverFactory resolverFactory) {
        if (resolverFactory == null) {
            resolverFactory = new MapVariableResolverFactory(getVariableResolverFactory());
        }
        Serializable serializable = MVEL.compileExpression(expression, parserContext);
        return MVEL.executeExpression(serializable, parserContext, resolverFactory);
    }

    public  void addGlobalMethod(String name, Method method) {
        if (builtInFunc.containsKey(name)) {
            throw new RuntimeException("name:" + name + " is already exist");
        }
        if (variableResolverFactory.containsKey(name)) {
            throw new RuntimeException("name:" + name + " is already exist");
        }
        builtInFunc.put(name, method);
    }

    public  void addGlobalVariable(String name, Object object) {
        if (builtInFunc.containsKey(name)) {
            throw new RuntimeException("name:" + name + " is already exist");
        }
        if (variableResolverFactory.containsKey(name)) {
            throw new RuntimeException("name:" + name + " is already exist");
        }
        variableResolverFactory.put(name, object);
    }
}
