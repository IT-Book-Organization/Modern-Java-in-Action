package chapter07;

import java.util.stream.LongStream;
import java.util.stream.Stream;

public class ParallelStreams {

    public static void main(String[] args) {
        System.out.println(sequentialSum(5));
        System.out.println(parallelSum(5));
    }


    // TODO: 7.1.1 순차 스트림을 병렬 스트림으로 변환하기

    public static long iterativeSum(long n) {
        long result = 0;
        for (long i = 0; i <= n; i++) {
            result += i;
        }
        return result;
    }

    // iterate (순차 스트림)
    public static long sequentialSum(long n) {
        return Stream.iterate(1L, i -> i + 1) // 무한 자연수 스트림 생성
                .limit(n)
                .reduce(Long::sum) // 스트림 리듀싱 연산
                .get();
    }

    // iterate (병렬 스트림)
    public static long parallelSum(long n) {
        return Stream.iterate(1L, i -> i + 1)
                .limit(n)
                .parallel() // 스트림을 병렬 스트림으로 변환
                .reduce(Long::sum)
                .get();
    }

    // LongStream.rangeClosed
    public static long rangedSum(long n) {
        return LongStream.rangeClosed(1, n)
                .reduce(Long::sum)
                .getAsLong();
    }

    // LongStream.rangeClosed (병렬 스트림)
    public static long parallelRangedSum(long n) {
        return LongStream.rangeClosed(1, n)
                .parallel()
                .reduce(Long::sum)
                .getAsLong();
    }


    // TODO: 병렬 스트림의 올바른 사용법

    public static long sideEffectSum(long n) {
        Accumulator accumulator = new Accumulator();
        LongStream.rangeClosed(1, n).forEach(accumulator::add);
        return accumulator.total;
    }

    public static long sideEffectParallelSum(long n) {
        Accumulator accumulator = new Accumulator();
        LongStream.rangeClosed(1, n).parallel().forEach(accumulator::add);
        return accumulator.total;
    }

    public static class Accumulator {
        private long total = 0;

        public void add(long value) {
            total += value;
        }
    }

}
