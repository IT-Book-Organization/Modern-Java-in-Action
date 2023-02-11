# Chapter 6. 스트림으로 데이터 수집

<br/>

> 자바 8의 스트림은 데이터 집합을 처리하는 `게으른 반복자`라고 설명할 수 있다.

### 중간 연산

- filter, map 등
- 한 스트림을 다른 스트림으로 변환하는 연산으로서, 여러 연산을 연결할 수 있다.
- 스트림 파이프라인을 구성하며, **스트림의 요소를 소비(consume)하지 않는다.**

### 최종 연산

- **스트림의 요소를 소비해서 최종 결과를 도출한다.**
- 스트림 파이프라인을 최적화하면서 계산 과정을 짧게 생략하기도 한다.

<br/>

이전 4장과 5장에서는 `toList`로 스트림 요소를 항상 리스트로만 변환했다.

이 장에서는 `reduce`가 그랬던 것처럼 최종 연산 `collect` 역시  
다양한 요소 누적 방식을 인수로 받아서 스트림을 최종 결과로 도출하는 **리듀싱 연산**을 수행할 수 있음을 설명한다.

다양한 요소 누적 방식은 `Collector` 인터페이스에 정의되어 있다.

<br/>
<br/>
<br/>

# 6.1 컬렉터란 무엇인가?

어떤 트랜잭션 리스트가 있는데 이들을 **통화별로 그룹화**한다고 가정하자.

<br/>

> ✅ 명령형 프로그래밍 버전

```java
Map<Currency, List<Transaction>> transactionsByCurrencies = new HashMap<>();

for (Transaction transaction : transactions) {
    Currency currency = transaction.getCurrency();
    List<Transaction> transactionsForCurrency = transactionsByCurrencies.get(currency);
    if (transactionsForCurrency == null) {
        transactionsForCurrency = new ArrayList<>();
        transactionsForCurrencies.put(currency, transactionsForCurrency);
    }
    transactionsForCurrency.add(transaction);
}
```

코드가 무엇을 실행하는지 한눈에 파악하기 어렵다.

즉, 다중 루프와 조건문을 추가하며 가독성과 유지보수성이 크게 떨어진다.

<br/>

> ✅ 함수형 프로그래밍 버전

```java
Map<Currency, List<Transaction>> transactionsByCurrencies =
        transactions.stream().collect(groupingBy(Transaction::getCurrency));
```

- `collect` 메서드로 `Collector 인터페이스 구현`(스트림의 요소를 어떤 식으로 도출할지에 대한 지정)을 전달했다.

- `groupingBy`를 통해 ‘각 키(통화) 버킷(bucket) 그리고 각 키 버킷에 대응하는 요소 리스트를 값으로 포함하는 맵(Map)을 만들라’는 동작을 수행한다.

<br/>
<br/>

## 6.1.1 고급 리듀싱 기능을 수행하는 컬렉터

스트림에 collect를 호출하면 스트림의 요소에 `리듀싱 연산`이 수행된다.

collect에서는 이를 이용해서 **스트림의 각 요소를 방문하면서** `컬렉터`가 작업을 처리한다.

<br/>

보통 함수를 요소로 변환할 때는 컬렉터를 적용하며, 최종 결과를 저장하는 자료구조에 값을 누적한다.

e.g.,

<p align="center"><img width="650" alt="리듀싱 연산" src="https://user-images.githubusercontent.com/86337233/218186797-8107ba75-cb41-48f5-89ee-6abaae1bd12b.png">

<br/>
<br/>

1. 스트림의 각 트랜잭션 탐색
2. 트랜잭션의 통화 추출
3. 통화/트랜잭션 쌍을 그룹화 맵으로 추가

<br/>
<br/>

**Collector 인터페이스의 메서드를 어떻게 구현하느냐에 따라** 스트림에 어떤 리듀싱 연산을 수행할지가 결정된다.

<br/>

Collector 유틸리티 클래스는 자주 사용하는 컬렉터 인스턴스를 손쉽게 생성할 수 있는 `정적 팩토리 메서드`를 제공한다.

e.g., `toList`

```java
List<Transaction> transactions = transactionStream.collect(Collectors.toList());
```

<br/>
<br/>

## 6.1.2 미리 정의된 컬렉터

`Collectors`에서 제공하는 메서드의 기능은 크게 세 가지로 구분할 수 있다.

<br/>

### 스트림 요소를 하나의 값으로 리듀스하고 요약(summarize)

트랜잭션 리스트에서 트랜잭션 총합을 찾는 등의 다양한 계산을 수행할 때
이들 컬렉터를 유용하게 활용할 수 있다.

스트림에 있는 객체의 숫자 필드 합계나 평균 등을 반환하는 연산에도 리듀싱 기능이 자주 사용되는데,
이러한 연산을 `요약(summarization) 연산`이라 부른다.

