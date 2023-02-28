# Chapter 19. 함수형 프로그래밍 기법

<br/>

# 19.1 함수는 모든 곳에 존재한다.

`함수형 언어` 프로그래머는 함수나 메서드가 수학의 함수처럼 부작용 없이 동작함을 의미하는 `함수형 프로그래밍`이라는 용어를 좀 더 폭넓게 사용한다.

즉, 함수를 마치 일반값처럼 사용해서 인수로 전달하거나, 결과로 반환받거나, 자료구조에 저장할 수 있음을 의미한다.

<br/>

일반값처럼 취급할 수 있는 함수를 `일급 함수(first-class function)`이라고 한다.

**메서드 참조**를 만들거나, **람다 표현식**으로 직접 함숫값을 표현해서 메서드를 함숫값으로 사용할 수 있다.

```java
Function<String, Integer> strToInt = Integer::parseInt;
```

<br/>

#### [ 참고 ] 일급 객체

아래 세 가지를 충족하는 객체를 말한다.

1. 변수에 할당할 수 있어야 한다.
2. 객체의 인자로 넘길 수 있어야 한다.
3. 객체의 리턴값으로 리턴할 수 있어야 한다.

<br/>
<br/>

## 19.1.1 고차원 함수

하나 이상의 동작을 수행하는 함수를 `고차원 함수(highter-order functions)`라 부른다.

- 하나 이상의 함수를 인수로 받음
- 함수를 결과로 반환

<br/>

자바 8에서는 함수를 인수로 전달할 수 있을 뿐 아니라 결과로 반환하고,  
지역 변수로 할당하거나, 구조체로 삽입할 수 있으므로 자바 8의 함수도 고차원 함수라고 할 수 있다.

<br/>

e.g., `Comparator.comparing` : 함수를 인수로 받아 다른 함수를 반환한다.

```java
Comparator<Apple> c = comparing(Apple::getWeight);
```

<br/>

<p align="center"><img width="400" alt="comparing" src="https://user-images.githubusercontent.com/86337233/221182412-30cf1733-0979-4399-bdfc-f97788f3ffe5.png">

<br/>
<br/>

### 부작용과 고차원 함수

고차원 함수나 메서드를 구현할 때 어떤 인수가 전달될지 알 수 없으므로 **인수가 부작용을 포함할 가능성**을 염두에 두어야 한다.

부작용을 포함하는 함수를 사용하면 부정확한 결과가 발생하거나 race condition 때문에 예상치 못한 결과가 발생할 수 있다.

함수를 인수로 받아 사용하면서 코드가 정확히 어떤 작업을 수행하고 프로그램의 상태를 어떻게 바꿀지 예측하기 어려워지며 디버깅도 어려워진다.

따라서 인수로 전달된 함수가 어떤 부작용을 포함하게 될지 정확하게 문서화하는 것이 좋다.

<br/>
<br/>

## 19.1.2 커링

`커링(currying)`은 함수를 모듈화하고 코드를 재사용하는 데 도움을 준다.

<br/>

커링은 x와 y라는 두 인수를 받는 `함수 f`를 `한 개의 인수를 받는 g라는 함수로 대체`하는 기법이다.

이때 g라는 함수 역시 하나의 인수를 받는 함수를 반환한다.

함수 g와 원래 함수 f가 최종적으로 반환하는 값은 같다. 즉, f(x, y) = (g(x))(y)가 성립한다.

<br/>

e.g., 국제화 단위 변환 문제

```java
// f: 변환 요소, b: 기준치 조정 요소
static DoubleUnaryOperator curriedConverter(double f, double b) {
    return (double x) -> x * f + b;
}

// 위 메서드에 변환 요소(f)와 기준치(b)만 넘겨주면 원하는 작업을 수행할 함수가 반환됨
DoubleUnaryOperator convertCtoF = curriedConverter(9.0/5, 32);
DoubleUnaryOperator convertUSDtoGBP = curriedConverter(0.6, 0);
```

<br/>

이런 방식으로 변환 로직을 재활용할 수 있으며 다양한 변환 요소로 다양한 함수를 만들 수 있다.

<br/>
<br/>
<br/>

# 19.2 영속 자료구조

함수형 프로그램에서 사용하는 자료구조는 함수형 자료구조, 불변 자료구조 또는 `영속(persistent) 자료구조`라고 부른다.

<br/>

함수형 메서드에서는 전역 자료구조나 인수로 전달된 구조를 갱신할 수 없다.

자료구조를 바꾼다면 같은 메서드를 두 번 호출했을 때 결과가 달라지면서 참조 투명성에 위배되고 인수를 결과로 단순하게 매핑할 수 있는 능력이 상실되기 때문이다.

