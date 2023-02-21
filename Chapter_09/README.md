# Chapter 9. 리팩터링, 테스팅, 디버깅

## 9.1 가독성과 유연성을 개선하는 리팩터링

### 9.1.1 코드 가독성 개선

자바 8의 새 기능을 이용해 코드를 간결하고 이해하기 쉽게 만들 수 있다.

세 가지 리팩터링 예제

- 익명 클래스를 람다 표현식으로 리팩터링하기
- 람다 표현식을 메서드 참조로 리팩터링하기
- 명령형 데이터 처리를 스트림으로 리팩터링하기

<br/>

### 9.1.2 익명 클래스를 람다 표현식으로 리팩터링하기

익명 클래스를 람다 표현식으로 바꿔 리팩터링할 수 있다.

그러나 모든 익명 클래스를 람다 표현식으로 바꿀 수 있는 것은 아니다.

1. 익명 클래스에서 사용한 this와 super는 람다 표현식에서 다른 의미를 갖는다.
    - 익명클래스의 this는 자신을 가리키지만 람다에서는 람다를 감싸는 클래스를 가리킨다.
2. 익명 클래스는 감싸고 있는 클래스의 변수를 가릴 수 있다(섀도 변수)
    - 람다는 사용할 수 없다.
3. 익명 클래스를 람다 표현식으로 변경하면 콘텍스트 오버로딩에 따른 모호함이 초래될 수 있다.

e.g.

```java
// 2번 예시
int a = 10;
Runnable r1 = () -> {
		int a = 2; // 람다는 섀도 변수를 사용할 수 없다. 즉, 컴파일 에러
    System.out.println(a);
};
Runnable r2 = new Runnable(){
    public void run(){
		int a = 2; // 정상 동작
    System.out.println(a);
    }
};

// 3번 예시
interface Task {
    public void execute();
}
public static void doSomething(Runnable r){ r.run(); }
public static void doSomething(Task a){ r.execute(); }

// 익명 클래스는 이렇게 명시적으로 전달할 수 있다.
doSomething(new Task() {
    public void execute() {
        System.out.println("Danger danger!!");
    }
});

// 람다 표현식이 Runnable을 구현하는지 Task를 구현하는지 알 수 없어 문제가 발생한다.
doSomething(() -> System.out.println("Danger danger!!"));

// 명시적 형변환으로 해결
doSomething((Task)() -> System.out.println("Danger danger!!"));
```

<br/>

### 9.1.3 람다 표현식을 메서드 참조로 리팩터링하기

e.g.

```java
// 람다
Map<CaloricLevel, List<Dish>> dishesByCaloricLevel =
          menu.stream().collect(groupingBy(dish -> {
                    if (dish.getCalories() <= 400) return CaloricLevel.DIET;
                    else if (dish.getCalories() <= 700) return CaloricLevel.NORMAL;
                    else return CaloricLevel.FAT;
}));

// 메서드 참조
public class Dish{
  ...
	public CaloricLevel getCaloricLevel() {
		if (this.getCalories() <= 400) return CaloricLevel.DIET;
		else if (this.getCalories() <= 700) return CaloricLevel.NORMAL;
		else return CaloricLevel.FAT;
	}
}
Map<CaloricLevel, List<Dish>> dishesByCaloricLevel = menu.stream().collect(groupingBy(Dish::getCaloricLevel));

// sort 메서드 참조
inventory.sort(comparing(Apple::getWeight));

// reducing 연산 메서드 참조
int totalCalories = menu.stream().collect(summingInt(Dish::getCalories));
```

<br/>


### 9.1.4 명령형 데이터 처리를 스트림으로 리팩터링하기

e.g.

```java
// 명령형
List<String> dishNames = new ArrayList<>();
	for(Dish dish: menu) {
    if(dish.getCalories() > 300){
        dishNames.add(dish.getName());
	}
}

// 스트림
menu.parallelStream().filter(d -> d.getCalories() > 300).map(Dish::getName).collect(toList());
```

<br/>


### 9.1.5 코드 유연성 개선

람다 표현식을 이용하여 동작 파라미터를 구현하고, 이는 코드의 유연성이 대폭 개선된다.

<br/>

### 조건부 연기 실행

실제 작업을 처리하는 코드 내부에 제어 흐름문이 복잡하게 얽힌 코드를 볼 수 있다.

e.g.

