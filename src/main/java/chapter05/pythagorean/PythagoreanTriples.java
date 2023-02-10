package chapter05.pythagorean;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PythagoreanTriples {

    public static void main(String[] args) {
        Stream<int[]> pythagoreanTriples = IntStream.rangeClosed(1, 100).boxed() // a값 생성
                .flatMap( // 생성된 각각의 스트림을 하나의 평준화된 스트림으로 생성해줌 -> 결과적으로 세 수(a, b, c)로 이루어진 스트림을 얻을 수 있음
                        a -> IntStream.rangeClosed(a, 100) // IntStream 반환
                                .filter(b -> Math.sqrt(a * a + b * b) % 1 == 0) // (a * a + b * b)의 제곱근이 정수인지 확인
//                                .boxed() // IntStream을 Stream<Integer>로 복원 -> b값 생성
//                                .map(b -> new int[]{a, b, (int) Math.sqrt(a * a + b * b)}) // 각 요소를 피타고라스 수로 변환
                                .mapToObj(b -> new int[]{a, b, (int) Math.sqrt(a * a + b * b)}) // 각 요소를 피타고라스 수로 변환
                );
        // map : IntStream을 반환
        // mapToObj : 개체값 스트림(Stream<Integer>)을 반환
        pythagoreanTriples.limit(5)
                .forEach(t -> System.out.println(t[0] + ", " + t[1] + ", " + t[2]));

        // 위 방식은 제곱근을 두 번 계산하기 때문에 최적화가 필요하다.

        System.out.println();

        // 개선된 코드
        // (a*a, b*b, a*a + b*b) 형식을 만족하는 세 수를 만든 다음, 원하는 조건에 맞는 결과만 필터링
        Stream<int[]> pythagoreanTriples2 = IntStream.rangeClosed(1, 100).boxed()
                .flatMap(
                        a -> IntStream.rangeClosed(a, 100)
                                .mapToObj(b -> new double[]{a, b, Math.sqrt(a * a + b * b)}) // 만들어진 세 수
                                .filter(c -> c[2] % 1 == 0)) // 세 수의 세 번째 요소는 반드시 정수여야 함
                .map(array -> Arrays.stream(array).mapToInt(a -> (int) a).toArray());

        pythagoreanTriples2.limit(5)
                .forEach(t -> System.out.println(t[0] + ", " + t[1] + ", " + t[2]));
    }

}
