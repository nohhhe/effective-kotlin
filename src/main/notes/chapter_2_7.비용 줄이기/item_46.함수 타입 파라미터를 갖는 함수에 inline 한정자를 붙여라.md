아이템 46.함수 타입 파라미터를 갖는 함수에 inline 한정자를 붙여라 
=========================
코틀린 표준 라이브러리의 고차 함수(higher-order function)를 살펴보면, 대부분 inline 한정자가 붙어 있는 것을 확인할 수 있다.

```kotlin
inline fun repeat(times: Int, action: (Int) -> Unit) {
    for (index in 0 until times) {
        action(index)
    }
}
```

inline 한정자의 역할은 컴파일 시점에 함수를 호출하는 부분을 함수의 본문으로 대체하는 것이다.
```kotlin
repeat(10) {
    print(it)
}

// 컴파일 시점에 아래와 같이 대체된다.
for (index in 0 until 10) {
    print(index)
}
```

일반적인 함수를 호출하면 함수 본문으로 점프하고, 본문의 모든 문장을 뒤에 호출했던 위치로 점프하는 과정을 거친다.
하지만 inline 한정자를 사용해 함수를 호출하는 부분을 함수의 본문으로 대체하면 이러한 점프가 일어나지 않는다.

#### inline 한정자의 장점
* 타입 아규먼트에 reified 한정자를 붙여서 사용할 수 있다.
* 함수 타입 파라미터를 가진 함수가 훨씬 빠르게 동작한다.
* 비지역(non-local) 리턴을 사용할 수 있다.

### 타입 아규먼트를 reified로 사용할 수 있다.
#### reified
코틀린은 일반적으로 제네릭 타입 파라미터는 런타임에서 타입 정보가 지워지는 특성을 가진다. 그러나 reified를 사용하면 타입 파라미터의 타입 정보를 런타임에도 유지할 수 있다.
inline 함수에서만 reified 타입 파라미터를 사용할 수 있는 이유는, 함수가 인라인화되면서 타입 파라미터가 실제 타입으로 대체되기 때문입니다.

JVM 바이트 코드 내부에는 제네릭이 존재하지 않아 컴파일을 하면 제네릭 타입과 관련된 내용이 제거된다.
```kotlin
any is List<Int> // 오류
any is List<*> // OK
```

같은 이유로 다음과 같은 타입 파라미터에 대한 연산도 오류가 발생한다.
```kotlin
fun <T> printTypeName() {
    print(T::class.simpleName) // 오류
}
```

함수를 인라인으로 만들면 이러한 제한을 무시할 수 있다. 함수 호출이 본문으로 대체되므로 reified 한정자를 지정하면 타입 파라미터를 사용한 부분이 타입 아규먼트로 대체된다.
```kotlin
inline fun <reified T> printTypeName2() {
    print(T::class.simpleName)
}

// 사용
printtypeName<Int>()
printtypeName<Char>()
printtypeName<String>()

// 컴파일 후
print(Int::class.simpleName)
print(Char::class.simpleName)
print(String::class.simpleName)
```

reified는 굉장히 유용한 한정자이다. 표준 라이브러리인 filterIsInstance도 특정 타입의 요소를 필터링할 때 활용된다.
```kotlin
class Worker
class Manager
    
val employees: List<Any> = listOf(Worker(), Manager(), Worker())
val workers: List<Worker> = employees.filterIsInstance<Worker>()
```

#### filterIsInstance 함수
```kotlin
inline fun <reified R> Iterable<*>.filterIsInstance(): List<R> {
    val destination = mutableListOf<R>()
    for (element in this) {
        if (element is R) {
            destination.add(element)
        }
    }
    return destination
}

fun main() {
    val items: List<Any> = listOf(1, "Hello", 2.5, "World", 3)

    // String 타입의 요소만 필터링
    val strings: List<String> = items.filterIsInstance<String>()

    println(strings)  // 출력: [Hello, World]
}
```

### 함수 타입 파라미터를 가진 함수가 훨씬 빠르게 동작한다.
모든 함수는 inline 한정자를 붙이면 조금 더 빠르게 동작한다. 함수 호출과 리턴을 위해 점프하는 과정과 백스택을 추적하는 과정이 없기 때문이다.
그래서 표준 라이브러리의 간단한 함수들에는 대부분 inline 한정자가 붙어있다. 하지만 함수 파라미터를 가지지 않는 함수에는 이러한 차이가 큰 성능 차이를 발생시키지 않는다.

함수 리터럴을 사용해 만들어진 이러한 종류의 객체는 어떤 방식으로든 저장되고 유지되어야 한다.
코틀린/JS에서는 자바스크립트가 함수를 일급 객체로 처리하므로, 굉장히 간단하게 변환이 이루어진다. 코틀린/JS에서 함수는 단순한 함수 또는 함수 레퍼런스이다.

#### 일급 객체
다른 객체들에 일반적으로 적용 가능한 연산을 모두 지원하는 객체
* 변수에 할당 가능: 함수를 변수에 저장할 수 있다.
* 함수의 인자로 전달 가능: 함수를 다른 함수의 인자로 전달할 수 있다.
* 함수의 반환값으로 사용 가능: 함수를 다른 함수의 반환값으로 사용할 수 있다.
* 데이터 구조에 저장 가능: 함수를 리스트, 배열, 객체 등과 같은 데이터 구조에 저장할 수 있다.