<br/>

### 요소 그룹화

다수준으로 그룹화하거나 각각의 결과 서브그룹에 추가로 리듀싱 연산을 적용할 수 있도록
다양한 컬렉터를 조합할 수 있다.

<br/>

### 요소 분할(partitioning)

프레디케이트를 그룹화 함수로 사용한다.

<br/>
<br/>
<br/>

# 6.2 리듀싱과 요약

## 6.2.1 스트림값에서 최댓값과 최솟값 검색

두 개의 메서드, `Collectors.maxBy`, `Collectors.minBy`를 이용해서 스트림의 최댓값과 최솟값을 계산할 수 있다.

<br/>

e.g.,

```java
Comparator<Dish> dishCaloriesComparator = Comparator.comparingInt(Dish::getCalories);
Optional<Dish> mostCalorieDish = menu.stream().collect(maxBy(dishCaloriesComparator));
```

<br/>
<br/>

## 6.2.2 요약 연산

Collectors 클래스는 특별한 **요약 팩토리 메서드**들을 제공한다.

- `Collectors.summingInt`
- `Collectors.summingLong`
- `Collectors.summingDouble`

<br/>

`Collectors.summingInt`의 경우,

- `객체를 int로 매핑하는 함수`를 인수로 받는다.
- 인수로 전달된 함수는 `객체를 int로 매핑한 컬렉터`를 반환한다.
- summingInt가 collect 메서드로 전달되면 요약 작업을 수행한다.

<br/>

e.g., 메뉴 리스트의 총 칼로리를 계산하는 코드

```java
int totalCalories = menu.stream().collect(summingInt(Dish::getCalories));
```

<br/>

칼로리로 매핑된 각 요리의 값을 탐색하면서 초깃값(0)으로 설정되어 있는 누적자에 칼로리를 더한다.

<br/>

<p align="center"><img width="650" alt="summingInt" src="https://user-images.githubusercontent.com/86337233/218186801-a88768f2-3a9a-4ed5-a0d7-ac389e7a43e3.png">

<br/>
<br/>

평균값 계산 등의 연산도 요약 기능으로 제공된다.

- `Collectors.averagingInt`
- `Collectors.averagingLong`
- `Collectors.averagingDouble`

```java
double avgCalories = menu.stream().collect(averagingInt(Dish::getCalories));
```

<br/>
<br/>

종종 이들 중 두 개 이상의 연산을 한 번에 수행해야 할 때도 있다.

아래 메서드들은 하나의 요약 연산으로, 모든 요소 수(count), 합계(sum), 평균(average), 최댓값(max), 최솟값(min)을 계산해준다.

- `Collectors.summarizingInt` → `IntSummaryStatistics` 클래스 반환
- `Collectors.summarizingLong` → `LongSummaryStatistics` 클래스 반환
- `Collectors.summarizingDouble` → `DoubleSummaryStatistics` 클래스 반환

```java
IntSummaryStatistics menuStatistics = menu.stream().collect(summarizingInt(Dish::getCalories));
```

<br/>
<br/>

## 6.2.3 문자열 연결

컬렉터에 `joining 팩토리 메서드`를 이용하면 스트림의 **각 객체에 toString 메서드를 호출해서**  
추출한 모든 문자열을 하나의 문자열로 연결해서 반환한다.

<br/>

e.g., 메뉴의 모든 요리명을 연결하는 코드

```java
String shortMenu = menu.stream().map(Dish::getName).collect(joining());
```

<br/>

`joining` 메서드는 **내부적으로 StringBuilder를 이용해서 문자열을 하나로 만든다.**

Dish 클래스가 요리명을 반환하는 toString 메서드를 포함하고 있다면 다음과 같이 코드를 수정할 수 있다.

```java
String shortMenu = menu.stream().collect(joining());
```

<br/>

연결된 두 요소 사이에 구분 문자열을 넣을 수 있도록 오버로드된 joining 팩토리 메서드도 있다.

```java
String shortMenu = menu.stream().collect(joining(", "));
```

<br/>
<br/>

## 6.2.4 범용 리듀싱 요약 연산

지금까지 살펴본 모든 컬렉터는 `reducing 팩토리 메서드(범용 Collectors.reducing)`로도 정의할 수 있다.

<br/>

e.g., 메뉴의 모든 칼로리 합계를 계산하는 코드

```java
int totalCalories = menu.stream().collect(reducing(0, Dish::getCalories, (i, j) -> i + j));
```

- 첫 번째 인수
    - 리듀싱 연산의 시작값
    - 스트림에 인수가 없을 때는 반환값
- 두 번째 인수 : 요리를 칼로리 정수로 변환할 때 사용한 변환 함수
- 세 번째 인수 : 같은 종류의 두 항목을 하나의 값으로 더하는 BinaryOperator

