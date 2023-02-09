# Chapter3. 람다 표현식

동작 파라미터화를 이용해서 변화하는 요구사항에 효과적으로 대응하는 코드를 구현할 수 있고, 정의한 코드 블록을 다른 메서드로 전달할 수 있다.

익명 클래스로 다양한 동작을 구현할 수 있지만 코드가 깔끔하지 않고, 깔끔하지 않은 코드는 동작 파라미터를 실전에 적용하는 것을 막는다.

깔끔한 코드로 동작을 구현하고 전달하는 자바 8의 새로운 기능인 람다 표현식은 익명 클래스처럼 이름이 없는 함수면서 메서드를 인수로 전달할 수 있다.

<br/>

## 3.1 람다란 무엇인가?


> 💡 **람다 표현식**은 메서드로 전달할 수 있는 익명 함수를 단순화한 것이라고 할 수 있다.

<br/>

### 람다의 특징

- 익명
    - 보통의 메서드와 달리 이름이 없다.
    - 메서드를 따로 정의하지 않아도 되어 구현해야할 코드에 대한 걱정거리가 줄어든다.
- 함수
    - 람다는 메서드처럼 특정 클래스에 종속되지 않으므로 함수라고 부른다.
    - 메서드처럼 파라미터 리스트, 바디, 반환 형식, 가능한 예외 리스트를 포함한다.
- 전달
    - 람다 표현식을 메서드 인수로 전달하거나 변수로 저장할 수 있다.
- 간결성
    - 익명 클래스처럼 많은 자질구레한 코드를 구현해야할 필요가 없다.

람다를 이용하면 동작 파라미터를 이용할 때 익명 클래스 등 판에 박힌 코드를 구현할 필요가 없고, 더 쉽게 구현할 수 있다.

결과적으로 코드가 견결하고 유연해진다.

<br/>

### 람다 형식

```
// 블록 스타일
(파라미터 리스트) -> {람다 바디};

or
// 표현식 스타일
(파라미터 리스트) -> 람다 바디;

e.g.
(Apple a) -> a.getColor() == Color.RED
```

- 파라미터 리스트
    - 함수의 파라미터 값을 전달한다.
- 화살표
    - 람다의 파라미터 리스트와 바디를 구분한다.
- 람다 바디
    - 반환값에 해당하는 표현식이다.
    - 반환값이 없다면 void를 반환한다.
    - 블록 스타일일 경우 (위에서 첫번째) 내부에 return 키워드를 사용하여 반환값을 나타내야한다.
    - 표현식 스타일일 경우 (위에서 두번째) 표현식이 return을 함축하고 있어 표현식의 값이 반환 값이 된다.

<br/>

### 유효한 람다 표현식 예

```
// 표현식 스타일
// String 형식의 파라미터를 갖고,표현식의 값이 int형이므로 int 값을 반환한다.
(String s) -> s.length() 

// 표현식 스타일
// 객체를 파라미터로 갖고, boolean 값을 반환한다.
(Apple a) -> a.getWeight() > 150

// 블록 스타일
// 파라미터 리스트를 받아온다.
// 블록이 반환값을 갖지 않으므로 void를 반환한다.
(int x, int y) -> {
	System.out.println("block : ");
	System.out.println(x + y);
}

// 표현식 스타일
// 파라미터가 없으며 int 42를 반환한다.
() -> 42
```

<br/>

## 3.2 어디에 어떻게 람다를 사용할까?

**함수형 인터페이스라는 문맥에서 람다 표현식을 사용할 수 있다.**

<br/>

### 3.2.1 함수형 인터페이스

2장에서 만들었던 `Predicate<T>` 가 바로 함수형 인터페이스다.

```java
interface Predicate<T> {
    boolean test(T t);
}
```

간단히 말해 함수형 인터페이스는 정확히 하나의 추상 메서드를 지정하는 인터페이스다.

2장에서 살펴본 자바 API의 함수형 인터페이스로 `Comparator`, `Runnable` 등이 있다.

