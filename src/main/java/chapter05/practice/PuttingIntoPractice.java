package chapter05.practice;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PuttingIntoPractice {

    public static void main(String[] args) {
        // 거래자 리스트
        Trader raoul = new Trader("Raoul", "Cambridge");
        Trader mario = new Trader("Mario", "Milan");
        Trader alan = new Trader("Alan", "Cambridge");
        Trader brian = new Trader("Brian", "Cambridge");

        // 트랜잭션 리스트
        List<Transaction> transactions = Arrays.asList(
                new Transaction(brian, 2011, 300),
                new Transaction(raoul, 2012, 1000),
                new Transaction(raoul, 2011, 400),
                new Transaction(mario, 2012, 710),
                new Transaction(mario, 2012, 700),
                new Transaction(alan, 2012, 950)
        );


        // TODO: 1. 2011년에 일어난 모든 트랜잭션을 찾아 값을 value 기준, 오름차순으로 정렬하시오.
        List<Transaction> tr2011 = transactions.stream()
                .filter(transaction -> transaction.getYear() == 2011) // 2011년에 발생한 트랜잭션을 필터링
                .sorted(comparing(Transaction::getValue))
                .collect(toList());
        System.out.println(tr2011);


        // TODO: 2. 거래자가 근무하는 모든 도시를 중복 없이 나열하시오.
        List<String> cities = transactions.stream()
                .map(transaction -> transaction.getTrader().getCity())
                .distinct() // 고유 도시만 선택
                .collect(toList());
        System.out.println(cities);


        // TODO: 3. 케임브리지에서 근무하는 모든 거래자를 찾아서 이름순으로 정렬하시오.
        List<Trader> cambridgeTraders = transactions.stream()
                .map(Transaction::getTrader) // 모든 거래자 추출
                .filter(trader -> trader.getCity().equals("Cambridge")) // 케임브리지의 거래자만 선택
                .distinct() // 중복 제거
                .sorted(comparing(Trader::getName)) // 결과 스트림의 거래자를 이름으로 정렬
                .collect(toList());
        System.out.println(cambridgeTraders);


        // TODO: 4. 모든 거래자의 이름을 알파벳순으로 정렬해서 하나의 문자열로 반환하시오.
        String traderStr_reduce = transactions.stream()
                .map(transaction -> transaction.getTrader().getName()) // 모든 거래자명 추출
                .distinct() // 중복 제거
                .sorted() // 알파벳순으로 정렬
                .reduce("", (n1, n2) -> n1 + n2 + " "); // 각각의 이름을 하나의 문자열로 연결
        System.out.println(traderStr_reduce);

        // reduce를 사용하는 코드보다 효율적 (6장에서 설명함)
        String traderStr_joining = transactions.stream()
                .map(transaction -> transaction.getTrader().getName())
                .distinct()
                .sorted()
                .collect(joining()); // joining은 내부적으로 StringBuilder를 이용함
        System.out.println(traderStr_joining);


        // TODO: 5. 밀라노에 거래자가 있는가?
        boolean hasTraderInMilan = transactions.stream()
                .anyMatch(transaction -> transaction.getTrader().getCity().equals("Milan"));
        System.out.println(hasTraderInMilan);


        // TODO: 6. 케임브리지에 거주하는 거래자의 모든 트랜잭션값을 출력하시오.
        transactions.stream()
                .filter(transaction -> "Cambridge".equals(transaction.getTrader().getCity()))
                .map(Transaction::getValue)
                .forEach(System.out::println); // 각 값을 출력


        // TODO: 7. 전체 트랜잭션 중 최댓값은 얼마인가?
        Optional<Integer> maxValue = transactions.stream()
                .map(Transaction::getValue)
                .reduce(Integer::max); // 결과 스트림의 최대값 계산
        System.out.println(maxValue);


        // TODO: 8. 전체 트랜잭션 중 최솟값을 가지는 트랜잭션은 무엇인가?
        Optional<Transaction> smallestTransaction_reduce = transactions.stream()
                .reduce((t1, t2) -> t1.getValue() < t2.getValue() ? t1 : t2); // 각 트랜잭션값을 반복 비교
        System.out.println(smallestTransaction_reduce);

        // 스트림은 최댓값이나 최솟값을 계산하는 데 사용할 키를 지정하는 Comparator를 인수로 받는 min과 max 메서드를 제공한다.
        Optional<Transaction> smallestTransaction_min = transactions.stream()
                .min(comparing(Transaction::getValue));
        System.out.println(smallestTransaction_min);
    }

}