<br/>
<br/>

**한 개의 인수를 가진** reducing 팩토리 메서드도 존재한다.

e.g., 가장 칼로리가 높은 요리를 찾는 코드

```java
Optional<Dish> mostCalorieDish = menu.stream()
        .collect(reducing((d1, d2) -> d1.getCalories() > d2.getCalories() ? d1 : d2));
```

- 첫 번째 인수 : 스트림의 첫 번째 요소
- 두 번째 인수 : 자신을 그대로 반환하는 `항등 함수(identity function)`

즉, 여기에는 리듀싱 연산의 시작값이 없다.

<br/>

만약 빈 스트림이 넘겨졌을 때 시작값이 설정되지 않는 상황이 벌어지기 때문에 이 메서드는 `Optional<T>` 객체를 반환한다.

<br/>

### collect vs. reduce

- `collect` 메서드 : 도출하려는 결과를 누적하는 **컨테이너를 바꾸도록** 설계되었다.
- `reduce` 메서드 :  두 값을 하나로 도출하는 **불변형 연산**이다.
    - reduce 메서드를 사용하면 리듀싱 연산을 병렬로 수행할 수 없다는 문제가 존재한다.  
      (여러 스레드가 동시에 같은 데이터 구조체를 고치게 될 수도 있기 때문)

<br/>

> 💡 가변 컨테이너 관련 작업이면서 병렬성을 확보하려면 `collect` 메서드로 리듀싱 연산을 구현하는 것이 바람직하다. (7장에서 자세히 설명)

<br/>
<br/>


### 컬렉션 프레임워크 유연성 : 같은 연산도 다양한 방식으로 수행할 수 있다

reducing 컬렉터를 사용한 이전 예제를 다음과 같이 좀 더 단순화할 수 있다.

```java
int totalCalories = menu.stream().collect(reducing(0, Dish::getCalories, Integer::sum));
// 인수들은 순서대로 '초깃값, 변환 함수, 합계 함수'이다.
```

> 누적자를 `초깃값`으로 초기화하고,  
> `합계 함수`를 이용해서 각 요소에 `변환 함수`를 적용한 결과 숫자를 반복적으로 조합한다.

<br/>

<p align="center"><img width="750" alt="컬렉션 프레임워크 유연성" src="https://user-images.githubusercontent.com/86337233/218186807-1ee9fc08-b03d-43b8-b38d-710cb3840519.png">

<br/>
<br/>
<br/>

### 제네릭 와일드카드 ‘?’ 사용법

와일드 카드 `?`는 컬렉터의 누적자 형식이 알려지지 않았음을, 즉 **누적자의 형식이 자유로움**을 의미한다.

e.g., counting 팩토리 메서드가 반환하는 컬렉터 시그니처의 두 번째 제네릭 형식으로 와일드 카드 `?`를 사용

```java
public static <T> Collector<T, ?, Long> counting() {
    return reducing(0L, e -> 1L, Long::sum);
}
```

<br/>
<br/>
<br/>

# 6.3 그룹화

팩토리 메서드 `Collectors.groupingBy`를 통해 데이터 집합을 하나 이상의 특성으로 분류해서 그룹화할 수 있다.

e.g., 메뉴를 그룹화하는 코드

<details>
<summary><code>List&lt;Dish> menu</code></summary>

```java
public static final List<Dish> menu = asList(
        new Dish("pork", false, 800, Type.MEAT),
        new Dish("beef", false, 700, Type.MEAT),
        new Dish("chicken", false, 400, Type.MEAT),
        new Dish("french fries", true, 530, Type.OTHER),
        new Dish("rice", true, 350, Type.OTHER),
        new Dish("season fruit", true, 120, Type.OTHER),
        new Dish("pizza", true, 550, Type.OTHER),
        new Dish("prawns", false, 400, Type.FISH),
        new Dish("salmon", false, 450, Type.FISH));
```

</details>

```java
Map<Dish.Type, List<Dish>> dishesByType = menu.stream().collect(groupingBy(Dish::getType));

// 실행 결과 : {FISH=[prawns, salmon], OTHER=[french fries, rice, season fruit, pizza], MEAT=[pork, beef, chicken]}
```

<br/>

groupingBy의 인수로는 어느 기준으로 그룹화를 진행할 것인지를 나타내는 `분류 함수(classification function)`를 전달하며,  
(예제에서는 `Dish::getType`)

반환값은 그룹화 함수가 반환하는 키, 각 키에 대응하는 스트림의 모든 항목 리스트를 값으로 갖는 `맵(Map)`이다.

<br/>

<p align="center"><img width="750" alt="그룹화" src="https://user-images.githubusercontent.com/86337233/218186811-5e163f20-a726-4e48-ad59-4d2d8ef3221e.png">