람다 표현식으로 함수형 인터페이스의 추상 메서드 구현을 직접 전달할 수 있으므로 **전체 표현식을 함수형 인터페이스의 인스턴스로 취급**(기술적으로 따지면 **함수형 인터페이스를 구현한 클래스의 인스턴스**)할 수 있다.

<br/>

*참고 : 인터페이스는 디폴트 메서드를 포함할 수 있고, 디폴트 메서드를 포함하고 있다고 하더라도 추상 메서드가 하나라면 함수형 인터페이스이다.*

<br/>

### 3.2.2 함수 디스크립터

함수형 인터페이스의 추상 메서드 시그니처(메서드 이름과 파라미터 리스트의 조합)는 람다 표현식의 시그니처를 가리킨다.

람다 표현식의 시그니처를 서술하는 메서드를 **함수 디스크립터**라고 부른다.

예를 들어, `Runnable` 인터페이스의 유일한 추상 메서드 `run`은 인수와 반환값이 없으므로 `Runnable` 인터페이스는 인수와 반환값이 없는 시그니처로 생각할 수 있다.

함수형 인터페이스 `Runnable` 의 메서드 `run`은 같은 시그니처를 갖는 람다 표현식만을 사용할 수 있다.

<br/>

**람다 표현식은 함수형 인터페이스의 추상 메서드와 같은 시그니처를 갖는다.**

<br/>

e.g.

```java
public void process(Runnable r) {
	r.run();
}

// Runnable 인터페이스와 같은 시그니처를 갖는 람다식이 온다.
process(() -> System.out.println("함수형 인터페이스"));
```

<br/>

### @FunctionalInterface는 무엇인가?

새로운 자바 API를 살펴보면 함수형 인터페이스에 @FunctionalInterface 어노테이션이 추가되어 있다.

이 어노테이션은 함수형 인터페이스임을 가리키는 어노테이션이다.

만약 이 어노테이션을 붙였는데 함수형 인터페이스가 아니라면 즉, 추상 메서드가 하나보다 많다면 에러가 발생한다.

<br/>

## 3.3 람다 활용 : 실행 어라운드 패턴

실제 자원을 처리하는 코드를 설정과 정리 두 과정이 둘러싸는 형태를 갖는다.

쉽게 말하자면, 하나의 로직을 수행할때 첫번째로 초기화/준비 코드가 수행되고 마지막에 정리/마무리 코드가 실행된다.

그리고 그 사이에 실제 자원을 처리하는 코드를 실행하는 것이다.

이렇게 **실행 전 준비와 마무리 작업이 있고, 그 사이에 어떤 자원을 처리하는 작업이 있는 패턴을 실행 어라운드 패턴**이라고 한다.

<br/>

실행 어라운드 패턴을 구현해보자.

```java
public String processFile() throw IOException {
	// try-with-resources 
	// try 문이 끝나면 자원도 반납한다.
	try (
      BufferedReader br =new BufferedReader(new FileReader("test.txt"))) {
				return br.readLine();// 실제 작업을 수행
    }
}
```

실제 자원을 처리하는 코드는 바뀔 수 있지만, 자원을 준비하고 정리하는 코드는 변하지 않으므로 동작 파라미터화를 적용할 수 있다.

<br/>

### 3.3.1 1단계 : **동작 파라미터화**

위 코드에서는 파일의 한줄씩 읽어들인다.

만약 파일을 한번에 두줄을 읽으려면 (실제 작업을 수정하려면) 기존의 설정/정리 과정은 재사용하고 실제 작업을 수행하는 한 줄의 코드만 수정하면 된다.

이제 익숙하겠지만, processFile의 변하는 동작을 파라미터화 시킬 수 있다.

<br/>

processFile 메서드에 동작을 람다로 전달

```java
String result = processFile((BufferedReader br) -> br.readLine() + br.readLine());

```

<br/>

### 3.3.2 2단계 : **함수형 인터페이스**

