### Lisp 프로그래밍
1958년에 만들어진 고수준 프로그래밍 언어로 리스트(List) 구조와 함수형 프로그래밍을 중심이고 주로 인공지능과 같은 복잡한 계산 작업에 사용되었습니다.

#### 장점
* 유연성: 매크로 시스템을 통해 새로운 구문을 정의할 수 있어 매우 유연하다.
* 동적 타이핑: 변수 타입을 런타임에 결정하므로 유연한 코드 작성이 가능하다.
* 함수형 프로그래밍: 함수를 1급 객체로 다루며, 코드의 재사용성과 추상화 수준을 높일 수 있다.

#### 단점
* 읽기 어려움: 독특한 문법과 괄호의 중첩으로 인해 코드가 읽기 어려울 수 있다.
* 성능: 다른 저수준 언어에 비해 실행 속도가 느릴 수 있다.

#### 사용처
* 인공지능 연구: 초기 AI 연구에서 많이 사용되었다.
* 교육: 함수형 프로그래밍과 알고리즘 교육용으로 사용된다.
* 개발 도구: Emacs 편집기 같은 개발 도구에서 스크립트 언어로 사용된다.
* Lisp는 그 유연성과 강력한 매크로 시스템으로 인해 선호되었다. 초기 해커 문화에서 중요한 역할을 했으며, "해커"가 창의적이고 깊이 있는 프로그래밍을 하는 사람들을 의미하는 것으로 확장되면서, Lisp는 해커들의 상징적인 언어가 되었다.

아이템 49.하나 이상의 처리 단계를 가진 경우에는 시퀀스를 사용하라 
=========================
Sequence는 lazy(지연) 처리된다. 시퀀스 처리 함수들을 사용하면 데코레이터 패턴으로 꾸며진 새로운 시퀀스가 리턴된다.
최종적인 계산은 toList 또는 count 등의 최종 연산이 이루어질 때 수행된다. 반면, Iterator은 처리 함수를 사용할 때마다 연산이 이루어져 List가 만들어진다.

```kotlin
public inline fun <T> Iterable<T>.filter(
    predicate: (T) -> Boolean
): List<T> {
    return filterTo(ArrayList<T>(), predicate)
}

public fun <T> Sequence<T>.filter(
    predicate: (T) -> Boolean
): Sequence<T> {
    return FilteringSequence(this, true, predicate)
}

// filterTo
public inline fun <T, C : MutableCollection<in T>> Iterable<T>.filterTo(destination: C, predicate: (T) -> Boolean): C {
    for (element in this) if (predicate(element)) destination.add(element)
    return destination
}

// FilteringSequence
// 코틀린은 내부적으로 해당 시퀀스의 iterator() 메서드를 호출하여 Iterator 객체를 가져온다. 루프가 iterator의 hasNext, next 함수가 호출되면서 동작 
internal class FilteringSequence<T>(
    private val sequence: Sequence<T>,
    private val sendWhen: Boolean = true,
    private val predicate: (T) -> Boolean
) : Sequence<T> {

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        val iterator = sequence.iterator()
        var nextState: Int = -1 // -1 for unknown, 0 for done, 1 for continue
        var nextItem: T? = null

        private fun calcNext() {
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (predicate(item) == sendWhen) {
                    nextItem = item
                    nextState = 1
                    return
                }
            }
            nextState = 0
        }

        override fun next(): T {
            if (nextState == -1)
                calcNext()
            if (nextState == 0)
                throw NoSuchElementException()
            val result = nextItem
            nextItem = null
            nextState = -1
            @Suppress("UNCHECKED_CAST")
            return result as T
        }

        override fun hasNext(): Boolean {
            if (nextState == -1)
                calcNext()
            return nextState == 1
        }
    }
}
```

컬렉션 처리 연산은 호출할 때 연산이 이루어진다. 반면, 시퀀스 처리 함수는 최종 연산이 이루어지기 전까지 각 단계에서 연산이 일어나지 않는다.

