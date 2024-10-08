아이템 43. API의 필수적이지 않는 부분을 확장 함수로 추출하라
=========================
클래스의 메서드를 정의할 때는 메서드를 멤버로 정의할 것인지 아니면 확장함수로 정의할 것인지 결정해야 한다.
두 가지 방법은 거의 비슷하다. 호출하는 방법도 비슷하고, 리플렉션으로 레퍼런싱하는 방법도 비슷하다.
```kotlin
// 멤버로 메서드 정의하기
class Workshop(/*...*/) {
	// ...
    fun makeEvent(date: DateTime): Event = //...
    
    val permalink
    	get() = "/workshop/$name"
}

// 확장 함수로 메서드 정의하기
class Workshop(/*...*/) {
	// ...
}

fun WorkShop.makeEvent(date: DateTime): Event = //...

val Workshop.permalink
	get() = "/workshop/$name"

fun useWorkshop(workshop: Workshop) {
    val event = workshop.makeEvent(date)
    val permalink = workshop.permalink

    val makeEventRef = Workshop::makeEvent
    val permalinkPropRef = Workshop::permalink
}
```

멤버와 확장의 가장 큰 차이점은 확장은 따로 가져와서 사용해야 한다는 것이다. 그래서 일반적으로 확장은 다른 패키지에 위치한다. 확장은 우리가 직접 멤버를 추가할 수 없는 경우, 데이터와 행위를 분리하도록 설계된 프로젝트에서 사용된다. 필드가 있는 프로퍼티는 클래스에 있어야 하지만, 메서드는 클래스의 public API만 활용한다면 어디에 위치해도 상관없다.

임포트해서 사용한다는 특징 덕분에 확장은 같은 타입에 같은 이름으로 여러 개 만들 수 있다. 따라서 여러 라이브러리에서 여러 메서드를 받을 수도 있고, 충돌이 발생하지도 않는다는 장점이 있다. 하지만 같은 이름으로 있다면, 그냥 멤버 함수로 만들어서 사용하는 것이 좋다. 그렇게 하면 컴파일러가 항상 확장 함수 대신 멤버 함수를 호출할 것이다.

또 다른 차이점은 확장은 가상이 아니라는 것이다. 즉, 파생 클래스에서 오버라이드 할 수 없다. 확장 함수는 컴파일 시점에 정적으로 선택된다. 따라서 확장 함수는 가상 멤버 함수와 다르게 동작한다. 상속을 목적으로 설계된 요소는 확장 함수로 만들면 안된다.

```kotlin
fun foo('this$receiver': C) = "c"
fun foo('this$receiver': D) = "d"

fun main() {
    val d = D()
    print(foo(d)) // d
    val c: C = d
    print(foo(c)) // c
    
    print(foo(D())) // d
    print(foo(D() as C)) // c
}
```

이러한 차이는 확장 함수가 "첫 번째 아규먼트로 리시버가 들어가는 일반 함수"로 컴파일 되기 때문에 발생한다.

추가로 확장 함수는 클래스가 아닌 타입에 정의하는 것이다. 그래서 nullable또는 구체적인 제네릭 타입에도 확장 함수를 정의할 수 있다.

```kotlin
inline fun CharSequence?.isNullOrBlank(): Boolean {
	contract {
    	return(false) implies (this@isNullOrBlank != null)
    }
}

public fun Iterable<Int>.sum(): Int {
	var sum: Int = 0
    for (element in this) {
    	sum += element
    }
    return sum
}
```

마지막으로 확장은 클래스 레퍼런스에 멤버로 표시되지 않는다. 그래서 확장 함수는 어노테이션 프로세서가 따로 처리하지 않는다. 따라서 필수적이지 않은 요소를 확장 함수로 추출하면, 어노테이션 프로세스로부터 숨겨진다. 이는 확장 함수가 클래스 내부에 있는 것이 아니기 때문이다.

### 정리다
* 확장 함수는 읽어 들여야 한다.
* 확장 함수는 가상이 아니다.
* 멤버는 높은 우선 순위를 갖는다.
* 확장 함수는 클래스 위가 아니라 타입 위에 만들어진다.
* 확장 함수는 클래스 레퍼런스에 나오지 않는다.

* 확장 함수는 우리에게 더 많은 자유과 유연성을 준다. 확장 함수는 상속, 어노테이션 처리 등을 지원하지 않고, 클래스 내부에 없으므로 약간 혼동을 줄 수도 있다. API의 필수적인 부분은 멤버로 두는 것이 좋지만, 필수적이지 않은 부분은 확장 함수로 만드는 것이 좋다.