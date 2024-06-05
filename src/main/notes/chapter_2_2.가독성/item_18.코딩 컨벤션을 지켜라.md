아이템 18. 코딩 컨벤션을 지켜라
=========================
코틀린은 잘 정리된 코딩 컨벤션을 갖고 있고 지켜야 아래 효과를 볼 수 있다.
* 어떤 프로젝트를 접해도 쉽게 이해할 수 있다
* 다른 외부 개발자도 프로젝트의 코드를 쉽게 이해할 수 있다
* 다른 개발자도 코드 작동 방식을 쉽게 추측할 수 있다
* 코드를 병합하고 한 프로젝트의 코드 일부를 다른 코드로 이동하는 게 쉽다

자주 위반되는 규칙 중 하나는 클래스, 함수 형식이다.

```kotlin
class FullName(val name: String, val surname: String)
```

하지만 많은 파라미터를 가진 클래스와 함수는 아래처럼 각각의 파라미터를 한 줄씩 작성하는 방법을 사용한다.

```kotlin
class Person(
    val id: Int = 0,
    val name: String = "",
    val surname: String = ""
): Human(Id, name) {
    //
}

public fun <T> Iterable<T>.joinToString(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): String {
    //
}
```

아래 코드는 2가지 측면에서 문제가 될 수 있다.
* 모든 클래스의 아규먼트가 클래스명에 따라서 다른 크기의 들여쓰기를 갖는다. 이런 형태로 작성하면 클래스명을 변경할 때 모든 기본 생성자 파라미터의 들여쓰기를 조정해야 한다
* 클래스가 차지하는 공간의 너비가 너무 크다. 처음 class 키워드가 있는 줄도 너비가 너무 크고 이름이 가장 긴 마지막 파라미터와 슈퍼 클래스 지정이 함께 있는 줄도 너무 크다

```kotlin
// 이렇게 하지 마세요.
class Person(val id: Int = 0,
             val name: String = "",
             val surname: String = "") : Human(Id, name) {
    // 본문
}
```

코딩 컨벤션은 굉장히 중요하다. 가독성 관련 어떤 책을 봐도 코딩 컨벤션과 관련된 내용을 강조한다는 것을 확인할 수 있을 것이다. 코딩 컨벤션을 확실하게 읽고 정적 검사기(static checker)를 써서 프로젝트의 코딩 컨벤션 일관성을 유지하라.