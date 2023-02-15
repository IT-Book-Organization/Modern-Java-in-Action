# Chapter 7. 병렬 데이터 처리와 성능

<br/>

이전에는 데이터 컬렉션을 병렬로 처리하기가 어려웠다.

자바 7은 `포크(fork)/조인(join) 프레임워크` 기능을 제공하여 더 쉽게 병렬화를 수행하면서 에러를 최소화할 수 있도록 하였다.

<br/>
<br/>
<br/>

# 7.1 병렬 스트림

컬렉션에 `parallelStream`을 호출하면 `병렬 스트림(parallel stream)`이 생성된다.

병렬 스트림이란 각각의 스레드에서 처리할 수 있도록 **스트림 요소를 여러 청크로 분할한 스트림**으로,
모든 멀티코어 프로세서가 각각의 청크를 처리하도록 할당할 수 있다.

<br/>
<br/>

## 7.1.1 순차 스트림을 병렬 스트림으로 변환하기

숫자 n을 인수로 받아서 1부터 n까지의 모든 숫자의 합계를 반환하는 메서드를 구현해보자.

```java
public static long sequentialSum(long n) {
    return Stream.iterate(1L, i -> i + 1) // 무한 자연수 스트림 생성
        .limit(n)
        .reduce(Long::sum) // 스트림 리듀싱 연산
        .get();
}
```

<br/>

순차 스트림에 `parallel` 메서드를 호출하면 기존의 함수형 리듀싱 연산이 병렬로 처리된다.

```java
public static long parallelSum(long n) {
    return Stream.iterate(1L, i -> i + 1)
        .limit(n)
        .parallel() // 스트림을 병렬 스트림으로 변환
        .reduce(Long::sum)
        .get();
}
```

<br/>

1. 스트림이 여러 청크로 분할되어 있어, 리듀싱 연산을 여러 청크에 병렬로 수행할 수 있다.
2. 리듀싱 연산으로 생성된 **부분 결과를 다시 리듀싱 연산으로 합쳐서** 전체 스트림의 리듀싱 결과를 도출한다.

<br/>

<p align="center"><img width="650" alt="병렬 리듀싱 연산" src="https://user-images.githubusercontent.com/86337233/218249495-a6d224dc-7240-45a5-9569-dfb8ea45c0ec.png">

<br/>
<br/>

### 병렬 스트림을 순차 스트림으로 변환하기

`sequential`로 병렬 스트림으르 순차 스트림으로 바꿀 수 있다.

<br/>

> 💡 `parallel`과 `sequential` 두 메서드 중 **최종적으로 호출된 메서드가** 전체 파이프라인에 영향을 미친다.

<br/>

### 병렬 스트림에서 사용하는 스레드 풀 설정

병렬 스트림은 내부적으로 `ForkJoinPool`을 사용한다. (7.2절에서 자세히 설명)

이는 기본적으로 프로세서 수, 즉 `Runtime.getRuntime().availableProcessors()`가 반환하는 값에 상응하는 스레드를 갖는다.

<br/>

아래 코드는 전역 설정 코드로, 모든 병렬 스트림 연산에 영향을 준다.

```java
System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "12");
```

<br/>
<br/>

## 7.1.2 스트림 성능 측정

`자바 마이크로벤치마크 하니스(Java Microbenchmark Harness, JMH) 라이브러리`를 통해 벤치마크를 구현하고, 위 코드들의 성능을 측정해보자.

코드 : [src/main/java/chapter07/ParallelStreamBenchmark.java](/src/main/java/chapter07/ParallelStreamBenchmark.java)

<br/>

병럴화를 이용하면 순차나 반복 형식에 비해 성능이 더 좋아질 것이라고 추측했지만 그 결과는 반대였다.

`전통적인 for 루프`를 사용해 반복하는 방법에 비해 `순차적 스트림`을 사용하는 버전은 4배 정도 느렸고,  
`병렬 스트림`을 사용하는 버전은 5배 정보나 느렸다.

<br/>

