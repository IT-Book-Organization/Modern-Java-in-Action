# Chapter10. 람다를 이용한 도메인 전용 언어

## 10.1 도메인 전용 언어 (domain-specific language, DSL)

- 특정 비즈니스 도메인의 문제를 해결하려고 만든 언어
- 특정 비스니스 도메인을 인터페이스로 만든 API
- 도메인을 표현할 수 있는 클래스와 메서드 집합이 필요하다

즉, DSL에서 동작과 용어는 특정 도메인에 국한되므로 다른 문제는 걱정할 필요 없고 오직 자신의 앞에 놓인 문제를 어떻게 해결할지에만 집중할 수 있다.

e.g. sql문을 통해 데이터베이스의 내부 구현 로직을 알지 못해도 데이터베이스를 CRUD 할 수 있다.

<br/>

### 10.1.1 DSL의 장단점

**장점**

- 간결함 : API는 비즈니스 로직을 간편하게 캡슐화하므로 반복을 피할 수 있고 코드를 간결하게 만들 수 있다.
- 가독성 : 도메인 영역의 용어를 사용하므로 비 도메인 전문가도 코드를 쉽게 이해할 수 있다. 다양한 조직 구성원 간에 코드와 도메인 영역이 공유될 수 있다.
- 유지보수 : 잘 설계된 DSL로 구현한 코드는 쉽게 유지보수하고 바꿀 수 있다.
- 높은 수준의 추상화 : DSL은 도메인과 같은 추상화 수준에서 동작하므로 도메인의 문제와 직접적으로 관련되지 않은 세부 사항을 숨긴다.
- 집중 : 비즈니스 도메인의 규칙을 표현할 목적으로 설계된 언어이므로 프로그래머가 특정 코드에 집중할 수 있다.
- 관심사분리(SoC) : 지정된 언어로 비즈니스 로직을 표현함으로 애플리케이션의 인프라구조와 관련된 문제와 독립적으로 비즈니스 관련된 코드에서 집중하기가 용이하다.

<br/>

**단점**

- DSL 설계의 어려움 : 간결하게 제한적인 언어에 도메인 지식을 담는 것이 쉬운 작업은 아니다.
- 개발 비용 : 코드에 DSL을 추가하는 작업은 초기 프로젝트에 많은 비용과 시간이 소모된다. 또한 DSL 유지보수와 변경은 프로젝트에 부담을 주는 요소다.
- 추가 우회 계층 : DSL은 추가적인 계층으로 도메인 모델을 감싸며 이 때 계층을 최대한 작게 만들어 성능 문제를 회피한다.
- 새로 배워야 하는 언어 : DSL을 프로젝트에 추가하면서 팀이 배워야 하는 언어가 한 개 더 늘어난다는 부담이 있다.
- 호스팅 언어 한계 : 일부 자바 같은 **범용 프로그래밍 언어**는 장황하고 엄격한 문법을 가졌다. 이런 언어로는 사용자 친화적 DSL을 만들기가 힘들다.

<br/>

### 10.1.2 JVM에서 이용할 수 있는 다른 DSL 해결책

### 내부 DSL

내부 DSL이란 자바로 구현한 DSL을 의미한다.

람다 표현식이 등장하며 쉽고 간단하며 표현력있는 DSL을 만들 수 있게 되었다.

익명 내부 클래스를 사용하는 것보다 람다를 사용하면 신호 대비 잡음 비율을 적정 수준으로 유지하는 DSL을 만들 수 있다.

<br/>

- 외부 DSL에 비해 새로운 패턴과 기술을 배워 DSL을 구현할 필요가 없다
- 순수 자바로 DSL을 구현하면 나머지 코드와 함께 DSL을 컴파일할 수 있다.
- 개발팀이 새로운 언어를 배울 필요가 없다.
- 기존 자바 IDE를 통해 자동 완성, 자동 리팩터링 같은 기능을 그대로 사용할 수 있다.

<br/>

e.g.

```java
List<String> numbers = Arrays.asList("one", "two", "three"); numbers.forEach( new Consumer<String>() {
    @Override
    public void accept( String s ) {
        System.out.println(s);
}
} );

// 내부 DSL 즉, 람다 표현식 사용
numbers.forEach(s -> System.out.println(s));
```