<br/>
<br/>
<br/>

단순한 속성 접근자 대신 더 복잡한 분류 기준이 필요한 상황에서는 메서드 참조를 분류 함수로 사용할 수 없다.

이때는 **람다 표현식**으로 필요한 로직을 구현할 수 있다.

```java
public enum CaloricLevel { DIET, NORMAL, FAT }
// DIET : 400 칼로리 이하
// NORMAL : 400~700 칼로리
// FAT : 700 칼로리 초과

Map<CaloricLevel, List<Dish>> dishesByCaloricLevel = menu.stream().collect(
        groupingBy(dish -> {
            if (dish.getCalories() <= 400) return CaloricLevel.DIET;
			else if (dish.getCalories() <= 700) return CaloricLevel.NORMAL;
			else return CaloricLevel.FAT;
        })
);
```

<br/>
<br/>

지금까지는 메뉴의 요리를 종류 **또는** 칼로리로 그룹화하는 방법을 보았다.

<br/>
<br/>

## 6.3.1 그룹화된 요소 조작

500 칼로리가 넘는 요리만 필터한다고 가정하자.

<br/>

다음처럼 **프레디케이트로 필터를 적용해** 문제를 해결할 수 있을 것이라고 생각할 것이다.

```java
Map<Dish.Type, List<Dish>> caloricDishesByType = menu.stream()
        .filter(dish -> dish.getCalories() > 500)
        .collect(groupingBy(Dish::getType));

// 실행 결과 : {OTHER=[french fries, pizza], MEAT=[pork, beef]}
```

우리의 필터 프레디케이트를 만족하는 FISH 종류 요리는 없기 때문에 **결과 맵에서 해당 키 자체가 사라지는 문제가 발생한다.**

<br/>

Collectors 클래스는 이 문제를 해결하기 위해  
일반적인 분류 함수에 **Collector 형식의 두 번째 인수를 갖도록** `groupingBy` 팩토리 메서드를 오버로드한다.

```java
Map<Dish.Type, List<Dish>> caloricDishesByType = menu.stream()
        .collect(
                groupingBy(Dish::getType, filtering(dish -> dish.getCalories() > 500, toList()))
        );

// 실행 결과 : {OTHER=[french fries, pizza], MEAT=[pork, beef], FISH=[]}
```

<br/>
<br/>

### mapping()

Collectors 클래스는 매핑 함수와 **각 항목에 적용한 함수를 모으는 데 사용하는 또 다른 컬렉터**를 인수로 받는 `mapping` 메서드를 제공한다.

<br/>

e.g., 그룹의 각 요리를 관련 이름 목록으로 변환하는 코드

```java
Map<Dish.Type, List<String>> dishNamesByType = menu.stream()
        .collect(
                groupingBy(Dish::getType, mapping(Dish::getName, toList()))
        );
```

<br/>
<br/>

### flatMapping()

groupingBy와 연계해 세 번째 컬렉터를 사용해서 일반 맵이 아닌 `flatMap` 변환을 수행할 수 있다.

이때 맵의 각 그룹은 이전 예제와는 다르게 요리가 아니라 문자열 리스트다.

<details>
<summary><code>Map&lt;String, List&lt;String>> dishTags</code></summary>

```java
public static final Map<String, List<String>> dishTags = new HashMap<>();

dishTags.put("pork", asList("greasy", "salty"));
dishTags.put("beef", asList("salty", "roasted"));
dishTags.put("chicken", asList("fried", "crisp"));
dishTags.put("french fries", asList("greasy", "fried"));
dishTags.put("rice", asList("light", "natural"));
dishTags.put("season fruit", asList("fresh", "natural"));
dishTags.put("pizza", asList("tasty", "salty"));
dishTags.put("prawns", asList("tasty", "roasted"));
dishTags.put("salmon", asList("delicious", "fresh"));
```
</details>

<br/>

각 요리에서 태그 리스트를 얻어야 한다고 해보자.

`flatMapping` 컬렉터를 이용하면 각 형식의 요리의 태그를 추출할 수 있다.

```java
Map<Dish.Type, Set<String>> dishNamesByType = menu.stream()
        .collect(
                groupingBy(Dish::getType,
                            flatMapping(dish -> dishTags.get(dish.getName()).stream(),
                            toSet()) // 집합으로 그룹화 -> 중복 태그를 제거
        );

// 실행 결과 : {MEAT=[salty, greasy, roasted, fried, crisp], FISH=[roasted, tasty, fresh, delicious], OTHER=[salty, greasy, natural, light, tasty, fresh, fried]}
```

<br/>
<br/>

## 6.3.2 다수준 그룹화

