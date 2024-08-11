아이템 45.불필요한 객체 생성을 피하라 
=========================
상황에 따라서는 객체 생성에 굉장히 큰 비용이 들어갈 수 있다. 따라서 불필요한 객체 생성을 피하는 것이 최적화의 관점에서 좋다.

JVM에서는 하나의 가상 머신에서 동일한 문자열을 처리하는 코드가 여러 개 있다면, 기준의 문자열을 재사용한다.
```kotlin
val str1 = "Lorem"
val str2 = "Lorem"

print(str1 == str2) // true
print(str1 === str2) // true
```

Integer와 Long처럼 박스화한 기본 자료형도 작은 경우 재사용 된다.(Integer는 -128 ~ 127 범위를 캐싱해둔다.)

```kotlin
val i1: Int? = 1
val i2: Int? = 1

println(i1 == i2) // true
println(i1 === i2) // true

// -128 ~ 127 범위를 벗어나면 새로운 객체를 생성한다.
val j1: Int? = 1024
val j2: Int? = 1024

println(j1 == j2) // true
println(j1 === j2) // false
```

Int를 사용하면 일반적으로 기본 자료형 int로 컴파일된다. 하지만 nullable이나 타입 아규먼트로 사용할 경우 Integer로 컴파일된다. 이러한 메커니즘은 객체 생성 비용에 큰 영향을 준다.

```kotlin
// 기본 Int 타입 (Java의 원시 타입 int로 컴파일)
    val nonNullableInt: Int = 42
    println("nonNullableInt: ${nonNullableInt::class.java}") // 출력: int

    // Nullable Int 타입 (Java의 Integer로 컴파일)
    val nullableInt: Int? = 42
    println("nullableInt: ${nullableInt!!::class.java}") // 출력: class java.lang.Integer
```

### 객체 생성 비용은 항상 클까?
객체를 랩하면 발생하는 세가지 비용
* 객체는 더 많은 용량을 차지한다. 객체 뿐만 아니라 객체에 대한 레퍼런스 또한 공간을 차지한다.
```
헤더 사이즈
    > JVM 64비트는 16바이트
        - Mark Word: 8바이트 (hashCode, GC 정보, Lock 정보 등 정보가 존재)
        - Class Pointer: 8바이트 (어떤 클래스이 인스턴스인지 가르킴, Compressed Oops 사용 시 4바이트로 줄어듬)
        ! Compressed Oops: 64비트 JVM에서 32비트 포인터를 사용해 객체 포인터의 크기를 줄여 메모리 사용량을 최적화하는 기술, 압축된 포인터는 사용될 때 다시 64비트 주소로 변환
    > JVM 32비트는 8바이트
        - Mark Word: 4바이트
        - Class Pointer: 4바이트
    ! 배열 객체인 경우 Array Length로 추가로 4바이트를 사용
    
객체의 크기
    > JVM 32비트는 8바이트
    > JVM 64비트는 16바이트
    > 객체는 8의 배수로 정렬 (헤더 + 필드 외 공간은 패딩이 추가됨)
```
* 요소가 캡슐화되어 있다면 접근에 추가적인 함수 호출이 필요하다. 이 비용은 굉장히 적지만 수 많은 객체를 처리하면 이 비용도 굉장히 커진다. 
* 객체는 생성되어야 한다. 메모리 영역에 할당되고, 이에 대한 레퍼런스를 만드는 등의 작업이 필요하다. 이는 적은 비용이지만 모이면 큰 비용이 된다.

``` kotlin
class A
private val a = A()

// 2.698 ns/op
fun accessA(blackhole: Blackhole) {
    blackhole.consume(a)
}

// 3.814 ns/op
fun accessA(blackhole: Blackhole) {
    blackhole.consume(A())
}

// 3828.540 ns/op
fun accessA(blackhole: Blackhole) {
    blackhole.consume(List(1000) { a })
}

// 5322.857 ns/op
fun accessA(blackhole: Blackhole) {
    blackhole.consume(List(1000) { A() })
}
```

객체를 제거하면 위에서 말한 세가지 비용 모두를 피할 수 있으며 객체를 재사용하면 첫번째와 세번째에 대한 비용을 제거할 수 있다.

### 객체 선언
객체를 재사용하는 간단한 방법은 객체 선언을 사용하는 것이다.(싱글톤)

```kotlin
sealed class LinkedList<T>

class Node<T>(
    val head: T,
    val tail: LinkedList<T>
): LinkedList<T>()

class Empty<T>: LinkedList<T>()

val list1: LinkedList<Int> = Node(1, Node(2, Node(3, Empty())))
val list2: LinkedList<String> = Node("A", Node("B", Empty()))
```

