# Chapter 13. 디폴트 메서드

<br/>

전통적인 자바에서는 인터페이스와 관련 메서드는 한 몸처럼 구성된다.

인터페이스를 구현하는 클래스는 인터페이스에서 정의하는 모든 메서드 구현을 제공하거나 아면 슈퍼클래스의 구현을 상속받아야 한다.

<br/>

따라서 만약 새로운 메서드를 추가하는 등 인터페이스를 바꾸었을 경우 이전에 해당 인터페이스를 구현했던 클래스들도 전부 고쳐야 한다는 문제가 존재한다.

- **바이너리 호환성**은 유지되어 새로운 메서드 구현이 없이도 기존 클래스 파일 구현은 잘 동작한다.
- 하지만 새로운 메서드를 호출하는 순간 `AbstractMethodError`가 발생하게 되며,
  전체 애플리케이션을 재빌드할 경우 컴파일 에러가 발생한다.

<br/>

#### [참고] 바이너리 호환성, 소스 호환성, 동작 호환성

- 바이너리 호환성
    - 뭔가를 바꾼 이후에도 에러 없이 기존 바이너리가 실행될 수 있는 상황을 말한다.
    - 바이너리 실행에는 인증(verification), 준비(preparation), 해석(resolution) 등의 과정이 포함된다.


- 소스 호환성
    - 코드를 고쳐도 기존 프로그램을 성공적으로 재컴파일할 수 있음을 의미한다.



- 동작 호환성
    - 코드를 바꾼 다음에도 같은 입력값이 주어지면 프로그램이 같은 동작을 실행한다는 의미다.

<br/>

---

<br/>

자바 8에서는 이 문제를 해결하기 위해 인터페이스를 정의하는 두 가지 방법을 제공한다.

1. 인터페이스 내부에 `정적 메서드(static method)`를 사용
2. 인터페이스의 기본 구현을 제공할 수 있도록 `디폴트 메서드(default method)` 기능을 사용

<br/>

`디폴트 메서드`를 이용하면 인터페이스의 기본 구현을 그대로 상속하므로 (즉, 바뀐 인터페이스에서 자동으로 기본 구현을 제공하므로)  
인터페이스에 자유롭게 새로운 메서드를 추가할 수 있게 되며, 기존 코드를 고치지 않아도 된다.

<br/>

<p align="center"><img width="800" alt="인터페이스에 메서드 추가" src="https://user-images.githubusercontent.com/86337233/220651860-3cb413a0-5142-4071-8b7a-2451a3bbbb3e.png">

<br/>
<br/>
<br/>
<br/>

# 13.2 디폴트 메서드란 무엇인가?

자바 8에서는 호환성을 유지하면서 API를 바꿀 수 있도록 새로운 기능인 `디폴트 메서드(default method)`를 제공한다.

- 인터페이스를 구현하는 클래스에서 구현하지 않은 메서드는 **인터페이스 자체에서 기본으로 제공한다.**
- 디폴트 메서드는 `default`라는 키워드로 시작하며, 다른 클래스에 선언된 메서드처럼 메서드 바디를 포함한다.

    ```java
    default void sort(Comparator<? super E> c) { // default 키워드는 해당 메서드가 디폴트 메서드임을 가리킴
    		Collections.sort(this, c);
    }
    ```

<br/>

>💡 함수형 인터페이스는 오직 하나의 추상 메서드를 포함하는데, 디폴트 메서드는 추상 메서드에 해당하지 않는다.

<br/>

e.g., 모든 컬렉션에서 사용할 수 있는 `removeIf` 메서드를 추가하려면?

- removeIf 메서드 : 주어진 프레디케이트와 일치하는 모든 요소를 컬렉션에서 제거하는 기능을 수행한다.
- 모든 컬렉션 클래스는 `java.util.Collection` 인터페이스를 구현한다.
- 따라서 Collection 인터페이스에 디폴트 메서드를 추가함으로써 소스 호환성을 유지할 수 있다.

```java
default boolean removeIf(Predicate<? super E> filter) {
    boolean removed = false;
    Iterator<E> each = iterator();
    while (each.hasNext()) {
        if (filter.test(each.next())) {
            each.remove();
            removed = true;
        }
    }
    return removed;
}
```

<br/>
<br/>

## 추상 클래스와 자바 8의 인터페이스

- 클래스는 하나의 추상 클래스만 상속받을 수 있지만 인터페이스를 여러 개 구현할 수 있다.
- 추상 클래스는 인스턴스 변수(필드)로 공통 상태를 가질 수 있으나, 인터페이스는 인스턴스 변수를 가질 수 없다.

<br/>
<br/>
<br/>

# 13.3 디폴트 메서드 활용 패턴

- 선택형 메서드(optional method)
- 동작 다중 상속(multiple inheritance of behavior)

<br/>

## 13.3.1 선택형 메서드

