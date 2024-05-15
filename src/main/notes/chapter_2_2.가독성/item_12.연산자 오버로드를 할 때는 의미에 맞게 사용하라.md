아이템 12. 연산자 오버로드를 할 때는 의미에 맞게 사용하라
=========================
연산자 오버로딩은 굉장히 강력한 기능이지만, 큰 힘에는 큰 책임이 따른다라는 말처럼 위험할 수 있다.

```kotlin
fun Int.factorial(): Int = (1..this).product()

fun Iterable<Int>.product(): Int = 
	fold(1) { acc, i -> acc * i }

print(10 * 6.factorial()) // 7200
```

팩토리얼은 ! 기호를 사용해 표기한다. 다음과 같이 연산자 오버로딩을 활용하면, 만들어낼 수 있다.

```kotlin
operator fun Int.not() = factorial()

print(10 * !6) // 7200

print(10 * 6.not()) // 7200
```

하지만 함수의 이름의 not이므로 논리 연산에 사용해야지, 팩토리얼 연산에 사용하면 안된다.

코틀린에서는 각각의 연산자에 구체적인 의미가 있으므로 혼란과 오해의 소지가 없게 위와 같이 사용하면 안된다.

### 분명하지 않은 경우
하지만 관례를 충족하는지 아닌 지 확실하지 않을 때가 문제이다.

예를 들어 함수를 세 배 한다는 것(* 연산자)은 무슨 의미일까? 세 번 반복하는 새로운 함수를 만들어 낸다고 생각할 수도 있고 함수를 3번 호출하는 것으로 생각할 수도 있다.

```kotlin
operator fun Int.times(operation: () -> Unit): ()-> Unit =
	{ repeat(this) { operation() } } 
    
val tripledHello = 3 * { print("Hello") } // 3번 반복 새로운 함수 생성

tripledHello() // 출력 : HelloHelloHello
```

```kotlin
operator fun Int.times(operation: () -> Unit) {
    repeat(this) { operation() }
}
    
3 * { print("Hello") } // 함수 3번 호출
```

의미가 명확하지 않다면, infix를 활용한 확장 함수를 사용하는 것이 좋다. 일반적인 이항 연산자 형태처럼 사용할 수 있다.

```kotlin
infix fun Int.timesRepeated(operation: ()->Unit): ()-> Unit = {
    repeat(this) { operation() }
}

val tripledHello = 3 timesRepeated { print("Hello") } // 이항 연산자 처럼 사용
tripleHello() // 출력 : HelloHelloHello
```

톱레벨 함수(top-level function)을 사용하는 것도 좋다.
#### 톱레벨 함수란?
코틀린에서 클래스나 객체 내부가 아닌, 파일의 최상위 레벨에 정의된 함수이다. 즉, 어떤 클래스에도 속하지 않으며, 파일 내 어디서든 직접 호출할 수 있는 함수

```kotlin
repeat(3) { print("Hello") } // 출력: HelloHelloHello
```

### 규칙을 무시해도 되는 경우
지금까지 설명한 연산자 오버로딩 규칙을 무시해도 되는 경우가 있다. 도메인 특화 언어(Domain Specific Language, DSL)를 설계할 때 이며, 해당 도메인을 위해 설계된 DSL 이기 때문에 이러한 규칙을 무시할 수 있다.

```kotlin
// HTML DSL. 코틀린 문법을 활용해서 만든 DSL
body {
    div {
        +"Some Text"
    }
}
```

### 정리
연산자 오버로딩은 이름의 의미에 맞게 사용해야 된다. 연산자 의미가 명확하지 않다면, 연산자 오버로딩을 사용하지 않는 것이 좋다. 대신 이름이 있는 일반 함수를 사용하자. 꼭 연산자 같은 형태로 사용하고 싶다면, infix 확장 함수 또는 톱레벨 함수를 활용해라.