package chapter05;

import chapter04.Dish;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static chapter04.Dish.menu;

public class Reducing {

    public static void main(String... args) {
        // sum
        List<Integer> numbers = Arrays.asList(3, 4, 5, 1, 2);
        int sum = numbers.stream().reduce(0, (a, b) -> a + b);
        System.out.println(sum);

        int sum2 = numbers.stream().reduce(0, Integer::sum); // 메서드 참조
        System.out.println(sum2);

        int calories = menu.stream()
                .map(Dish::getCalories)
                .reduce(0, Integer::sum);
        System.out.println("Number of calories: " + calories);

        // max
        int max = numbers.stream().reduce(0, (a, b) -> Integer.max(a, b));
        System.out.println(max);

        // min
        Optional<Integer> min = numbers.stream().reduce(Integer::min);
        min.ifPresent(System.out::println);
    }

}
