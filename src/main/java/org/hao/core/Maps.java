package org.hao.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Description:
 * ClassName: Maps
 * Author: wanghao
 * date: 2021.12.13 11:27
 * version: 1.0
 */
public class Maps {
    public static Map<String, Object> asMap(Object[]... entrys) {
        return asMap(Arrays.asList(entrys));
    }

    public static Map<String, Object> asMap(Supplier<Collection<Object[]>> collectionSupplier) {
        return asMap(collectionSupplier.get());
    }

    public static Map<String, Object> asMap(Collection<Object[]> entrys) {
        LinkedHashMap<String, Object> stringObjectLinkedHashMap = new LinkedHashMap<>();
        for (Object[] entry : entrys) {
            stringObjectLinkedHashMap.put((String) entry[0], entry[1]);
        }
        return stringObjectLinkedHashMap;
    }

    public static <T> Map<String, T> asMap(Collection<Object[]> entrys, Class<T> tClass) {
        LinkedHashMap<String, T> stringObjectLinkedHashMap = new LinkedHashMap<>();
        for (Object[] entry : entrys) {
            stringObjectLinkedHashMap.put((String) entry[0], (T) entry[1]);
        }
        return stringObjectLinkedHashMap;
    }

    public static <T> T asMap(Class<T> tClass, Object[]... entrys) {
        return asMap(tClass, Arrays.asList(entrys));
    }

    public static <T> T asMap(Class<T> tClass, Supplier<Collection<Object[]>> collectionSupplier) {
        return asMap(tClass, collectionSupplier.get());
    }


    public static <T> T asMap(Class<T> tClass, Collection<Object[]> entrys) {
        Map<String, Object> stringObjectLinkedHashMap = null;
        try {
            T t = tClass.newInstance();
            stringObjectLinkedHashMap = (Map) t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Object[] entry : entrys) {
            stringObjectLinkedHashMap.put((String) entry[0], entry[1]);
        }
        return (T) stringObjectLinkedHashMap;
    }

    public static <T, V> T asMap(Class<T> tClass, Class<V> vClass, Object[]... entrys) {
        return asMap(tClass, vClass, Arrays.asList(entrys));
    }

    public static <T, V> T asMap(Class<T> tClass, Class<V> vClass, Supplier<Collection<Object[]>> collectionSupplier) {
        return asMap(tClass, vClass, collectionSupplier.get());
    }

    public static <T, V> T asMap(Class<T> tClass, Class<V> vClass, Collection<Object[]> entrys) {
        Map<String, V> stringObjectLinkedHashMap = null;
        try {
            T t = tClass.newInstance();
            stringObjectLinkedHashMap = (Map) t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Object[] entry : entrys) {
            stringObjectLinkedHashMap.put((String) entry[0], (V) entry[1]);
        }
        return (T) stringObjectLinkedHashMap;
    }


    public static Object[] put(String key, Object val) {
        return new Object[]{key, val};
    }

    public static Object[] put(String key, Supplier val) {
        return new Object[]{key, val.get()};
    }

    public static Object[] put(String key, Supplier val, Boolean isExecute) {
        return new Object[]{key, null == isExecute ? val : isExecute ? val.get() : val};
    }

    public static Object[] put(String key, Function val) {
        return new Object[]{key, val};
    }

}
