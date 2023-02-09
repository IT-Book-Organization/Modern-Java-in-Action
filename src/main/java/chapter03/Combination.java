package chapter03;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.util.Comparator.comparing;

public class Combination {
    public static void main(String[] args) {

        List<Apple> inventory = new ArrayList<>();
        inventory.addAll(Arrays.asList(
                new Apple(Color.GREEN, 80),
                new Apple(Color.GREEN, 155),
                new Apple(Color.RED, 120)
        ));

        inventory.sort(comparing(Apple::getWeight).reversed().thenComparing(Apple::getColor));

        java.util.function.Predicate<Apple> redApple = (a) -> a.getColor() == Color.RED;
        redApple.negate();
        redApple.and(apple -> apple.getWeight() > 150);
        redApple.and(apple -> apple.getWeight() > 150).or(a ->a.getColor() == Color.GREEN);

        Function<Integer, Integer> f = x -> x + 1;
        Function<Integer, Integer> g = x -> x *2;
        Function<Integer, Integer> h = f.compose(g);
        h.apply(1);

    }
}