함수형 인터페이스 자리에 람다를 사용할 수 있으므로 `BufferedReader -> String`과 `IOException`을 던질 수 있는 시그니처와 일치하는 함수형 인터페이스를 만들어야 한다.

```java
@FunctionalInterface
public interface BufferedReaderProcessor {
  String process(BufferedReader b)throws IOException;
}

// 위 함수형 인터페이스를 processFile 메서드의 인수로 전달한다.
public String processFile(BufferedReaderProcessor p)throws IOException {
  ...
}
```

<br/>

### 3.3.3 3단계 : 동작 실행

람다 표현식으로 함수형 인터페이스의 추상 메서드 구현을 직접 전달할 수 있으며, 전달된 코드는 함수형 인터페이스의 인스턴스로 전달된 코드와 같은 방식으로 처리한다.

```java
public String processFile(BufferedReaderProcessor p)throws IOException {
	try (
    BufferedReader br =new BufferedReader(new FileReader("test.txt"))) {
			return p.process(br);
    }
  )
}
```

<br/>

### 3.3.4 4단계 : 람다 전달

이제 람다를 사용해서 다양한 동작을 processFile 메서드로 전달할 수 있다.

```java
String oneLine = processFile((BufferedReader br) -> br.readLine());
String twoLines = processFile((BufferedReader br) -> br.readLine() + br.readLine());

```

<br/>

## 3.4 함수형 인터페이스 사용

다양한 람다 표현식을 사용하려면 공통의 함수 디스크립터를 기술하는 함수형 인터페이스 집합이 필요하다.

자바 8 라이브러리 설계자들은 `java.util.function` 패키지로 여러 가지 새로운 함수형 인터페이스를 제공한다.

<br/>

### 3.4.1 Predicate<T>

`Predicate` 인터페이스는 `test`라는 추상 메서드를 정의하며 `test`는 제너릭 형식 `T`의 객체를 인수로 받아 불리언을 반환한다.

`T` 형식의 객체를 사용하는 불리언 표현식이 필요한 상황에서 `Predicate` 인터페이스를 사용할 수 있다.

<br/>

e.g.

```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);
}

public class Main {
    public static void main(String[] args) {
        List<String> strings = Arrays.asList("모던","","자바","인","","액션");
        Predicate<String> nonEmptyStringPredicate = (String s) -> !s.isEmpty();
        List<String> nonEmpty = filter(strings, nonEmptyStringPredicate);
    }

    public static <T> List<T> filter(List<T> list, Predicate<T> p) {
        List<T> result = new ArrayList<>();
        for (T e : list) {
            if (p.test(e)) {
                result.add(e);
            }
        }
        return result;
    }
}
```

<br/>

### 3.4.2 Consumer<T>

`Consumer` 인터페이스는 제너릭 형식 `T` 객체를 받아서 `void`를 반환하는 `accept` 라는 추상 메서드를 정의한다.

`T` 형식의 객체를 인수로 받아서 어떤 동작을 수행하고 싶을 때 `Consumer` 인터페이스를 사용할 수 있다.

<br/>

e.g.

```java
@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);
}

public class Main {
    public static void main(String[] args) {
        forEach(
                Arrays.asList(1,2,3,4,5),
                (Integer i) -> System.out.println(i)
        );
    }

    public static <T> void forEach(List<T> list, Consumer<T> c) {
        for (T t : list) {
            c.accept(t);
        }
    }
}
```

<br/>

### 3.4.3 Function<T, R>

`Function<T, R>` 인터페이스는 제너릭 형식 T를 인수로 받아서 제너릭 형식 `R` 객체를 반환하는 추상 메서드 `apply`를 정의한다.

입력을 출력으로 매핑하는 람다를 정의할 때 `Function` 인터페이스를 활용할 수 있다.

```java
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);
}

public class Main {
    public static void main(String[] args) {
        List<Integer> integerList = map(
                Arrays.asList("모던", "", "자바", "인", "", "액션"),
                (String s) -> s.length()
        );
    }

    public static <T, R> List<R> map(List<T> list, Function<T, R> f) {
        List<R> result = new ArrayList<>();
        for (T t : list) {
            result.add(f.apply(t));
        }
        return result;
    }
}
```

