# Chapter 11. null 대신 Optional 클래스

<br/>

# 11.1 값이 없는 상황을 어떻게 처리할까?

## 11.1.2 null 때문에 발생하는 문제

null로 값이 없다는 사실을 표현하는 것은 좋은 방법이 아니다.

- 에러의 근원이며, 코드를 어지럽힌다.
- null은 아무 의미도 표현하지 않는다.
- 개발자로부터 모든 포인터를 숨긴 자바 철학에 위배된다. (null 포인터)
- null은 무형식이며 정보를 포함하고 있지 않으므로 모든 참조 형식에 null을 할당할 수 있다.  
  따라서 형식 시스템에 구멍을 만든다.

<br/>

e.g., Person/Car/Insurance 데이터 모델

```java
public class Person {
    private Car car;

    public Car getCar() {
        return car;
    }
}

public class Car {
    private Insurance insurance;

    public Insurance getInsurance() {
        return insurance;
    }
}

public class Insurance {
    private String name;

    public String getName() {
        return name;
    }
}
```

<br/>

이때 차를 소유하지 않은 사람에 대하여 아래의 메서드를 호출하면 `NullPointerException`이 발생하면서 프로그램 실행이 중단된다.

```java
public String getCarInsuranceName(Person person){
    return person.getCar().getInsurance().getName();
}
```

<br/>
<br/>
<br/>

# 11.2 Optional 클래스 소개

자바 8은 ‘선택형값’ 개념의 영향을 받아서 `java.util.Optional<T>`라는 새로운 클래스를 제공한다.

<br/>

<p align="center"><img width="450" alt="Car" src="https://user-images.githubusercontent.com/86337233/220388742-1bcac093-97de-4a2b-9cc8-06dd10474069.png">

<br/>
<br/>

- 값이 있으면 Optional 클래스는 값을 감싼다.
- 값이 없으면 `Optional.empty` 메서드로 Optional을 반환한다.

<br/>

e.g., Optional로 Person/Car/Insurance 데이터 모델 재정의

```java
public class Person {
    private Optional<Car> car; // 사람은 차를 소유했을 수도, 소유하지 않았을 수도 있음

    public Optional<Car> getCar() {
        return car;
    }
}

public class Car {
    private Optional<Insurance> insurance; // 자동차가 보험에 가입되어 있을 수도, 가입되어 있지 않았을 수도 있음

    public Optional<Insurance> getInsurance() {
        return insurance;
    }
}

public class Insurance {
    private String name; // 보험회사에는 이름이 반드시 있음

    public String getName() {
        return name;
    }
}
```

<br/>

Optional을 사용하면

1. 모델의 의미(semantic)가 더 명확해지며,
2. 값이 없는 상황이 우리 데이터에 문제가 있는 것인지 아니면 알고리즘의 버그인지 명확하게 구분할 수 있다.

<br/>
<br/>
<br/>

# 11.3 Optional 적용 패턴

## 11.3.1 Optional 객체 만들기

### 빈 Optional

정적 팩토리 메서드 `Optional.empty`로 빈 Optional 객체를 얻을 수 있다.

```java
Optional<Car> optCar=Optional.empty();
```

<br/>

### null이 아닌 값으로 Optional 만들기

정적 팩토리 메서드 `Optional.of`로 null이 아닌 값을 포함하는 Optional을 만들 수 있다.

```java
Optional<Car> optCar = Optional.of(car);
// car가 null이라면 NullPointerException가 발생함
```

<br/>

### null값으로 Optional 만들기

정적 팩토리 메서드 `Optional.ofNullable`로 null값을 저장할 수 있는 Optional을 만들 수 있다.

```java
Optional<Car> optCar = Optional.ofNullable(car);
// car가 null이라면 빈 Optional 객체가 반환됨
```

<br/>
<br/>

## 11.3.2 맵으로 Optional의 값을 추출하고 변환하기

`스트림의 map`은 스트림의 각 요소에 제공된 함수를 적용하는 연산이었는데, `Optional의 map` 메서드는 이와 비슷하다.

<br/>

<p align="center"><img width="750" alt="map" src="https://user-images.githubusercontent.com/86337233/220388764-d6bd7c9f-8fb2-4cf9-948a-635388dce448.png">

<br/>
<br/>

