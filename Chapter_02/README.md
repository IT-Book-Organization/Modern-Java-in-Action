# 동작 파라미터화 코드 전달하기


> 💡 **동작 파라미터화**란 아직은 어떻게 실행할 것인지 결정하지 않은 코드 블록을 의미한다.


즉, 코드 블록의 실행을 나중으로 미뤄진다.

예를 들어, 나중에 실행될 메서드의 인수로 코드 블록을 전달할 수 있다.

결과적으로 **코드 블록에 따라 메서드의 동작이 파라미터화** 된다.

<br/>

## 2.1 변화하는 요구사항에 대응하기

1장에서 봤던 것과 같이 List<Apple>에서 녹색 사과만 필터링한다고 가정해보자.

<br/>

### 2.1.1 첫번째 시도 : 녹색 사과 필터링

```java
enum Color {
    RED,
    GREEN
}

public static List<Apple> filterGreenApples(List<Apple> inventory) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
			// 필요 조건
      if (apple.getColor() == Color.GREEN) {
        result.add(apple);
      }
    }
    return result;
}
```

그러나 빨간 사과도 필터링하고 싶어졌다고 가정해보자.

`filterRedApples`라는 메서드를 단순히 하나 더 복사 붙여넣기로 만들 수도 있겠지만(비효율적이고 좋지않다.) 더 다양한 색으로 필터링을 원할 때를 대비하려 한다.

<br/>

### 2.1.2 두번째 시도 : 색을 파라미터화

**색을 파라미터화할 수 있도록 메서드에 파라미터를 추가**하면 변화하는 요구사항에 좀 더 우연하게 대응하는 코드를 만들 수 있다.

```java
public static List<Apple> filterApplesByColor(List<Apple> inventory, Color color) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
      if (apple.getColor() == color) {
        result.add(apple);
      }
    }
    return result;
}
```

<br/>

다음으로 색 이외에도 150그램을 기준으로 가벼운 사과와 무거운 사과로 구분할 수 있도록 요구사항이 추가되었다고 해보자.

```java
public static List<Apple> filterApplesByWeight(List<Apple> inventory, int weight) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
      if (apple.getWeight() > weight) {
        result.add(apple);
      }
    }
    return result;
}
```

무게의 기준도 얼마든지 바뀔 수 있기 때문에 위와 같이 무게 정보 파라미터를 통해 유연하게 대응할 수 있다.

<br/>

그러나 색을 통해 필터링하는 코드와 무게를 통해 필터링하는 코드가 대부분 중복된다.

이는 소프트웨어공학의 DRY(don’t repeat yourself, 같은 것을 반복하지 말 것) 원칙을 어긴다.

이렇게 반복하게 된다면 탐색 과정에 변화가 생긴다면 탐색하는 모든 메소드를 찾아 고쳐야만 할 것이다.

<br/>


### 2.1.3 세번째 시도 : 가능한 모든 속성으로 필터링

```java
public static List<Apple> filterApples(List<Apple> inventory, int weight, Color color, boolean flag) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
      if ((flag && apple.getColor() == color) || (!flag && apple.getWeight() > weight)) {
        result.add(apple);
      }
    }
    return result;
  }
```

형편없는 코드다.

flag가 어떤 것을 의미하는 것인지 알 수도 없고, 새로운 요구사항에 유연하게 대응할 수도 없다.

동작 파라미터화를 이용해서 유연성을 얻는 방법을 알아보자.

<br/>

## 2.2 동작 파라미터화

선택 조건은 결국 사과의 어떤 속성에 기초해서 불리언 값을 반환(사과가 녹색인가? 150그램 이상인가?)하는 것이다.

참 또는 거짓을 반환하는 함수를 `프레디케이트`라고한다.

선택 조건을 결정하는 인터페이스를 정의하자.

```java
interface ApplePredicate {
    boolean test(Apple a);
}
```

이제 인터페이스를 상속받아 실제 선택 조건을 구현하는 클래스를 만들 수 있다.

```java
static class AppleWeightPredicate implements ApplePredicate {
    @Override
    public boolean test(Apple apple) {
      return apple.getWeight() > 150;
    }
}
static class AppleColorPredicate implements ApplePredicate {
    @Override
    public boolean test(Apple apple) {
      return apple.getColor() == Color.GREEN;
    }
}
```

