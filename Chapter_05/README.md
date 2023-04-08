# Chapter 5. 스트림 활용

<br/>

다음은 데이터 컬렉션 반복을 명시적으로 관리하는 외부 반복 코드다.

```java
List<Dish> vegetarianDishes = new ArrayList<>();
for (Dish d : menu) {
    if (d.isVegetarian()) {
        vegetarianDishes.add(d);
    }
}
```

<br/>

이는 스트림 API를 이용해서 내부 반복으로 바꿀 수 있다.

아래처럼 `filter` 메서드에 **필터링 연산을 인수로 넘겨주면** 된다.

```java
List<Dish> vegetarianDishes = menu.stream()
        .filter(Dish::isVegetarian) // 메서드 참조 (채식 요리인지 확인)
        .collect(toList());
```

<br/>

스트림 API가 지원하는 연산을 이용해서 필터링, 슬라이싱, 매핑, 검색, 매칭, 리듀싱 등 다양한 데이터 처리 질의를 표현할 수 있다.

<br/>
<br/>
<br/>

# 5.1 필터링

> 스트림의 요소를 선택하는 방법

<br/>

## 5.1.1 프레디케이트로 필터링

스트림 인터페이스의 `filter` 메서드는 **프레디케이트(boolean을 반환하는 함수)를 인수로 받아,**  
프레디케이트와 일치하는 모든 요소를 포함하는 스트림을 반환한다.

<br/>

위의 vegetarianDishes 예제가 이에 해당한다.

<br/>

<p align="center"><img width="750" alt="프레디케이트로 필터링" src="https://user-images.githubusercontent.com/86337233/218111055-dfb26318-bafa-427a-932c-a6ee6c117dd3.png">

<br/>
<br/>
<br/>

## 5.1.2 고유 요소 필터링

스트림은 **고유 요소로 이루어진 스트림을 반환**하는 `distinct` 메서드를 지원한다.

고유 여부는 스트림에서 만든 객체의 hashCode, equals로 결정된다.

<br/>

e.g., 리스트의 모든 짝수를 선택하고 중복을 필터링하는 코드

```java
List<Integer> numbers = Arrays.asList(1, 2, 1, 3, 3, 2, 4);
numbers.stream()
        .filter(i -> i % 2 == 0)
        .distinct()
        .forEach(System.out::println); // void를 반환
```

<br/>

<p align="center"><img width="750" alt="고유 요소 필터링" src="https://user-images.githubusercontent.com/86337233/218111052-3378f863-94f9-483b-a089-dd3b0b65543c.png">

<br/>
<br/>
<br/>
<br/>

# 5.2 스트림 슬라이싱

여기서는 **스트림의 요소를 선택하거나 스킵**하는 다양한 방법을 설명한다.

<br/>

## 5.2.1 프레디케이트를 이용한 슬라이싱

자바 9는 두 가지 새로운 메서드를 지원한다.

- `takeWhile`
- `dropWhile`

<br/>

### TAKEWHILE 활용

> 💡 **takeWhile**을 이용하면 스트림에 프레디케이트를 적용해 스트림을 슬라이스할 수 있다.

<br/>

e.g.,

요리 리스트를 가지고 있다고 했을 때, 320 칼로리 이하의 요리들만을 선택하고 싶다고 하자.

`filter` 연산을 이용하면 전체 스트림을 반복하면서 각 요소에 프레디케이트를 적용하게 된다.

<br/>

이때, **리스트가 칼로리 순으로 정렬되어 있다면** 320 칼로리보다 크거나 같은 요리가 나왔을 때 반복 작업을 중단할 수 있다.

이것은 takeWhile 연산을 이용해서 간단하게 처리할 수 있다.

```java
List<Dish> slicedMenu1 = specialMenu.stream()
        .takeWhile(dish -> dish.getCalories() < 320) // 320 칼로리 이하의 요리들
        .collect(toList());
```

<br/>

### DROPWHILE 활용

> 💡 dropWhile은 프레디케이트가 처음으로 거짓이 되는 지점까지 발견된 요소를 버린다.

즉, 프레디케이트가 거짓이 되면 그 지점에서 작업을 중단하고 남은 모든 요소를 반환한다.

<br/>

e.g., 320 칼로리보다 큰 요리들에 대한 탐색