병렬 스트림이 더 느렸던 이유는 다음과 같다.

- 반복 결과로 박싱된 객체가 만들어지므로 숫자를 더하려면 언박싱을 해야 한다.
- 반복 작업(iterate)은 병렬로 수행할 수 있는 독립 단위로 나누기가 어렵다. (본질적으로 순차적이기 때문)

<br/>

<p align="center"><img width="300" alt="iterate" src="https://user-images.githubusercontent.com/86337233/218249511-10d0051b-20ae-4204-bd9d-bf35b4d501a0.png">

<br/>
<br/>

이런 상황에서는 ‘7.1.1 순차 스트림을 병렬 스트림으로 변환하기’에서의 그림처럼 리듀싱 연산이 수행되지 않는다.

리듀싱 과정을 시작하는 시점에 전체 숫자 리스트가 준비되지 않았으므로 스트림을 병렬로 처리할 수 있도록 청크로 분할할 수 없기 때문이다.

<br/>

이처럼 병렬 프로그래밍을 오용하면 오히려 전체 프로그램의 성능이 더 나빠질 수 있다.

<br/>

### 더 특화된 메서드 사용

5장에서 배운 `LongStream.rangeClosed`을 사용해보자.

- 기본형 long을 직접 사용하므로 박싱과 언박싱 오버헤드가 사라진다.
- 쉽게 청크로 분할할 수 있는 숫자 범위를 생산한다.

<br/>

```java
@Benchmark
public long rangedSum() {
    return LongStream.rangeClosed(1, N)
        .reduce(0L, Long::sum);
}

// 병령 스트림
@Benchmark
public long parallelRangedSum() {
    return LongStream.rangeClosed(1, N)
        .parallel()
        .reduce(0L, Long::sum);
}
```

<br/>

성능을 측정해보면 기존의 iterate 팩토리 메서드로 생성한 순차 버전에 비해 숫자 스트림 처리 속도가 더 빠르다!

이번에는 ‘7.1.1 순차 스트림을 병렬 스트림으로 변환하기’에서의 그림처럼 실질적으로 리듀싱 연산이 병렬로 수행된다.

<br/>

> 💡 함수형 프로그래밍을 올바로 사용하면 병렬 실행의 힘을 직접적으로 얻을 수 있다.

<br/>

**하지만 병렬화가 완전 공짜는 아니다.**

스트림을 재귀적으로 분할해야 하고,  
각 서브스트림을 서로 다른 스레드의 리듀싱 연산으로 할당하고,  
이들 결과를 하나의 값으로 합쳐야 한다.

멀티코어 간의 데이터 이동은 생각보다 비싸다.

<br/>
<br/>

## 7.1.3 병렬 스트림의 올바른 사용법

병렬 스트림을 잘못 사용하면서 발생하는 많은 문제는 **공유된 상태를 바꾸는 알고리즘**을 사용하기 때문에 일어난다.

> 💡 병렬 스트림과 병렬 계산에서는 **공유된 가변 상태를 피해야 한다.**

<br/>
<br/>

## 7.1.4 병렬 스트림 효과적으로 사용하기

### 확신이 서지 않으면 직접 측정하라

언제나 병렬 스트림이 순차 스트림보다 빠른 것은 아니며, 병렬 스트림의 수행 과정은 투명하지 않을 때가 많다.

순차 스트림과 병렬 스트림 중 어떤 것이 좋을지 모르겠다면 적절한 벤치마크로 직접 성능을 측정하는 것이 바람직하다.

<br/>

### 박싱을 주의하라

**자동 박싱과 언박싱**은 성능을 크게 저하시킬 수 있는 요소다.

자바 8은 박싱 동작을 피할 수 있도록 기본형 특화 스트림(`IntStream`, `LongStream`, `DoubleStream`)을 제공하며, 되도록이면 이들을 사용하는 것이 좋다.

<br/>

### 순차 스트림보다 병렬 스트림에서 성능이 떨어지는 연산이 있다