<br/>

### 기본형 특화

제너릭 파라미터에는 참조형만 사용할 수 있고, 위의 인터페이스들에는 기본형 타입을 사용할 수 없다.

자바에서는 기본형을 참조형으로 변환하는 기능을 제공한다.

이 기능을 **박싱**이라고 하고, 참조형을 기본형으로 반환하는 반대 동작을 **언박싱**이라고 한다.

또, 박싱과 언박싱이 자동으로 이루어지는 **오토박싱**이라는 기능도 제공한다.

하지만, 오토박싱은 비용이 소모된다.

박싱한 값은 기본형을 감사는 래퍼며 힙에 저장된다.

따라서 박싱한 값은 메모리를 더 소비하며 기본형을 가져올 때도 메모리를 탐색하는 과정이 필요하다.

자바 8에서는 기본형을 입출력으로 사용하는 상황에서 오토박싱 동작을 피할 수 있도록 특별한 버전의 함수형 인터페이스를 제공한다.

<br/>

e.g.

```java
@FunctionalInterface
public interface IntPredicate {
    boolean test(int t);
}

// 박싱 되지 않음
IntPredicate evenNumbers = (int i) -> i % 2 == 0;
evenNumbers.test(1000);

// int 형값이 Integer 타입으로 오토박싱 됨
Predicate<Integer> oddNumbers = (Integer i) -> i % 2 != 0;
evenNumbers.test(1000);
```

<br/>

### 예외, 람다, 함수형 인터페이스의 관계

직접 함수형 인터페이스를 만들려고할 때, 확인된 예외를 추상 메서드에 잡을 수 있다.

<br/>

e.g.

```java
@FunctionalInterface
public interface BufferedReaderProcessor {
  String process(BufferedReader b)throws IOException;
}

BufferedReaderProcessor p = (BufferedReader br) -> br.readLine(); 
```

<br/>

그러나 우리는 Function<T, R> 형식의 함수형 인터페이스를 기대하는 API를 사용하고 있고, 직접 함수형 인터페이스를 만들기 어려운 상황이다.

이러한 상황에서는 람다 표현식에서 명시적으로 확인된 예외를 잡을 수 있다.

```java
Function<BufferedReader, String> f = (BufferedReader b) -> {
	try {
		return br.readLine();
	}
	catch(IOException) {
		throw new RuntimeException(e);
	}
}
```

<br/>

## 3.5 형식 검사, 형식 추론, 제약

### 3.5.1 형식 검사

람다가 사용되는 콘텍스트를 이용해서 람다의 형식을 추론할 수 있다.

어떤 콘텍스트(예를 들면 람다가 전달될 메서드 파라미터나 할당되는 변수 등)에서 기대되는 표현식의 형식을 **대상 형식**이라고 부른다.

<br/>

다음 람다 표현식을 사용할 때 실제 어떤 일이 일어나는지 확인해보자.

```java
filter(inventory, a -> a.getWeight() > 150);
```

1. `filter` 메서드(콘텍스트)의 선언을 확인한다.
2. `filter` 메서드는 두번째 파라미터로 `Predicate<Apple>` 형식(대상 형식)을 기대한다.
3. `Predicate<Apple>`은 `test` 추상 메서드 하나만 가진 함수형 인터페이스다.
4. `test` 메서드는 `Apple`을 받아 `boolean`을 반환하는 함수 디스크립터를 묘사한다.
5. `filter` 메서드로 전달된 람다는 이와 같은 요구 사항을 만족해야한다.

<br/>

### 3.5.2 같은 람다, 다른 함수형 인터페이스

대상 형식이라는 특징 때문에 **같은 람다 표현식**이라도 호환되는 추상 메서드를 가진 **다른 함수형 인터페이**스로 사용될 수 있다.

즉, 서로 다른 함수형 인터페이스의 추상 메서드가 같은 대상 형식(파라미터와 반환값)을 갖는다면 같은 람다 표현식이 사용될 수 있다.