코틀린/JVM에서는 익명 클래스 또는 일반 클래스를 기반으로 함수를 객체로 만들어낸다.

```kotlin
val lambda: () -> Unit = {
    // 코드
}

// 익명 클래스로 컴파일
Function0<Unit> lambda = new Function0<Unit>() {
    public Unit invoke() {
        // 코드
    }
}

// 일반 클래스로 컴파일
public class Test$lambda implements Function0<Unit> {
    public Unit invoke() {
        // 코드
    }
}

// 사용
Function0 lambda = new Test$lambda()
```

JVM에서 아규먼트가 없는 함수 타입은 Function0 타입으로 변환된다. 다른 타입의 함수는 아래와 같은 형태로 변환된다.
* () -> Unit는 Function0<Unit>로 컴파일
* () -> Int는 Function0<Int>로 컴파일
* (Int) → Int는 Function1<Int, Int>로 컴파일
* (Int, Int) → Int는 Function2<Int, Int, Int>로 컴파일

이러한 모든 인터페이스는 모두 코틀린 컴파일러에 의해 생성된다. 요청이 있을 때 생성되므로, 이를 명시적으로 사용할 수는 없다. 대신 함수 타입을 사용할 수 있다.

#### FunctionN 인터페이스
Kotlin에서 함수 타입을 표현하기 위해 제공되는 인터페이스, 0 ~ 22까지 존재, N은 함수의 파라미터 개수를 의미한다.
* 주요 특징
  * Invoke 메서드: 각 FunctionN 인터페이스는 invoke라는 메서드를 가지고 있다. 이 메서드는 함수 타입이 실제로 호출될 때 실행되는 코드이다.
  * 람다 및 익명 함수의 표현: Kotlin의 람다 식이나 익명 함수는 FunctionN 인터페이스를 구현하는 익명 클래스 객체로 컴파일된다.
  * 고차 함수 지원: FunctionN 인터페이스는 고차 함수에서 함수 타입을 전달하거나 반환할 때 사용된다. 이를 통해 함수형 프로그래밍의 패턴을 효과적으로 지원할 수 있다.
  * JVM 호환성: Kotlin 함수 타입을 JVM에서 사용 가능한 형태로 변환하기 위해 FunctionN 인터페이스가 사용된다. JVM에서 함수는 일반적으로 객체로 표현되므로, FunctionN 인터페이스는 이러한 함수를 객체로 다룰 수 있게 한다.

함수 본문을 객체로 랩(wrap)하면 코드의 속도가 느려지기 때문에 다음과 같은 두 함수가 있을 때 첫 번째 함수가 더 빠르다.
```kotlin
inline fun repeat(times: Int, action: (Int) -> Unit) {
    for (index in 0 until times) {
        action(index)
    }
}

// 랩된 객체는 메모리 할당되고 호출 시 객체를 호출하고 내부 invoke 메서드를 호출해야 하므로 느리다.
fun repeatNoinline(times: Int, action: (Int) -> Unit) {
    for (index in 0 until times) {
        action(index)
    }
}
```

❓아이템 42. compareTo의 규악을 지켜라와 어떤 연관관계 있는 걸까?

인라인 함수와 인라인 함수가 아닌 함수의 더 중요한 차이는 함수 리터럴 내부의 지역 변수를 캡처할 때 확인할 수 있다.
```kotlin
var l = 1L
noinelineRepeat(100_000_000) {
    l += it
}

// 컴파일 후
var a = Ref.LongRef()
a.element = 1L

noinelineRepeat(100_000_000) {
    a.element = a.element + it
}
```

인라인이 아닌 람다 표현식에서는 지역 변수 l을 직접 사용하지 않고 컴파일 과정 중 레퍼런스 객체로 래핑되고 람다 표현식 내부에서 이를 사용한다.

#### @Benchmark 어노테이션
주로 Java 및 Kotlin에서 성능 테스트를 수행하기 위해 사용하는 JMH (Java Microbenchmark Harness) 라이브러리에서 사용되는 어노테이션이다.
이 어노테이션이 붙은 메서드는 JMH에 의해 반복적으로 실행되며, 성능 데이터(예: 실행 시간, 처리량)를 수집한다.
```kotlin
// 평균적으로 30ms
@Benchmark
fun nothingInline(blackhole: Blackhole) {
    var l = 0L
    repeat(100_000_000) {
        l += it
    }
    blackhole.consume(l)
}

// 평균적으로 274ms
@Benchmark
fun repeatNoinline(blackhole: Blackhole) {
    var l = 0L
    noinelineRepeat(100_000_000) {
        l += it
    }
    blackhole.consume(l)
}
```

함수 타입 파라미터를 활용해서 유틸리티 함수를 만들 때는 그냥 인라인을 붙여준다 생각하는 것도 좋다. 이러한 이유로 표준 라이브러리가 제공하는 대부분의 함수 타입 파라미터를 가진 확장 함수는 인라인으로 정의된다.

