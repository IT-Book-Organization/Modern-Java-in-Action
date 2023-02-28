# Chapter8. 컬렉션 API 개선

## 8.1 컬렉션 팩토리

자바 8 버전에서는 `Arrays.asList()` 메서드를 이용하여 리스트를 만들 수 있다.

e.g.

```java
List<String> abc = Arrays.asList("abc");
abc.add("ds");
```

<br/>

### UnsupportedOperationException 예외 발생

내부적으로 고정된 크기의 변환할 수 있는 배열로 구현되어있기 때문에 위처럼 만든 List는 요소를 갱신하는 작업은 괜찮지만, 요소를 추가하거나 삭제하는 작업을 할 수 없다.

<br/>

### 8.1.1 리스트 팩토리

자바 9에서는 List.of 팩토리 메서드를 이용해서 간단하게 리스트를 만들 수 있다.

그러나 이렇게 만들어진 리스트는 `add()`를 사용하면 `UnsupportedOperationException` 예외가 발생하며, `set()` 메서드로 아이템을 바꾸려해도 비슷한 예외가 발생한다.

이런 제약은 의도치 않게 컬렉션이 변하는 것을 막아 꼭 나쁘지만은 않다.

리스트를 바꿔야하는 상황에서는 직접 리스트를 만들면 된다.

<br/>

### 오버로딩 vs 가변 인수

List 인터페이스를 보면 List.of의 다양한 오버로드 버전이 있다. (Set.of와 Map.of에서도 이와 같은 패턴이 등장함)

```java
static <E> List<E> of(E e1, E e2, E e3, E e4)
static <E> List<E> of(E e1, E e2, E e3, E e4, E e5)
```

<br/>

왜 다음처럼 다중 요소를 받도록 만들지 않았을까?

```java
static <E> List<E> of(E... e)
```

내부적으로 가변 인수는 추가 배열을 할당해서 리스트로 감싼다.

따라서 배열을 할당하고 초기화하며 가비지 컬렉션을 하는 비용을 지불해야 한다.

10개 이상의 요소에 대해서는 물론 가변 인수를 이용하도록 구현되어 있다.

<br/>

### 8.1.2 집합 팩토리

`List.of` 메서드와 같은 방식으로 `Set.of` 메서드를 이용해 집합을 만들 수 있다.

그러나 중복된 요소를 포함하여 만들면 예외를 발생시킨다.

<br/>

### 8.1.3 맵 팩토리

맵을 만들기 위해서는 키와 값이 필요하다.

`Map.of` ,`Map.ofEntries` 두가지 방법으로 맵을 초기화할 수 있다.

e.g.

```java
Map<String,Integer> map = Map.of("a",1,"b",2,"c",3);

// entry 메서드는 Map.Entry 객체를 만드는 팩토리 메서드다.
Map.ofEntries(entry("a",1)),
							entry("b",2)),
							entry("c",3)));
```

<br/>

## 8.2 리스트와 집합 처리

자바 8에서는 List, Set 인터페이스에 다음과 같은 메서드를 추가했다.

- removeIf : 프레디케이트를 만족하는 요소를 제거한다.
- replaceAll : 리스트에서 이용할 수 있는 기능으로 `UnaryOperator` 함수를 이용해 요소를 바꾼다.
- sort : List 인터페이스에서 제공하는 기능으로 리스트를 정렬한다.

이 메서드들은 새로운 컬렉션을 만드는 것이 아니라 호출한 컬렉션 자체를 바꾼다.

<br/>

### 8.2.1 removeIf 메서드

트랜잭션 리스트에서 숫자로 시작되는 참조 코드를 가진 트랜잭션을 삭제하는 코드가 있을 때, for-each 루프를 사용하여 트랜잭션 리스트에서 트랜잭션을 remove() 하면 에러를 일으킨다.

```java
for (Transaction transaction : transactions) {
    if(Character.isDigit(transaction.getReferenceCode().charAt(0))) {
    transactions.remove(transaction);
    }
}

// 위 코드의 실질적인 코드
// for-each 루프는 Iterator 객체를 사용한다.
// Iterator의 상태와 transactions의 상태는 서로 동기화되지 않아 오류를 발생시킨다.

for (Iterator<Transaction> iterator = transactions.iterator();
     iterator.hasNext(); ) {
   Transaction transaction = iterator.next();
   if(Character.isDigit(transaction.getReferenceCode().charAt(0))) {
       transactions.remove(transaction);
   }
}

// 해결 1번 Iterator에서 직접 삭제
// 코드가 복잡하다.
for (Iterator<Transaction> iterator = transactions.iterator(); iterator.hasNext();) {
	Transaction transaction = iterator.next();
	if(Character.isDigit(transaction.getReferenceCode().charAt(0))) {
		iterator.remove();
	}
}

// 해결 2번 removeIf 메서드 이용
// Predicate<T> 를 인수로 받는다.
transactions.removeIf(transaction -> Character.isDigit(transaction.getReferenceCode().charAt(0)));
```