<br/>

## 19.2.1 파괴적인 갱신과 함수형

e.g., A에서 B까지 기차여행을 의미하는 가변 TrainJourney 클래스

```java
class TrainJourney {
    public int price;
    public TrainJourney onward;
    public TrainJourney(int p, TrainJourney t) {
        price = p;
        onward = t;
    }
}
```

두 개의 TrainJourney 객체를 연결해서 하나의 여행을 만들고자 한다.

<br/>

### 파괴적인 append

```java
// 기차여행을 연결(link)
// firstJourney가 secondJourney를 포함하면서 파괴적인 갱신(firstJourney의 변경)이 일어남
static TrainJourney link(TrainJourney a, TrainJourney b) {
    if (a == null) return b;
    TrainJourney t = a;
    while (t.onward != null) {
        t = t.onward;
    }
    t.onward = b;
    return a;
}
```

<br/>

<p align="center"><img width="600" alt="파괴적인 append" src="https://user-images.githubusercontent.com/86337233/221182407-e049305b-2a3f-44a5-95cc-b41148f5b490.png">

<br/>
<br/>

### 함수형 append

계산 결과를 표현할 자료구조가 필요하면 기존의 자료구조를 갱신하지 않도록 새로운 자료구조를 만들어야 한다.

```java
// a가 n 요소(새로운 노드)의 시퀀스고 b가 m 요소(TrainJourney b과 공유되는 요소의 시퀀스라면,
// n+m 요소의 시퀀스를 반환
static TrainJourney append(TrainJourney a, TrainJourney b) {
    return a == null ? b : new TrainJourney(a.price, append(a.onward, b));
}
```

<br/>

<p align="center"><img width="600" alt="함수형 append" src="https://user-images.githubusercontent.com/86337233/221182415-7670498f-1f7e-400b-8dec-089888798fe9.png">

<br/>
<br/>
<br/>

## 19.2.2 트리를 사용한 예제

HashMap 같은 인터페이스를 구현할 때는 이진 탐색 트리가 사용된다.

아래 예제는 문자열 키와 int값을 포함한다.

```java
class Tree {
    private String key;
    private int val;
    private Tree left, right;
    public Tree(String k, int v, Tree l, Tree r) {
        key = k; val = v; left = l;, right = r;
    }
}

class TreeProcessor {
    public static int lookup(String k, int defaultval, Tree t) {
        if (t == null) return defaultval;
        if (k.equals(t.key)) return t.val;
        return lookup(k, defaultval,
                k.compareTo(t.key) < 0 ? t.left : t.right);
    }
    // 트리의 다른 작업을 처리하는 기타 메서드
}
```

<br/>

주어진 키와 연관된 값을 어떻게 갱신할 수 있을까?

아래 두 가지 update 버전 모두 기존 트리를 변경한다. (즉, 트리에 저장된 맵의 모든 사용자가 **변경에 영향을 받음**)

```java
public static void update(String k, int newval, Tree t) {
    if (t == null) { /* 새로운 노드 추가 */ }
    else if (k.equals(t.key)) t.val = newval;
    else update(k, newval, k.compareTo(t.key) < 0 ? t.left : t.right);
}
```

```java
public static Tree update(String k, int newval, Tree t) {
    if (t == null) t = new Tree(k, newval, null, null);
    else if (k.equals(t.key)) t.val = newval;
    else if (k.compareTo(t.key) < 0) t.left = update(k, newval, t.left);
    else t.right = update(k, newval, t.right);
    return t;
}
```

<br/>

> 모든 사용자가 같은 자료구조를 공유하며 프로그램에서 누군가 자료구조를 갱신했을 때 영향을 받는다.

<br/>
<br/>

## 19.2.3 함수형 접근법 사용

위의 트리 문제를 함수형으로 처리해보고자 한다.

1. 새로운 키/값 쌍을 저장할 새로운 노드를 만들어야 한다.
2. 트리의 루트에서 새로 생성한 노드의 경로에 있는 노드들도 새로 만들어야 한다.

```java
public static Tree fupdate(String k, int newval, Tree t) {
    return (t == null) ?
        new Tree(k, newval, null, null) :
        k.equals(t.key) ?
            new Tree(k, nweval, t.left, t.right) :
        k.compareTo(t.key) < 0 ?
            new Tree(t.key, t.val, fupdate(k, newval, t.left), t.right) :
            new Tree(t.key, t.val, t.left, fupdate(k, newval, t.right));
}
```

<br/>

fupdate에서는 기존의 트리를 갱신하는 것이 아니라 새로운 트리를 만든다.