인터페이스를 구현하는 클래스에서 메서드의 내용이 비어있는 상황을 본 적이 있을 것이다.

디폴트 메서드를 이용하면 그러한 메서드에 기본 구현을 제공할 수 있으므로, 인터페이스를 구현하는 클래스에서 빈 구현을 제공할 필요가 없다.

따라서 불필요한 코드를 줄일 수 있다.

<br/>

## 13.3.2 동작 다중 상속

디폴트 메서드를 이용하면 기존에는 불가능했던 동작 다중 상속 기능도 구현할 수 있다.

자바에서 클래스는 한 개의 다른 클래스만 상속할 수 있지만 인터페이스는 여러 개 구현할 수 있기 때문이다.

#### 단일 상속

<p align="center"><img width="330" alt="단일 상속" src="https://user-images.githubusercontent.com/86337233/220651894-8fa2bf88-9087-4f21-9f5a-bff2a24cdbeb.png">

<br/>
<br/>

#### 다중 상속

<p align="center"><img width="400" alt="다중 상속" src="https://user-images.githubusercontent.com/86337233/220651886-275ab2da-d45f-4b0f-a838-30bb900c7a6c.png">

<br/>
<br/>

### 다중 상속 형식

e.g., 자바 API에 정의된 ArrayList 클래스

```java
public class ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, Serializable { // 4개의 인터페이스를 구현함
}
```

결과적으로 ArrayList는 AbstractList, List, RandomAccess, Cloneable, Serializable, Iterable, Collection의 `서브형식(subtype)`이 된다.

<br/>

> 기능이 중복되지 않는 최소의 인터페이스를 유지한다면 우리 코드에서 동작을 쉽게 재사용하고 조합할 수 있다.

<br/>
<br/>

## 옳지 못한 상속

상속으로 코드 재사용 문제를 모두 해결할 수 있는 것은 아니다.  
e.g., 한 개의 메서드를 재사용하기 위해 100개의 메서드와 필드가 정의되어 있는 클래스를 상속받는 것

이럴 때는 `델리게이션(delegation)`, 즉 멤버 변수를 이용해서 클래스에서 필요한 메서드를 직접 호출하는 메서드를 작성하는 것이 좋다.

<br/>
<br/>
<br/>

# 13.4 해석 규칙

자바의 클래스는 하나의 부모 클래스만 상속받을 수 있지만 여러 인터페이스를 동시에 구현할 수 있다.

만약 어떤 클래스가 같은 디폴트 메서드 시그니처를 포함하는 두 인터페이스를 구현하는 상황이라면, 클래스는 어떤 인터페이스의 디폴트 메서드를 사용하게 될까?

<br/>

## 13.4.1 알아야 할 세 가지 해결 규칙

다른 클래스나 인터페이스로부터 같은 시그니처를 갖는 메서드를 상속받을 때는 세 가지 규칙을 따라야 한다.

<br/>

> 1️⃣ 클래스가 항상 이긴다.

클래스나 슈퍼클래스에서 정의한 메서드가 디폴트 메서드보다 우선권을 갖는다.

<br/>

> 2️⃣ 1번 규칙 이외의 상황에서는 서브인터페이스가 이긴다.

상속관계를 갖는 인터페이스에서 같은 시그니처를 갖는 메서드를 정의할 때는 서브인터페이스가 이긴다.  
(즉, B가 A를 상속받는다면 B가 A를 이김)

<br/>

> 3️⃣ 여전히 디폴트 메서드의 우선순위가 결정되지 않았다면 여러 인터페이스를 상속받는 클래스가 명시적으로 디폴트 메서드를 오버라이드하고 호출해야 한다.

<br/>
<br/>


## 13.4.2 디폴트 메서드를 제공하는 서브인터페이스가 이긴다.

### 예시 1

```java
public interface A {
    default void hello() {
        System.out.println("Hello from A");
    }
}

public interface B extends A {
    default void hello() {
        System.out.println("Hello from B");
    }
}

public class C implements B, A {
    public static void main(String[] args) {
        new C().hello();
    }
}
```

- B와 A는 hello라는 디폴트 메서드를 정의하며, B가 A를 상속받는다.
- 클래스 C는 B와 A를 구현한다.

<br/>

<p align="center"><img width="550" alt="서브인터페이스가 이김" src="https://user-images.githubusercontent.com/86337233/220651898-af419ba0-0475-4268-8cd8-97d716dea130.png">

<br/>
<br/>

컴파일러는 누구의 hello 메서드 정의를 사용할까?

13.4.1의 규칙 2에서는 서브인터페이스가 이긴다고 했고, B가 A를 상속받았으므로 컴파일러는 B의 hello를 선택한다. (’Hello from B’가 출력됨)

<br/>

### 예시 2

C가 D를 상속받는다면 어떤 일이 일어날까?

```java
public class D implements A { }

public class C extends D implements B, A {
    public static void main(String[] args) {
        new C().hello();
    }
}
```