```java
List<Dish> slicedMenu2 = specialMenu.stream()
        .dropWhile(dish -> dish.getCalories() < 320)
        .collect(toList());
```

<br/>
<br/>

## 5.2.2 스트림 축소

스트림은 **주어진 값 이하의 크기를 갖는 새로운 스트림을 반환**하는 `limit(n)` 메서드를 지원한다.

<br/>

e.g., 프레디케이트와 일치하는 처음 세 요소를 선택한 다음, 즉시 결과를 반환

```java
List<Dish> dishes = specialMenu.stream()
        .filter(dish -> dish.getCalories() > 300)
        .limit(3)
        .collect(toList());
```

<br/>
<br/>

## 5.2.3 요소 건너뛰기

스트림은 **처음 n개 요소를 제외한 스트림을 반환**하는 `skip(n)` 메서드를 지원한다.

n개 이하의 요소를 포함하는 스트림에 이 메서드를 호출하면 빈 스트림이 반환된다.

<br/>

e.g.,

```java
List<Dish> dishesSkip2 = specialMenu.stream()
        .filter(dish -> dish.getCalories() > 300)
        .skip(2)
        .collect(toList());
```

<br/>
<br/>
<br/>

# 5.3 매핑

스트림 API의 `map`과 `flatMap` 메서드는 특정 데이터를 선택하는 기능을 제공한다.

<br/>

## 5.3.1 스트림의 각 요소에 함수 적용하기

`map` 메서드는 함수를 인수로 받으며,  
그 함수는 각 요소에 적용되어 **함수를 적용한 결과가 새로운 요소로 매핑(mapping)된다.**  
(≈ 변환, transforming)

<br/>

e.g., `Dish::getName`을 map 메서드로 전달해서 요리명을 추출하는 코드

```java
List<String> dishNames = menu.stream()
        .map(Dish::getName)
        .collect(toList());
System.out.println(dishNames);
```

`getName`은 문자열을 반환하기 때문에 **map 메서드의 출력 스트림**은 `Stream<String>` 형식을 갖는다.

<br/>

만약 각 요리명의 길이도 알고 싶다면, 다음처럼 다른 map 메서드를 <b>연결(chaining)</b>할 수 있다.

```java
List<Integer> dishNameLengths = menu.stream()
        .map(Dish::getName)
        .map(String::length)
        .collect(toList());
```

<br/>
<br/>

## 5.3.2 스트림 평면화

String 리스트에서 **고유 문자**로 이루어진 리스트를 반환해보자.

예를 들어 [”Hello”, “World”] 리스트가 있다면 결과로 [”H”, “e”, “l”, “o”, “w”, “r”, “d”]를 포함하는 리스트가 반환되어야 한다.

<br/>

`distinct`를 통해 중복된 문자를 필터링하여 이를 해결할 수 있다고 추측할 수 있다.

```java
List<String> words = Arrays.asList("Hello", "World");

words.stream()
        .map(word -> word.split(""))
        .distinct()
        .collect(toList());
```

<br/>

하지만 이 코드는 문제가 존재한다. 아래 그림을 통해 어느 부분이 잘못된 것인지 확인해보자.

(map 메서드가 반환한 스트림의 형식 : `Stream<String[]>`)

<br/>

<p align="center"><img width="750" alt="고유 문자 찾기 실패" src="https://user-images.githubusercontent.com/86337233/218111046-80345c66-5e72-46ac-845f-c69ff1990459.png">

<br/>
<br/>

이는 `flatMap`이라는 메서드를 통해 해결할 수 있다.

<br/>

### flatMap 사용

우선 배열 스트림 대신 문자열 스트림이 필요하다.

`Arrays.stream()` 메서드는 문자열을 받아 스트림을 만들어 반환한다.

```java
String[] arrayOfWords = {"Goodbye", "World"};
Stream<String> streamOfWords = Arrays.stream(arrayOfWords);
```

<br/>

하지만 위 예제에 아래처럼 `Arrays.stream()` 메서드만을 적용한다면 `List<Stream<String>>`가 만들어지면서 문제가 해결되지 않는다.

```java
words.stream()
        .map(word -> word.split("")) // 각 단어를 개별 문자열 배열로 변환
        .map(Arrays::stream) // 각 배열을 별도의 스트림으로 생성
        .distinct()
        .collect(toList());
```

