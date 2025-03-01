package org.hao.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class Lists {

    /**
     * 根据指定长度和元素生成器创建一个列表
     * 此方法用于当需要创建一个特定长度的列表，并且列表中的每个元素都需要通过给定的生成器函数来生成时
     * 通过使用泛型和函数式接口Supplier，此方法能够灵活地生成包含任何类型的元素的列表
     *
     * @param <T> 列表中元素的类型
     * @param length 列表的长度，即列表中将包含的元素数量
     * @param supplier 元素的生成器，用于生成列表中的每个元素
     * @return 返回一个包含由生成器生成的元素的列表
     */
    public static <T> List<T> generateList(int length, Supplier<T> supplier) {
        // 创建一个ArrayList实例来存储生成的元素
        ArrayList<T> arrayList = new ArrayList<>();
        // 循环指定次数，每次调用生成器来添加元素到列表中
        for (int i = 0; i < length; i++) {
            // 调用Supplier的get方法来生成一个新的元素，并添加到列表中
            arrayList.add(supplier.get());
        }
        // 返回填充了生成元素的列表
        return arrayList;
    }


    public static <T> List<T> asList(T... values) {
        ArrayList<T> arrayList = new ArrayList<>();
        Collections.addAll(arrayList, values);
        return arrayList;
    }


    public static <T> List<T> asList(Class<? extends List> aClass, T... values) {
        List<T> list = null;
        try {
            list = aClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Collections.addAll(list, values);
        return list;
    }


}