**두 인수를 받는 팩토리 메서드** `Collectors.groupingBy`를 이용해서 항목을 다수준으로 그룹화(두 가지 이상의 기준을 동시에 적용)할 수 있다.

<br/>

아래는 **두 번째 groupingBy 컬렉터를 외부(첫 번째) 컬렉터로 전달해서** 다수준 그룹화 연산을 구현했다.

```java
Map<Dish.Type, Map<CaloricLevel, List<Dish>>> dishesByTypeCaloricLevel = menu.stream()
        .collect(
                groupingBy(Dish::getType, // 첫 번째 수준의 분류 함수
                            groupingBy(dish -> { // 두 번째 수준의 분류 함수
                                if (dish.getCalories() <= 400)
                                    return CaloricLevel.DIET;
                                else if (dish.getCalories() <= 700)
                                    return CaloricLevel.NORMAL;
                                else return CaloricLevel.FAT;
                            })
                )
        );

// 실행 결과 (두 수준의 맵) : {MEAT={DIET=[chicken], NORMAL=[beef], FAT=[pork]}, FISH={DIET=[prawns], ... }
```

<br/>

<p align="center"><img width="700" alt="중첩 맵" src="https://user-images.githubusercontent.com/86337233/218186815-fa763719-32f7-451e-bdad-45d33b16197e.png">

<br/>
<br/>

> 💡 n수준 그룹화의 결과는 n수준 트리 구조로 표현되는 n수준 맵이 된다.

<br/>
<br/>

## 6.3.3 서브그룹으로 데이터 수집

첫 번째 groupingBy**로 넘겨주는** 컬렉터의 **형식은 제한이 없다.**

e.g., 두 번째 인수로 `counting` 컬렉터를 전달하여 메뉴에서 요리의 수를 종류별로 계산하는 코드

```java
Map<Dish.Type, Long> typesCount = menu.stream().collect(groupingBy(Dish::getType, counting()));

// 실행 결과 : {MEAT=3, FISH=2, OTHER=4}
```

<br/>

분류 함수 한 개의 인수를 갖는 `groupingBy(f)`는 사실 `groupingBy(f, toList())`의 축약형이다.

<br/>

요리의 **종류**를 분류하는 컬렉터로 메뉴에서 가장 높은 칼로리를 가진 요리를 찾는 코드도 구현할 수 있다.

```java
Map<Dish.Type, Optional<Dish>> mostCaloricByType = menu.stream()
        .collect(
                groupingBy(Dish::getType, maxBy(comparingInt(Dish::getCalories)))
        );

// 실행 결과 (key - 요리의 종류 : value - Optional<Dish>)
// {FISH=Optional[salmon], OTHER=Optional[pizza], MEAT=Optional[pork]}
```

> groupingBy 컬렉터는 스트림의 첫 번째 요소를 찾은 이후에야 그룹화 맵에 새로운 키를 (게으르게) 추가한다.

<br/>

### 컬렉터 결과를 다른 형식에 적용하기

팩토리 메서드 `Collectors.collectingAndThen`으로 컬렉터가 반환한 결과를 다른 형식으로 활용할 수 있다.

```java
Map<Dish.Type, Optional<Dish>> mostCaloricByType = menu.stream()
        .collect(
                groupingBy(Dish::getType, // 분류 함수
                            collectingAndThen(maxBy(comparingInt(Dish::getCalories)),
                            Optional::get)) // 변환 함수 : Optional에 포함된 값을 추출함
        );

// 실행 결과 : {FISH=salmon, OTHER=pizza, MEAT=pork}
```

<br/>

가장 외부 계층에서 안쪽으로 다음과 같은 작업이 수행된다. (점선 : 컬렉터)

<br/>

<p align="center"><img width="600" alt="중첩" src="https://user-images.githubusercontent.com/86337233/218186819-d12eea69-ce72-4fec-87a6-1281280855df.png">

<br/>
<br/>

1. `groupingBy`는 가장 바깥쪽에 위치하면서 요리의 종류에 따라 메뉴 스트림을 세 개의 서브 스트림으로 **그룹화**한다.


2. groupingBy 컬렉터는 `collectingAndThen` 컬렉터를 감싼다.  
   따라서 두 번째 컬렉터는 그룹화된 세 개의 서브스트림에 적용된다.


3. collectingAndThen 컬렉터는 세 번째 컬렉터 `maxBy`를 감싼다.


4. 리듀싱 컬렉터가 서브스트림에 연산을 수행한 결과에 collectingAndThen의 `Optional::get 변환 함수`가 적용된다.


5. groupingBy 컬렉터가 반환하는 맵의 분류 키에 대응하는 세 값이 각각의 요리 형식에서 가장 높은 칼로리다.

<br/>
<br/>

## groupingBy와 함께 사용하는 다른 컬렉터 예제

### groupingBy + mapping 컬렉터