<br/>


### 8.2.2 replaceAll 메서드

스트림 API를 이용해 리스트의 각 요소를 바꿔 새로운 객체를 만들어낼 수 있었다.

그러나 우리는 기존의 컬렉션 요소를 바꾸고 싶고, 이를 위해서라면 `removeIf` 해결 1번처럼 `iterator.set()` 을 해야만한다.

replaceAll을 사용해 이를 간단히 바꿀 수 있다.

```java
// a1, b1,c1 -> A1, B1, C1
referenceCodes.stream().map(code -> Character.toUpperCase(code.charAt(0)) + code.substring(1))
							         .collect(Collectors.toList())
							         .forEach(System.out::println);

// 스트림의 map과 비슷하다고 보면 된다.
referenceCodes.replaceAll(code -> Character.toUpperCase(code.charAt(0)) + code.substring(1));
```

<br/>

## 8.3 맵 처리

자바 8에서는 `Map` 인터페이스에 몇 가지 디폴트 메서드를 추가했다.

### 8.3.1 forEach 메서드

맵에서 키와 값을 반복하는 작업을 위해 `forEach` 메서드를 제공한다.

```java
// forEach 메서드 미사용
for(Map.Entry<String, Integer> entry: ageOfFriends.entrySet()) {
	String friend = entry.getKey();
	Integer age = entry.getValue();
  System.out.println(friend + " is " + age + " years old");
}

// forEach 메서드 사용
// BiConsumer를 인수로 받는다.
ageOfFriends.forEach((friend, age) -> System.out.println(friend + " is " + age + " years old"));
```

<br/>

### 8.3.2 정렬 메서드

```java
Map<String, String> favouriteMovies = Map.ofEntries(entry("Raphael", "Star Wars"),entry("Cristina", "Matrix"),entry("Olivia", "James Bond"));

// Entry.comparingByKey(), Entry.comparingByValue()로 정렬할 수 있다.
favouriteMovies.entrySet().stream().sorted(Entry.comparingByKey()).forEachOrdered(System.out::println);
```

<br/>

### HashMap 성능

기존의 맵의 항목은 많은 키가 같은 해시코드를 반환하는 상황이 되면 O(n)의 시간이 걸리는 `LinkedList`로 버킷을 반환해야 하므로 성능이 저하되었다.

최근에는 버킷이 너무 커지면 O(log(n))의 시간이 소요되는 정렬된 트리를 이용해 동적으로 치환해 충돌이 일어나는 요소 반환 성능을 개선했다.

(키가 `Comparable` 형태여야 정렬된 트리를 이용할 수 있다.)

<br/>

### 8.3.3 getOrDefault 메서드

키가 존재하지 않으면 결과가 널이 나오므로 문제가 될 수 있는데 `getOrDefault` 메서드는 이 문제를 해결한다.

```java
Map<String, String> favouriteMovies = Map.ofEntries(entry("Raphael", "Star Wars"), entry("Olivia", "James Bond"));

// James Bond 출력
System.out.println(favouriteMovies.getOrDefault("Olivia", "Matrix"));

// Default인 Matrix 출력
System.out.println(favouriteMovies.getOrDefault("Thibaut", "Matrix"));
```

<br/>

### 8.3.4 계산 패턴

키가 존재하는지 여부에 따라 어떤 동작을 실행하고 결과를 저장해야하는 상황이 필요한 때가 있다.

다음의 3가지 연산이 이런 상황에 도움이 된다.

- `computeIfAbsent` : 제공된 키에 해당하는 값이 없으면 키를 이용해 새 값을 계산하고 맵에 추가한다.
- `computeIfPresent` : 제공된 키가 존재하면 새 값을 계산하고 맵에 추가한다.
- `compute` : 제공된 키로 새 값을 계산하고 맵에 저장한다.

<br/>

e.g., Map을 이용한 캐시 구현

```java
// 캐시
Map<String, byte[]> dataToHash = new HashMap<>();

// 각 라인을 SHA-256의 해시 값으로 계산해서 저장하기 위한 계산 객체
MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

// 키가 없다면 line과 계산된 해시 값이 key,value로 들어감
lines.forEach(line -> dataToHash.computeIfAbsent(line,this::calculateDigest));

// 키의 해시를 계산해서 반환
private byte[] calculateDigest(String key) {
   return messageDigest.digest(key.getBytes(StandardCharsets.UTF_8));
}
```

<br/>

e.g. 키가 존재하지 않으면 값을 반환

```java
// 키가 있었다면 영화 리스트를 반환하여 반환된 리스트에 add
// 키가 없었다면 새로운 리스트에 add
friendsToMovies.computeIfAbsent("Raphael", name -> new ArrayList<>()).add("Star Wars");
```

<br/>

### 8.3.5 삭제 패턴

자바 8에서는 키가 특정한 값과 연관되었을 때만 항목을 제거하는 오버로드 버전 메서드를 제공한다.