위 구현에서는 List를 만들 때 마다 Empty 인스턴스를 만들어야 한다는 문제점이 있다.
Empty를 하나만 만들고 모든 리스트에서 활용할 수 있게 한다면 좋겠지만 제네릭 타입이 일치하지 않아 문제가 될 수 있다.
이를 해결하기 위해 LinkedList<Nothing>을 활용할 수 있다. 여기서 리스트는 immutable이고 이 타입은 out 위치에만 사용되므로 현재 상황에서 타입 아규먼트를 covariant로 만드는 것은 의미있는 일이다.

```kotlin
sealed class LinkedList<out T>

class Node<T>(
    val head: T,
    val tail: LinkedList<T>
): LinkedList<T>()

object Empty: LinkedList<Nothing>()

val list1: LinkedList<Int> = Node(1, Node(2, Node(3, Empty)))
val list2: LinkedList<String> = Node("A", Node("B", Empty))
```

이러한 트릭은 immutable sealed 클래스를 정의할 때 자주 사용된다. mutable 객체에 사용하면 공유 상태 관리와 관련된 버그를 검출하기 어려울 수 있으므로 좋지 않다. mutable 객체는 캐시하지 않는다는 규칙을 지키는 것이 좋다.

### 캐시를 활용하는 팩토리 함수
일반적으로 객체는 생성자를 사용해서 만든다. 하지만 팩토리 메소드를 사용해서 만드는 경우도 있다. 팩토리 함수는 캐시를 가질 수 있으며 팩토리 함수에서는 항상 같은 객체를 리턴하게 만들 수 있다.

```kotlin
// stdlib의 emptyList
internal object EmptyList : List<Nothing>, Serializable, RandomAccess { //... }
public fun <T> emptyList(): List<T> = EmptyList
```

객체 생성이 무겁거나, 동시에 여러 mutable 객체를 사용해야 하는 경우에는 객체 풀을 사용하는 것이 좋다. (코루틴의 디폴트 디스패처, DB의 커넥션 풀 등)

```kotlin
private val connections = mutableMapOf<String, Connection>()

fun getConnection(host: String) = connections.getOrPut(host) { createConnection(host) }

private val FIB_CACHE = mutableMapOf<Int, BigInteger>()

fun ib(n: Int): BigInteger = FIB_CACHE.getOrPut(n) {
    if (n <= 1) BigInteger.ONE else fib(n - 1) + fib(n - 2)
}
```

모든 순수 함수는 캐싱을 활용할 수 있으며 이를 메모이제이션(memoization)이라고 부른다. 메모이제이션은 캐시를 위한 Map을 저장해야 하므로 더 많은 메모리를 사용하는 단점이 있다.

메모리가 필요할 때 가비지 컬렉터가 자동으로 메모리를 해제해주는 SoftReference를 사용하면 더 좋다.

#### WeakReference와 SoftReference 차이
* WeakReference: 객체에 대한 약한 참조(weak reference)를 유지한다. 약한 참조는 가비지 컬렉션을 방해하지 않으므로, 메모리 부족 상황이 아니더라도 가비지 컬렉터가 필요에 따라 이 객체를 제거할 수 있다.
```kotlin
class WeakReferenceExample {
    fun doSomething() {
        println("Doing something...")
    }
}

fun main() {
    var weakReferenceExample: WeakReferenceExample? = WeakReferenceExample()
    val weakRef = WeakReference(weakReferenceExample)

    println("Before GC (WeakReference): ${weakRef.get()}")

    System.gc()

    println("After GC (WeakReference): ${weakRef.get()}")

    // 참조 해제
    weakReferenceExample = null

    println("After setting weakReferenceExample to null (WeakReference): ${weakRef.get()}")

    System.gc()

    println("After GC with nullified weakReferenceExample (WeakReference): ${weakRef.get()}")
}
```
* SoftReference: 객체에 대한 강한 참조가 없는 경우에도 메모리가 부족해지기 전까지는 객체를 유지한다. 가비지 컬렉터는 메모리가 부족할 때만 SoftReference로 참조된 객체를 수거한다. 이 때문에, SoftReference는 캐시를 구현할 때 유용하다. 객체가 메모리에 남아 있을 수 있으며, 필요할 때 다시 사용할 수 있기 때문이다.
```kotlin
class SoftReferenceExample {
    fun doSomething() {
        println("Doing something...")
    }
}

fun main() {
    val softReferenceExample = SoftReferenceExample()
    val softRef = SoftReference(softReferenceExample)

    println("Before GC (SoftReference): ${softRef.get()}")

    System.gc()

    println("After GC (SoftReference): ${softRef.get()}")

    // 메모리 압박을 주기 위해 큰 객체 생성
    try {
        val largeArray = Array(100_000_000) { ByteArray(1024) }
    } catch (e: OutOfMemoryError) {
        println("OutOfMemoryError: ${e.message}")
    }

    System.gc()

    println("After memory pressure GC (SoftReference): ${softRef.get()}")
}
```

