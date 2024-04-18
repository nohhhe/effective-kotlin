아이템 5. 예외를 활용해 코드에 제한을 걸어라
=========================
확실하게 어떤 형태로 동작해야 하는 코드가 있다면 예외를 활용해 제한을 걸어주는 게 좋다.
* require 블록 : 아규먼트를 제한할 수 있다
* check 블록 : 상태와 관련된 동작을 제한할 수 있다
* assert 블록 : 어떤 게 true인지 확인할 수 있다. assert 블록은 테스트 모드에서만 작동한다
* return 또는 throw와 같이 쓰는 Elvis 연산자


```kotlin
fun pop(num: Int = 1): List<T> {
    require(num <= size) {
        "Cannot remove more elements than current size"
    }
    check(isopen) { "Cannot pop from closed stack" }
    val let = collection.take(num)
    collection = collection.drop(num)
    assert(ret.size == num)
    return ret
}
```

#### 제한을 걸었을 때의 장점
* 제한을 걸면 문서를 읽지 않은 개발자도 문제를 확인할 수 있다
* 문제가 있을 경우 함수가 예상 못한 동작을 하지 않고 예외를 throw한다. 예상 못한 동작을 하는 건 예외를 throw하는 것보다 위험하며 상태를 관리하는 게 굉장히 힘들다. 이런 제한으로 문제를 놓치지 않을 수 있고 코드가 더 안정적으로 작동하게 된다
* 코드가 어느 정도 자체적으로 검사된다. 따라서 이와 관련된 단위 테스트를 줄일 수 있다
* 스마트 캐스트 기능을 활용할 수 있게 되서 캐스트(타입 변환)를 적게 할 수 있다

### 아규먼트
함수를 정의할 때 타입 시스템을 활용해서 아규먼트(argument)에 제한을 거는 코드를 많이 쓴다.

```kotlin
// 숫자를 아규먼트로 받아서 팩토리얼을 계산한다면 숫자는 양의 정수여야 한다
fun factorial(n: Int): Long {
    require(n >= 0)
    return if (n <= 1) 1 else factorial(n - 1) * n
}

// 좌표들을 아규먼트로 받아서 클러스터를 찾을 때는 비어 있지 않은 좌표 목록이 필요하다
fun findClusters(points: List<Point>): List<Cluster> {
    require(points.isNotEmpty())
    // ...
}

// 사용자로부터 이메일 주소를 입력받을 때는 값이 입력돼 있는지, 이메일 형식이 맞는지 확인해야 한다
fun sendEmail(user: User, message: String) {
    requireNotNull(user.email)
    require(isValidEmail(user.email))
    // ...
}
```

일반적으로 아규먼트 관련 제한을 걸 때는 require()를 쓴다. require()는 제한을 확인하고 제한을 만족하지 못할 경우 예외를 throw한다.

require()는 조건을 만족하지 못할 때 무조건적으로 IllegalArgumentException을 발생시키므로 제한을 무시할 수 없다. 일반적으로 이런 처리는 함수의 가장 앞부분에 하게 되므로 코드를 읽을 때 쉽게 확인할 수 있다.

또한 아래 방법으로 람다를 써서 지연 메시지를 정의할 수도 있다.

```kotlin
fun factorial(n: Int): Long {
    require(n >= 0) { "Cannot calculate factorial of $n because it is smaller than 0" }
    return if (n <= 1) 1 else factorial(n - 1) * n
}
```

### 상태
어떤 구체적 조건을 만족할 때만 함수를 사용할 수 있게 해야 할 때가 있다. 상태와 관련된 제한을 걸 때는 일반적으로 check()를 쓴다.
```kotlin
// 어떤 객체가 미리 초기화되어 있어야만 처리하게 하고 싶은 함수
fun speak(text: String) {
    check(isInitialized)
    // ...
}

// 사용자가 로그인했을 때만 처리를 하게 하고 싶은 함수

fun getUserInfo(): UserInfo {
    checkNotNull(token)
    // ...
}

// 객체를 쓸 수 있는 시점에 사용하고 싶은 함수
fun next(): T {
    check(isOpen)
    // ...
}
```
check()는 require()과 비슷하지만 지정된 예측을 만족하지 못할 때 IllegalStateException을 throw한다. 상태가 올바른지 확인할 때 사용한다.
예외 메시지는 require()과 마찬가지로 지연 메시지를 사용해서 변경할 수 있다. 함수 전체에 어떤 예측이 있을 때는 일반적으로 require 블록 뒤에 배치한다. check()를 나중에 하는 것이다.

### Assert 계열 함수 사용
함수가 올바르게 구현됐다면 확실하게 참을 낼 수 있는 코드들이 있다. 그런데 함수가 올바르게 구현돼 있지 않을 수도 있다. 
처음부터 구현을 잘못했을 수도 있고 해당 코드를 이후에 다른 누군가가 변경(or 리팩토링)해서 제대로 작동하지 않게 된 걸 수도 있다. 
이런 구현 문제로 발생할 수 있는 추가적 문제를 예방하려면 단위 테스트를 사용하는 게 좋다.