<br/>

### 다중 DSL

같은 자바 바이트코드를 사용하는 JVM 기반 프로그래밍 언어(Jython, JRuby, 코틀린 등)를 이용하여 DSL을 만들 수 있다.

- 문법적 잡음이 없으며 개발자가 아닌 사람도 코드를 쉽게 이해할 수 있다.
- 자바 언어가 가지는 한계를 넘을 수 있다 (스칼라는 꼬리 호출 최적화를 통해 함수 호출을 스택에 추가하지 않는다).
- 누군가가 해당 언어에 대해 고급 기술을 사용할 수 있을 정도의 충분한 지식을 가지고 있어야 한다.
- 두 개 이상의 언어가 혼재하므로 여러 컴파일러로 소스를 빌드하도록 빌드 과정을 개선해야 한다.
- 호환성 문제를 고려해야한다

<br/>

### 외부 DSL

자신만의 문법과 구문으로 새 언어를 설계해야한다.

<br/>

## 10.2 최신 자바 API의 작은 DSL

### 10.2.1 스트림 API는 **컬렉션을 조작하는 DSL**

(앞에서 전부 본 내용으로 자세한 설명 생략)

<br/>

### 10.2.2 **데이터를 수집하는 DSL**인 Collectors

(앞에서 전부 본 내용으로 자세한 설명 생략)

<br/>

## 10.3 자바로 DSL을 만드는 패턴과 기법

예제 도메인

주어진 시장에 주식가격을 모델링하는 순수 자바 빈즈

```java
public class Stock {

  private String symbol;
  private String market;

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol( String symbol ) {
    this.symbol = symbol;
  }

  public String getMarket() {
    return market;
  }

  public void setMarket( String market ) {
    this.market = market;
  }

  @Override
  public String toString() {
    return String.format("Stock[symbol=%s, market=%s]", symbol, market);
  }

}
```

<br/>

주어진 양의 주식을 사거나 파는 거래

```java
public class Trade {

  public enum Type {
    BUY,
    SELL
  }

  private Type type;
  private Stock stock;
  private int quantity;
  private double price;

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public Stock getStock() {
    return stock;
  }

  public void setStock(Stock stock) {
    this.stock = stock;
  }

  public double getValue() {
    return quantity * price;
  }

  @Override
  public String toString() {
    return String.format("Trade[type=%s, stock=%s, quantity=%d, price=%.2f]", type, stock, quantity, price);
  }

}
```

<br/>

거래의 주문

```java
public class Order {

  private String customer;
  private List<Trade> trades = new ArrayList<>();

  public void addTrade( Trade trade ) {
    trades.add( trade );
  }

  public String getCustomer() {
    return customer;
  }

  public void setCustomer( String customer ) {
    this.customer = customer;
  }

  public double getValue() {
    return trades.stream().mapToDouble( Trade::getValue ).sum();
  }

  @Override
  public String toString() {
    String strTrades = trades.stream().map(t -> "  " + t).collect(Collectors.joining("\n", "[\n", "\n]"));
    return String.format("Order[customer=%s, trades=%s]", customer, strTrades);
  }

}
```

<br/>

### 10.3.1 메서드 체인

메서드 호출 체인으로 거래 주문을 정의할 수 있다.

