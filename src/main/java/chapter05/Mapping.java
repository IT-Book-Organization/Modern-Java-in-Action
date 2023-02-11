package chapter05;

import chapter04.Dish;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static chapter04.Dish.menu;

public class Mapping {

    public static void main(String... args) {
        // TODO: map

        List<String> dishNames = menu.stream()
                .map(Dish::getName)
                .collect(toList());
        System.out.println(dishNames);

        List<String> words = Arrays.asList("Hello", "World");
        List<Integer> wordLengths = words.stream()
                .map(String::length)
                .collect(toList());
        System.out.println(wordLengths);

        // ---

        // TODO: flatMap

        words.stream()
                .flatMap((String line) -> Arrays.stream(line.split("")))
                .distinct()
                .forEach(System.out::println);

        List<Integer> numbers1 = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> numbers2 = Arrays.asList(6, 7, 8);
        List<int[]> pairs = numbers1.stream()
                .flatMap((Integer i) -> numbers2.stream()
                        .map((Integer j) -> new int[]{i, j}) // 모든 숫자 쌍의 리스트를 반환
                )
                .filter(pair -> (pair[0] + pair[1]) % 3 == 0) // 합이 3으로 나누어떨어지는 쌍만 반환
                .collect(toList());
        pairs.forEach(pair -> System.out.printf("(%d, %d) ", pair[0], pair[1]));
    }

}