#### 중간 연산
* map: 각 요소에 주어진 변환 함수를 적용하여 새로운 시퀀스를 만든다.
* filter: 주어진 조건을 만족하는 요소들만 포함하는 시퀀스를 만든다.
* flatMap: 각 요소를 다른 시퀀스로 변환하고, 이를 하나의 시퀀스로 병합한다.
* take: 앞에서부터 주어진 수만큼의 요소를 포함하는 시퀀스를 만든다.
* drop: 앞에서부터 주어진 수만큼의 요소를 제외한 시퀀스를 만든다.
* takeWhile: 조건이 참인 동안의 요소들을 포함하는 시퀀스를 만든다.
* dropWhile: 조건이 참인 동안의 요소를 제외한 시퀀스를 만든다.
* distinct: 중복된 요소를 제거한 시퀀스를 만든다.
* sorted: 요소들을 정렬된 순서로 포함하는 시퀀스를 만든다.
* zip: 두 시퀀스를 병합하여 쌍(pair)을 이루는 시퀀스를 만든다.
* withIndex: 각 요소와 그 인덱스를 쌍으로 갖는 시퀀스를 만든다.
* chunked: 시퀀스를 주어진 크기만큼의 리스트로 분할하여 새로운 시퀀스를 만든다.

#### 최종 연산
* toList / toSet / toMap: 시퀀스의 모든 요소를 리스트, 세트, 또는 맵으로 변환하여 반환한다.
* reduce: 시퀀스의 요소들을 왼쪽부터 차례대로 누적하여 단일 결과를 반환한다.
* fold: 초기값과 함께 시퀀스의 요소들을 왼쪽부터 차례대로 누적하여 단일 결과를 반환한다.
* forEach: 시퀀스의 각 요소에 주어진 함수를 적용하며, 반환값이 없는 연산이다.
* count: 시퀀스의 요소 개수를 반환한다.
* sum / sumBy / sumOf: 시퀀스의 요소들의 합을 계산하여 반환한다.
* average: 시퀀스의 숫자 요소들의 평균값을 반환한다.
* max / min: 시퀀스에서 최대값 또는 최소값을 반환한다.
* first / last: 시퀀스의 첫 번째 또는 마지막 요소를 반환하며, 요소가 없으면 예외를 던진다.
* firstOrNull / lastOrNull: 시퀀스의 첫 번째 또는 마지막 요소를 반환하며, 요소가 없으면 null을 반환한다.
* find: 조건을 만족하는 첫 번째 요소를 반환하며, 없으면 null을 반환한다.
* any: 시퀀스의 요소 중 하나라도 주어진 조건을 만족하면 true를 반환한다.
* all: 시퀀스의 모든 요소가 주어진 조건을 만족하면 true를 반환한다.
* none: 시퀀스의 모든 요소가 주어진 조건을 만족하지 않으면 true를 반환한다.
* indexOf: 시퀀스에서 특정 요소의 첫 번째 인덱스를 반환하며, 없으면 -1을 반환한다.
* joinToString: 시퀀스의 요소들을 문자열로 연결하여 반환한다.

#### 시퀀스의 지연 처리의 장점
* 자연스러운 처리 순서를 유지
* 최소한만 연산
* 무한 시퀀스 형태로 사용 가능
* 각각의 단계에서 컬렉션을 만들지 않음

### 순서의 중요성
시퀀스 처리는 요소 하나하나에 지정한 연산을 한꺼번에 적용한다. 이를 element-by-element order 또는 lazy order라고 부른다.
반면 이터러블은 요소 전체를 대상으로 연산을 차근차근 적용한다. 이를 step-by-step order 또는 eager order라고 부른다.
```kotlin
sequenceOf(1,2,3)
	.filter { print("F$it, "); it % 2 == 1 }
    .map { print("M$it, "); it * 2 }
    .forEach { print("E$it, ") }
// print : F1, M1, E2, F2, F3, M3, E6,

listOf(1,2,3)
	.filter { print("F$it, "); it % 2 == 1 }
    .map { print("M$it, "); it * 2 }
    .forEach { print("E$it, ") }
// print : F1, F2, F3, M1, M3, E2, E6,

// 일반 반복문 사용
for (e in listOf(1,2,3)) {
    print("F$e, ")
    if (e % 2 == 1) {
        print("M$e, ")
        val mapped = e * 2
        print("E$mapped, ")
    }
}
// print : F1, M1, E2, F2, F3, M3, E6,
```

반복문을 이용해 다음과 같이 구현한다면 시퀀스와 같다. 따라서 시퀀스 처리에서 사용되는 element by element order가 훨씬 자연스러운 처리라고 할 수 있다.