```java
// 주문 객체를 포함하고 메서드 체인 DSL을 제공하는 주문 빌더
public class MethodChainingOrderBuilder {

  public final Order order = new Order();

  private MethodChainingOrderBuilder(String customer) {
    order.setCustomer(customer);
  }

  public static MethodChainingOrderBuilder forCustomer(String customer) {
    return new MethodChainingOrderBuilder(customer);
  }

	// 주문 만들기를 종료하고 반환
  public Order end() {
    return order;
  }
	
	// 주식을 사는 트레이더 빌더를 만든다.
  public TradeBuilder buy(int quantity) {
    return new TradeBuilder(this, Trade.Type.BUY, quantity);
  }
	
	// 주식을 파는 트레이더 빌더를 만든다.
  public TradeBuilder sell(int quantity) {
    return new TradeBuilder(this, Trade.Type.SELL, quantity);
  }
	
	// 주문에 주식을 추가
  private MethodChainingOrderBuilder addTrade(Trade trade) {
    order.addTrade(trade);
    return this;
  }

// 트레이드 빌더
// 주문을 가지고 StcokBuilder를 생성한다.
public static class TradeBuilder {

    private final MethodChainingOrderBuilder builder;
    public final Trade trade = new Trade();

    private TradeBuilder(MethodChainingOrderBuilder builder, Trade.Type type, int quantity) {
      this.builder = builder;
      trade.setType(type);
      trade.setQuantity(quantity);
    }

    public StockBuilder stock(String symbol) {
      return new StockBuilder(builder, trade, symbol);
    }
}

// 스톡 빌더
// 주식의 시장을 지정하고, 거래에 주식을 추가하고, 최종 빌더를 반환하는 on메서드 정의 
public static class StockBuilder {

    private final MethodChainingOrderBuilder builder;
    private final Trade trade;
    private final Stock stock = new Stock();

    private StockBuilder(MethodChainingOrderBuilder builder, Trade trade, String symbol) {
      this.builder = builder;
      this.trade = trade;
      stock.setSymbol(symbol);
    }

    public TradeBuilderWithStock on(String market) {
      stock.setMarket(market);
      trade.setStock(stock);
      return new TradeBuilderWithStock(builder, trade);
    }
}

// 공개 메서드 TradeBuilderWithStock은 거래되는 주식의 단위 가격을 설정한 다음 원래 주문 빌더를 반환한다. 
//  
public static class TradeBuilderWithStock {

    private final MethodChainingOrderBuilder builder;
    private final Trade trade;

    public TradeBuilderWithStock(MethodChainingOrderBuilder builder, Trade trade) {
      this.builder = builder;
      this.trade = trade;
    }

    public MethodChainingOrderBuilder at(double price) {
      trade.setPrice(price);
      return builder.addTrade(trade);
    }

}

}

// 이용
Order order = forCustomer("BigBank")
        .buy(80).stock("IBM").on("NYSE").at(125.00)
        .sell(50).stock("GOOGLE").on("NASDAQ").at(375.00)
        .end();
```

이렇게 빌드를 구현하여 메소드 체인 패턴을 사용함으로써 사용자는 따로 생성자를 통해 객체를 만들 수 없게 구현한다.

즉, 사용자는 `forCustomer` 메서드만을 통해 `MethodChainingOrderBuilder`를 만들 수 있고 사용자는 미리 지정된 절차에 따라 `플루언트 API`(자신 혹을 객체를 반환하여 체인 형식으로 메서드 호출하는 방식)의 메서드를 호출하도록 강제한다.

덕분에 사용자가 다음 거래를 설정하기 전에 기존 거래를 올바로 설정할 수 있다.

그러나 빌더를 구현해야하고, 상위 수준의 빌더를 하위 수준의 빌더와 연결할 많은 접착 코드가 필요해진다.

<br/>

### 10.3.2 중첩된 함수 이용

다른 함수 안에 함수를 이용해 도메인 모델을 만든다.

```java
public class NestedFunctionOrderBuilder {

  public static Order order(String customer, Trade... trades) {
    Order order = new Order();
    order.setCustomer(customer);
    Stream.of(trades).forEach(order::addTrade);
    return order;
  }

  public static Trade buy(int quantity, Stock stock, double price) {
    return buildTrade(quantity, stock, price, Trade.Type.BUY);
  }

  public static Trade sell(int quantity, Stock stock, double price) {
    return buildTrade(quantity, stock, price, Trade.Type.SELL);
  }

  private static Trade buildTrade(int quantity, Stock stock, double price, Trade.Type buy) {
    Trade trade = new Trade();
    trade.setQuantity(quantity);
    trade.setType(buy);
    trade.setStock(stock);
    trade.setPrice(price);
    return trade;
  }

  public static double at(double price) {
    return price;
  }

  public static Stock stock(String symbol, String market) {
    Stock stock = new Stock();
    stock.setSymbol(symbol);
    stock.setMarket(market);
    return stock;
  }

  public static String on(String market) {
    return market;
  }

}

public void nestedFunction() {
    Order order = order("BigBank",
        buy(80,
            stock("IBM", on("NYSE")),
            at(125.00)),
        sell(50,
            stock("GOOGLE", on("NASDAQ")),
            at(375.00))
    );
}
```