<br/>

<p align="center"><img width="550" alt="하나의 클래스를 상속받아 두 개의 인터페이스를 구현" src="https://user-images.githubusercontent.com/86337233/220651902-6d9b9301-baab-473c-bf22-b99fa15fcf9d.png">

<br/>
<br/>

- 규칙 1에서는 클래스의 메서드 구현이 이긴다고 했다.
    - D는 hello를 오버라이드하지 않았고 단순히 인터페이스 A를 구현했다.
    - 따라서 D는 인터페이스 A의 디폴트 메서드 구현을 상속받는다.


- 규칙 2에서는 **클래스나 슈퍼클래스에 메서드 정의가 없을 때는** 디폴트 메서드를 정의하는 서브인터페이스가 선택된다.

<br/>

따라서 컴파일러는 인터페이스 A의 hello나 인터페이스 B의 hello 둘 중 하나를 선택해야 한다.

여기서 B가 A를 상속받는 관계이므로 이번에도 컴파일러는 B의 hello를 선택한다.

<br/>

### 예시 3

D가 명시적으로 A의 hello 메서드를 오버라이드한다면 어떤 일이 일어날까?

```java
public class D implements A {
    void hello() {
        System.out.println("Hello from D");
    }
}

public class C extends D implements B, A {
    public static void main(String[] args) {
        new C().hello();
    }
}
```

규칙 1에 의해 슈퍼클래스의 메서드 정의가 우선권을 갖기 때문에 ‘Hello from D’가 출력된다.

<br/>

만약 D가 다음처럼 구현되었다면 A에서 디폴트 메서드를 제공함에도 불구하고 C는 hello를 구현해야 한다.

```java
public class D implements A {
    public abstract void hello();
}
```

<br/>
<br/>

## 13.4.3 충돌 그리고 명시적인 문제 해결

이번에는 B가 A를 상속받지 않는 상황이라고 가정하자.

```java
public interface A {
    default void hello() {
        System.out.println("Hello from A");
    }
}

public interface B {
    default void hello() {
        System.out.println("Hello from B");
    }
}

public class C implements B, A { }
```

<br/>

<p align="center"><img width="350" alt="두 개의 인터페이스 구현" src="https://user-images.githubusercontent.com/86337233/220651907-9067ec7b-60ad-4f76-bb6b-9108b9936a19.png">

<br/>
<br/>

인터페이스 간에 상속관계가 없으므로 2번 규칙을 적용할 수 없기에 A와 B의 hello 메서드를 구별할 기준이 없다.

따라서 자바 컴파일러는 어떤 메서드를 호출해야 할지 알 수 없으므로 다음과 같은 에러를 발생한다.

```
Error: class C inherits unrelated defaults for hello() from types B and A.
```

<br/>

### 충돌 해결

클래스와 메서드 관계로 디폴트 메서드를 선택할 수 없는 상황에서는 선택할 수 있는 방법이 없다.

개발자가 직접 클래스 C에서 사용하려는 메서드를 명시적으로 선택해야 한다.

<br/>

즉, 클래스 C에서 hello 메서드를 오버라이드한 다음에 호출하려는 메서드를 명시적으로 선택해야 한다.

```java
public class C implements B, A {
    void hello() {
        B.super.hello();
    }
}
```

<br/>
<br/>

## 13.4.4 다이아몬드 문제

### 예제 1

1️⃣ D는 B와 C 중 누구의 디폴트 메서드 정의를 상속받을까?

```java
public interface A {
    default void hello() {
        System.out.println("Hello from A");
    }
}

public interface B extends A { }

public interface C extends A { }

public class D implements B, C {
    public static void main(String[] args) {
        new D().hello();
    }
}
```

<br/>

<p align="center"><img width="600" alt="다이아몬드 문제" src="https://user-images.githubusercontent.com/86337233/220651917-b9960955-3b1b-4ed2-a2c6-6081f8ba0d93.png">

<br/>
<br/>

A만 디폴트 메서드를 정의하고 있으므로 프로그램 출력 결과는 ‘Hello from A’가 된다.

<br/>

2️⃣ B에도 같은 시그니처의 디폴트 메서드 hello가 있다면 2번 규칙에 의해 B가 선택된다.

3️⃣ B와 C가 모두 디폴트 메서드 hello 메서드를 정의한다면 충돌이 발생하므로, 둘 중 하나의 메서드를 명시적으로 호출해야 한다.

<br/>

### 예제 2

인터페이스 C에 **추상 메서드** hello를 추가하면 어떤 일이 벌어질까?

```java
public interface C extends A {
    void hello();
}
```

<br/>

C는 A를 상속받으므로 C의 추상 메서드 hello가 A의 디폴트 메서드 hello보다 우선권을 갖는다.

따라서 컴파일 에러가 발생하며, 클래스 D가 어떤 hello를 사용할지 명시적으로 선택해서 에러를 해결해야 한다.