즉, 사용하는 구현 클래스에 따라 선택 조건을 달리할 수 있게 되고, 이를 전략 패턴이라고 부른다.

전략 디자인 패턴은 전략이라고 불리는 **알고리즘을 캡슐화하는 알고리즘 패밀리를 정의해둔 다음에 런타임에 알고리즘을 선택**하는 기법이다.

`filterApples` 메서드에서 `ApplePredicate` 객체를 **파라미터로 받아** test 메서드를 사용하도록 해야한다.

이렇게 **동작 파라미터화**, 즉 메서드가 다양한 전략을 받아서 내부적으로 다양한 동작을 수행할 수 있다.

이를 통해 메서드 내부에서 **컬랙션을 반복하는 로직과 컬렉션의 각 요소에 적용할 동작을 분리**할 수 있고, 이는 소프트웨어 엔지니어링적으로 큰 이득이다.

<br/>

### 2.2.1 네번째 시도 : 추상적 조건으로 필터링

```java
public static List<Apple> filter(List<Apple> inventory, ApplePredicate p) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
      if (p.test(apple)) {
        result.add(apple);
      }
    }
    return result;
}

List<Apple> greenApples2 = filter(inventory, new AppleColorPredicate());
```

전달한 `ApplePredicate` 객체에 의해 메서드의 동작이 결정된다.

즉, 우리는 `filterApples` 메서드의 동작을 파라미터화한 것이다.

메서드는 객체만 인수로 받으므로 `test` 메서드를 `ApplePredicate` 객체로 감싸서 전달해야 한다.

`test` 메서드를 구현하는 객체를 이용해서 불리언 표현식 등을 전달할 수 있으므로 이는 ‘코드를 전달’할 수 있는 것이나 다름 없다.

이제 한 메서드가 다른 동작을 수행하도록 재활용할 수 있고, 따라서 유연한 API를 만들 때 동작 파라미터화가 중요한 역할을 한다.

<br/>

## 2.3 복잡한 과정 간소화

메서드에 새로운 동작을 전달하려면 인터페이스를 만들고, 구현하는 여러 클래스를 정의한 다음 인스턴스화해야하고 이는 상당히 번거로운 작업이다.

<br/>

### 2.3.1 익명 클래스

**익명 클래스**는 자바의 지역 클래스와 비슷한 개념으로 말 그대로 이름이 없는 클래스다.

**익명 클래스**를 이용하면 클르새 선언과 인스턴스화를 동시에 할 수 있다. 즉, 필요한 구현을 즉석에서 만들어서 사용할 수 있다.

<br/>

### 2.3.2 다섯 번째 시도 : 익명 클래스

```java
List<Apple> redApples2 = filter(inventory, new ApplePredicate() {
      @Override
      public boolean test(Apple a) {
        return a.getColor() == Color.RED;
      }
});
```

많은 코드를 줄였고 인터페이스를 구현하는 여러 클래스를 선언하는 과정을 줄였지만, 익명 클래스의 단점도 있다.

- 여전히 많은 공간을 차지한다.
- 많은 프로그래머가 익명 클래스의 사용에 익숙하지 않다.

익명 클래스로 인한 코드의 장황함은 나쁜 특성으로 구현하고 유지보수하는 데 시간이 오래 걸린다.

또한, 여전히 코드 조각을 전달하는 과정에서 결국은 객체를 만들고 명시적으로 새로운 동작을 정의하는 메서드를 구현해야 한다.

<br/>

### 2.3.3 여섯 번째 시도 : 람다 표현식 사용

(람다 표현식에 관해서는 3장에서 더 자세히 다룬다.)

```java
List<Apple> redApples2 = filter(inventory, a -> a.getColor() == Color.RED);
```

런타임에 기능을 전달하는 유연함은 그대로 가져가면서 코드가 훨씬 간결해졌다.

<br/>

### 2.3.4 일곱 번째 시도 : 리스트 형식으로 추상화

```java
public interface Predicate<T> {
    boolean test(T t);
}

// 형식 파라미터 T의 등장
public <T> List<T> filter(List<T> list, Predicate<T> p) {
    List<T> result = new ArrayList<>();
    for (T e : list) {
        if (p.test(e)) {
            result.add(e);
        }
    }
    return result;
}

filter(numbers, (Integer i) -> i%2 == 0);
```