`mapping`은 입력 요소를 누적하기 전에 매핑 함수를 적용해서 다양한 형식의 객체를 주어진 형식의 컬렉터에 맞게 변환하는 역할을 한다.

e.g., 각 요리 형식에 존재하는 모든 CaloricLevel 값을 반환하는 코드

```java
menu.stream().collect(
        groupingBy(Dish::getType,
                    mapping(dish -> {
                        if (dish.getCalories() <= 400) {
                            return CaloricLevel.DIET;
                        } else if (dish.getCalories() <= 700) {
                            return CaloricLevel.NORMAL;
                        } else {
                            return CaloricLevel.FAT;
                        }
                    },
                    toSet())
        )
);

// 실행 결과 : {OTHER=[DIET, NORMAL], MEAT=[DIET, NORMAL, FAT], FISH=[DIET, NORMAL]}
```

<br/>

### toCollection

이전 예제는 Set의 형식이 정해져 있지 않았다.

`toCollection`을 이용하면 원하는 방식으로 결과를 제어할 수 있다.

<br/>

e.g., `HashSet::new`

```java
menu.stream().collect(
        groupingBy(Dish::getType,
                    mapping(dish -> {
                        if (dish.getCalories() <= 400) {
                            return CaloricLevel.DIET;
                        } else if (dish.getCalories() <= 700) {
                            return CaloricLevel.NORMAL;
                        } else {
                            return CaloricLevel.FAT;
                        }
                    },
                    toCollection(HashSet::new)
        )
);
```

<br/>
<br/>
<br/>

# 6.4 분할

분할은 `분할 함수(partitioning function)`**라 불리는 프레디케이트를 분류 함수로 사용하는** 특수한 그룹화 기능이다.

맵의 키 형식은 `Boolean`이다.

<br/>

e.g., 모든 요리를 채식 요리와 채식이 아닌 요리로 분류하는 코드

```java
Map<Boolean, List<Dish>> partitionedMenu = menu.stream()
        .collect(partitioningBy(Dish::isVegetarian)); // 분할 함수

// 실행 결과 : {false=[pork, beef, chicken, ... ], true=[french fries, rice, ... ]}
```

<br/>
<br/>


## 6.4.1 분할의 장점

분할 함수가 반환하는 **참, 거짓 두 가지 요소의 스트림 리스트를 모두 유지한다**는 것이 분할의 장점이다.

<br/>

e.g., **컬렉터를 두 번째 인수로 전달할 수 있는** 오버로드된 버전의 partitioningBy 메서드

```java
Map<Boolean, Map<Dish.Type, List<Dish>>> vegetarianDishesByType = menu.stream()
        .collect(
                partitioningBy(Dish::isVegetarian, // 분할 함수
                groupingBy(Dish::getType)) // 두 번째 컬렉터
        ));

// 실행 결과 (두 수준의 맵) : {false={FISH=[prawns, salmon], MEAT=[pork, beef, chicken]}, true={OTHER=[french fries, rice, season fruit, pizza]}}
```

<br/>

e.g., 채식 요리와 채식이 아닌 요리 각각의 그룹에서 가장 칼로리가 높은 요리를 찾는 코드

```java
Map<Boolean, Dish> mostCaloricPartitionedByVegetarian = menu.stream()
        .collect(
                partitioningBy(
                        Dish::isVegetarian,
                        collectingAndThen(
                                maxBy(comparingInt(Dish::getCalories)),
                                Optional::get
                )
        );

// 실행 결과 : {false=pork, true=pizza}
```

<br/>
<br/>

## 6.4.2 숫자를 소수와 비소수로 분할하기

정수 n을 인수로 받아서 2에서 n까지의 자연수를 소수(prime)와 비소수(nonprime)로 나누는 프로그램을 구현하자.

<br/>

1. `isPrime` : 주어진 수가 소수인지 아닌지 판단하는 프레디케이트

    ```java
    public boolean isPrime(int candidate) {
        int candidateRoot = (int) Math.sqrt((double) candidate); // 소수의 대상을 주어진 수의 제곱근 이하의 수로 제한
        return IntStream.rangeClosed(2, cadidateRoot) // 자연수 생성
    	        .noneMatch(i -> candidate % i == 0); // candidate를 나눌 수 없으면 참을 반환
    }
    ```

<br/>

2. `partitionPrimes` 컬렉터

    ```java
    public Map<Boolean, List<Integer>> partitionPrimes(int n) {
        return IntStream.rangeClosed(2, n).boxed()
                .collect(
                    partitioningBy(candidate -> isPrime(candidate))
        );
    }
    ```

<br/>
<br/>

## 요약

<p align="center"><img width="700" alt="요약 1" src="https://user-images.githubusercontent.com/86337233/218186826-87714a10-8246-4937-91be-497e53580d21.png">

