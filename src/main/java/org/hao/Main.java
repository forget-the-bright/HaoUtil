package org.hao;

import cn.hutool.core.io.LineHandler;
import cn.hutool.core.util.ReflectUtil;
import org.hao.core.ExpressionUtil;
import org.hao.core.print.PrintUtil;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.compiler.CompiledExpression;
import org.mvel2.compiler.ExpressionCompiler;
import org.mvel2.integration.impl.ImmutableDefaultFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;

import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {
    public static void main(String[] args) {
        //TIP 当文本光标位于高亮显示的文本处时按 <shortcut actionId="ShowIntentionActions"/>
        // 查看 IntelliJ IDEA 建议如何修正。
        System.out.printf("Hello and welcome!");

        for (int i = 1; i <= 5; i++) {
            //TIP 按 <shortcut actionId="Debug"/> 开始调试代码。我们已经设置了一个 <icon src="AllIcons.Debugger.Db_set_breakpoint"/> 断点
            // 但您始终可以通过按 <shortcut actionId="ToggleLineBreakpoint"/> 添加更多断点。
            System.out.println("i = " + i);
            PrintUtil.BLUE.Println("i = " + i);
        }

        // 创建上下文
        MapVariableResolverFactory resolverFactory = new MapVariableResolverFactory(new HashMap<>());
        // resolverFactory.createVariable("base", new Main());
       // resolverFactory.createVariable("Math", Math.class);
        // resolverFactory.createVariable("add2", (BiFunction<Integer, Integer, Integer>) Main::add);

        // test();
        // 定义表达式
        String expression =
                "print(\"hello world!!!\");" +
                        "a = sub(4.125458,8.1); print(a); toDate(now()); now();b= offsetDay(toDate('2024-08-11 23:00:59'),10);" +
                        "Math.addExact(a,10); offsetYear(b,10);";

      // expression = "print(\"hello world!!!\");";
        ParserContext context = ExpressionUtil.buildParserContext();
        context.addImport(Math.class.getSimpleName(), Math.class);
       // resolverFactory.createVariable("a", Object.class);
        Serializable serializable = MVEL.compileExpression(expression, context);
        Object result = MVEL.executeExpression(serializable, context,resolverFactory);
        PrintUtil.RED.Println("eval :" + result);
    }

    private static void test() {
        ParserContext parserContext = new ParserContext();
        parserContext.addImport(Math.class.getSimpleName(), Math.class);
        parserContext.addImport("add", ReflectUtil.getMethod(Math.class, "addExact", int.class, int.class));
        Serializable s = MVEL.compileExpression("add(1,2)", parserContext);
        Object o = MVEL.executeExpression(s, parserContext);
    }

}