이제 사과 리스트뿐만 아니라 정수 리스트, 문자열 리스트 등에 모두 필터 메서드를 사용할 수 있다.

<br/>

## 2.4 실전 예제

### 2.4.1 Comparator로 정렬하기

컬렉션 정렬은 반복되는 프로그래밍 작업이다.

개발자에게는 변화하는 요구사항에 쉽게 대응할 수 있는 다양한 정렬 동작을 수행할 수 있는 코드가 필요하다.

자바 8의 `List`는 `sort` 메서드를 포함하고 있다.

다음과 같은 인터페이스를 갖는 `Comparator` 객체를 이용해서 `sort`의 동작을 파라미터화할 수 있다.

즉, `Comparator`를 구현해서 `sort`의 메서드의 동작을 다양화할 수 있다.

```java
public interface Comparator<T> {
	int compare(T o1, T o2);
}
```

<br/>

예를 들어 사과의 무게가 적은 순으로 정렬해보자.

```java
inventory.sort(new Comparator<Apple>() {
  @Override
  public int compare(Apple o1, Apple o2) {
    return Integer.compare(o1.getWeight(), o2.getWeight());
  }
});
```

정렬 요구사항이 바뀌더라도 새로운 `Comparator`를 만들어 전달하면 되고, 정렬 세부사항은 추상화되어 있으므로 신경쓰지 않아도 된다.

이를 람다 표현식으로 이용하여 표현하면 다음과 같다.

```java
inventory.sort((o1, o2) -> Integer.compare(o1.getWeight(), o2.getWeight()));
```

<br/>

### 2.4.2 Runnable로 코드 블록 실행하기

자바 스레드를 이용하면 병렬로 코드 블록을 실행할 수 있다.

어떤 코드를 실행할 것인지 스레드에게 알려주어야 하고, 여러 스레드는 각자 다른 코드를 실행할 수 있어야 한다.

나중에 실행할 수 있는 코드를 구현할 방법이 필요하다.

자바 8까지는 `Thread` 생성자에 객체만을 전달할 수 있었으므로 보통 결과를 반환하지 않는 `run` 메소드를 포함하는 **익명 클래스가 `Runnable` 인터페이스를 구현**하도록 하는 것이 일반적인 방법이었다.

```java
Thread t = new Thread(new Runnable() {
   @Override
   public void run() {
     System.out.println("Hello world");
   }
});
```

자바 8에서 지원하는 람다 표현식을 이용하면 다음처럼 구현할 수 있다.

```java
Thread t = new Thread(() -> System.out.println("Hello world"));
```

<br/>

### 2.4.3 Callable을 결과로 반환하기

`ExecutorService`를 이용하면 태스크를 스레드 풀로 보내고 결과를 `Future`로 저장할 수 있다.

`ExecutorService` 인터페이스는 태스크 제출과 실행 과정의 연관성을 끊어준다.

여기서 `Callable` 인터페이스를 이용해 결과를 반환하는 태스크를 만든다.

```java
public interface Callable<V> {
	V call();
}
```

실행 서비스에 동작 파라미터한 태스크를 제출해서 `Callable`을 활용할 수 있다.

```java
ExecutorService executorService = Executors.newCachedThreadPool();
  Future<String> threadName = executorService.submit(new Callable<String>() {
  @Override
  public String call() throws Exception {
    return Thread.currentThread().getName();
  }
});
```

람다를 이용하면 다음과 같다.

```java
Future<String> threadName = executorService.submit(() -> Thread.currentThread().getName());
```

<br/>

### 2.4.4 GUI 이벤트 처리하기

모든 동작에 반응할 수 있어야 하기 때문에 GUI 프로그래밍에서도 변화에 대응할 수 있는 유연한 코드가 필요하다.

`addActionListener` 메서드에 `ActionEvent` 인터페이스를 전달하여 이벤트에 어떻게 반응할지 설정할 수 있다.

즉, `ActionEvent` 가 `addActionListener` 메서드의 동작을 파라미터화한다.

```java
// GUI
Button button = new Button("Send");
button.addActionListener(new ActionListener() {
   @Override
   public void actionPerformed(ActionEvent e) {
     button.setLabel("Sent!!");
   }
});

button.addActionListener(e -> button.setLabel("Sent!!"));
```