<br/>
<br/>

> 💡 `flatMap` 메서드는 스트림의 각 값을 다른 스트림으로 만든 다음에 모든 스트림을 하나의 스트림으로 연결하는 기능을 수행한다.

즉, 이는 하나의 평면화된 스트림을 반환한다.

<br/>

<p align="center"><img width="750" alt="flatMap" src="https://user-images.githubusercontent.com/86337233/218111057-6e9b690f-94bb-49e1-afe4-9b6c2c7f65f9.png">

<br/>
<br/>

```java
List<String> uniqueCharacters = words.stream()
        .map(word -> word.split("") // 각 단어를 개별 문자를 포함하는 배열로 변환
        .flatMap(Arrays::stream) // 생성된 스트림을 하나의 스트림으로 평면화
        .distinct()
        .collect(toList());
```

<br/>
<br/>
<br/>

# 5.4 검색과 매칭

스트림 API의 `allMatch`, `anyMatch`, `noneMatch`, `findFirst`, `findAny` 등은 **특정 속성이 데이터 집합에 있는지 여부를 검색**하는 데이터 처리를 도와준다.

<br/>

## 5.4.1 프레디케이트가 적어도 한 요소와 일치하는지 확인

이때는 `anyMatch` 메서드를 이용한다.

이 메서드는 **boolean을 반환**하므로 최종 연산이다.

<br/>

e.g., menu에 채식요리가 있는지 확인하는 코드

```java
if(menu.stream().anyMatch(Dish::isVegetarian)) {
    System.out.println("The menu is (somewhat) vegetarian friendly!!");
}
```

<br/>
<br/>


## 5.4.2 프레디케이트가 모든 요소와 일치하는지 검사

`allMatch`는 스트림의 모든 요소가 주어진 프레디케이트와 일치하는지 검사한다.

e.g., 모든 요리가 1000 칼로리 미만인지 확인하는 코드

```java
boolean isHealthy = menu.stream()
        .allMatch(dish -> dish.getCalories() < 1000);
```

<br/>

### NONEMATCH

`noneMatch`는 주어진 프레디케이트와 일치하는 요소가 **없는지**를 확인한다. (allMatch와는 정반대)

e.g., 모든 요리가 1000 칼로리 미만인지 확인하는 코드

```java
boolean isHealthy = menu.stream()
        .noneMatch(dish -> dish.getCalories() >= 1000);
```

<br/>

### 쇼트서킷 연산

`쇼트서킷`은 전체 스트림을 처리하지 않았더라도 **원하는 요소를 찾았으면 즉시 결과를 반환**할 수 있다.

위의 세 메서드(`anyMatch`, `allMatch`, `noneMatch`)는 스트림 쇼트서킷 기법을 활용하며,  
`limit`도 스트림의 모든 요소를 처리할 필요 없이 주어진 크기의 스트림을 생성하기 때문에 쇼트서킷 연산이다.

<br/>
<br/>

## 5.4.3 요소 검색

`findAny` 메서드는 현재 스트림에서 **가장 먼저 탐색되는 요소 1개**를 반환한다.

이것도 쇼트서킷을 이용해서 결과를 찾는 즉시 실행을 종료한다.

<br/>

e.g., filter와 findAny를 통해 채식 요리를 선택하는 코드

```java
Optional<Dish> dish = menu.stream()
        .filter(Dish::isVegetarian)
        .findAny();
```

<br/>

### Optional이란?

`Optional<T>` 클래스(java.util.Optional)는 값의 존재나 부재 여부를 표현하는 컨테이너 클래스며,  
**값이 존재하는지 확인하고 값이 없을 때 어떻게 처리할지 강제하는 기능**을 제공한다.

<br/>

위의 채식 요리 선택 예제를 다시 보면, `findAny`는 `Optional<Dish>`를 반환함을 유추할 수 있다.

만약 menu에 채식 요리가 하나도 없었다면 `findAny`는 **null**을 반환하게 되는데,  
이것으로 인해 발생하는 버그들을 방지하기 위해서 Optional 클래스를 반환하는 것이다.

(자세한 내용은 10장에서 설명함)

<br/>