```java
// 자바 8 이전
String key = "Raphael";
String value = "Jack Reacher 2";
if (favouriteMovies.containsKey(key) && Objects.equals(favouriteMovies.get(key), value)) {
   favouriteMovies.remove(key);
   return true;
} else {
   return false;
}

// 자바 8 이후
favouriteMovies.remove(key, value);
```

<br/>

### 8.3.6 교체 패턴

맵의 항목을 바꾸는데 사용하는 메서드

- `replaceAll` : BiFunction을 적용한 결과로 각 항목의 값을 교체한다.
- `replace` : 키가 존재하면 맵의 값을 바꾼다. 키가 특정 값으로 매핑되었을 때만 값을 교체하는 오버로드 버전도 있다.

e.g.

```java
Map<String, String> favouriteMovies = new HashMap<>();
favouriteMovies.put("Raphael", "Star Wars"); 
favouriteMovies.put("Olivia", "james bond"); 

favouriteMovies.replaceAll((friend, movie) -> movie.toUpperCase()); 
```

<br/>

### 8.3.7 합침

`putAll` 메서드를 이용하여 두 맵을 합칠 수 있다.

그러나 `putAll`은 중복된 키가 있다면 제대로 동작하지 않는다.

이때는 중복된 키를 어떻게 합칠지 결정하는 `BiFunction`을 인수로 받는 `merge` 메서드를 사용한다.

e.g.

```java
// putAll
Map<String, String> family = Map.ofEntries(entry("Teo", "Star Wars"), entry("Cristina", "James Bond"));
Map<String, String> friends = Map.ofEntries(entry("Raphael", "Star Wars"));

Map<String, String> everyone = new HashMap<>(family);
everyone.putAll(friends);

// merge
// 중복된 키가 있다면 두 값을 연결
// 중복되지 않는다면 즉, everyone의 get(k) 값이 null이라면 k,v 그대로 저장
Map<String, String> everyone = new HashMap<>(family);
friends.forEach((k, v) ->  everyone.merge(k, v, (movie1, movie2) -> movie1 + " & " + movie2));
System.out.println(everyone);

// merge
// 초기화 검사
// 원하는 값이 초기화 되어 있다면 +1
// 초기화 되어있지 않아 null이라면 moviename, 1 저장
moviesToCount.merge(movieName, 1L, (key, count) -> count + 1L);
```

<br/>

## 8.4 개선된 ConcurrentHashMap

ConcurrentHashMap 클래스는 동시성 친화적이며 최신 기술을 반영했다.

내부 자료구조의 특정 부분만 잠궈 동시 추가, 갱신 작업을 허용하여 HashTable 버전에 비해 읽기 쓰기 연산 성능이 월등하다.

### 8.4.1 리듀스와 검색

스트림에서 봤던 것과 비슷한 종류의 3가지 연산을 지원한다.

- `forEach` : 키,값 쌍에 주어진 액션을 실행
- `reduce` : 키,값 쌍에 제공된 리듀스 함수를 이용해 결과를 합침
- `search` : 널이 아닌 값을 반환할 때까지 각 키,값 쌍에 함수를 적용

모든 연산은 (키,값), (키), (값), (엔트리) 에 대해서 연산을 할 수 있도록 각종 메서드를 제공한다. (forEach, forEachKey, forEachValues, forEachEntry 등)

<br/>

이 연산은 `ConcurrentHashMap`의 상태를 잠그지 않고 연산을 수행하여 연산에 제공한 함수는 계산이 진행되는 동안 바뀔 수 있는 값, 객체 등에 의존하지 않아야 한다.

또한, 연산에 병렬성 기준값을 지정해야하는데, 맵의 크기가 기준값보다 작으면 순차적으로 연산을 실행한다.

1로 지정하면 공통 스레드 풀을 이용해 병렬성을 극대화하고, `Long.MAX_VALUE`를 기준값으로 설정하면 한개의 스레드로 연산을 실행한다.

<br/>

e.g.

```java
ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();

// 병렬성 기준값
long parallelismThreshold = 1;
// 최댓값을 찾는다.
Optional<Integer> maxValue = Optional.ofNullable(map.reduceValues(parallelismThreshold, Long::max));
```

<br/>

### 8.4.2 계수

기존의 size 메서드 대신 맵의 매핑 개수를 반환하는 `mappingCount` 메서드를 제공한다.

이를 통해 매핑의 개수가 int의 범위를 넘어서는 이후의 상황을 대처할 수 있게 된다.

<br/>

### 8.4.3 집합뷰

`ConcurrentHashMap`을 집합 뷰로 반환하는 `keySet` 메서드를 제공한다.

맵을 바꾸면 집합도 바뀌고 집합을 바뀌면 맵도 영향을 받는다.

`newKeySet`이라는 새 메서드를 이용해 `ConcurrentHashMap`으로 유지되는 집합을 만들 수도 있다.