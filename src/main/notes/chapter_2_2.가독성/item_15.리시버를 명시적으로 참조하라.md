```아이템 15. 리시버를 명시적으로 참조하라
=========================
함수와 프로퍼티를 지역 또는 톱레벨 변수가 아닌 다른 리시버로부터 가져온다는 것을 타나낼 때가 있다.

```kotlin
class User: Person() {
	private var beersDrunk: Int = 0
    
    fun drinkBeers(num: Int) {
    	// ...
        this.beersDrunk += num
        // ...
    }
}
```

비슷하게 확장 리시버(확장 메서드에서의 this)를 명시적으로 참조하게 할수도 있다.

```kotlin
fun <T : Comparable<T>> List<T>.quickSort(): List<T> {
    if (size < 2) return this
    val pivot = first()
    val (smaller, bigger) = drop(1).partition { it < pivot }
    return smaller.quickSort() + pivot + bigger.quickSort()
}

// 명시적으로 표기
fun <T : Comparable<T>> List<T>.quickSort(): List<T> {
    if (this.size < 2) return this
    val pivot = this.first()
    val (smaller, bigger) = this.drop(1).partition { it < pivot }
    return smaller.quickSort() + pivot + bigger.quickSort()
}
```

### 여러 개의 리시버
스코프 내부에 둘 이상의 리시버가 있는 경우, 리시버를 명시적으로 나타내면 좋다.
apply, with, run 함수를 사용할 때가 대표적인 예이다. 다음 코드를 예시로 자세하게 알아보자

```kotlin
class Node(val name: String) {
	fun makeChild(childName: String) = create("$name.$childName")
										.apply{ print("Created ${name}") }

	fun create(name: String): Node? = Node(name)
}
 
fun main() {
    val node = Node("parent")
    node.makeChild("child")
}
```

이 코드는 Created parent.child가 출력된다고 예상하지만, 실제로는 Created parent가 출력된다.

```kotlin
class Node(val name: String) {
	fun makeChild(childName: String) = create("$name.$childName")
										.apply{ print("Created ${this.name}") } // 컴파일 오류

	fun create(name: String): Node? = Node(name)
}
 
fun main() {
 	val node = Node("parent")
    node.makeChild("child") 
}
```

문제는 apply 함수 내부에서 this의 타입이 Node?라서, 이를 직접 사용할 수 없다. 이를 사용하려면 언팩(unpack)하고 호출해야 된다.

```kotlin
class Node(val name: String) {
	fun makeChild(childName: String) = create("$name.$childName")
										.apply{ print("Created ${this?.name}") } 
                                        
	fun create(name: String): Node? = Node(name)
}
 
fun main() {
    val node = Node("parent")
    node.makeChild("child")
}
```

also를 쓰면 문제 자체가 일어나지 않는다. 이전과 마찬가지로 명시적으로 리시버를 지정하게 된다. 일반적으로 also 또는 let을 쓰는 게 nullable 값을 처리할 때 훨씬 좋은 선택지다.

```kotlin
class Node(val name: String) {
	fun makeChild(childName: String) = create("$name.$childName")
										.also{ print("Created ${it?.name}") } 
                                        
	fun create(name: String): Node? = Node(name)
}
```

리시버가 명확하지 않다면 명시적으로 리시버를 적어서 이를 명확하게 하라. 레이블 없이 리시버를 쓰면 가장 가까운 리시버를 의미한다. 외부에 있는 리시버를 쓰려면 레이블을 써야 한다.

```kotlin
class Node(val name: String) {
	fun makeChild(childName: String) = create("$name.$childName")
        .apply{ 
            print("Created ${this?.name} in " + "${this@Node.name}") 
	    } 
                                       
	fun create(name: String): Node? = Node(name)
}

fun main() {
    val node = Node("parent")
    node.makeChild("child")
    // Created parent.child in parent
}
```

이렇게 명확하게 작성하면 코드를 안전하게 사용할 수 있고 가독성도 향상된다.

### DSL 마커
코틀린 DSL을 사용할 때는 여러 리시버를 가진 요소들이 중첩되더라도, 리시버를 명시적으로 붙이지 않는다. DSL은 원래 그렇게 쓰도록 설계되었기 때문이다. 그런데 DSL에선 외부 함수를 사용하는 게 위험한 경우가 있다.


```kotlin
table {
	tr {
		td {+"Column 1"}
		td {+"Column 2"}
	}
    tr {
		td {+"Value 1"}
		td {+"Value 2"}
	}
}
```

기본적으로 모든 스코프에서 외부 스코프에 있는 리시버의 메서드를 사용할 수 있다. 하지만 이렇게 하면 코드에 문제가 발생한다.

```kotlin
table {
	tr {
		td {+"Column 1"}
		td {+"Column 2"}
        tr {
			td {+"Value 1"}
			td {+"Value 2"}
		}	
	}
}
```

암묵적으로 외부 리시버를 사용하는 것을 막는 DslMarker라는 메타 어노테이션(어노테이션을 위한 어노테이션)을 사용한다.

#### 메타 어노테이션과 합성 어노테이션
* 메타 어노테이션: 보통 애노테이션은 클래스나 메소드 앞에 붙지만, 애노테이션 위에도 애노테이션을 또 붙일 수 있다. 이때 애노테이션 위에 붙어있는 애노테이션을 '메타 애노테이션'이라 한다.
* 합성 어노테이션: 하나 이상의 메타 어노테이션이 적용된 어노테이션을 말한다. (인진님👍 LoginCheck 어노테이션 등)
* https://velog.io/@gmtmoney2357/%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B6%80%ED%8A%B84-%EB%A9%94%ED%83%80-%EC%95%A0%EB%85%B8%ED%85%8C%EC%9D%B4%EC%85%98%EA%B3%BC-%ED%95%A9%EC%84%B1-%EC%95%A0%EB%85%B8%ED%85%8C%EC%9D%B4%EC%85%98

```kotlin
@DslMarker
annotation class HtmlDsl

fun table(f: TableDsl.() -> Unit) { /*...*/ }

@HtmlDsl
class TableDsl { /*...*/ }
```

```kotlin
table {
	tr {
		td {+"Column 1"}
		td {+"Column 2"}
        tr { // 컴파일 오류
			td {+"Value 1"}
			td {+"Value 2"}
		}	
	}
}
```
```kotlin
table {
	tr {
		td {+"Column 1"}
		td {+"Column 2"}
        this@table.tr { // 명시적으로 표기
			td {+"Value 1"}
			td {+"Value 2"}
		}	
	}
}
```

DSL 마커는 가장 가까운 리시버만을 사용하게 하거나 명시적으로 외부 리시버를 사용하지 못하게 할 때 활용할 수 있는 중요한 매커니즘이다.

### 정리
짧게 적을 수 있단 이유만으로 리시버를 제거하지 말아라. 여러 개의 리시버가 있는 상황 등에는 리시버를 명시적으로 적어 주는 것이 좋다. 리시버를 명시적으로 지정하면, 어떤 리시버의 함수인지를 명확하게 알 수 있으므로, 가독성이 향상된다. DSL에서 외부 스코프에 있는 리시버를 명시적으로 적게 강제하고 싶다면, DslMaker 메타 어노테이션을 사용한다.