`limit`나 `findFirst`처럼 **요소의 순서에 의존하는 연산**은 병렬 스트림에서 수행하려면 비싼 비용을 치러야 한다.

`findAny`는 요소의 순서와 상관없이 연산하므로 `findFirst`보다 성능이 좋다.

<br/>

### 스트림에서 수행하는 전체 파이프라인 연산 비용을 고려하라

전체 스트림 파이프라인 처리 비용 = `N*Q`

- `N` : 처리해야 할 요소 수
- `Q` : 하나의 요소를 처리하는 데 드는 비용

Q가 높아진다 → 병렬 스트림으로 성능을 개선할 수 있는 가능성이 있다.

<br/>

### 소량의 데이터에서는 병렬 스트림이 도움 되지 않는다.

부가 비용을 상쇄할 수 있을 만큼의 이득을 얻지 못한다.

<br/>

### 스트림을 구성하는 자료구조가 적절한지 확인하라

`ArrayList`를 `LinkedList`보다 효율적으로 분할할 수 있다.

- `ArrayList` : 요소를 탐색하지 않고도 리스트를 분할할 수 있다.
- `LinkedList` : 분할하려면 모든 요소를 탐색해야 한다.

<br/>

`range 팩토리 메서드`로 만든 기본형 스트림도 쉽게 분해할 수 있다.

<br/>

### 스트림의 특성과 파이프라인의 중간 연산이 스트림의 특성을 어떻게 바꾸는지에 따라 분해 과정의 성능이 달라질 수 있다

`SIZED 스트림`은 정확히 같은 크기의 두 스트림으로 분해할 수 있으므로 효과적으로 스트림을 병렬 처리할 수 있다.

`필터 연산`이 있으면 스트림의 길이를 예측할 수 없으므로 효과적으로 스트림을 병렬 처리할 수 있을지 알 수 없게 된다.

<br/>

### 최종 연산의 병합 과정 비용을 살펴보라

e.g., Collector의 combiner 메서드

병합 과정의 비용이 비싸다면 병렬 스트림으로 얻은 성능의 이익이 서브스트림의 부분 결과를 합치는 과정에서 상쇄될 수 있다.

<br/>

### 병렬 스트림이 수행되는 내부 인프라구조도 살펴보라

자바 7에서 추가된 `포크/조인 프레임워크`로 병렬 스트림이 처리된다.

<br/>

### 스트림 소스와 분해성

다음은 다양한 스트림 소스의 병렬화 친밀도를 요약한 것이다.

<br/>

<p align="center"><img width="400" alt="분해성" src="https://user-images.githubusercontent.com/86337233/218249508-3aee6a50-c413-45da-ac2e-1af90044f45a.png">

<br/>
<br/>
<br/>

# 7.2 포크/조인 프레임워크

이는 병렬화할 수 있는 작업을 **재귀적으로 작은 작업으로 분할**한 다음에 **서브태스크 각각의 결과를 합쳐서** 전체 결과를 만들도록 설계되었다.

<br/>

포크/조인 프레임워크에서는 서브태스크를 스레드 풀(ForkJoinPool)의 작업자 스레드에 분산 할당하는 `ExecutorService 인터페이스`를 구현한다.

<br/>
<br/>

## 7.2.1 RecursiveTask 활용

스레드 풀을 이용하려면 `RecursiveTask<R>`의 서브클래스를 만들어야 한다.

<br/>

`R`

- 병렬화된 태스크가 생성하는 결과 형식
- 결과가 없을 때는 RecursiveAction 형식

<br/>

RecursiveTask를 정의하려면 `추상 메서드 compute`를 구현해야 하는데,  
이는 태스크를 서브태스크로 분할하는 로직과 더 이상 분할할 수 없을 때 개별 서브태스크의 결과를 생산할 알고리즘을 정의한다.

```
// pseudo code

if (태스크가 충분히 작거나 더 이상 분할할 수 없으면) {
	순차적으로 태스크 계산
} else {
	태스크를 두 서브태스크로 분할
	태스크가 다시 서브태스크로 분할되도록 이 메서드를 재귀적으로 호출함
	모든 서브태스크의 연산이 완료될 때까지 기다림
	각 서브태스크의 결과를 합침
}
```

