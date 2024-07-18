아이템 35. 복잡한 객체를 생성하기 위한 DSL을 정의하라
=========================
DSL은 복잡한 객체, 계층구조를 갖고있는 객체를 정의할 때 굉장히 유용하다. 보일러 플레이트와 복잡성을 숨기면서 명확하게 표현할 수 있다.

```kotlin
// HTML
body {
    div {
        a("https://kotlinlang.org") {
            target = ATarget.blank + "Main site"
        }
    }
    +"some content"
}

// 코틀린 테스트 케이스
class MyTest : StringSpec({
    "length sould return size of string" {
        "hello".length shouldBe 5
    }
    "strtsWith should test for a prefix" {
        "world" should startWith("wor")
    }

})

// Gradle
plugins {
    'java-library'
}

dependencies {
    api("junit:junit:4.12")
    implementation("junit:junit:4.12")
    testImplementation("junit:junit:4.12")
}

configurations {
    implementation {
        resolutionStrategy.failOnVersionConflict()
    }
}
```

DSL을 활용하면 복잡하고 계층적인 자료 구조를 만들 수 있다. 코틀린 DSL은 type-safe이므로, 여러 가지 유용한 힌트를 활용할 수 있다.

### 사용자 정의 DSL 만들기
사용자 정의 DSL을 만드는 방법을 이해하려면, 리시버를 사용하는 함수 타입에 대한 개념을 이해해야 한다.

#### 함수 타입을 만드는 기본적인 방법
* 람다 표현식
* 익명 함수
* 함수 레퍼런스

```kotlin
fun plus(a: Int, b: Int) = a + b

// 유사 함수
val plus1 : (Int, Int) -> Int = { a , b -> a + b }
val plus2 : (Int, Int) -> Int = fun(a, b) = a + b
val plus3 : (Int, Int) -> Int = ::plus

// 아규먼트 타입을 지정하여 함수의 형태를 추론하게 만듬
val plus4 = { a : Int , b : Int -> a + b }
val plus5 = fun(a: Int, b : Int) = a + b

// 확장 함수
fun Int.myPlus(other : Int) = this + other

// 익명 함수
val myPlus = fun Int.(other : Int) = this + other

// 리시버를 가진 함수 타입
val myPlus: Int. (Int) -> Int = fun Int.(other : Int) = this + other

// 리시버를 가진 람다 표현식
val myPlus : Int.(Int) -> Int = { this + it }
```

#### 리시버를 가진 익명 확장 함수와 람다 표현식 호출 방법
* 일반적인 객체처럼 invoke 메서드를 사용
* 확장 함수가 아닌 함수처럼 사용
* 일반적인 확장 함수처럼 사용

```kotlin
myPlus.invoke(1, 2)
myPlus(1, 2)
1.myPlus(2)
```

리시버를 가진 함수 타입의 가장 중요한 특징은 this의 참조 대상을 변경할 수 있다는 것이다. this는 apply 함수에서 리시버 객체의 메서드와 프로퍼티를 간단하게 참조할 수 있게 해주기도 한다.

```kotlin
inline fun <T> T.apply(block: T.() -> Unit): T {
    this.block()
    return this
}

class User {
    var name: String = ""
    var surname: String = ""
}

val user = User().apply {
    name = "Marcin"
    surname = "jiwon"
}
```

리시버를 가진 함수 타입은 코틀린 DSL을 구성하는 가장 기본적인 블록이다.

```kotlin
// HTML 표를 만드는 DSL
fun createTable(): TableDsl = table {
	tr {
    	for(i in 1..2) {
        	td {
            	+ "This is column $i"
            }
        }
    }
}

fun table(init : TableBuilder.() -> Unit) : TableBuilder {
    //...
}

class TableBuilder {
    fun tr(init : TrBuilder.() -> Unit) { /*...*/ }
}

class TrBuilder {
    fun td(init : TdBuilder.() -> Unit) { /*...*/ }
}

class TdBuilder {
    var text = ""

    operator fun Stirng.unaryPlus(){
        text += this
    }
}

fun table(init : TableBuilder.() -> Unit) : TableBuilder {
    val TableBuilder = TableBulder()
    init.invoke(tableBuilder)
    return tableBuilder
}

// apply 사용
fun table(init : TableBuidler.() -> Unit) = TableBuilder().apply(init)
```

### 언제 사용하면 좋을까?
* 복잡한 자료 구조
* 계층적인 구조
* 거대한 양의 데이터

DSL은 많이 사용되는 구조의 반복을 제거할 수 있게 해준다.

### 정리
DSL은 언어 내부에서 사용할 수 있는 특별한 언어이다. 복잡한 객체를 만들거나, 복잡한 계층 구조를 갖는 객체를 만들 때만 활용하는 것이 좋다.