<br/>

### 다이아몬드 연산자

다이아몬드 연산자로 콘텍스트에 따른 제네릭 형식을 추론할 수 있다.

```java
List<String> strings = new ArrayList<>();
List<Integer> integers = new ArrayList<>();
```

<br/>

### 특별한 void 호환 규칙

람다의 바디에 일반 표현식이 있으면 void를 반환하는 함수 디스크립터와 호환된다. (물론 파라미터 리스트도 호환되어야 한다.)

예를 들어, List의 add 메서드는 Consumer 콘텍스트가 기대하는 void 대신 boolean을 반환하지만 유효하다.

```java
Consumer<String> b = s -> list.add(s);
```

<br/>

### 3.5.3 형식 추론

자바 컴파일러는 람다 표현식이 사용된 콘텍스트(대상 형식)을 이용해서 람다 표현식과 관련된 함수형 인터페이스를 추론한다.

즉, 대상 형식을 이용해서 함수 디스크립터를 알 수 있으므로 **컴파일러는 람다의 시그니처도 추론할 수 있다.**

<br/>

e.g.

```java
// o1과 o2의 형식을 추론함
Comparator<Apple> = (o1, o2) -> Integer.compare(o1.getWeight(), o2.getWeight());

// o1과 o2의 형식을 추론하지 않음
Comparator<Apple> = (Apple o1, Apple o2) -> Integer.compare(o1.getWeight(), o2.getWeight());
```

상황에 따라 형식을 포함하는 것이 좋을 때도 있고 배제하는 것이 가독성을 향상시킬 때도 있다.

어떤 방법이 좋은지 정해진 규칙은 없다.

<br/>

### 3.5.4 지역 변수 사용

람다 표현식에서는 익명 함수가 하는 것처럼 자유 변수(파라미터가 아닌 외부에서 정의된 변수)를 활용할 수 있다.

이와 같은 동작을 **람다 캡처링**이라고 부른다.

<br/>

### 지역 변수의 제약

람다에서 사용하는 지역변수는 final로 선언되어있어야 하거나 실질적으로 final로 선언된 변수와 같이 사용되어야 한다.

람다는 메서드와 같이 보이지만 실질적으로는 함수형 인터페이스를 구현한 인스턴스이다.

인스턴스 변수는 힙에 저장되는 반면 지역 변수는 스택에 위치한다.

람다에서 지역 변수에 바로 접근할 수 있다는 가정하에 람다가 스레드에서 실행된다면 변수를 할당한 스레드가 사라져서 변수 할당이 해제되었는데도 람다를 실행하는 스레드에서는 해당 변수에 접근하려 할 수 있다.

따라서 자바 구현에서는 원래 변수에 접근을 허용하는 것이 아니라 자유 지역 변수의 복사본을 제공한다.

이 복사본은 값이 바뀌지 않아야 하므로 final 제약이 생긴 것이다.

또한 지역 변수의 제약 때문에 외부 변수를 변화시키는 일반적인 명령형 프로그래밍 패턴에 제동을 걸 수 있다.

<br/>

e.g.

```java
int portNumber = 1337;
Runnable r = () -> System.out.println(portNumber);

// 오류
portNumber = 31337;
```

<br/>

### 클로저

클로저란 함수의 비지역 변수를 자유롭게 참조할 수 있는 함수의 인스턴스를 가리킨다.

예를 들어 클로저를 다른 함수의 인수로 전달할 수 있다.

클로저와 람다는 비슷한 동작을 수행한다.

다만 다른 점은 클로저는 외부에 정의된 변수의 값에 접근하고, 값을 바꿀 수 있지만 위에 설명했듯 람다는 final 변수에만 접근할 수 있으므로 값을 바꿀 수 없다.

덕분에 람다는 변수가 아닌 값에 국한되어 어떤 동작을 수행한다는 사실이 명확해진다.

<br/>

## 3.6 메서드 참조