### 최소 연산
중간 처리 단계를 모든 요소에 적용할 필요가 없는 경우에는 시퀀스를 사용하는 것이 좋다.
```kotlin
(1..10).asSequence()
     .filter { print("F$it, "); it % 2 == 1 }
     .map { print("M$it, "); it * 2 }
     .find { it > 5 }
     // F1, M1, F2, F3, M3,

(1..10)
     .filter { print("F$it, "); it % 2 == 1 }
     .map { print("M$it, "); it * 2 }
     .find { it > 5 }
     // F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, M1, M3, M5, M7, M9,
```

### 무한 시퀀스
시퀀스는 실제로 최종 연산이 일어나기 전까지는 컬렉션에 어떠한 처리도 하지 않는다. 따라서 무한 시퀀스를 만들고 필요한 부분까지만 값을 추출하는 것도 가능하다.
무한 시퀀스를 만드는 일반적인 방법은 generateSequence 또는 sequence를 사용하는 것이다. 먼저 generateSequence는 '첫 번째 요소'와 '그 다음 요소를 계산하는 방법'을 지정해야 한다.
```kotlin
generateSequence(1) { it + 1 }
	.map { it * 2 } 
    .take(10)
    .forEach { print("$it, ") }
// 2, 4, 6, 8, 10, 12, 14, 16, 18, 20,
```

두 번째로 sequence는 중단 함수로 요소들을 지정한다. 시퀀스 빌더는 중단 함수 내부에서 yield로 값을 하나씩 만들어 낸다.
```kotlin
val fibonacci = sequence {
	yield(1)
    var current = 1
    var prev = 1
    while (true) {
    	yield(current)
        val temp = prev
        prev = current
        current += temp
    }
}

print(fibonacci.take(10).toList())
// [1, 1, 2, 3, 5, 8, 13, 21, 34, 55]
```

무한 시퀀스를 실제로 사용할 때는 값을 몇 개 활용할지 지정해야 한다. 그렇지 않으면 무한 반복한다.
이전 코드처럼 take를 사용하거나, first, find, any, all, none, indexOf와 같은 일부 요소만 선택하는 종격 연산을 활용해야 한다.
any는 true를 리턴하지 못하면 무한 반복에 빠지고 all과 none은 false를 리턴하지 못하면 무한 반복에 빠진다.

### 각각의 단계에서 컬렉션을 만들어 내지 않음
표준 컬렉션 처리 함수는 각각의 단계에서 새로운 컬렉션을 만들어낸다. 각각의 단계에서 만들어진 결과를 활용하거나 저장할 수 있다는 것은 컬렉션의 장점이지만, 가각의 단계에서 결과가 만들어지면서 공간을 차지하는 비용이 든다는 것은 큰 단점이다.
```kotlin
numbers
    .filter { it % 10 == 0 } // 여기에서 컬렉션 하나
    .map { it * 2 } // 여기에서 컬렉션 하나
    .sum()
    // 전체적으로 2개의 컬렉션이 만들어진다.

numbers
    .asSequence()
    .filter { it % 10 == 0 }
    .map { it * 2 }
    .sum()
    // 컬렉션이 만들어지지 않는다.
```

useLines를 사용하면, Sequence<String>을 사용할 수 있다. 단, 파일의 줄을 한 번만 사용할 수 있다. (아이템 9.use를 사용하여 리소스를 닫아라 참조)
```kotlin
public inline fun <T> Reader.useLines(block: (Sequence<String>) -> T): T =
    buffered().use { block(it.lineSequence()) }
```

처리 단계가 많아질수록 비용에 대한 차이가 커지므로, 큰 컬렉션으로 여러 처리 단계를 커쳐야 한다면, 시퀀스 처리가 더 효율적이다.
filter 함수는 인라인이므로 작은 컬렉션과 처리 단계가 적다면, 일반 컬렉션이 더 빠르다. (아이템 46.함수 타입 파라미터를 갖는 함수에 inline 한정자를 붙여라 참조)

### 시퀀스가 빠르지 않은 경우
컬렉션 전체를 기반으로 처리해야 하는 연산은 시퀀스를 사용해도 빠르지 않다. 유일한 예로 코틀린 stdlib의 sorted가 있다.
sorted는 sequence를 list로 변환한뒤에, 자바 stdlib의 sort를 사용해 처리한다. 이러한 변환 처리로 인해서, 시퀀스가 컬렉션 처리보다 느려진다.
참고로 무한 시퀀스처럼 시퀀스의 다음 요소를 lazy하게 구하는 시퀀스에 sorted에 적용하면 무한 반복에 빠진다.
```kotlin
generateSequence(0) { it + 1 }.take(10).sorted().toList()
// [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]

generateSequence(0) { it + 1}.sorted().take(10).toList()
// not return
```

