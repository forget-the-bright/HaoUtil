package org.hao.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lists {

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