메서드 참조를 이용하면 기존의 메서드 정의를 재활용해서 람다처럼 전달할 수 있다.

e.g.

```java
inventory.sort((Apple o1,Apple o2) -> o1.getWeight().compareTo(o2.getWeight());

//메서드 참조 이용
inventory.sort(comparing(Apple::getWeight));
```

<br/>

### 3.6.1 요약

메서드 참조는 특정 메서드만을 호출하는 람다의 축양형이라고 생각할 수 있다.

메서드 참조를 이용하면 기존 메서드 구현으로 람다 표현식을 만들 수 있다.

이때 명시적으로 메서드명을 참조함으로써 **가독성을 높일 수 있다.**

메서드 명 앞에 구분자(::)를 붙이는 방식으로 메서드 참조를 활용할 수 있다.

<br/>

e.g.

```java
// 람다 표현식을 메서드 참조형으로 표현
(Apple a) -> a.getWeight()
Apple::getWeight
```

<br/>

### 메서드 참조를 만드는 방법

<p align="center"><img width="400" src="https://user-images.githubusercontent.com/76640167/217036585-dfedc6e9-a392-4d52-b408-a4d6bb9b0c01.png"></p> 

1. 정적 메서드 참조
    - 예를 들어 `Integer`의 `parseInt` 메서드는 `Integer::parseInt`로 표현할 수 있다.
2. 다양한 형식의 인스턴스 메서드 참조
    - 예를 들어 `String`의 `length` 메서드는 `String::length`로 표현할 수 있다.
    - 이를 람다 표현식으로 나타내면 `(String s) -> s.length()` 와 같다.
3. 기존 객체의 인스턴스 메서드 참조
    - 예를 들어 `Transaction` 객체를 할당 받은 `expensiveTransaction` 지역 변수가 있고, `Transaction` 객체에는 `getValue` 메서드가 있다면, 이를 `expensiveTransaction::getValue`라고 표현할 수 있다.
    - 이를 람다 표현식으로 나타내면 `() → expensiveTransaction.getValue()`와 같다.

세번째 유형의 메서드 참조는 비공개 헬퍼 메서드를 정의한 상황에서 유용하게 활용할 수 있다.

컴파일러는 람다 표현식의 형식을 검사하던 방식과 비슷한 과정으로 메서드 참조가 주어진 함수형 인터페이스와 호환되는지 확인한다.

즉, 해당 메서드 참조의 파라미터 리스트와 반환값 타입을 확인해 콘텍스트(함수형 인터페이스)와 일치하여야 한다.

<br/>

e.g.

```java
// 비공개 헬퍼 메서드
private boolean isValidName(String string) {
    return Character.isUpperCase(string.charAt(0));
}

// 메서드 참조
filter(result,this::isValidName)
```

```java
// List에 포함된 문자열을 대소문자를 구분하지 않고 정렬하는 프로그램
// sort 메서드는 인자로 Comparator 인터페이스를 갖는데, 이는 (T,T) -> int 함수 디스크립터를 갖는다.
List<String> strings = Arrays.asList("A","b","a","B");
str.sort((s1,s2) -> s1.compareToIgnoreCase(s2));

// 메서도 참조로 간단히 만든다.
str.sort(String::compareToIgnoreCase);
```

<br/>

### 3.6.2 생성자 참조

Class::new 처럼 클래스명과 new 키워드를 이용해서 기존 생성자의 참조를 만들 수 있다.

1. 인수가 없는 생성자

```java
// 람다 표현식
// Supplier<T>는 ()->T 와 같은 메서드 시그니처를 갖는다.
Supplier<Apple> c1 = () -> new Apple();
Apple apple = c1.get();

// 메서드 참조
Supplier<Apple> c1 = Apple::new;
Apple apple = c1.get();
```

<br/>

2. Apple(Integer weight) 시그니처를 갖는 생성자