`findAny`에서 **반환된 요리들의 이름을 바로 출력**하고 싶다고 하면 다음과 같이 코드를 작성할 수 있다.

```java
menu.stream()
        .filter(Dish::isVegetarian)
        .findAny(); // Optional<Dish> 반환
        .ifPresent(dish -> System.out.println(dish.getName()); // 값이 있으면 출력되고, 값이 없으면 아무 일도 일어나지 않음
```

- `isPresent()` : Optional이 값을 포함하면 **true**, 값을 포함하지 않으면 **false**를 반환한다.
- `ifPresent(Consumer<T> block)` : 값이 있으면 주어진 블록을 실행한다.
    - **Consumer 함수형 인터페이스**에는 T 형식의 인수를 받으며, void을 반환하는 람다를 전달할 수 있다.

<br/>
<br/>


## 5.4.4 첫 번째 요소 찾기

리스트 또는 정렬된 연속 데이터로부터 생성된 스트림처럼 일부 스트림에는 **논리적인 아이템 순서**가 정해져 있을 수 있다.

이때 스트림에서 **첫 번째 요소**를 찾으려면 `findFirst` 메서드를 사용하면 된다.

<br/>

e.g., 숫자 리스트에서 3으로 나누어떨어지는 첫 번째 제곱값을 반환하는 코드

```java
List<Integer> someNumbers = Arrays.asList(1, 2, 3, 4, 5);
Optional<Integer> firstSquareDivisibleByThree = someNumbers.stream()
        .map(n -> n * n)
        .filter(n -> n % 3 == 0)
        .findFirst(); // 9
```

<br/>

### findFirst vs. findAny

스트림에서 **어떤 조건에 일치하는 요소(element) 1개를 찾을 때**, `findAny`와 `findFirst` API를 사용할 수 있다.

- `findAny` : 스트림에서 **가장 먼저 탐색되는** 요소를 반환
- `findFirst` : 조건에 일치하는 요소들 중 **스트림에서 순서가 가장 앞에 있는** 요소를 반환

<br/>

이 둘이 필요한 이유는 **병렬성** 때문이다. 병렬 실행에서는 첫 번째 요소를 찾기 어렵다.

요소의 반환 순서가 상관없다면 병렬 스트림에서는 제약이 적은 `findAny`를 사용한다.

<br/>
<br/>
<br/>

# 5.5 리듀싱

`리듀싱(reducing) 연산`은 **모든 스트림 요소를 처리해서 값으로 도출**하는 연산을 의미한다.

함수형 프로그래밍 언어 용어로는 `폴드(fold)`라고 부른다.

<br/>

e.g., 메뉴의 모든 칼로리의 합계는? / 메뉴에서 칼로리가 가장 높은 요리는?

이러한 질의를 수행하기 위해서는 Integer 같은 **결과가 나올 때까지 스트림의 모든 요소를 반복적으로 처리해야 한다.**

<br/>
<br/>

## 5.5.1 요소의 합

우선 for-each 루프를 이용해서 리스트의 숫자 요소를 더하는 코드를 보자.

```java
int sum = 0;
for (int x : numbers) {
    sum += x; // numbers의 각 요소는 결과에 반복적으로 더해짐
}
```

numbers 리스트에서 하나의 숫자가 남을 때까지 reduce 과정을 반복한다.

<br/>

코드에는 파라미터 두 개를 사용했다.

- sum 변수의 초깃값 0
- 리스트의 모든 요소를 조합하는 연산(+)

<br/>

`reduce`를 이용하면 애플리케이션의 반복된 패턴을 추상화할 수 있다.

```java
int sum = numbers.stream().reduce(0, (a, b) -> a + b);
```

<br/>

`reduce`는 두 개의 인수를 가진다.

- 초깃값 0
- 두 요소를 조합해서 새로운 값을 만드는 `BinaryOperator<T>`  
  (예제에서는 람다 표현식(`(a, b) → a + b`)를 사용하였음)

<br/>
<br/>

아래 그림은 **스트림에서의 reduce 연산 과정**을 보여준다.

<br/>

<p align="center"><img width="750" alt="sum" src="https://user-images.githubusercontent.com/86337233/218111060-8c47c3b6-8ac1-4492-8da7-b73de01cc012.png">

<br/>
<br/>
<br/>

