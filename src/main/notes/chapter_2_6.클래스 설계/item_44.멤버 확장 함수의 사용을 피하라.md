아이템 44. 멤버 확장 함수의 사용을 피하라
=========================
어떤 클래스에 대한 확장 함수를 정의할 때 이를 멤버로 추가하는 건 좋지 않다. 확장 함수는 첫 번째 아규먼트로 리시버를 받는 단순한 일반 함수로 컴파일된다.
```kotlin
fun String.isPhoneNumber(): Boolean = length == 7 && all { it.isDigit() }

// 컴파일 된 후
fun isPhoneNumber('$this': String): Boolean =
    '$this'.length == 7 && '$this'.all { it.isDigit() }
```

DSL을 만들 때를 제외하면 확장 함수를 클래스 멤버로 정의하는 것은 좋지 않다. 특히 가시성 제한을 위해 확장 함수를 멤버로 정의하는 것은 굉장히 좋지 않다.
가시성을 제한하지 못할 뿐 아니라 확장 함수를 사용하는 형태를 어렵게 만들 뿐이다.
```kotlin
class PhoneBookCorrect {
    fun String.isPhoneNumber() = 
        length == 7 && all { it.isDigit() }
}

PhoneBookCorrect().apply { "1234567890".isPhoneNumber() }
```

#### 멤버 확장을 피해야 하는 이유
* 레퍼런스를 지원하지 않는다.
```kotlin
val ref = String::isPhoneNumber
val str = "1234567890"
val boundedRef = str::isPhoneNumber

val refX = PhoneBookCorrect::isPhoneNumber // 컴파일 에러
val book = PhoneBookCorrect() 
val bookRefX = book::isPhoneNumber // 컴파일 에러
```

* 암묵적 접근을 할 때 두 리시버 중에 어떤 리시버가 선택될지 혼동된다.
```kotlin
class A {
    val a = 10
}

class B {
    val a = 20
    val b = 30
    
    fun A.test() = a + b // 40일까? 50일까?
}
```

* 확장 함수가 외부에 있는 다른 클래스를 리시버로 받았을 때, 해당 함수가 어떤 동작을 하는지 명확하지 않다.
```kotlin
class A {
    var state = 10
}

class B {
    var state = 20
    
    fun A.update() = state + 10  // A와 B중에서 어떤 것을 업데이트 할까요?
}
```

* 경험이 적은 개발자의 경우 확장 함수를 보면, 직관적이지 않을 수 있다.