```java
// 람다 표현식
// Function<T, R>은 (T) -> R 과 같은 메서드 시그니처를 갖는다.
// 즉 Integer를 인수로 받아 새로운 객체를 생성하는 new Apple(Integer weight)와 시그니처가 일치한다.
Function<Integer, Apple> c2 = (weight) -> new Apple(weight);
Apple apple2 = c2.apply(100);

// 메서드 참조
Function<Integer, Apple> c2 = Apple::new;
Apple apple2 = c2.apply(100); 
```

위 Function 생성자 참조를 통해 간편하게 다양한 무게를 포함하는 사과 리스트를 만들 수 있다.

```java
List<Integer> weights = Arrays.asList(1, 2, 3, 4);
List<Apple> apples = map(weights, Apple::new);

// 전에 만들었던 Funtion을 이용하여 T 리스트를 이용하여 R 리스트를 만들어내는 함수
public static <T, R> List<R> map(List<T> list, Function<T, R> f) {
    List<R> result = new ArrayList<>();
    for (T t : list) {
        result.add(f.apply(t));
    }
    return result;
}
```

<br/>

3. Apple(String color, Integer weight) 시그니처를 갖는 생성자

```java
// 람다 표현식
// BiFunction<T, U, R>은 (T,U) -> R 과 같은 메서드 시그니처를 갖는다.
// 즉 Integer와 Color를 인수로 받아 새로운 객체를 생성하는 new Apple(String color, Integer weight)와 시그니처가 일치한다.
BiFunction<Color ,Integer, Apple> c3 = (color, integer) -> new Apple(color,integer);
Apple apple3 = c3.apply(Color.RED,100);

// 메서드 참조
BiFunction<Color ,Integer, Apple> c3 = Apple::new;
Apple apple3 = c3.apply(Color.RED,100); 
```

<br/>

4. 인스턴스화 하지않고도 생성자에 접근하는 기능

```java
// 생성하는 기능들만 구현
static Map<String , Function<Integer, Fruit>> map = new HashMap<>();
static {
    map.put("apple", Apple::new);
}

// apply 메서드에 정수 파라미터를 제공해서 실제 인스턴스 생성
public static Fruit getFruit(String fruit, Integer weight) {
    return map.get(fruit.toLowerCase()).apply(weight);
}
```

<br/>

## 3.7 람다, 메서드 참조 활용하기

사과 리스트 정렬 문제를 메서드 참조로 최종 변경해보자

목표 코드는 다음과 같다.

```java
inventory.sort(comparing(Apple::getWeight));
```

<br/>

### 3.7.1 1단계 : 코드 전달

sort 메서드는 다음과 같은 함수 시그니처를 갖는다.

```java
void sort(Comparator<? super E> c)
```

정렬 전략이 구현되어있는 Comparator 객체를 인수로 받아 두사과를 비교한다.

<br/>

1단계의 코드는 다음과 같이 완성할 수 있다.

```java
static class AppleComparator implements Comparator<Apple> {
  @Override
  public int compare(Apple a1, Apple a2) {
    return a1.getWeight() - a2.getWeight();
  }
}

inventory.sort(new AppleComparator());
```

<br/>

### 3.7.2 2단계 : 익명 클래스 사용

AppleComparator는 한번만 사용되므로 다음과 같이 익명 클래스로 구현하는 것이 좋다.

```java
inventory.sort(new Comparator<Apple>() {
    @Override
    public int compare(Apple a1, Apple a2) {
      return a1.getWeight() - a2.getWeight();
    }
});
```

### 3.7.3 3단계 : 람다 표현식 사용

Comparator는 함수형 인터페이스이기 때문에 람다 표현식을 사용할 수 있다.

Compartator의 함수 디스크립터는 `<T,T> -> int` 이고, 우리는 사과를 비교할 것이므로 람다 표현식의 시그니처를 `<Apple, Apple> -> int` 로 표현할 수 있다.

```java
inventory.sort((Apple a1, Apple a2) -> a1.getWeight() - a2.getWeight());

// 자바 컴파일러는 형식을 추론한다. 
// 즉, 컴파일러가 inventory 내부 원소의 자료형이 Apple임을 알게되어 a1과 a2의 형식을 지정하지 않아도 됨.
inventory.sort((a1, a2) -> a1.getWeight() - a2.getWeight());
```