메서드 참조를 통해 코드를 더 간결하게 만들 수 있다.

```java
int sum = numbers.stream().reduce(0, Integer::sum); // Integer 클래스의 정적 sum 메서드 (자바 8에서 제공)
```

<br/>

### 초깃값 없음

초깃값을 받지 않도록 오버로드된 reduce도 있다.

```java
Optional<Integer> sum = numbers.stream().reduce((a, b) -> (a + b));
```

스트림에 아무 요소도 없는 상황에 대한 버그를 방지하기 위해서 이 reduce는 `Optional` 객체로 감싼 결과를 반환한다.

<br/>
<br/>


## 5.5.2 최댓값과 최솟값

reduce를 이용해서 스트림에서 최댓값과 최솟값을 찾을 수 있다.

<br/>

reduce는 두 인수를 받는다.

- 초깃값
- 스트림의 두 요소를 합쳐서 하나의 값으로 만드는 데 사용할 람다

<br/>

최댓값 계산을 하는 리듀싱 연산을 보자.

```java
Optional<Integer> max = numbers.stream().reduce(Integer::max);
```

<br/>

<p align="center"><img width="750" alt="max" src="https://user-images.githubusercontent.com/86337233/218111063-06d33e63-8810-4233-a773-41e1face3804.png">

<br/>
<br/>

reduce 연산은 새로운 값을 이용해서 **스트림의 모든 요소를 소비할 때까지** 람다를 반복 수행하면서 최댓값을 생산한다.

<br/>

최댓값 계산 코드는 다음과 같다.

```java
Optional<Integer> min = numbers.stream().reduce(Integer::min);
```

<br/>
<br/>

## 스트림 연산 : 상태 없음과 상태 있음

`map`, `filter` 등은 입력 스트림에서 각 요소를 받아 0 또는 결과를 출력 스트림으로 보낸다.

따라서 이들은 보통 상태가 없는, 즉 `내부 상태를 갖지 않는 연산(stateless operation)`이다.

<br/>

`reduce`, `sum`, `max` 같은 연산은 **결과를 누적할 내부 상태가 필요하다.**

내부 상태의 크기는 `한정(bounded, 바운드)`되어 있다.

<br/>

`sorted`나 `distinct` 같은 연산은 filter나 map처럼 스트림을 입력으로 받아 다른 스트림을 출력하는 것처럼 보일 수 있으나, 이들은 서로 다르다.

스트림의 요소를 정렬하거나 중복을 제거하려면 과거의 이력을 알고 있어야 하기에  
이들은 `내부 상태를 갖는 연산(stateful operation)`이라 한다.

즉, **모든 요소가 버퍼에 추가되어 있어야 하며,**  
연산을 수행하는 데 필요한 저장소 크기는 정해져 있지 않기 때문에 데이터 스트림의 크기가 크거나 무한이면 문제가 발생할 수 있다.

<br/>

### 요약

<p align="center"><img width="650" alt="요약 1" src="https://user-images.githubusercontent.com/86337233/218111062-63541acf-b483-44f1-87f3-c299cf7e02b5.png">

<br/>
<br/>

<p align="center"><img width="650" alt="요약 2" src="https://user-images.githubusercontent.com/86337233/218111067-6d603b4d-5a24-4b7d-b251-ba5dceda8111.png">

<br/>
<br/>
<br/>
<br/>

# 5.6 실전 연습

코드 : [src/main/java/chapter05/practice](/src/main/java/chapter05/practice)

<br/>
<br/>
<br/>

# 5.7 숫자형 스트림

`reduce` 메서드를 통해 **스트림 요소의 합**을 구할 수 있었는데, 여기에는 `박싱 비용`이 숨어있다.

<br/>

e.g.,

```java
int calories = menu.stream()
        .map(Dish::getCalories)
        .reduce(0, Integer::sum); // 내부적으로 합계를 계산하기 전에 Integer를 기본형으로 언박싱해야 함
```

`menu.stream().map(…).sum();`처럼 sum 메서드를 직접 호출할 수 있다면 좋겠지만, map 메서드가 Stream<T>를 생성하기 때문에 불가능하다.

<br/>

다행히도 스트림 API는 숫자 스트림을 효율적으로 처리할 수 있는 `기본형 특화 스트림(primitive stream specialization)`을 제공한다.