### 무거운 객체를 외부 스코프로 보내기
무거운 객체를 외부 스코프로 보내는 방법이 있다. 컬렉션 처리에서 이루어지는 무거운 연산은 컬렉션 처리 함수 내부에서 외부로 빼는 것이 좋다.
```kotlin
fun <T: Comparable<T>> Iterable<T>.countMax(): Int {
    return count { it == this.max() }
}

// 무거운 객체를 외부로 보낸 경우
fun <T: Comparable<T>> Iterable<T>.countMax(): Int {
    val max = this.max()
    return count { it == max }
}

fun String.isValidIpAddress(): Boolean {
    return this.matches("\\A(?:(?:25[0-5]2[0-4]....\\z".toRegex())
}

// 무거운 객체를 외부로 보냄 + 지연 초기화
private val IS_VALID_EMAIL_REGEX by lazy {
    "\\A(?:(?:25[0-5]2[0-4]....\\z".toRegex()
}

fun String.isValidIpAddress(): Boolean {
    return this.matches(IS_VALID_EMAIL_REGEX)
}
```

### 지연 초기화
무거운 클래스를 만들 때 지연되게 만드는 것이 좋을 때가 있다.
```kotlin
class A {
    val b by lazy { B() }
    val c by lazy { D() }
    val d by lazy { D() }
    
    // ..
}
```

다만 이러한 지연 초기화는 단점도 가지고 있다.
* 처음 호출 때 무거운 객체의 초기화가 필요해 첫번째 호출 때 응답 시간이 오래 걸린다.
* 성능 테스트가 복잡해지는 문제가 있다.

### 기본 자료형 사용하기
JVM은 숫자와 문자 등의 기본 요소를 나타내기 위한 기본 자료형을 가지고 있다.

코틀린 / JVM 컴파일러는 내부적으로 이러한 기본 자료형을 사용하지만 다음과 같은 상황에서는 기본 자료형을 랩(wrap)한 자료형이 사용된다.
* nullable 타입을 연산할 때(기본 자료형은 null일 수 없으므로)
* 타입을 제네릭으로 사용할 때

```kotlin
fun Iterable<Int>.maxOrNull(): Int? {
    var max: Int? = null
    for (i in this) {
        max = if(i > (max ?: Int.MIN_VALUE)) i else max
    }
    return max
}

// 기본 자료형을 사용한 경우
fun Iterable<Int>.maxOrNull(): Int? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var max: Int = iterator.next()
    while (iterator.hasNext()) {
        val e = iterator.next()
        if (max < e) max = e
    }
    return max
}
```

숫자와 관련된 연산은 큰 컬렉션을 처리할 떄 외에는 차이를 확인할 수 없다. 결과적으로 코드와 라이브러리의 성능이 굉장히 중요한 부분에서만 적용하는 것이 좋다.(프로파일러를 활용하면 어떤 부분이 성능에 중요한 역할을 하는지 찾을 수 있다.)

#### 프로파일러 종류
* JProfiler: Java 애플리케이션의 분석 도구, 사용자 인터페이스가 직관적이며, 실시간 프로파일링을 통해 성능 문제를 쉽게 파악할 수 있다.
  * 특징: CPU 프로파일링, 메모리 프로파일링, 쓰레드 디버깅, SQL 및 NoSQL 데이터베이스 프로파일링 등을 지원
* VisualVM: JDK와 함께 제공되는 무료 도구, Java 애플리케이션의 성능을 분석할 수 있다.
  * 특징: 실시간 모니터링, 스레드 분석, 힙 덤프 분석, 가비지 컬렉션 분석 등을 지원
* YourKit: Java와 Kotlin 애플리케이션의 프로파일링 도구, CPU, 메모리, 쓰레드 사용에 대한 심층 분석을 지원하며, 성능 문제를 빠르게 파악할 수 있다.
  * 특징: 사용자 친화적인 인터페이스, 정교한 CPU 및 메모리 프로파일링, 원격 프로파일링 지원 등
* 그외 (Eclipse MAT, IntelliJ IDEA Profiler)