<br/>

Comparator는 Comparable 키를 추출해서 Comparator 객체로 만드는 Function 함수를 인수로 받는 정적 메서드 comparing을 포함한다.

```java
//comparing 메소드는 비교하는데 사용될 값을 Function<T,R> 인터페이스로 받아서 Comparator를 반환
Comparator<Apple> c = Comparator.comparing((Apple a) -> a.getWeight());

// c 변수를 생성해 넣어도 되지만 아래처럼 한줄로 간소화한다.
inventory.sort(comparing(apple -> apple.getWeight()));
```

<br/>

### 3.7.4 4단계 : 메서드 참조 사용

람다 표현식을 메서드 참조로 변경할 수 있다.

```java
inventory.sort(comparing(Apple::getWeight));
```

<br/>

## 3.8 람다 표현식을 조합할 수 있는 유용한 메서드

자바 8 API의 함수형 인터페이스는 디폴트 메서드를 이용하여 다양한 유틸리티 메서드를 제공한다.

예를 들어, `Comparator`, `Function`, `Predicate` 같은 함수형 인터페이스는 람다 표현식을 조합할 수 있도록 유틸리티 메서드를 제공한다.

<br/>

### 3.8.1 Comparator 조합

1. 역정렬

`Comparator` 인터페이스는 주어진 비교자의 순서를 뒤바꾸는 `reverse`라는 디폴트 메서드를 제공한다.

```java
inventory.sort(comparing(Apple::getWeight).reversed());
```

<br/>

2. `Comperator` 연결

정렬 시 무게가 같다면 어떻게 해야할까?

이럴 땐 비교 결과를 더 다듬을 수 있는 두번째 `Comparator`를 만들 수 있다.

`theComparing` 메서드로 두번째 비교자를 만들 수 있다.

`theComparing`은 함수를 인수로 받아 첫 번째 비교자를 이용해서 두 객체가 같다고 판단되면 두번째 비교자에 객체를 전달한다.

```java
// 무게가 같으면 색깔별로 정렬
inventory.sort(comparing(Apple::getWeight).reversed().theComparing(Apple::getColor));
```

<br/>

### 3.8.2 Predicate 조합

`Predicate` 인터페이스는 복잡한 프레디케이트를 만들 수 있도록 `negate`, `and`, `or` 세가지 메서드를 제공한다.

```java
Predicate<Apple> redApple = (a) -> a.getColor() == Color.RED;

// 빨간 사과가 아닌 사과
// Predicate 결과에 not을 씌운다.
redApple.negate();

// 빨간 사과이면서 150그램 이상인 사과
redApple.and(apple -> apple.getWeight() > 150);

// 빨간 사과이면서 150그램 이상인 사과 이거나 초록 사과
redApple.and(apple -> apple.getWeight() > 150).or(a ->a.getColor() == Color.GREEN);
```

<br/>

### 3.8.3 Function 조합

`Function` 인터페이스는 `Function` 인스턴스를 반환하는 `andThen`, `compose` 두 가지 디폴트 메서드를 제공한다.

<br/>

andThen 메서드는 주어진 함수를 먼저 적용한 결과를 다른 함수의 입력으로 전달하는 함수를 반환한다.

```java
Function<Integer, Integer> f = x -> x + 1;
Function<Integer, Integer> g = x -> x *2;
Function<Integer, Integer> h = f.andThen(g);
// 결과는 4 즉, g(f(x))가 수행된다.
h.apply(1);
```

<br/>

compose 메서드는 인수로 주어진 함수를 먼저 실행한 다음에 그 결과를 외부 함수의 인수로 제공한다.

```java
Function<Integer, Integer> f = x -> x + 1;
Function<Integer, Integer> g = x -> x *2;
Function<Integer, Integer> h = f.compose(g);
// 결과는 3 즉, f(g(x))가 수행된다.
h.apply(1);
```