- Optional이 값을 포함하면 map의 인수로 제공된 함수가 값을 바꾼다.
- Optional이 비어있으면 아무 일도 일어나지 않는다.

<br/>

e.g., 보험회사의 이름을 추출하는 코드

```java
String name = null;
if(insurance != null){
    name = insurance.getName();
}

// Optional 사용 시
Optional<Insurance> optInsurance = Optional.ofNullable(insurance);
Optional<String> name = optInsurance.map(Insurance::getName);
```

<br/>
<br/>

## 11.3.3 flatMap으로 Optional 객체 연결

아래 자동차의 보험회사 이름을 찾는 코드는 컴파일되지 않는다.

첫번째와 두번째 map 연산 결과는 `이차원 Optional 형식(Optional<Optional<T>>)`이기 때문이다.

```java
Optional<Person> optPerson = Optional.of(person);
Optional<String> name =
        optPerson.map(Person::getCar) // Optional<Optional<Car>>
        .map(Car::getInsurance) // Optional<Optional<Insurance>>
        .mape(Insurance::getName);
```

<br/>

`스트림의 flatMap` 메서드를 통해,  
함수를 적용해서 생성된 모든 스트림이 하나의 스트림으로 병합되어 평준화할 수 있었다.

Optional에서도 똑같이 `flatMap` 메서드를 통해 이차원 Optional을 일차원 Optional로 평준화할 수 있다.

> Optional에서의 **평준화 과정** : 두 Optional을 합치는 기능을 수행하면서 둘 중 하나라도 null이면 빈 Optional을 생성하는 연산

<br/>

<p align="center"><img width="750" alt="flatMap" src="https://user-images.githubusercontent.com/86337233/220388756-bfca07c0-c470-417e-817a-9bde6a90e83b.png">

<br/>
<br/>
<br/>

따라서 Optional로 자동차의 보험회사 이름을 찾는 코드를 `flatMap` 메서드와 `map` 메서드를 통해 아래와 같이 재구현할 수 있다.

```java
public String getCarInsuranceName(Optional<Person> person) {
    return person.flatMap(Person::getCar)
        .flatMap(Car::getInsurance)
        .map(Insurance::getName)
        .orElse("Unknown"); // 결과 Optional이 비어있으면 기본값 사용
```

<br/>

Optional을 이용한 Person/Car/Insurance 참조 체인


<p align="center"><img width="750" alt="참조 체인" src="https://user-images.githubusercontent.com/86337233/220388770-6b45cf5e-ee65-4bac-8a51-d6db55c4bce6.png">

<br/>
<br/>
<br/>

## 도메인 모델에 Optional을 사용했을 때 데이터를 직렬화할 수 없는 이유

Optional 클래스의 설계자는 Optional의 용도가 **선택형 반환값을 지원하는 것**이라고 명확하게 못박았다.

즉, **Optional 클래스는 필드 형식으로 사용할 것을 가정하지 않았으므로** Serializable 인터페이스를 구현하지 않았고,  
이에 도메인 모델에 Optional을 사용한다면 직렬화 모델을 사용하는 도구나 프레임워크에서 문제가 생길 수 있다.

<br/>

직렬화 모델이 필요하다면 Optional로 값을 반환받을 수 있는 메서드를 추가하는 방식을 권장한다.

```java
public class Person {
    private Car car;

    public Optional<Car> getCarAsOptional() {
        return Optional.ofNullable(car);
    }
}
```

<br/>
<br/>

## 11.3.4 Optional 스트림 조작

자바 9에서는 Optional을 포함하는 스트림을 쉽게 처리할 수 있도록 Optional에 `stream()` 메서드를 추가했다.

Optional 스트림을 값을 가진 스트림으로 변환할 때 이 기능을 유용하게 활용할 수 있다.

<br/>

e.g., 자동차를 소유한 사람들이 가입한 보험 회사의 이름들을 반환하는 메서드

- 인수 : List<Person>
- 반환형 : Set<String>