```kotlin
class StackTest {
    @Test
    fun `Stack pops correct number of elements`() {
        val stack = Stack(20) { it }
        val let = stack.pop(10)
        assertEquals(10, ret.size)
    }
}
```

단위 테스트는 구현의 정확성을 확인하는 가장 기본적인 방법이다.

```kotlin
fun pop(num: Int = 1): List<T> {
    // ...
    assert(ret.size == num)
    return ret
}
```

이런 코드도 코드가 예상대로 동작하는지 확인하므로 테스트라고 할 수 있다. 다만 프로덕션 환경에선 오류가 발생하지 않는다. 테스트를 할 때만 활성화되므로 오류가 발생해도 사용자가 알아차릴 수는 없다.
만약 이 코드가 정말 심각한 오류고 심각한 결과를 초래할 수 있는 경우엔 check를 쓰는 게 좋다.

#### 단위 테스트 대신 함수에서 assert를 썻을 때 장점 
* Assert 계열 함수는 코드를 자체 점검하며 더 효율적으로 테스트할 수 있게 해준다
* 특정 상황이 아닌 모든 상황에 대한 테스트를 할 수 있다
* 실행 시점에 정확하게 어떻게 되는지 확인할 수 있다
* 실제 코드가 더 빠른 시점에 실패하게 만든다. 따라서 예상 못한 동작이 언제 어디서 실행됐는지 쉽게 찾을 수 있다

이걸 활용해도 단위 테스트는 따로 작성해야 한다. 표준 애플리케이션 실행에서는 assert가 예외를 throw하지 않는다.

### nullability와 스마트 캐스팅
코틀린에서 require와 check 블록으로 어떤 조건을 확인해서 true가 나왔다면 해당 조건은 이후로도 true일 거라고 가정한다.

```kotlin
public inline fun require(value: Boolean): Unit {
    // 컴파일러에게 함수가 반환될 때 value가 true라는 것을 알려준다.
    contract { 
        returns() implies value
    }
    require(value) {
        "Failed requirement"
    }
}
```

따라서 이를 활용해 타입 비교를 했다면 스마트 캐스트가 작동한다.

```kotlin
fun changeDress(person: Person) {
    require(person.outfit is Dress)
    val dress: Dress = person.outfit
    // ...
}
```

이런 특징은 어떤 대상이 null인지 확인할 때 굉장히 유용하다.

```kotlin
class Person(val email: String?)

fun sendEmail(person: Person, message: String) {
    require(person.email != null)
    val email: String = person.email
}
```

이 경우 requireNotNull, checkNotNull이란 특수 함수를 써도 괜찮다. 둘 다 스마트 캐스트를 지원하므로 변수를 언팩하는 용도로 활용할 수 있다.
    
```kotlin
class Person(val email: String?)
fun validateEmail(email: String) { /* ... */ }

fun sendEmail(person: Person, text: String) {
    val email = requireNotNull(person.email)
    validateEmail(email)
}

fun sendEmail(person: Person, text: String) {
    requireNotNull(person.email)
    validateEmail(person.email)
    // ...
}
```

nullability를 목적으로 오른쪽에 throw 또는 return을 두고 엘비스 연산자를 활용하는 경우가 많다. 첫 번째로 오른쪽에 return을 넣으면 오류를 발생시키지 않고 단순하게 함수를 중지할 수도 있다.

```kotlin
fun sendEmail(person: Person, text: String) {
    val email: String = person.email ?: return
}
```

프로퍼티에 문제가 있어서 null일 때 여러 처리를 해야 할 때도 return/throw와 run {}를 조합해서 활용하면 된다. 이는 함수가 중지된 이유를 로그에 출력해야 할 때 사용할 수 있다.

```kotlin
fun sendEmail(person: Person, text: String) {
    val email: String = person.email ?: run {
        log("Email not sent, no email address")
        return
    }
    // ...
}
```

이처럼 return, throw를 활용한 엘비스 연산자는 nullable을 확인할 때 굉장히 많이 쓰이는 관용적인 방법이다. 이런 코드는 함수의 앞부분에 넣어서 잘 보이게 만드는 게 좋다.

### 정리
예외를 활용해 제한을 걸면 아래와 같은 장점이 있다.
1. 제한을 훨씬 더 쉽게 확인할 수 있다.
2. 애플리케이션을 더 안정적으로 지킬 수 있다.
3. 코드를 잘못 쓰는 상황을 막을 수 있다.
4. 스마트 캐스팅을 활용할 수 있다.

#### 예외를 활용한 방법
1. require 블록: 아규먼트와 관련된 예측을 정의할 때 사용
2. check 블록: 상태와 관련된 예측을 정의할 때 사용
3. assert 블록: 테스트 모드에서 테스트를 할 때 사용
4. return과 throw와 함께 Elvis 연산자 사용