```java
// logger의 상태가 isLoggable이라는 메서드에 의해 클라이언트 코드로 노출됨
// logger의 상태를 매번 확인해야함
if (logger.isLoggable(Log.FINER)) {
            logger.finer("Problem: " + generateDiagnostic());
}

// 개선 1.
// logger가 활성화되어 있지 않더라도 항상 로깅 메시지를 평가하게됨
logger.log(Level.FINER, "Problem: " + generateDiagnostic());

// 개선 2.
// 람다를 통해 특정 조건에서만 메시지가 생성될 수 있도록 생성과정을 연기
// 자바 8에 추가된 log 메서드의 시그니처
// public void log(Level level, Supplier<String> msgSupplier)
logger.log(Level.FINER, () -> "Problem: " + generateDiagnostic());

// log 메서드 내부
public void log(Level level, Supplier<String> msgSupplier) {
    if(logger.isLoggable(level)){
        log(level, msgSupplier.get());
    }
}
```

<br/>

### 실행 어라운드

매번 같은 준비, 종료 과정을 반복적으로 수행하는 코드가 있다면 이를 람다로 변환할 수 있다.

e.g.

```java
String oneLine = processFile((BufferedReader b) -> b.readLine());
String twoLines = processFile((BufferedReader b) -> b.readLine() + b.readLine());

public static String processFile(BufferedReaderProcessor p) throws IOException {
    try(BufferedReader br = new BufferedReader(new FileReader("ModernJavaInAction/chap9/data.txt"))) {
        return p.process(br);
    }
}

public interface BufferedReaderProcessor {
    String process(BufferedReader b) throws IOException;
}
```

<br/>

## 9.2 람다로 객체지향 디자인 패턴 리팩터링하기

### 9.2.1 전략

전략 패턴은 한 유형의 알고리즘을 보유한 상태에서 런타임에 적절한 알고리즘을 선택하는 기법이다.

전략 디자인 패턴은 알고리즘을 나타내는 인터페이스, 다양한 알고리즘을 나타내는 인터페이스의 구현, 전략 객체를 사용하는 클라이언트로 구성된다.

여기서 인터페이스가 함수형 인터페이스라면, 당연히 인터페이스의 구현 없이 람다도 사용될 수 있다.

e.g.

```java
// 전략 인터페이스, 함수형 인터페이스이다.
public interface ValidationStrategy {
	boolean execute(String s);
}

// 전략을 구현하는 구현 객체
public class IsAllLowerCase implements ValidationStrategy {
	public boolean execute(String s){
		return s.matches("[a-z]+");
	}
}

// 클라이언트 전략을 생성자로 부터 받아와 이용한다.
public class Validator {
  private final ValidationStrategy strategy;
  public Validator(ValidationStrategy v) {
		this.strategy = v;
	}
	public boolean validate(String s) {
		return strategy.execute(s);
	} 
}
Validator lowerCaseValidator = new Validator(new IsAllLowerCase());

// 람다 사용
Validator numericValidator = new Validator((String s) -> s.matches("[a-z]+"));
```

<br/>

### 9.2.2 템플릿 메서드

알고리즘의 개요를 제시한 다음에 알고리즘의 일부를 고칠 수 있는 유연함을 제공해야 할 때 템플릿 메서드 디자인 패턴을 이용한다.

템플릿 메서드에서도 람다를 이용해서 문제를 해결할 수 있다.

```java
// processCustomer가 알고리즘 개요를 제시
// OnlinBanking을 상속받는 구현체가 makeCustomerHappy 메서드를 구현하여 알고리즘을 구현
abstract class OnlineBanking {
	public void processCustomer(int id){
		Customer c = Database.getCustomerWithId(id);
		makeCustomerHappy(c);
	}
	abstract void makeCustomerHappy(Customer c);
}

// 람다 표현식 사용
// makeCustomerHappy와 시그니처가 일치하도록 인자 추가
public void processCustomer(int id, Consumer<Customer> makeCustomerHappy) {
	Customer c = Database.getCustomerWithId(id);
	makeCustomerHappy.accept(c);
}

// 람다로 알고리즘 전달
new OnlineBankingLambda().processCustomer(1337, (Customer c) -> System.out.println("Hello " + c.getName());
```

<br/>

### 9.2.3 옵저버

어떤 이벤트가 발생했을 때 한 객체가 다른 객체 리스트에 자동으로 알림을 보내는 상황에서 옵저버 디자인 패턴을 사용한다.

