package org.hao.core;

import cn.hutool.core.io.IoUtil;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @Description TODO
 * @Author wanghao(helloworlwh @ 163.com)
 * @Date 2024/12/3 下午5:45
 */
public class ExpressionUtil {


    private static final String rulesTemplate = initTemplate();

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
}