<br/>
<br/>


## 5.7.1 기본형 특화 스트림

자바 8에서는 박싱 비용을 피할 수 있도록 세 가지 기본형 특화 스트림을 제공한다.

- int 요소에 특화된 `IntStream`
- double 요소에 특화된 `DoubleStream`
- long 요소에 특화된 `LongStream`

<br/>

1. 숫자 관련 리듀싱 연산 수행 메서드를 제공한다. (e.g., sum, max)
2. 필요할 때 다시 객체 스트림으로 복원하는 기능을 제공한다.

> 💡 특화 스트림은 오직 박싱 과정에서 일어나는 **효율성**과 관련이 있으며, 스트림에 추가 기능을 제공하지는 않는다.

<br/>


### 숫자 스트림으로 매핑

map과 정확히 같은 기능을 수행하지만, Stream<T> 대신 **특화된 스트림을 반환한다.**

- `mapToInt`
- `mapToDouble`
- `mapToLong`

<br/>

e.g.,

```java
int calories = menu.stream() // Stream<Dish> 반환
        .mapToInt(Dish::getCalories) // IntStream 반환 (Stream<Integer>가 아님!)
        .sum(); // 스트림이 비어있으면 기본값 0을 반환
```

<br/>

이들은 `sum` 뿐만 아니라 `max`, `min`, `average` 등 다양한 유틸리티 메서드도 지원한다.

<br/>

### 객체 스트림으로 복원하기

`boxed` 메서드를 통해 특화 스트림을 **특화되지 않은 스트림으로 변환**할 수 있다.

<br/>

e.g.,

```java
IntStream intStream = menu.stream.mapToInt(Dish::getCalories); // 스트림을 숫자 스트림으로 변환
Stream<Integer> stream = intStream.boxed(); // 숫자 스트림을 스트림으로 변환
```

<br/>

### 기본값 : OptionalInt

합계를 구할 때(`sum()`)는 기본값 0이 있기 때문에 문제가 없었지만,  
IntStream에서 최댓값(`max()`)이나 최솟값(`min()`)을 찾을 때에는 0이라는 기본값 때문에 잘못된 결과가 도출될 수 있다.

**즉, 스트림에 요소가 없는 상황과 실제 최댓값이 0인 상황을 구별할 수가 없다.**

<br/>

이를 해결하기 위해서는 Optional을 Integer, String 등의 `참조 형식으로 파라미터화`할 수 있으며,  
제공된 아래의 **세 가지 기본형 특화 스트림 버전**을 사용할 수 있다.

- `Optionalnt`
- `OptionalDouble`
- `OptionalLong`

<br/>

e.g., `OptionalInt`를 이용해서 `IntStream`의 최댓값 요소를 찾는 코드

```java
OptionalInt maxCalories = menu.stream()
        .mapToInt(Dish::getCalories)
        .max();

// 최댓값이 없는 상황에 사용할 기본값을 정의할 수 있음
int max = maxCalories.orElse(1); // 값이 없을 때 기본 최댓값을 명시적으로 설정
```

<br/>
<br/>


## 5.7.2 숫자 범위

자바 8의 `IntStream`과 `LongStream`에서는 `range`와 `rangeClosed`라는 두 가지 정적 메서드를 제공한다.

- 첫 번째 인수 : 시작값
- 두 번째 인수 : 종료값

<br/>

- `range` : 시작값과 종료값이 결과에 포함되지 않는다.
- `rangeClosed` : 시작값과 종료값이 결과에 포함된다.

<br/>

e.g.,

```java
// range
IntStream evenNumbers = IntStream.range(1, 100) // (1, 100)
        .filter(n -> n % 2 == 0); // 2부터 99까지의 짝수 스트림
System.out.println(evenNumbers.count()); // 49

// rangeClosed
IntStream evenNumbers = IntStream.rangeClosed(1, 100) // [1, 100]
        .filter(n -> n % 2 == 0); // 1부터 100까지의 짝수 스트림
System.out.println(evenNumbers.count()); // 50
```

<br/>
<br/>


## 5.7.3 숫자 스트림 활용 : 피타고라스 수

코드 : [src/main/java/chapter05/pythagorean/PythagoreanTriples.java](/src/main/java/chapter05/pythagorean/PythagoreanTriples.java)