<br/>

이 알고리즘은 `분할 후 정복(divide-and-conquer) 알고리즘`의 병렬화 버전이다.

<br/>

<p align="center"><img width="650" alt="포크:조인 과정" src="https://user-images.githubusercontent.com/86337233/218249500-911ab5ad-a8d2-4743-9901-32cd5f169bf8.png">


<br/>
<br/>

### ForkJoinSumCalculator

포크/조인 프레임워크를 이용해서 n까지의 자연수 덧셈 작업을 병렬로 수행하는 방법은 다음과 같다.

코드 : [src/main/java/chapter07/forkjoin](/src/main/java/chapter07/forkjoin)

<br/>

<p align="center"><img width="650" alt="ForkJoinSumCalculator" src="https://user-images.githubusercontent.com/86337233/218249505-cc830cb3-6182-43d8-9fec-9af137ffdab5.png">


<br/>
<br/>

성능을 측정해보면 병렬 스트림을 이용할 때보다 성능이 나빠진 것을 볼 수 있다.

이는 `ForkJoinSumCalculator` 태스크에서 사용할 수 있도록 전체 스트림을 `long[]`으로 변환했기 때문이다.

<br/>
<br/>

## 7.2.2 포크/조인 프레임워크를 제대로 사용하는 방법

### 두 서브태스크가 모두 시작된 다음에 join을 호출해야 한다

`join` 메서드를 태스크에 호출하면 **태스크가 생산하는 결과가 준비될 때까지** 호출자를 블록(block)시킨다.

<br/>

### RecursiveTask 내에서는 ForkJoinPool의 invoke 메서드를 사용하지 말아야 한다

대신 `compute`나 `fork` 메서드를 직접 호출할 수 있다.

**순차 코드에서 병렬 계산을 시작할 때만** `invoke`를 사용한다.

<br/>

### 서브태스크에 fork 메서드를 호출해서 ForkJoinPool의 일정을 조절할 수 있다

왼쪽 작업과 오른쪽 작업 모두에 `fork` 메서드를 호출하는 것이 자연스러울 것 같지만  
한쪽 작업에는 fork를 호출하는 것보다는 `compute`를 호출하는 것이 효율적이다.

그러면 두 서브태스크의 **한 태스크에는 같은 스레드를 재사용할 수 있으므로** 풀에서 불필요한 태스크를 할당하는 오버헤드를 피할 수 있다.

<br/>

### 멀티코어에 포크/조인 프레임워크를 사용하는 것이 순차 처리보다 무조건 빠를 거라는 생각은 버려야 한다

병렬 처리로 성능을 개선하려면 태스크를 여러 독립적인 서브태스크로 분할할 수 있어야 한다.

각 서브태스크의 실행시간은 새로운 태스크를 포킹하는 데 드는 시간보다 길어야 한다.

<br/>
<br/>

## 7.2.3 작업 훔치기

> 💡 포크/조인 분할 전략에서는 주어진 서브태스크를 더 분할할 것인지 결정할 기준을 정해야 한다.

<br/>

포크/조인 프레임워크에서는 `작업 훔치기(work stealing) 기법`을 통해 ForkJoinPool의 모든 스레드를 거의 공정하게 분배한다.

<br/>

각각의 스레드는 자신에게 할당된 태스크를 포함하는 `이중 연결 리스트(doubly linked list)`를 참조하면서 **작업이 끝날 때마다 큐의 헤드에서 다른 태스크를 가져와서 작업을 처리한다.**

한 스레드는 다른 스레드보다 자신에게 할당된 태스크를 더 빨리 처리할 수 있고, 할일이 없어진 스레드는 다른 스레드 큐의 꼬리에서 작업을 훔쳐온다.

<br/>

<p align="center"><img width="700" alt="작업 훔치기 알고리즘" src="https://user-images.githubusercontent.com/86337233/218249509-c7da0d8d-0ef5-4cef-a211-3287a4baf092.png">