```java
public Set<String> getCarInsuranceNames(List<Person> persons) {
    Stream<Optional<String>> = persons.stream()
        .map(Person::getCar) // 사람 목록을 각 사람이 보유한 자동차의 Optional<Car> 스트림으로 변환
        .map(optCar->optCar.flatMap(Car::getInsurance) // Optional<Car>를 해당 Optional<Insurance>로 변환
        .map(optIns->optIns.flatMap(Insurance::getName)) // Optional<Insurance>를 해당 이름의 Optional<String>으로 변환
        .flatMap(Optional::stream) // Stream<Optional<String>>을 현재 이름을 포함하는 Stream<String>으로 변환
        .collect(toSet());
}
```

<br/>

위 코드는 세 번의 변환 과정(map)을 거친 결과 Stream<Optional<String>>를 얻는데 **결과가 비어있을 수 있다.**  
(사람이 차를 갖고 있지 않거나 차가 보험에 가입되어 있지 않은 경우)

<br/>

Optional은 이런 종류의 연산을 널 걱정없이 안전하게 처리할 수 있지만, 마지막 결과를 얻으려면 빈 Optional을 제거하고 값을 언랩해야 한다.

```java
public Set<String> getCarInsuranceNames(List<Person> persons) {
    Stream<Optional<String>>stream = persons.stream()
        .map(Person::getCar) // 사람 목록을 각 사람이 보유한 자동차의 Optional<Car> 스트림으로 변환
        .map(optCar -> optCar.flatMap(Car::getInsurance) // Optional<Car>를 해당 Optional<Insurance>로 변환
        .map(optIns -> optIns.flatMap(Insurance::getName)); // Optional<Insurance>를 해당 이름의 Optional<String>으로 변환
        
    return stream.filter(Optional::isPresent)
        .map(Optional::get)
        .collect(toSet());
}
```

<br/>
<br/>

## 11.3.5 디폴트 액션과 Optional 언랩

Optional 클래스는 Optional 인스턴스에 포함된 값을 읽는 다양한 방법을 제공한다.

<br/>

### get()

래핑된 값이 있으면 해당 값을 반환하고, 값이 없으면 `NoSuchElementException`을 발생시킨다.

Optional에 값이 반드시 있다고 가정할 수 있는 상황이 아니면 이 메서드를 사용하지 않는 것이 좋다.

<br/>

### orElse(T other)

Optional이 값을 포함하지 않을 때 기본값을 제공할 수 있다.

<br/>

### orElseGet(Supplier<? extends T> other)

Optional에 값이 없을 때만 Supplier가 실행된다. (orElse 메서드에 대응하는 게으른 버전의 메서드)

디폴트 메서드를 만드는 데 시간이 걸리거나 Optional이 비어있을 때만 기본값을 생성하고 싶다면 이 메서드를 사용해야 한다.

<br/>

### orElseThrow(Supplier<? extends X> exceptionSupplier)

Optional이 비어있을 때 예외를 발생시키는데, 발생시킬 예외의 종류를 선택할 수 있다.

<br/>

### ifPresent(Consumer<? super T> consumer)

값이 존재할 때 인수로 넘겨준 동작을 실행할 수 있다.

값이 없으면 아무 일도 일어나지 않는다.

<br/>

### ifPresent(Consumer<? super T> action, Runnable emptyAction)

자바 9에서 추가된 인스턴스 메서드이다.

Optional이 비었을 때 실행할 수 있는 Runnable을 인수로 받는다.

<br/>
<br/>

## 11.3.6 두 Optional 합치기

Person과 Car 정보를 이용해서 가장 저렴한 보험료를 제공하는 보험회사를 찾는 로직을 구현하려고 한다.

```java
public Insurance findCheapestInsurance(Person person, Car car) {
    // 다양한 보험회사가 제공하는 서비스 조회
    // 모든 결과 데이터 비교
    return cheapestCompany;
}
```

<br/>

두 Optional을 인수로 받아서 Optional<Insurance>를 반환하는 null 안전 버전(nullsafe version)의 메서드를 구현해야 한다.

- `isPresent` : Optional이 값을 포함하는지 여부를 알려주는 메서드

```java
public Optional<Insurance> nullSafeFindCheapestInsurance(Optional<Person> person, Optional<Car> car) {
    if (person.isPresent() && car.isPresent()) {
        return Optional.of(findCheapestInsurance(person.get(), car.get()));
    } else {
        return Optional.empty();
    }
}

// map과 flatMap 메서드를 통한 재구현
public Optional<Insurance> nullSafeFindCheapestInsurance(Optional<Person> person, Optional<Car> car) {
    return person.flatMap(p -> car.map(c -> findCheapestInsurance(p, c)));
}
```