<br/>

<p align="center"><img width="500" alt="피타고라스 이론" src="https://user-images.githubusercontent.com/86337233/218111070-63d8bc2a-225c-41bc-af55-53b44fc8b381.png">

<br/>
<br/>
<br/>
<br/>

# 5.8 스트림 만들기

이 절에서는 일련의 값, 배열, 파일, 함수를 이용한 무한 스트림 만들기 등 다양한 방식으로 스트림을 만드는 방법을 설명한다.

<br/>

## 5.8.1 값으로 스트림 만들기

`Stream.of`를 통해서 스트림을 만들 수 있다. 이는 임의의 수를 인수로 받는다.

<br/>

e.g., 문자열 스트림 생성 코드

```java
Stream<String> stream = **Stream.of**("Java 8", "Lambdas", "In", "Action");
stream.map(String::toUpperCase).forEach(System.out::println); // 모든 문자열을 대문자로 변환한 후 하나씩 출력
```

<br/>

`empty` 메서드를 이용해서 스트림을 비울 수도 있다.

```java
Stream<String> emptyStream = Stream.empty();
```

<br/>
<br/>


## 5.8.2 null이 될 수 있는 객체로 스트림 만들기

자바 9에서는 null이 될 수 있는 객체를 스트림으로 만들 수 있는 메서드, `Stream.ofNullable`가 추가되었다.

(객체가 null이라면 빈 스트림)

<br/>

예를 들어, `System.getProperty`는 제공된 키에 대응하는 속성이 없으면 null을 반환한다고 하자.

**이런 메서드를 스트림에 활용하려면 null을 명시적으로 확인해주어야 했다.**

```java
String homeValue = System.getProperty("home");
Stream<String> homeValueStream = homeValue == null ? Stream.empty() : Stream.of(value); // null에 대한 명시적 확인
```

<br/>

`Stream.ofNullable`을 통해서 위 코드를 간단하게 변경할 수 있다.

```java
Stream<String> homeValueStream = Stream.ofNullable(System.getProperty("home"));
```

<br/>
<br/>


null이 될 수 있는 객체를 포함하는 `flatMap`과 함께 사용하는 상황에서는 이 패턴을 더 유용하게 사용할 수 있다.

```java
Stream<String> values = Stream.of("config", "home", "user")
        .flatMap(
                key -> Stream.ofNullable(System.getProperty(key))
        );
```

<br/>
<br/>


## 5.8.3 배열로 스트림 만들기

배열을 인수로 받는 정적 메서드 `Arrays.stream`을 이용해서 스트림을 만들 수 있다.

<br/>

e.g., **기본형 int**로 이루어진 배열을 `IntStream`으로 변환하는 코드

```java
int[] numbers = {2, 3, 5, 7, 11, 13};
int sum = Arrays.stream(numbers).sum(); // 41
```

<br/>
<br/>


## 5.8.4 파일로 스트림 만들기

파일을 처리하는 등의 I/O 연산에 사용하는 자바의 `NIO API(비블록 I/O)`도 스트림 API를 활용할 수 있도록 업데이트되었다.

`java.nio.file.Files`의 많은 정적 메서드가 스트림을 반환한다.

<br/>

e.g., 파일에서 고유한 단어 수를 찾는 코드

```java
long uniqueWords = 0;

try (Stream<String> lines = Files.lines(Paths.get("data.txt"), Charset.defaultCharset()) {
    uniqueWords = lines.flatMap(line -> Arrays.stream(line.split(" ")))
        .distinct()
        .count(); // 고유 단어 수 계산
} catch (IOException e) {
    // 파일을 열다가 예외가 발생하면 처리
}

```

- `Files.lines`로 **파일의 각 행 요소를 반환하는 스트림**을 얻을 수 있다.
- 스트림은 자원을 자동으로 해제할 수 있는 `AutoCloseable`이므로 try-finally가 필요없다.

<br/>
<br/>


## 5.8.5 함수로 무한 스트림 만들기

`무한 스트림(infinite stream 또는 언바운드 스트림(unbounded stream))`은 크기가 고정되지 않은 스트림을 말한다.

스트림 API에서 제공하는 두 정적 메서드 `Stream.iterate`와 `Stream.generate`을 통해 무한 스트림을 만들 수 있다.