<br/>
<br/>

풀에 있는 작업자 스레드의 태스크를 재분배하고 균형을 맞출 때 작업 훔치기 알고리즘을 사용한다.

하지만 우리는 분할 로직을 개발하지 않고도 병렬 스트림을 이용할 수 있었다.

즉, 스트림을 자동으로 분할해주는 기능이 존재한다. (`Spliterator`)

<br/>
<br/>
<br/>

# 7.3 Spliterator 인터페이스

자바 8은 `Spliterator(splitable iterator, 분할할 수 있는 반복자)`라는 새로운 인터페이스를 제공한다.

Iterator처럼 소스의 요소 탐색 기능을 제공하지만, 병렬 작업에 특화되어 있다.

<br/>

자바 8은 컬렉션 프레임워크에 포함된 모든 자료구조에 사용할 수 있는 디폴트 Spliterator 구현을 제공한다.

컬렉션은 `spliterator`라는 메서드를 제공하는 Spliterator 인터페이스를 구현한다.

```java
public interface Spliterator<T> {
    boolean tryAdvance(Consumer<? super T> action); // Spliterator의 요소를 하나씩 순차적으로 소비하면서 탐색해야 할 요소가 남아있으면 참을 반환
    Spliterator<T> trySplit(); // Spliterator의 일부 요소(자신이 반환한 요소)를 분할해서 두 번째 Spliterator를 생성
    long estimateSize(); // 탐색해야 할 요소 수 정보 제공
    int characteristics(); // Spliterato 자체의 특성 집합을 포함하는 int를 반환
}
```

<br/>
<br/>

## 7.3.1 분할 과정

스트림을 여러 스트림으로 분할하는 과정은 재귀적으로 일어난다.

<br/>

<p align="center"><img width="650" alt="재귀 분할 과정" src="https://user-images.githubusercontent.com/86337233/218249512-63a68c03-1571-4fdd-b61a-9028f75fe869.png">

<br/>
<br/>

이 분할 과정은 `characteristics` 메서드로 정의하는 **Spliterator의 특성**에 영향을 받는다.

<br/>

### Spliterator 특성

<p align="center"><img width="570" alt="Spliterator 특성" src="https://user-images.githubusercontent.com/86337233/218249520-7ac9828e-f4e2-409c-b21d-2ea027047b14.png">

<br/>
<br/>
<br/>

## 7.3.2 커스텀 Spliterator 구현하기

문자열의 단어 수를 계산하는 메서드를 구현해보자.

전체 코드 : [src/main/java/chapter07/wordcount](/src/main/java/chapter07/wordcount)

<br/>

### WordCounter

코드 : src/main/java/chapter07/wordcount/WordCounter.java

<br/>

<p align="center"><img width="500" alt="WordCounter" src="https://user-images.githubusercontent.com/86337233/218249523-20c10e2a-c441-43b9-90fc-76b91e4368e5.png">

<br/>
<br/>

하지만 이것을 실행해보면 원하는 결과가 나오지 않는다.

원래 문자열을 **임의의 위치에서 둘로 나누다보니** 하나의 단어를 둘로 계산하는 상황이 발생할 수 있기 때문이다.

<br/>

> 💡 순차 스트림을 병렬 스트림으로 바꿀 때 스트림 분할 위치에 따라 잘못된 결과가 나올 수 있다.

<br/>

### WordCounterSpliterator

문자열을 단어가 끝나는 위치에서만 분할하는 방법으로 위의 문제를 해결할 수 있다.

코드 : [src/main/java/chapter07/wordcount/WordCounterSpliterator.java](/src/main/java/chapter07/wordcount/WordCounterSpliterator.java)

<br/>

Spliterator는 첫 번째 탐색 시점, 첫 번째 분할 시점, 또는 첫 번째 예상 크기(estimatedSize) **요청 시점에 요소의 소스를 바인딩할 수 있다.**

이와 같은 동작을 `늦은 바인딩 Spliterator`라고 부른다.