<br/>
<br/>

<p align="center"><img width="700" alt="요약 2" src="https://user-images.githubusercontent.com/86337233/218186829-abe41e1e-e381-4193-b725-031ac33c7791.png">

<br/>
<br/>
<br/>
<br/>

# 6.5 Collector 인터페이스

모든 컬렉터는 `Collector 인터페이스`를 구현한다.

`Collector 인터페이스`는 리듀싱 연산(즉, 컬렉터)을 어떻게 구현할지 제공하는 메서드 집합으로 구성된다.

<br/>

다음 코드는 Collector 인터페이스의 시그니처와 다섯 개의 메서드 정의를 보여준다.

```java
public interface Collector<T, A, R> {
    Supplier<A> supplier();
    BiConsumber<A, T> accumulator();
    Function<A, R> finisher();
    BinaryOperator<A> combiner();
    Set<Characteristics> characteristics();
}
```

- `T` : **수집될** 스트림 항목의 제네릭 형식
- `A` : **누적자**, 즉 수집 과정에서 중간 결과를 누적하는 객체의 형식
- `R` : **수집 연산 결과** 객체의 형식 (대개 컬렉션 형식)

<br/>

누적 과정에서 사용되는 객체가 수집 과정의 최종 결과로 사용된다.

<br/>
<br/>

## 6.5.1 Collector 인터페이스의 메서드 살펴보기

먼저 살펴볼 네 개의 메서드는 `collect` 메서드에서 **실행하는 함수를 반환**하는 반면,

다섯 번째 메서드 `characteristics()`는 collect 메서드가 어떤 최적화(e.g., 병렬화)를 이용해서  
리듀싱 연산을 수행할 것인지 결정하도록 돕는 **힌트 특성 집합**을 제공한다.

<br/>

예를 들어, **tream<T>의 모든 요소를 List<T>로 수집하는** `ToListCollector<T>`라는 클래스를 구현할 수 있다.

```java
public class ToListCollector<T> implements Collector<T, List<T>, List<T>>
```

### supplier 메서드 : 새로운 결과 컨테이너 만들기

> 💡 `supplier` 메서드는 **빈 누적자 인스턴스**를 만드는, 파라미터가 없는 함수다.

따라서 **빈 결과로 이루어진 Supplier를 반환한다.**

<br/>

`ToListCollector`에서 supplier는 다음처럼 빈 리스트를 반환한다.

```java
// 람다 표현식
public Supplier<List<T>> supplier() {
    return () -> new ArrayList<T>();
}

// 생성자 참조
public Supplier<List<T>> supplier() {
    return ArrayList::new;
}
```

<br/>

### accumulator 메서드 : 결과 컨테이너에 요소 추가하기

> 💡 `accumulator` 메서드는 **리듀싱 연산을 수행하는 함수**를 반환한다.

- 스트림에서 n번째 요소를 탐색할 때 두 인수를 함수에 적용한다.
    1. 누적자(스트림의 첫 n-1개 항목을 수집한 상태)
    2. n번째 요소


- 반환값은 void이다.
    - 요소를 탐색하면서 적용하는 함수에 의해 누적자 내부 상태가 바뀌므로
      누적자가 어떤 값인지 단정할 수 없다.

<br/>

`ToListCollector`에서 accumulator가 반환하는 함수는 이미 탐색한 항목을 포함하는 리스트에 현재 항목을 추가하는 연산을 수행한다.

```java
// 람다 표현식
public BiConsumer<List<T>, T> accumulator() {
    return (list, item) -> list.add(item);
}

// 메서드 참조
public BiConsumer<List<T>, T> accumulator() {
    return List::add;
}
```

<br/>

### finisher 메서드 : 최종 변환값을 결과 컨테이너로 적용하기

> 💡 `finisher` 메서드는 스트림 탐색을 끝내고 누적자 객체를 최종 결과로 변환하면서 **누적 과정을 끝낼 때 호출할 함수**를 반환해야 한다.

<br/>

때로는 누적자 객체가 이미 최종 결과인 상황도 있다.

이럴 때는 변환 과정이 필요하지 않으므로 finisher 메서드는 항등 함수를 반환한다.

```java
public Function<List<T>, List<T>> finisher() {
    return Function.identity();
}
```

<br/>
<br/>

지금까지 살펴본 세 가지 메서드로도 `순차적 스트림 리듀싱 기능`을 수행할 수 있다.

<br/>

<p align="center"><img width="700" alt="순차 리듀싱 과정" src="https://user-images.githubusercontent.com/86337233/218186832-2c648e5d-ed59-4ea9-ad97-68c1676fec7a.png">

<br/>
<br/>

### combiner 메서드 : 두 결과 컨테이너 병합

> 💡 `combiner` 메서드는 **리듀싱 연산에서 사용할 함수**를 반환한다.