메서드 체인에 비해 함수의 중첩 방식이 도메인 객체 계층 구조에 그대로 반영된다.

그러나 더 많은 괄호를 사용해야하고, 인수 목록을 정적 메서드에 넘겨줘야 한다.

<br/>

### 10.3.3 람다 표현식을 이용한 함수 시퀀싱

이 DSL 패턴은 람다 표현식으로 정의한 함수 시퀀스를 사용한다.

```java
public class LambdaOrderBuilder {

  private Order order = new Order();

  public static Order order(Consumer<LambdaOrderBuilder> consumer) {
    LambdaOrderBuilder builder = new LambdaOrderBuilder();
    consumer.accept(builder);
    return builder.order;
  }

  public void forCustomer(String customer) {
    order.setCustomer(customer);
  }

	// TradeBuilder Consumer를 인수로 받는다.
	// 즉, 이 메서드가 불리면 Consumer인자에 들어오는 람다의 전략대로
	// TradeBuilder를 초기화 한다.
  public void buy(Consumer<TradeBuilder> consumer) {
    trade(consumer, Trade.Type.BUY);
  }

  public void sell(Consumer<TradeBuilder> consumer) {
    trade(consumer, Trade.Type.SELL);
  }
	
	// TraderBuilder의 Type을 결정하고 람다의 전략대로 TradeBuilder를 수정하고 order에 trade를 더한다.
	// 즉, 일종의 템플릿 메소드 패턴이다.
  private void trade(Consumer<TradeBuilder> consumer, Trade.Type type) {
    TradeBuilder builder = new TradeBuilder();
    builder.trade.setType(type);
    consumer.accept(builder);
    order.addTrade(builder.trade);
  }
	
	// TradeBuilder Trade 인스턴스를 가지고 있고, 이를 설정할 수 있는 메서드를 가지고 있다.
  public static class TradeBuilder {

    private Trade trade = new Trade();

    public void quantity(int quantity) {
      trade.setQuantity(quantity);
    }

    public void price(double price) {
      trade.setPrice(price);
    }
		
		// 위 buy, sell 메서드와 비슷하게 StockBuilder를 람다의 전략대로 바꾸고 trade의 stock을 설정한다.
    public void stock(Consumer<StockBuilder> consumer) {
      StockBuilder builder = new StockBuilder();
      consumer.accept(builder);
      trade.setStock(builder.stock);
    }

  }

  public static class StockBuilder {

    private Stock stock = new Stock();

    public void symbol(String symbol) {
      stock.setSymbol(symbol);
    }

    public void market(String market) {
      stock.setMarket(market);
    }

  }

}

public void lambda() {
    Order order = LambdaOrderBuilder.order( o -> {
      o.forCustomer( "BigBank" );
      o.buy( t -> {
        t.quantity(80);
        t.price(125.00);
        t.stock(s -> {
          s.symbol("IBM");
          s.market("NYSE");
        });
      });
      o.sell( t -> {
        t.quantity(50);
        t.price(375.00);
        t.stock(s -> {
          s.symbol("GOOGLE");
          s.market("NASDAQ");
        });
      });
    });
}
```

이 패턴은 메서드 체인 패턴처럼 플루언트 방식으로 거래 주문을 정의할 수 있고, 중첩 함수 형식처럼 다양한 람다 표현식의 중첩 수준과 비슷하게 도메인 객체의 계층 구조를 유지한다.

그러나 많은 설정 코드가 필요하고, DSL 자체가 자바 8 람다 표현식 문법에 의한 잡음의 영향을 받는다.

<br/>

### 10.3.4 조합하기

한 DSL에 한 개의 패턴만 사용하라는 법은 없다.