그리고 **인수를 이용해서 가능한 한 많은 정보를 공유한다.**

<br/>

<p align="center"><img width="550" alt="Tree" src="https://user-images.githubusercontent.com/86337233/221182420-f85466d1-f0a0-4e68-8014-790130782da4.png">

<br/>
<br/>
<br/>

이와 같이 **저장된 값이 다른 누군가에 의해 영향을 받지 않는 상태**의 함수형 자료구조를 `영속(persistent)`이라고 한다.

‘`결과 자료구조를 바꾸지 말라`’는 것이 자료구조를 사용하는 모든 사용자에게 요구하는 단 한 가지 조건이다.

- 이 조건을 무시한다면 fupdate로 전달된 자료구조에 의도치 않은 그리고 원치 않은 갱신이 일어난다.
- 이 법칙 덕분에 조금 다른 구조체 간의 공통부분을 공유할 수 있다.

<br/>
<br/>
<br/>

# 19.4 패턴 매칭

함수형 프로그래밍을 구분하는 또 하나의 중요한 특징으로 (구조적인) `패턴 매칭(pattern matching)`을 들 수 있다.  
*(이는 정규표현식 그리고 정규표현식과 관련된 패턴 매칭과는 다름)*

자료형이 복잡해지면 이러한 작업을 처리하는 데 필요한 코드(if-then-else나 switch문 등)의 양이 증가하는데, 패턴 매칭을 통해 불필요한 잡동사니를 줄일 수 있다.

<br/>

### 트리 탐색 예제

숫자와 바이너리 연산자로 구성된 간단한 수학언어가 있다고 가정하자.

```java
class Expr { ... }
class Number extends Expr { int val; ... }
class BinOp extends Expr { String opname; Expr left, right; ... }
```

<br/>

표현식을 단순화하는 메서드를 구현해야 한다고 하자.

e.g., 5 + 0은 5로 단순화할 수 있음  
→ `new BinOp(”+”, new Number(5), new Number(0))`은 `Number(5)`로 단순화할 수 있음

```java
Expr simplifyExpression(Expr expr) {
    if (expr instanceof BinOp)
        && ((BinOp)expr).opname.equals("+"))
        && ((BinOp)expr).right instanceof Number
        && ...
        && ... ) {
        return (BinOp)expr.left;
    }
    ...
}
```

코드가 깔끔하지 못하다.

<br/>
<br/>

## 19.4.1 방문자 디자인 패턴

자바에서는 `방문자 디자인 패턴(visitor design pattern)`으로 자료형을 언랩할 수 있으며,  
특히 특정 데이터 형식을 ‘방문’하는 알고리즘을 캡슐화하는 클래스를 따로 만들 수 있다.

<br/>

방문자 패턴의 동작 과정

1. `방문자 클래스`는 지정된 데이터 형식의 인스턴스를 입력으로 받는다.
2. 방문자 클래스가 인스턴스의 모든 멤버에 접근한다.
3. SimplifyExprVisitor를 인수로 받는 `accept`를 BinOp에 추가한 다음, BinOp 자신을 SimplifyExprVisitor로 전달한다.

<br/>

```java
class BinOp extends Expr {
    ...
    public Expr accept(SimplifyExprVisitor v) {
        return v.visit(this);
    }
}
```

<br/>

이제 SimplifyExprVisitor는 BinOp 객체를 언랩할 수 있다.

```java
public class SimplifyExprVisitor {
    ...
    public Expr visit(BinOp e) {
        if ("+".equals(e.opname) && e.right instanceof Number && ...) {
            return e.left;
        }
        return e;
    }
}
```

<br/>
<br/>

## 19.4.2 패턴 매칭의 힘

패턴 매칭으로 조금 더 단순하게 문제를 해결할 수 있다.

수식을 표현하는 Expr이라는 자료형이 주어졌을 때 스칼라 프로그래밍 언어로는 다음처럼 수식을 분해하는 코드를 구현할 수 있다. (자바는 패턴 매칭을 지원하지 않음)

```scala
def simplifyExpression(expr: Expr): Expr = expr match {
	case BinOp("+", e, Number((0)) => e // 0 더하기
	case BinOp("*", e, Number(1)) => e // 1 곱하기
	case BinOp("/", e, Number(1)) => e // 1로 나누기
	case _ => expr // expr를 단순화할 수 없음
}
```

- 스칼라는 표현지향, 자바는 구문지향
- 패턴 매칭을 지원하는 언어의 가장 큰 실용적인 장점은 커다란 switch문이나 if-then-else 문을 피할 수 있는 것이다.
