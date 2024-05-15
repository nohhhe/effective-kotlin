아이템 13. Unit?을 리턴하지 말라
=========================
Boolean이 true 또는 false를 갖는 것처럼 Unit?은 Unit 또는 null이란 값을 가질 수 있다. 따라서 Boolean과 Unit? 타입은 서로 바꿔서 사용할 수 있다.

```kotlin
// Boolean
fun keyIsCorrect(key: String): Boolean {
    //...
}

fun main() {
    if (!keyIsCorrect(key = "key")) return
}

// Unit?
fun verifyKey(key: String): Unit? {
    // ...
}

fun main() {
    verifyKey(key = "key") ?: return
}
```

Unit?으로 Boolean을 표현하는 것은 오해의 소지가 있으며 예측하기 어려운 오류를 만들 수 있다.

```kotlin
getData()?.let { view.showData(it) } ?: view.showError()
```

showData()가 null을 리턴하고 getData()가 null이 아닌 값을 리턴할 때 showData(), showError()가 모두 호출된다.

```kotlin
if (!keyIsCorrect(key)) return
    
verifyKey("key") ?: return
```

Unit?을 사용한 코드는 오해를 불러일으키기 쉽다. 따라서 Boolean을 사용하는 형태로 바꾸는 게 좋다. 기본적으로 Unit?을 리턴하거나 이를 기반으로 연산하지 않는 게 좋다.