```java
// 중첩된 함수 패턴과 람다 기법의 혼용
public class MixedBuilder {

	// 중첩된 함수 패턴
  public static Order forCustomer(String customer, TradeBuilder... builders) {
    Order order = new Order();
    order.setCustomer(customer);
    Stream.of(builders).forEach(b -> order.addTrade(b.trade));
    return order;
  }
	
	// 람다
  public static TradeBuilder buy(Consumer<TradeBuilder> consumer) {
    return buildTrade(consumer, Trade.Type.BUY);
  }

  public static TradeBuilder sell(Consumer<TradeBuilder> consumer) {
    return buildTrade(consumer, Trade.Type.SELL);
  }

  private static TradeBuilder buildTrade(Consumer<TradeBuilder> consumer, Trade.Type buy) {
    TradeBuilder builder = new TradeBuilder();
    builder.trade.setType(buy);
    consumer.accept(builder);
    return builder;
  }
	
	// 메소드 체인 패턴 사용됨
  public static class TradeBuilder {

    private Trade trade = new Trade();
		
    public TradeBuilder quantity(int quantity) {
      trade.setQuantity(quantity);
      return this;
    }

    public TradeBuilder at(double price) {
      trade.setPrice(price);
      return this;
    }

    public StockBuilder stock(String symbol) {
      return new StockBuilder(this, trade, symbol);
    }

  }

	// 메소드 체인 패턴 사용됨
  public static class StockBuilder {

    private final TradeBuilder builder;
    private final Trade trade;
    private final Stock stock = new Stock();

    private StockBuilder(TradeBuilder builder, Trade trade, String symbol) {
      this.builder = builder;
      this.trade = trade;
      stock.setSymbol(symbol);
    }

    public TradeBuilder on(String market) {
      stock.setMarket(market);
      trade.setStock(stock);
      return builder;
    }

  }

}

public void mixed() {
    Order order =
        forCustomer("BigBank",
            buy(t -> t.quantity(80)
                .stock("IBM")
                .on("NYSE")
                .at(125.00)),
            sell(t -> t.quantity(50)
                .stock("GOOGLE")
                .on("NASDAQ")
                .at(375.00)));
}
```

이 패턴은 여러 장점이 모두 들어가있지만, 사용자가 DSL을 배우기까지 시간이 오래걸린다.

<br/>

### 10.3.5 DSL에 메서드 참조 사용하기

주식 거레 모델의 최종값에 세금을 추가해 최종값을 계산하는 기능을 추가해보자.

```java
public class Tax {

  public static double regional(double value) {
    return value * 1.1;
  }

  public static double general(double value) {
    return value * 1.3;
  }

  public static double surcharge(double value) {
    return value * 1.05;
  }

}

public class TaxCalculator {
	
	// 주문 값에 적용된 모든 세금을 계산하는 함수
  public DoubleUnaryOperator taxFunction = d -> d;

  public TaxCalculator with(DoubleUnaryOperator f) {
		// 새로운 세금 계산 함수를 얻어서 현재 함수와 함친다.
    taxFunction = taxFunction.andThen(f);
    return this;
  }

  public double calculateF(Order order) {
		// 전달되어있는 함수를 계산한다.
    return taxFunction.applyAsDouble(order.getValue());
  }
	
}

value = new TaxCalculator().with(Tax::regional)
        .with(Tax::surcharge)
        .calculateF(order);
```

<br/>

## DSL 패턴 장단점 정리

| 패턴 이름 | 장점 | 단점 |
| --- | --- | --- |
| 메서드 체인 | 메서드 이름이 키워드 인수 역할을한다. | 구현이 장황하다 |
|  | 선택형 파라미터와 잘 동작한다. | 빌드를 연결하는 접착 코드가 필요하다 |
|  | DSL 사용자가 정해진 순서로 메서드를 호출하도록 강제할 수 있다. | 들여쓰기 규칙으로만 도메인 객체 계층을 정의한다. |
|  | 정적 메서드를 최소화하거나 없앨 수 있다. |  |
|  | 문법적 잡음을 최소화한다. |  |
| 중첩 함수 | 구현의 장황함을 줄일 수 있다. | 정적 메서드의 사용이 빈번하다. |
|  | 함수 중첩으로 도메인 객체 계층을 반영한다. | 이름이 아닌 위치로 인수를 정의한다. |
|  |  | 선택형 파라미터를 처리할 메서드 오버로딩이 필요하다. |
| 람다를 이용한 함수 시퀀싱 | 선택형 파라미터와 잘 동작한다. | 구현이 장황하다. |
|  | 정적 메서드를 최소화하거나 없앨 수 있다. | 람다 표현식으로 인한 문법적 잡음이 DSL에 존재한다. |
|  | 람다 중첩으로 도메인 객체 계층을 반영한다. |  |
|  | 빌더의 접착 코드가 없다. |  |