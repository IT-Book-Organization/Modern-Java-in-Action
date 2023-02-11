package chapter05;

import chapter04.Dish;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static chapter04.Dish.menu;

public class Filtering {

    public static void main(String[] args) {
        // TODO: 필터링

        // 프레디케이트로 필터링
        System.out.println("\nFiltering with a predicate:");
        List<Dish> vegetarianMenu = menu.stream()
                .filter(Dish::isVegetarian)
                .collect(toList());
        vegetarianMenu.forEach(System.out::println);

        // 고유 요소로 필터링
        System.out.println("\nFiltering unique elements:");
        List<Integer> numbers = Arrays.asList(1, 2, 1, 3, 3, 2, 4);
        numbers.stream()
                .filter(i -> i % 2 == 0)
                .distinct()
                .forEach(System.out::println);

        // ---

        // TODO: 스트림 슬라이싱

        // 칼로리 값을 기준으로 오름차순 정렬되어 있음
        List<Dish> specialMenu = Arrays.asList(
                new Dish("season fruit", true, 120, Dish.Type.OTHER),
                new Dish("prawns", false, 300, Dish.Type.FISH),
                new Dish("rice", true, 350, Dish.Type.OTHER),
                new Dish("chicken", false, 400, Dish.Type.MEAT),
                new Dish("french fries", true, 530, Dish.Type.OTHER));

        // filter 연산
        System.out.println("\nFiltered sorted menu:");
        List<Dish> filteredMenu = specialMenu.stream()
                .filter(dish -> dish.getCalories() < 320)
                .collect(toList());
        filteredMenu.forEach(System.out::println);

        // takeWhile
        /*
        System.out.println("\nSorted menu sliced with takeWhile():");
        List<Dish> slicedMenu1 = specialMenu.stream()
                .takeWhile(dish -> dish.getCalories() < 320)
                .collect(toList());
        slicedMenu1.forEach(System.out::println);
        //*/

        // dropWhile
        /*
        System.out.println("\nSorted menu sliced with dropWhile():");
        List<Dish> slicedMenu2 = specialMenu.stream()
                .dropWhile(dish -> dish.getCalories() < 320)
                .collect(toList());
        slicedMenu2.forEach(System.out::println);
        //*/

        // 스트림 축소
        List<Dish> dishesLimit = specialMenu.stream()
                .filter(dish -> dish.getCalories() > 300)
                .limit(3)
                .collect(toList());
        System.out.println("\nTruncating a stream:");
        dishesLimit.forEach(System.out::println);

        // 요소 생략
        List<Dish> dishesSkip = menu.stream()
                .filter(dish -> dish.getCalories() > 300)
                .skip(2)
                .collect(toList());
        System.out.println("\nSkipping elements:");
        dishesSkip.forEach(System.out::println);
    }

}
