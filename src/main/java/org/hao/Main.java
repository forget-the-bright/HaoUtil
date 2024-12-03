package org.hao;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import org.hao.core.ExpressionUtil;
import org.hao.core.HutoolPlus;
import org.hao.core.print.PrintUtil;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.integration.impl.MapVariableResolverFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;


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
         resolverFactory.createVariable("Math", Math.class);
        // resolverFactory.createVariable("add2", (BiFunction<Integer, Integer, Integer>) Main::add);


        // 定义表达式
        String expression =
                "print(\"hello world!!!\");" +
                "a = sub(4.125458,8.1); print(a); toDate(null); now(); offsetDay(toDate('2024-08-11 23:00:59'),10);" +
                "Math.addExact(a,10)";

        ParserContext context = new ParserContext();
        MVEL.compileExpression(ExpressionUtil.rulesTemplate, context);

        // 获取函数列表
        context.getFunctions().forEach((name, function) -> {
            System.out.println("Function name: " + name);
        });
        // 执行表达式
        Object result = ExpressionUtil.eval(expression, resolverFactory);
        PrintUtil.RED.Println("eval :" + result);
    }

}