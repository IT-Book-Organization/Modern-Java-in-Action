package chapter03;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class Main {

    static Map<String , Function<Integer, Fruit>> map = new HashMap<>();
    static {
        map.put("apple", Apple::new);
    }
    public static void main(String[] args) {
        List<String> strings = Arrays.asList("모던","","자바","인","","액션");
        Predicate<String> nonEmptyStringPredicate = (String s) -> !s.isEmpty();
        List<String> nonEmpty = filter(strings, nonEmptyStringPredicate);

        forEach(
                Arrays.asList(1,2,3,4,5),
                (Integer i) -> System.out.println(i)
        );

        List<Integer> integerList = map(
                Arrays.asList("모던", "", "자바", "인", "", "액션"),
                (String s) -> s.length()
        );

        // 박싱 되지 않음
        IntPredicate evenNumbers = (int i) -> i % 2 == 0;
        evenNumbers.test(1000);

        // int 형값이 Integer 타입으로 오토박싱 됨
        Predicate<Integer> oddNumbers = (Integer i) -> i % 2 != 0;
        evenNumbers.test(1000);

        Supplier<Apple> c1 = Apple::new;
        Apple apple = c1.get();

        Function<Integer, Apple> c2 = Apple::new;
        Apple apple2 = c2.apply(100);

        List<Integer> weights = Arrays.asList(1, 2, 3, 4);
        List<Apple> apples = map(weights, Apple::new);

        BiFunction<Color ,Integer, Apple> c3 = (color, integer) -> new Apple(color,integer);
        Apple apple3 = c3.apply(Color.RED,100);

    }

    public static <T> List<T> filter(List<T> list, Predicate<T> p) {
        List<T> result = new ArrayList<>();
        for (T e : list) {
            if (p.test(e)) {
                result.add(e);
            }
        }
        return result;
    }

    public static <T> void forEach(List<T> list, Consumer<T> c) {
        for (T t : list) {
            c.accept(t);
        }
    }

    public static <T, R> List<R> map(List<T> list, Function<T, R> f) {
        List<R> result = new ArrayList<>();
        for (T t : list) {
            result.add(f.apply(t));
        }
        return result;
    }
    public static Fruit getFruit(String fruit, Integer weight) {
        return map.get(fruit.toLowerCase()).apply(weight);
    }
}