옵저버가 함수형 인터페이스일 때 람다를 사용하여 옵저버를 전달할 수 있다.

그러나 옵저버가 상태를 가지며 여러 메서드를 정의해야 한다면 기존의 클래스로 옵저버를 전달하는 방식이 바람직하다.

<br/>

**주제 객체**

```java
// 구성하고 있는 옵저버 리스트에 옵저버를 등록하고 옵저버에 notify할 수 있는 메서드를 가져야한다.
interface Subject {
	void registerObserver(Observer o);
	void notifyObservers(String tweet);
}

// 옵저버 리스트를 구성하고 있다.
class Feed implements Subject {
	private final List<Observer> observers = new ArrayList<>();
  public void registerObserver(Observer o) {
		this.observers.add(o);
  }
  public void notifyObservers(String tweet) {
		observers.forEach(o -> o.notify(tweet));
	}
}

```

<br/>

**옵저버**

```java
// 알림을 받을 notify 메서드를 가져야한다.
interface Observer {
    void notify(String tweet);
}

// 여러 옵저버들을 구현할 수 있다.
// 주제 객체가 notify하면 옵저버들의 notify 함수가 불려 알림을 준다.
class NYTimes implements Observer {
    public void notify(String tweet) {
        if(tweet != null && tweet.contains("money")){
            System.out.println("Breaking news in NY! " + tweet);
		}
}
class Guardian implements Observer {
    public void notify(String tweet) {
        if(tweet != null && tweet.contains("queen")){
            System.out.println("Yet more news from London... " + tweet);
        }
		} 
}

// 옵저버들을 등록하고, 알림을 준다.
Feed f = new Feed();
f.registerObserver(new NYTimes());
f.registerObserver(new Guardian());
f.registerObserver(new LeMonde());
f.notifyObservers("The queen said her favourite book is Modern Java in Action!");
```

<br/>

**람다**

```java
// Observer가 함수형 인터페이스이므로 람다를 통해 옵저버의 notify가 불리면 행해질 일을 전달할 수 있다.
// Observer notify 시그니처에 맞게 전달한다.
f.registerObserver((String tweet) -> {
        if(tweet != null && tweet.contains("money")){
            System.out.println("Breaking news in NY! " + tweet);
        }
});
f.registerObserver((String tweet) -> {
        if(tweet != null && tweet.contains("queen")){
            System.out.println("Yet more news from London... " + tweet);
}
});
```

<br/>

### 9.2.4 의무 체인

작업 처리 객체의 체인을 만들 때는 의무 체인 패턴을 사용한다.

한 객체가 어떤 작업을 처리한 다음에 다른 객체로 결과를 전달하고, 다른 객체도 해야 할 작업을 처리한 다음에 또 다른 객체로 전달하는 식이다.


<p align="center"><img width="300" alt="의무 체인" src="https://user-images.githubusercontent.com/76640167/219341024-6319ec48-d437-46cf-88eb-05a42bea9b09.png">

e.g.

```java
// 템플릿 메서드가 이용됨
// 전체적인 작업의 개요를 handle 메서드가 기술하고, 구체적인 알고리즘은 구현하는 객체가 handleWork를 구현하여 책임진다.
// 구성하고 있는 successor 객체에 결과를 전달하며 호출
public abstract class ProcessingObject<T> {
	protected ProcessingObject<T> successor;
	public void setSuccessor(ProcessingObject<T> successor){
		this.successor = successor;
	}
	
	public T handle(T input) {
		T r = handleWork(input);
		if(successor != null){
			return successor.handle(r);
		}
		return r; 
	}
	abstract protected T handleWork(T input);
}

public class HeaderTextProcessing extends ProcessingObject<String> {
    public String handleWork(String text) {
        return "From Raoul, Mario and Alan: " + text;
    }
}
public class SpellCheckerProcessing extends ProcessingObject<String> {
    public String handleWork(String text) {
        return text.replaceAll("labda", "lambda");
	}
}

ProcessingObject<String> p1 = new HeaderTextProcessing();
ProcessingObject<String> p2 = new SpellCheckerProcessing();
// 두 객체를 연결
p1.setSuccessor(p2); 
// p1.handle(r) -> r1 -> p2.handle(r1) -> r2
String result = p1.handle("Aren't labdas really sexy?!!");
```

<br/>

**람다**

