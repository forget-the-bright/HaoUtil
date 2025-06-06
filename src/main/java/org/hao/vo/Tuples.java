package org.hao.vo;

import java.util.Objects;

/**
 * 三元组数据结构，用于封装三个相关联的对象。
 *
 * <p>该类常用于需要返回多个值或临时组合三个对象的场景。</p>
 *
 * @param <T> 第一个元素的类型
 * @param <U> 第二个元素的类型
 * @param <P> 第三个元素的类型
 * @author wanghao (helloworlwh@163.com)
 * @since 2024/11/13 下午4:46
 */
public class Tuples<T, U, P> {

    private T first;
    private U second;
    private P third;

    public Tuples() {

    }

    public Tuples(T first, U second, P third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public static <T, U, P> Tuples<T, U, P> newTuples(T first, U second, P third) {
        return new Tuples<>(first, second, third);
    }

    public T getFirst() {
        return first;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public U getSecond() {
        return second;
    }

    public void setSecond(U second) {
        this.second = second;
    }

    public P getThird() {
        return third;
    }

    public void setThird(P third) {
        this.third = third;
    }

    @Override
    public String toString() {
        return "Tuples{" +
                "first=" + first +
                ", second=" + second +
                ", third=" + third +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuples<?, ?, ?> tuples = (Tuples<?, ?, ?>) o;
        return Objects.equals(first, tuples.first) && Objects.equals(second, tuples.second) && Objects.equals(third, tuples.third);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }
}