### 비지역적 리턴(non-local return)을 사용할 수 있다.
```kotlin
fun repeatNoinline(times: Int, action: (Int) -> Unit) {
    for (index in 0 until times) {
        action(index)
    }
}

fun main() {
    repeatNoinline(10) {
        print(it)
        return // 오류
    }
}
```

함수 리터럴이 컴파일 될 때 함수가 객체로 래핑되어서 발생하는 문제이다. 함수가 다른 클래스에 위치하므로 return을 사용해 main으로 돌아올 수 없는 것이다.
인라인 함수라면 제한이 없다. 함수가 main 함수 내부에 박히기 때문이다.
```kotlin
fun main() {
    repeat(10) {
        print(it)
        return
    }
}
```

### inline 한정자의 비용
인라인 함수는 재귀적으로 동작할 수 없다. 재귀적으로 사용하면, 무한하게 대체되는 문제가 발생한다.
```kotlin
inline fun a() { b() }
inline fun b() { c() }
inline fun c() { a() }
```

인라인 함수는 더 많은 가시성 제한을 가진 요소를 사용할 수 없다. pubilc 인라인 함수 내부에서는 private와 internal 가시성을 가진 함수와 프로퍼티를 사용할 수 없다.
```kotlin
internal inline fun read() {
    val reader = Reader() // 오류
}

private class Reader {
}
```

inline 한정자를 남용하면 코드 크기가 쉽게 커진다.
```kotlin
inline fun printTree() {
    print(3)
}

inline fun threePrintThree() {
    printTree()
    printTree()
    printTree()
}

inline fun threeThreePrintThree() {
    threePrintThree()
    threePrintThree()
    threePrintThree()
}

// 컴파일 후
inline fun printTree() {
  print(3)
}

inline fun threePrintThree() {
  print(3)
  print(3)
  print(3)
}

inline fun threeThreePrintThree() {
  print(3)
  print(3)
  print(3)
  print(3)
  print(3)
  print(3)
  print(3)
  print(3)
  print(3)
}
```

### crossinline과 noinline
일부 함수 타입 파라미터는 inline으로 받고 싶지 않은 경우가 있다. 이러한 경우에는 crossinline과 noinline 한정자를 사용한다.
* crossinline: 아규먼트로 인라인 함수를 받지만, 비지역적 리턴을 하는 함수는 받을 수 없게 만든다. 인라인으로 만들지 않은 다른 람다 표현식과 조합해서 사용할 때 문제가 발생하는 경우 활용한다.
> 특정 람다가 인라인 되지 않도록 하여 코드 크기를 줄이거나 함수 객체가 필요한 경우(재사용 등)에 사용한다.
* noinline: 아규먼트로 인라인 함수를 받을 수 없게 만든다. 인라인 함수가 아닌 함수를 아규먼트로 사용하고 싶을 때 활용한다.
> 람다에서 비지역 return을 방지하여 코드의 흐름을 예측 가능하게 만들고, 중첩된 함수 호출에서도 안전한 흐름을 보장하기 위해 사용한다.

```kotlin
inline fun requestNewToken (
    hasToken: Boolean,
    crossinline onRefresh: ()->Unit,
    noinline onGenerate: ()->Unit
) {
    if (hasToken) {
        httpCall("get-token", onGenerate)
        // 인라인이 아닌 함수를 아규먼트로 함수에 전달하려면 noinline을 사용
    } else {
        httpCall("get-token") {
            onRefresh()
            // Non-local 리턴이 허용되지 않는 컨텍스트에서 
            // inline 함수를 사용하고 싶다면 crossinline 사용 
            onGenerate()
        }
    }
}

fun httpCall(url: String, callback: ()->Unit) {
    /* ... */
}
```

```kotlin
// crossinline 사용 예시
inline fun exampleCrossinlineFunction(crossinline block: () -> Unit) {
  println("Before block execution")
  run {
    block() // block에서 return을 사용할 수 없음
  }
  println("After block execution")
}

fun main() {
  exampleCrossinlineFunction {
    println("This is a crossinline block")
    // return 사용 불가
  }
}

// noinline 사용 예시
inline fun exampleFunction(inlineLambda: () -> Unit, noinline noinlineLambda: () -> Unit) {
  println("Before inline lambda")
  inlineLambda()
  println("After inline lambda")

  println("Before noinline lambda")
  noinlineLambda()
  println("After noinline lambda")
}

fun main() {
  exampleFunction(
    inlineLambda = { println("This is an inline lambda") },
    noinlineLambda = { println("This is a noinline lambda") }
  )
}
```

### 정리
인라인 함수가 사용되는 주요 사례
* print 처럼 매우 많이 사용되는 경우
* filterIsInstance 함수처럼 타입 아규먼트로 reified 타입을 전달받는 경우
* 함수 타입 파라미터를 갖는 톱레벨 함수를 정의해야 하는 경우, 컬렉션 처리와 같은 헬퍼(map, filter, flatMap), 스코프 함수(also, apply, let), 톱레벨 유틸리티 함수(repeat, run, with)의 경우