```java
// 함수 조합 이용
// 작업 처리 객체를 UnaryOperator<String> 형식의 인스턴스로 표현
// andThen 메서드로 함수를 조합해서 체인을 만들 수 있다.
UnaryOperator<String> headerProcessing = (String text) -> "From Raoul, Mario and Alan: " + text;
UnaryOperator<String> spellCheckerProcessing = (String text) -> text.replaceAll("labda", "lambda");
Function<String, String> pipeline = headerProcessing.andThen(spellCheckerProcessing);
String result = pipeline.apply("Aren't labdas really sexy?!!");
```

<br/>

### 9.2.5 팩토리

인스턴스화 로직을 클라이언트에 노출하지 않고 객체를 만들 때 팩토리 디자인 패턴을 사용한다.

e.g.

```java
public class ProductFactory {
    public static Product createProduct(String name) {
        switch(name){
					case "loan": return new Loan();
					case "stock": return new Stock();
					case "bond": return new Bond();
					default: throw new RuntimeException("No such product " + name);
				}
		}
}

// 람다 사용

// 다음과 같이 Supplier로 새로운 객체를 생성할 수 있다.
Supplier<Product> loanSupplier = Loan::new;
Loan loan = loanSupplier.get();

final static Map<String, Supplier<Product>> map = new HashMap<>();
static {
    map.put("loan", Loan::new);
    map.put("stock", Stock::new);
    map.put("bond", Bond::new);
}

public static Product createProduct(String name){
    Supplier<Product> p = map.get(name);
    if(p != null) return p.get();
    throw new IllegalArgumentException("No such product " + name);
}

// 생성자로 여러 인수가 필요할 때
// TriFunction 등의 새로운 함수형 인터페이스를 만들어야 한다.
// 세 인수가 필요하다고 가정
public interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);
}
Map<String, TriFunction<Integer, Integer, String, Product>> map = new HashMap<>();
```

<br/>

## 9.3 람다 테스팅

### 9.3.1 보이는 람다 표현식의 동작 테스팅

일반 메서드에 반해 람다는 익명이므로 테스트 코드 이름을 호출할 수 없다.

즉, 필요하다면 람다를 필드에 저장해서 재사용할 수 있으며 람다의 로직을 테스트할 수 있다.

e.g.

```java
// 결국 람다는 함수형 인터페이스의 인스턴스를 생성한다.
public class Point {
	public final static Comparator<Point> compareByXAndThenY = comparing(Point::getX).thenComparing(Point::getY);
  
	...
}

@Test
public void testComparingTwoPoints() throws Exception {
	Point p1 = new Point(10, 15);
	Point p2 = new Point(10, 20);
	int result = Point.compareByXAndThenY.compare(p1, p2);
	assertTrue(result < 0);
}
```

<br/>

### 9.3.2 람다를 사용하는 메서드의 동작에 집중하라

람다의 목표는 정해진 동작을 다른 메서드에서 사용할 수 있도록 하나의 조각으로 캡슐화하는 것이다.

람다 표현식을 사용하는 메서드의 동작을 테스트함으로써 람다를 공개하지 않으면서도 람다 표현식을 검증할 수 있다.

```java
public static List<Point> moveAllPointsRightBy(List<Point> points, int x) {
            return points.stream()
                         .map(p -> new Point(p.getX() + x, p.getY()))
                         .collect(toList());
}

// Point의 equals를 적절하게 다시 구현해야한다.
@Test
public void testMoveAllPointsRightBy() throws Exception {
	List<Point> points = Arrays.asList(new Point(5, 5), new Point(10, 5));
	List<Point> expectedPoints = Arrays.asList(new Point(15, 5), new Point(20, 5));
	List<Point> newPoints = Point.moveAllPointsRightBy(points, 10);
	assertEquals(expectedPoints, newPoints);
}
```

<br/>

### 9.3.3 복잡한 람다를 개별 메서드로 분할하기

람다 표현식을 참조할 수 없는데, 복잡한 람다 표현식을 어떻게 테스트할 수 있을까?

한가지 해결책은 람다 표현식을 메서드 참조로 변경하는 것이다.

그러면 일반 메서드를 테스트하듯이 람다 표현식을 테스트할 수 있다.

<br/>

### 9.3.4 고차원 함수 테스팅

함수를 인수로 받거나 다른 함수를 반환하는 메서드를 고차원 함수라고 한다.

메서드가 람다를 인수로 받는다면 다른 람다로 메서드의 동작을 테스트할 수 있다.

