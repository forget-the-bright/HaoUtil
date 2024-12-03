package org.hao.core;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ReflectUtil;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.integration.VariableResolverFactory;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * ExpressionUtil 是一个用于处理表达式的工具类
 *
 * @author wanghao(helloworlwh @ 163.com)
 * date 2024/12/3 下午5:45
 */
public class ExpressionUtil {


    public static final String rulesTemplate = initTemplate();
    public static final ParserContext parserContext = buildParserContext();

    private static String initTemplate() {
        InputStream inputByClassPath = HutoolPlus.getInputByClassPath("META-INF/rulesTemplate.js");
        return IoUtil.read(inputByClassPath, StandardCharsets.UTF_8);
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
        Method[] methods = ReflectUtil.getMethodsDirectly(ExpressionBuiltInFunc.class,false,false);
        for (Method method : methods) {
            parserContext.addImport(method.getName(), method);
        }
        return parserContext;
    }
}