<br/>

iterate와 generate에서 만든 스트림은 **요청할 때마다 주어진 함수를 이용해서 값을 만들기에** 무제한으로 값을 계산할 수 있다.

- `limit`을 이용해서 명시적으로 스트림의 크기를 제한해야 한다.
    - 그렇지 않으면 최종 연산(e.g., forEach)을 수행했을 때 아무 결과도 계산되지 않는다.
- 무한적으로 계산이 반복되므로 정렬하거나 reduce 할 수 없다.

<br/>

### iterate 메서드

**연속된 일련의 값**을 만들 때는 `iterate`를 사용한다.

e.g., 짝수 스트림을 생성하는 코드

```java
Stream.iterate(0, n -> n + 2) // 초깃값, 람다
        .limit(10) // 스트림의 크기를 명시적으로 제한
        .forEach(System.out::println);
```

<br/>

자바 9의 `iterate` 메서드는 **프레디케이트를 지원한다.**

e.g., 0에서 시작해서 100보다 크면 숫자 생성을 중단하는 코드

```java
IntStream.iterate(0, n -> n < 100, n -> n + 4) // 두 번째 인수 : 프레디케이트 (언제까지 작업을 수행할 것인가?)
        .forEach(System.out::println);

IntStream.iterate(0, n -> n + 4)
        .takeWhile(n -> n < 100) // takeWhile (스트림 쇼트서킷을 지원)
        .forEach(System.out::println);
// filter 메서드는 언제 이 작업을 중단해야 하는지를 알 수 없기 때문에 이 예제에서는 사용이 불가하다.
```

<br/>

### generate 메서드

`generate`도 iterate처럼 **요구할 때 값을 계산**하는 무한 스트림을 만들 수 있으나,  
iterate와 달리 `Supplier<T>`를 인수로 받아서 새로운 값을 생산한다.

<br/>

e.g., 0 ~ 1 사이에서 임의의 double 숫자 다섯 개를 생성하는 코드

```java
Stream.generate(Math::random) // Math.random : 임의의 새로운 값을 생성
        .limit(5) // limit가 없다면 스트림은 언바운드 상태가 됨
        .forEach(System.out::println);
```

<br/>
<br/>
<br/>

위 예제에서 사용한 `발행자(supplier, 즉 메서드 참조 Math.random)`는 상태가 없는 메서드이다.

나중 계산에 사용할 어떤 값도 저장해두지 않는다는 것이다.

<br/>

하지만 **발행자가 상태를 저장한 다음**, 스트림의 다음 값을 만들 때 상태를 고치는 경우도 있다.

(병렬 코드에서는 **발행자에 상태가 있으면 안전하지 않으므로**, 아래는 단지 설명에 필요할 예제일 뿐 실제로는 피해야 함)

<br/>

피보나치 수열을 출력하는 코드를 구현해보자.

> ✅ `iterate`을 통한 구현

```java
Stream.iterate(new int[]{0, 1},
        t -> new int[]{t[1], t[0] + t[1]})
        .limit(20)
        .map(t -> t[0])
        .forEach(System.out::println); // 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, ...
```

각 과정에서 새로운 값을 생성하면서도 기존 상태를 바꾸지 않는 순수한 `불변(immutable) 상태`를 유지한다.

<br/>

> ✅ `generate`을 통한 구현

```java
IntSupplier fib = new IntSupplier() { // 익명 클래스를 사용
    // getAsInt 메서드의 연산을 커스터마이즈할 수 있는 상태 필드를 정의
    private int previous = 0;
    private int current = 1;

    @Override
    public int getAsInt() {
        int nextValue = previous + current;
        previous = current;
        current = nextValue; // 상태를 갱신
        return previous;
    }
};

IntStream.generate(fib)
        .limit(10)
        .forEach(System.out::println);
```

`IntSupplier` 인스턴스는 기존 피보나치 요소와 두 인스턴스 변수에 어떤 피보나치 요소가 들어있는지 추적하므로 `가변(mutable) 상태` 객체다.

<br/>
<br/>


> 💡 스트림을 병렬로 처리하면서 올바른 결과를 얻으려면 **불변 상태 기법**을 고수해야 한다. (7장에서 자세히 설명함)