combiner는 스트림의 서로 다른 서브파트를 병렬로 처리할 때 **누적자가 이 결과를 어떻게 처리할지** 정의한다.

<br/>

e.g., `toList`의 combiner

```java
public BinaryOperator<List<T>> combiner() {
    return (list1, list2) -> {
        list1.addAll(list2);
        return list1;
    }
}
```

<br/>
<br/>

네 번째 메서드를 이용하면 **스트림의 리듀싱을 병렬로 수행할 수 있다.**

스트림의 리듀싱을 병렬로 수행할 때 자바 7의 `포크/조인 프레임워크`와 `Spliterator`를 사용한다. (7장에서 자세히 다룸)

<br/>

<p align="center"><img width="750" alt="병렬화 리듀싱 과정" src="https://user-images.githubusercontent.com/86337233/218186837-3a5bcc6c-b4ca-4b99-9d23-78dc0f6793eb.png">

<br/>
<br/>

1. 스트림을 분할해야 하는지 정의하는 조건이 거짓으로 바뀌기 전까지 **원래 스트림을 재귀적으로 분할**한다.


2. 모든 서브스트림(substream)의 **각 요소에 리듀싱 연산을 순차적으로 적용**해서 서브스트림을 병렬로 처리할 수 있다.


3. 컬렉터의 `combiner` 메서드가 반환하는 함수로 **모든 부분결과를 쌍으로 합친다.**  
   즉, 분할된 모든 서브스트림의 결과를 합치면서 연산이 완료된다.


<br/>

### Characteristics 메서드

> 💡 `characteristics` 메서드는 **컬렉터의 연산을 정의하는** Characteristics 형식의 불변 집합을 반환한다.

즉, 스트림을 병렬로 리듀스할 것인지 그리고 병렬로 리듀스한다면 어떤 최적화를 선택해야 할지 힌트를 제공한다.

<br/>

`Characteristics`는 다음 세 항목을 포함하는 열겨형이다.

- `UNORDERED` : 리듀싱 결과는 스트림 요소의 방문 순서나 누적 순서에 영향을 받지 않는다.


- `CONCURRENT`
    - 다중 스레드에서 accumulator 함수를 동시에 호출할 수 있으며, 이 컬렉터는 스트림의 병렬 리듀싱을 수행할 수 있다.
    - 컬렉터의 플래그에 UNORDERED를 함께 설정하지 않았다면  
      **데이터 소스가 정렬되어 있지 않은(즉, 집합처럼 요소의 순서가 무의미한) 상황에서만** 병렬 리듀싱을 수행할 수 있다.


- `IDENTITY_FINISH`
    - finisher 메서드가 반환하는 함수는 단순히 identity를 적용할 뿐이므로 이를 생략할 수 있다.
        - **따라서 리듀싱 과정의 최종 결과로 누적자 객체를 바로 사용할 수 있다.**
    - 또한 누적자 A를 결과 R로 안전하게 형변환할 수 있다.

<br/>

e.g., `ToListCollector`

- 스트림의 요소를 누적하는 데 사용한 리스트가 최종 결과 형식이기에 추가 변환이 필요 없으므로 `IDENTITY_FINISH`이다.
- 리스트의 순서는 상관이 없으므로 `UNORDERED`이다.
- `CONCURRENT`
    - 하지만 요소의 순서가 무의미한 데이터 소스여야 병렬로 실행할 수 있다.

<br/>
<br/>

## 6.5.2 응용하기

위의 다섯 가지 메서드를 이용해서 자신만의 커스텀 `ToListCollector`를 구현할 수 있다.

<br/>

```java
public class ToListCollector<T> implements Collector<T, List<T>, List<T>> {

    @Override
    public Supplier<List<T>> supplier() {
        return ArrayList::new; // 수집 연산의 시작점
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return List::add; // 탐색한 항목을 누적하고 바로 누적자를 고친다.
    }

    @Override
    public Function<List<T>, List<T>> finisher() {
        return Function.identity(); // 항등 함수
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        return (list1, list2) -> { // 두 번째 콘텐츠와 합쳐서 첫 번째 누적자를 고친다.
            list1.addAll(list2); // 변경된 첫 번째 누적자를 반환한다.
            return list1;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(IDENTITY_FINISH, CONCURRENT));
    }

}
```

<br/>

자바에서 제공하는 API 대신, 우리가 만든 컬렉터를 메뉴 스트림의 모든 메뉴 요리를 수집하는 예제에 사용할 수 있다.

```java
// 기존 코드
List<Dish> dishes = menuStream.collect(toList()); // toList는 팩토리

// ToListCollector는 new로 인스턴스화한다.
List<Dish> dishes = menuStream.collect(new ToListCollector<Dish>());
```
