아이템 17. 이름 있는 아규먼트를 사용하라
=========================
코드에서 아규먼트의 의미가 명확하지 않은 경우가 있다.

```kotlin
val text = (1..10).joinToString("ㅣ")
```

joinToString()에 대해 알고 있다면 "|"이 구분자라는 걸 알 것이다. 하지만 모른다면 접두사로 생각할 수도 있다.

파라미터가 명확하지 않은 경우에는 이를 직접 지정해서 명확하게 만들 수 있다. 아래 코드처럼 이름 있는 아규먼트를 사용하면 된다.

```kotlin
val text = (1..10).joinToString(separator = "I")

// 변수를 사용하여 의미를 명확하게 하는 경우
val separator = "|"
val text = (1..10).joinToString(separator)

// 그래도 실제 코드에서 제대로 사용하는 지 알 수 없으므로, 이름 있는 아규먼트를 사용하는 것이 좋다.
val separator = "|"
val text = (1..10).joinToString(separator = separator)
```

### 이름 있는 아규먼트는 언제 사용해야 할까?
#### 아규먼트를 사용할 때 장점
* 이름을 기반으로 값이 무엇을 나타내는지 알 수 있다.
* 파라미터 입력 순서와 상관 없으므로 안전하다.

```kotlin
// 100ms, 100s인지 명확하지 않음
sleep(100)

// 이름있는 아규먼트를 사용하여 명확하게 처리
sleep(timeMillis = 100)

// 함수를 만들어서 시간 단위를 표현할 수도 있음
sleep(Millis(100))

// 확장 프로퍼티로 DSL과 유사한 문법을 만들어서 활용할 수도 있음
sleep(100.ms)
```

타입은 이런 정보를 전달하는 좋은 방법이라고 할 수 있다. 만약 성능에 영향을 줄 거 같아서 걱정된다면 인라인 클래스를 사용해라. 하지만 여전히 파라미터 순서를 잘못 입력하는 등의 문제가 발생할 수 있다. 그래서 이름 있는 아규먼트를 추천한다.

#### 이름있는 아규먼트를 추천하는 경우
* 디폴트 아규먼트의 경우
* 같은 타입의 파라미터가 많은 경우
* 함수 타입의 파라미터가 있는 경우(마지막 경우 제외)

### 디폴트 아규먼트의 경우
일반적으로 함수 이름은 필수 파라미터들과 관련되어 있기 때문에 디폴트 값을 갖는 옵션 파라미터의 설명이 명확하지 않다. 따라서 이런 것들은 이름을 붙여 쓰는 게 좋다.

### 같은 타입의 파라미터가 많은 경우
파라미터에 같은 타입이 있다면 잘못 입력했을 때 문제를 찾아내기 어려울 수 있다.

```kotlin
fun sendEmail(to: String, message: String) { /**/ }

// 이름 있는 아규먼트를 사용한 경우
sendEmail(
    to = "abc@abc.com", 
    message = "Hello, World!"
)
```

### 함수 타입 파라미터
일반적으로 함수 타입 파라미터는 마지막 위치에 배치하는 게 좋다. 함수 이름이 함수 타입 아규먼트를 설명해 주기도 한다.
그 밖의 모든 함수 타입 아규먼트는 이름 있는 아규먼트를 사용하는 것이 좋다.

```kotlin
// 함수 이름이 함수 타입 아규먼트를 설명하는 경우
thread {
    // ...
}

// 그 밖의 경우, 빌더와 리스너 구분이 힘들다.
val view = linearLayout {
    text("Click below")
    button({/* 1 */}, {/* 2 */})
}

// 이름 있는 아규먼트를 사용한 경우
val view = linearLayout {
    text("Click below")
    button(onClick = {/* 1 */}) {
        /* 2 */
    }
}
```

여러 함수 타입의 옵션 파라미터가 있는 경우에는 더 헷갈린다.

```kotlin
fun call(before: () -> Unit = {}, after: () -> Unit = {}) {
    before()
    print("Middle")
    after()
}

call({print("CALL")}) //CALLMiddle
call{print("CALL")} //MiddleCALL

// 이름 있는 아규먼트를 사용한 경우
call(before = {print("CALL")}) //CALLMiddle
call(after = {print("CALL")}) //MiddleCALL
```

리액티브 라이브러리에서 자주 볼 수 있는 형태이다. 예를 들어 RxJava에서 Observable을 구독할 떄 함수를 설정한다.
* 각각의 아이템을 받을 때 (onNext)
* 오류가 발생했을 때 (onError)
* 전체가 완료되었을 때 (onComplete)

```java
// Java
observable.getUers()
    .subscribe((List<User> users) -> { // onNext
        // ...
    }, (Throwable throwable) -> { // onError
        // ...
    }, () -> { // onComplete
        // ...
    });
```
    
```kotlin
// Kotlin
observable.getUsers()
    .subscribeBy(
        onNext = { users: List<User> ->
            // ...
        },
        onError = { throwable ->
            // ...
        },
        onComplete = {
            // ...
        }
    )
```

### 정리
#### 이름 있는 아규먼트의 장점
* 개발자가 코드를 읽을 때 편리하다.
* 코드의 안정성을 향상 시킬 수 있다.

#### 이름 있는 아규먼트 사용 시점
* 함수에 같은 타입의 파라미터가 여러 개 있는 경우
* 함수 타입의 파라미터가 있는 경우
* 옵션 파라미터가 있는 경우

#### 이름 있는 아규먼트를 사용하지 않아도 되는 예외
* 마지막 파라미터가 DSL처럼 특별한 의미를 가지고 있는 경우

#### 이름 있는 아규먼트를 사용할 수 없는 경우
* Java로 작성된 함수를 Kotlin에서 사용할 때
* 람다 표현식이나 고차 함수를 사용할 때

```kotlin
// 함수 타입을 사용할 때
fun calculate(operation: (Int, Int) -> Int): Int {
    return operation(2, 3)
}

fun main() {
    val sum = calculate { x, y -> x + y } // 람다 표현식, named arguments 사용 불가
    // val sum = calculate { x = 2, y = 3 -> x + y } // 컴파일 오류 발생
    println(sum)
}
```