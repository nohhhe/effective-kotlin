아이템 38. 연산 또는 액션을 전달할 때는 인터페이스 대신 함수 타입을 사용하라
=========================
대부분 프로그래밍 언어에는 함수 타입이란 개념이 없어서 연산 또는 액션을 전달할 때 메서드가 하나만 있는 인터페이스를 활용한다. 
이런 인터페이스를 SAM(Single-Abstract Method)이라고 부른다. 예를 들어 아래 코드는 뷰를 클릭했을 때 발생하는 정보를 전달하는 SAM이다.

```kotlin
interface OnClick {
    fun clicked(view: View)
}

// 인터페이스 구현
fun main() {
    setOnClickListener(object: OnClick {
        override fun clicked(view: View) {
            // ...
        }
    })
}

fun setOnClickListener(listener: OnClick) {
    // ...
}

// 함수 타입으로 변경
fun setOnClickListener(listener: (View) -> Unit) {
  // ...
}
```

함수 타입 사용 방법 3가지
```kotlin
// 람다식 또는 익명 함수로 전달
setOnClickListener { /* ... */ }
setOnClickListener(fun(view) { /* ... */ })

// 함수 레퍼런스 또는 제한된 함수 레퍼런스로 전달
setOnClickListener(::println)
setOnClickListener(this::showUsers)

// 선언된 함수 타입을 구현한 객체로 전달
fun main() {
  setOnClickListener(ClickListener())
}

class ClickListener: (View) -> Unit {
  override fun invoke(view: View) {
    // ...
  }
}
```

타입 별칭(typealias)을 쓰면 함수 타입도 이름을 붙일 수 있다.
```kotlin
typealias OnClick = (View) -> Unit
```

파라미터도 이름을 가질 수 있다. 이름을 붙이면 IDE의 지원을 받을 수 있다는 큰 장점이 있다.
```kotlin
fun setOnClickListener(listener: OnClick) { /* ... */ }
typealias OnClick = (view: View) -> Unit
```

람다식을 사용할 때는 아규먼트 분해(destructure argument)도 쓸 수 있다. 이건 SAM보다 함수 타입을 쓰는 게 훨씬 더 좋은 이유다.
```kotlin
// 인터페이스 방식
class CalendarView {
    var listener: Listener? = null
    
    interface Listener {
        fun onDateClicked(date: Date)
        fun onPageChanged(date: Date)
    }
}

// 함수 타입 방식
class CalendarView {
  var onDateClicked: ((date: Date) -> Unit)? = null
  var onPageClicked: ((date: Date) -> Unit)? = null
}
```

이렇게 onDateClicked, onPageChanged를 한꺼번에 묶지 않으면 각각의 것을 독립적으로 바꿀 수 있다는 장점이 생긴다. 인터페이스를 써야 하는 특별한 이유가 없다면 함수 타입을 활용하는 게 좋다.

### 언제 SAM을 써야 하는가?
코틀린이 아닌 다른 언어에서 사용할 클래스를 설계할 때 SAM을 쓰는 게 좋다. 자바에선 인터페이스가 더 명확하다. 함수 타입으로 만들어진 클래스는 자바에서 타입 별칭과 IDE의 지원 등을 제대로 받을 수 없다. 
마지막으로 다른 언어(자바 등)에서 코틀린의 함수 타입을 사용하려면 Unit을 명시적으로 리턴하는 함수가 필요하다.

```kotlin
// 코틀린
class CalendarView() {
    var onDateClicked: ((date: Date) -> Unit)? = null
    var onPageChanged: OnDateClicked? = null
}

interface OnDateClicked {
    fun onClick(date: Date)
}

// 자바
CalendarView c = new CalendarView();
c.setOnDateClicked(date -> Unit.INSTANCE);
c.setOnPageChanged(date -> {});
```

자바에서 사용하기 위한 API를 설계할 때는 함수 타입보다 SAM을 쓰는 게 합리적이다. 이외의 경우에는 함수 타입을 쓰는 게 좋다.