<br/>
<br/>

## 11.3.7 필터로 특정값 거르기

객체의 메서드를 호출해서 어떤 프로퍼티를 확인해야 할 때, Optional 객체에 `filter` 메서드를 이용할 수 있다.

- 인수 : 프레디케이트
- Optional 객체가 값을 가지며 프레디케이트와 일치하면 filter는 그 값을 반환하고, 그렇지 않으면 빈 Optional 객체를 반환한다.

<br/>

e.g., 보험회사 이름이 ‘CambridgeInsurance’인지 확인하는 코드

```java
Optional<Insurance> optInsurance = ...;
optInsurance.filter(insurance -> "CambridgeInsurance".equals(insurance.getName()))
        .ifPresent(x -> System.out.println("ok"));
```

<br/>

e.g., 인수 person이 minAge 이상의 나이일 때만 보험회사 이름을 반환하는 코드

```java
public String getCarInsuranceName(Optional<Person> person, int minAge){
    return person.filter(p -> p.getAge() >= minAge)
        .flatMap(Person::getCar)
        .flatMap(Car::getInsurance)
        .map(Insurance::getName)
        .orElse("Unknown");
}
```

<br/>
<br/>

## Optional 클래스의 메서드

<p align="center"><img width="700" alt="1" src="https://user-images.githubusercontent.com/86337233/220388775-65fddd13-1ece-469a-b0a0-90293a8d2c7f.png">

<br/>
<br/>

<p align="center"><img width="700" alt="2" src="https://user-images.githubusercontent.com/86337233/220388784-d2c3ea4d-91de-4802-9b10-e5115619cc39.png">

<br/>
<br/>
<br/>
<br/>

# 11.4 Optional을 사용한 실용 예제

호환성을 유지하다보니 기존 자바 API는 Optional을 적절하게 활용하지 못하고 있는데,  
코드에 작은 유틸리티 메서드를 추가하는 방식으로 이 문제를 해결할 수 있다.

<br/>

## 11.4.1 잠재적으로 null이 될 수 있는 대상을 Optional로 감싸기

다음 코드는 문자열 ‘key’에 해당하는 값이 없으면 null이 반환될 것이다.

```java
Object value = map.get("key");
```

<br/>

해결법은 두 가지이다.

1. if-then-else를 추가
2. Optional.ofNullable을 이용

    ```java
    Optional<Object> value = Optional.ofNullable(map.get("key"));
    ```

<br/>
<br/>

## 11.4.2 예외와 Optional 클래스

자바 API는 어떤 이유에서 값을 제공할 수 없을 때 null을 반환하는 대신 예외를 발생시킬 때도 있는데, 이것의 대표적인 예시는 `Integer.parseInt(String)`이다.

이 메서드는 문자열을 정수로 바꾸지 못할 때 `NumberFormatException`을 발생시켜 문자열이 숫자가 아니라는 사실을 예외로 알린다.

<br/>

정수로 변환할 수 없는 문자열 문제를 빈 Optional로 해결할 수 있다.

즉, parseInt가 Optional을 반환하도록 모델링할 수 있다.

```java
public static Optional<Integer> stringToInt(String s) {
    try {
        return Optional.of(Integer.parseInt(s)); // 정수로 변환된 값을 포함하는 Optional을 반환
    } catch(NumberFormatException e){
        return Optional.empty(); // 빈 Optional을 반환
    }
}
```

이를 통해 기존처럼 거추장스러운 try/catch 로직을 사용할 필요가 없다.

<br/>
<br/>

## 11.4.3 기본형 Optional을 사용하지 말아야 하는 이유

Optional도 스트림처럼 기본형으로 특화된 OptionalInt, OptionalLong, OptionalDouble 등의 클래스를 제공한다.

- Optional의 최대 요소 수는 한 개이므로 Optional에서는 기본형 특화 클래스로 성능을 개선할 수 없다.
- 기본형 특화 Optional은 Option 클래스의 메서드(map, flatMap, filter) 등을 지원하지 않는다.

<br/>
<br/>

## 11.4.4 응용

코드 : [src/main/java/chapter11](/src/main/java/chapter11)