### 자바 스트림의 경우
자바 8부터 컬렉션 처리를 위해 스트림 기능이 추가됐다. 코틀린의 시퀀스와 비슷한 형태로 동작한다.
```kotlin
productList.asSequence()
	.filter { it.bought }
    .map { it.price }
    .average()
    
productList.stream()
	.filter { it.bought }
    .mapToDouble { it.price }
    .average()
    .orElse(0.0)
```

자바 8의 스트림도 lazy하게 동작하며, 마지막 처리 단계에서 연산이 일어난다.

#### 시퀀스와 스트림의 차이
* 시퀀스가 더 많은 처리함수를 갖고 있고 사용하기 더 쉽다.
* 스트림은 병렬 함수를 사용해서 병렬 모드로 실행할 수 있다.
* 시퀀스는 코틀린/JVM, 코틀린/JS, 코틀린/네이티브 등의 일반적인 모듈에서 모두 사용할 수 있다. 스트림은 코틀린/JVM, JVM 8 버전 이상일 때만 동작한다.

병렬 모드로 성능적 이득을 얻을 수 있는 곳에서만 자바 스트림을 사용하고, 이외의 일반적인 경우에는 코틀린 시퀀스를 사용하는 것이 좋다.

#### 스트림의 병렬 모드
스트림은 병렬 처리를 쉽게 수행할 수 있도록 parallelStream() 또는 stream().parallel() 메서드를 제공합니다. 병렬 스트림을 사용하면 데이터를 여러 CPU 코어에서 동시에 처리할 수 있어, 특히 대용량 데이터나 복잡한 연산에 대해 성능을 크게 향상시킬 수 있다.
> 병렬 스트림은 내부적으로 Fork/Join 프레임워크를 사용한다. 이 프레임워크는 작업을 여러 작은 작업으로 나누고, 이를 다양한 코어에서 병렬로 처리한 후 결과를 합치는 방식으로 동작한다.
```java
import java.util.Arrays;
import java.util.List;

public class ParallelStreamExample {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // 순차 스트림 사용
        int sumSequential = numbers.stream()
            .map(n -> n * n) // 각 숫자를 제곱
            .reduce(0, Integer::sum); // 합계 구하기

        System.out.println("Sequential sum: " + sumSequential);

        // 병렬 스트림 사용
        int sumParallel = numbers.parallelStream()
            .map(n -> n * n) // 각 숫자를 제곱
            .reduce(0, Integer::sum); // 합계 구하기

        System.out.println("Parallel sum: " + sumParallel);
    }
}
```

#### 병렬 스트림의 문제점
* 성능 오버헤드: 병렬 스트림은 Fork/Join 프레임워크를 사용하여 작업을 병렬로 처리하지만, 작은 데이터셋이나 간단한 작업에서는 오히려 성능이 저하될 수 있다.
* 순서 보장 문제: 병렬 스트림은 기본적으로 순서를 보장하지 않으며, 순서가 중요한 작업에서는 예기치 않은 결과가 발생할 수 있다.
* 공유 상태 문제: 병렬 스트림에서 여러 스레드가 공유 자원에 동시 접근할 경우 경쟁 조건이 발생할 수 있다.
* Boxing/Unboxing 오버헤드: 기본형 타입을 처리할 때 오토박싱과 언박싱으로 인해 성능이 저하될 수 있다. (스트림은 일반적으로 객체(참조형 타입)를 처리하는 방식으로 동작)
* 데드락 위험: 병렬 스트림과 동기화된 코드가 결합될 때 데드락이 발생할 위험이 있다.
* 불균형 작업 분배: 병렬 스트림의 작업 분할이 불균형하게 이루어질 경우 성능 저하로 이어질 수 있다.
* 스레드 풀 관리: 병렬 스트림은 공유된 Fork/Join 스레드 풀을 사용하기 때문에, 과도한 사용 시 스레드 풀이 포화 상태가 되어 다른 작업의 성능에 영향을 줄 수 있다.

### 코틀린 시퀀스 디버깅
시퀀스와 스트림 모두 단계적으로 요소의 흐름을 추적할 수 있는 디버깅 기능이 지원된다.
시퀀스는 'Kotlin Sequence Debugger'라는 이름의 플러그인, 자바 스트림은 'Java Stream Debugger'라는 이름의 플러그인을 활용하면 된다. (지금은 제공 안하는듯 하다...ㅠ)