메서드가 다른 함수를 반환한다면 함수형 인터페이스의 인스턴스로 간주하고 함수의 동작을 테스트할 수 있다.

e.g.

```java
@Test
public void testFilter() throws Exception {
	List<Integer> numbers = Arrays.asList(1, 2, 3, 4);
	List<Integer> even = filter(numbers, i -> i % 2 == 0);
	List<Integer> smallerThanThree = filter(numbers, i -> i < 3);
	assertEquals(Arrays.asList(2, 4), even);
	assertEquals(Arrays.asList(1, 2), smallerThanThree);
}
```

<br/>

## 9.4 디버깅

디버깅할 때 개발자는 다음 두 가지를 가장 먼저 확인해야한다.

- 스택 트레이스
- 로깅

<br/>

### 9.4.1 스택 트레이스 확인

스택 프레임에서 프로그램이 어디에서 멈췄고 어떻게 멈추게 되었는지 확인할 수 있다.

<br/>

**스택 프래임에 저장되는 정보**

- 프로그램에서의 메서드 호출 위치
- 호출할 때의 인수값
- 호출된 메서드의 지역 변수
- 등등을 포함한 호출 정보

프로그램이 멈췄다면 어떻게 멈추었는지 프레임별로 보여주는 스택 트레이스를 얻을 수 있다. 즉, 문제가 발생한 지점의 메서드 호출 리스트를 얻을 수 있다.

```java
import java.util.*;
	public class Debugging{
	public static void main(String[] args) {
		List<Point> points = Arrays.asList(new Point(12, 2), null); // null값을 리스트에 넣음
		points.stream().map(p -> p.getX()).forEach(System.out::println); //에러 발생
	}
}
```

```
// 에러 메시지
Exception in thread "main" java.lang.NullPointerException
    at Debugging.lambda$main$0(Debugging.java:6)
    at Debugging$$Lambda$5/284720968.apply(Unknown Source)
    at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:193)
		at java.util.Spliterators$ArraySpliterator.forEachRemaining(Spliterators.java:948)

```

위의 이해하기 힘든 람다 관련 에러는 람다 표현식 내부에서 에러가 발생했음을 가리킨다.

람다 표현식은 이름이 없으므로 컴파일러가 람다를 참조하는 이름 `lambda$main$0`을 만들어낸 것이다.

클래스에 여러 람다가 존재한다면 문제가 될 수 있다.

<br/>

메서드 참조를 이용해도 마찬가지다.

```java
points.stream().map(Point::getX).forEach(System.out::println);
```

```
// 에러 메시지
Exception in thread "main" java.lang.NullPointerException
    at Debugging$$Lambda$5/284720968.apply(Unknown Source)
    at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline
.java:193)
```

<br/>

그러나 메서드 참조를 사용하는 클래스와 같은 곳에 선언되어 있는 메서드를 참조할 대는 메서드 참조 이름이 스택트레이스에 나타난다.

```java
import java.util.*;
public class Debugging{
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3);
        numbers.stream().map(Debugging::divideByZero).forEach(System
     .out::println);
    }
    public static int divideByZero(int n){
        return n / 0;
}
}
```

```
// 에러 메시지
Exception in thread "main" java.lang.ArithmeticException: / by zero
    at Debugging.divideByZero(Debugging.java:10)
    at Debugging$$Lambda$1/999966131.apply(Unknown Source)
    at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:193)
```

<br/>

### 9.4.2 정보 로깅

스트림의 파이프라인 연산을 로깅할 때 스트림 파이프라인에 적용된 각각의 연산이 어떤 결과를 도출하는지 peek이라는 스트림 연산을 활용할 수 있다.

forEach로도 출력 혹은 로깅할 수 있으나 forEach는 전체 스트림이 소비되어 버린다.

그러나 peek은 소비한 것처럼 동작하나 실제로 소비하지는 않는다.

```java
List<Integer> result =
  numbers.stream()
         .peek(x -> System.out.println("from stream: " + x))
         .map(x -> x + 17)
         .peek(x -> System.out.println("after map: " + x))
         .filter(x -> x % 2 == 0)
         .peek(x -> System.out.println("after filter: " + x))
         .limit(3)
         .peek(x -> System.out.println("after limit: " + x))
				 .collect(toList());
```

```
from stream: 2
after map: 19
from stream: 3
after map: 20
after filter: 20
after limit: 20
from stream: 4
after map: 21
from stream: 5
after map: 22
after filter: 22
after limit: 22
```