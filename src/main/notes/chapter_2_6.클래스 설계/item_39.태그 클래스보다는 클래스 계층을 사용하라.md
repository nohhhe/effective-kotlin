아이템 39. 태그 클래스보다는 클래스 계층을 사용하라
=========================
상수 모드를 태그라고 부르며 태그를 포함한 클래스를 태그 클래스라고 부른다. 문제는 서로 다른 책임을 한 클래스에 태그로 구분해서 넣는다는 것에서 시작한다.

```kotlin
class ValueMatcher<T> private constructor(
    private val value: T? = null,
    private val matcher: Matcher
) {
    enum class Matcher {
        EQUAL,
        NOT_EQUAL,
        LIST_EMPTY,
        LIST_NOT_EMPTY
    }
    
    fun match(value: T?) = when (matcher) {
        Matcher.EQUAL -> value == this.value
        Matcher.NOT_EQUAL -> value != this.value
        Matcher.LIST_EMPTY -> value is List<*> && value.isEmpty()
        Matcher.LIST_NOT_EMPTY -> value is List<*> && value.isNotEmpty()
    }
    
    companion object {
        fun <T> equal(value: T) = ValueMatcher(value = value, matcher = Matcher.EQUAL)
        fun <T> notEqual(value: T) = ValueMatcher(value = value, matcher = Matcher.NOT_EQUAL)
        fun <T> emptyList() = ValueMatcher<T>(matcher = Matcher.LIST_EMPTY)
        fun <T> notEmptyList() = ValueMatcher<T>(matcher = Matcher.LIST_NOT_EMPTY)
    }
}
```

위 소스의 단점
* 한 클래스에 여러 모드를 처리하기 위한 상용구(boilerplate)가 추가된다.
* 여러 목적으로 써야 하므로 프로퍼티가 일관되지 않게 사용될 수 있으며 더 많은 프로퍼티가 필요하다. 위 코드에서 value는 LIST_EMPTY 또는 LIST_NOT_EMPTY일 때 아예 쓰이지 않는다. 
* 요소가 여러 목적을 가지고, 요소를 여러 방법으로 설정할 수 있는 경우 상태의 일관성, 정확성을 지키기 어렵다 .
* 팩토리 메서드를 써야 하는 경우가 많다. 그렇지 않으면 객체가 제대로 생성됐는지 확인하는 자체가 굉장히 어렵다.

코틀린은 일반적으로 태그 클래스보다 sealed 클래스를 많이 사용한다. 한 클래스에 여러 모드를 만드는 방법 대신 각각이 모드를 여러 클래스로 만들고 타입 시스템과 다형성을 활용하는 것이다. 그리고 이런 클래스에는 sealed 한정자를 붙여 서브클래스 정의를 제한한다.

```kotlin
sealed class ValueMatcher<T> {
    abstract fun match(value: T): Boolean
    
    class Equal<T>(val value: T): ValueMatcher<T>() {
        override fun match(value: T): Boolean = value == this.value
    }
    
    class NotEqual<T>(val value: T): ValueMatcher<T>() {
        override fun match(value: T): Boolean = value != this.value
    }
    
    class EmptyList<T> : ValueMatcher<T>() {
        override fun match(value: T): Boolean = value is List<*> && value.isEmpty()
    }
    
    class NotEmptyList<T> : ValueMatcher<T>() {
        override fun match(value: T): Boolean = value is List<*> && value.isNotEmpty()
    }
}
```

### sealed 한정자
sealed 한정자는 외부 파일에서 서브클래스를 만드는 행위 자체를 모두 제한한다. 외부에서 추가적인 서브클래스를 만들 수 없기 때문에 타입이 추가되지 않을 거라는 게 보장된다. 따라서 when을 쓸 때 else 브랜치를 따로 만들 필요가 없다.

when은 모드를 구분해서 다른 처리를 만들 때 편리하다. 어떤 처리를 각 서브클래스에 구현할 필요 없이 when을 활용하는 확장 함수로 정의하면 한 번에 구현할 수 있다.

```kotlin
fun <T> ValueMatcher<T>.reversed(): ValueMatcher<T> = when (this) {
    is ValueMatcher.EmptyList -> ValueMatcher.NotEmptyList()
    is ValueMatcher.NotEmptyList -> ValueMatcher.EmptyList()
    is ValueMatcher.Equal -> ValueMatcher.NotEqual(value)
    is ValueMatcher.NotEqual -> ValueMatcher.Equal(value)
}
```

abstract 키워드를 사용하면 새로운 인스턴스를 만들어 사용할 수도 있다. 이러한 경우에는 함수를 abstract로 선언하고 서브클래스 내부에 구현해야 한다. 
when을 쓰면 프로젝트 외부에서 새 클래스가 추가될 때 함수가 제대로 동작하지 않을 수 있기 때문이다. 

sealed 한정자를 쓰면 확장 함수를 써서 클래스에 새로운 함수를 추가하거나 클래스의 다양한 변경을 쉽게 처리할 수 있다. 클래스의 서브클래스를 제어하려면 sealed 한정자를 써야 한다.

```kotlin
sealed class SealedClass {
    fun display() {
        println("This is a sealed class.")
    }
}

// Sealed 클래스를 위한 확장 함수
fun SealedClass.extendedMethod() {
    println("This is an extension function for the sealed class.")
}

// 사용 예시
fun main() {
    val obj = SealedClass()
    obj.display()
    obj.extendedMethod() // "This is an extension function for the sealed class." 출력
}
```

### 태그 클래스와 상태 패턴의 차이
태그 클래스, 상태 패턴을 혼동하면 안 된다. 상태 패턴은 객체 내부가 변화할 때 객체 동작이 변하는 소프트웨어 디자인 패턴이다. 상태 패턴은 프론트엔드 컨트롤러, 프레젠터, 뷰모델을 설계할 때 많이 사용된다.

상태 패턴을 쓴다면 서로 다른 상태를 나타내는 클래스 계층 구조를 만들게 된다. 그리고 현재 상태를 나타내기 위한 읽고 쓸 수 있는 프로퍼티도 만들게 된다.

```kotlin
sealed class WorkoutState

class PrepareState(val exercise: Exercise): WorkoutState()

class ExerciseState(val exercise: Exercise): WorkoutState()

object DoneState: WorkoutState()

fun List<Exercise>.toStates(): List<WorkoutState> = flatMap { exercise ->
    listOf(PrepareState(exercise), ExerciseState(exercise))
} + DoneState

class WorkoutPresenter(/*...*/) {
    private var state: WorkoutState = states.first()
}
```

#### 상태 패턴의 차이점
* 상태는 더 많은 책임을 가진 클래스다.
* 상태는 바꿀 수 있다.

#### 태그 클래스와 상태 패턴의 차이점
* 태그 클래스: 클래스 내부에 상태를 나타내는 변수를 두고, 메서드 내부에서 상태에 따라 분기하는 방식. 유지보수가 어렵고, 상태 관련 코드가 분산되어 있습니다.
* 상태 패턴 각 상태를 개별 클래스로 분리하고, 상태와 관련된 행동을 해당 클래스 내에서 구현하는 방식. 상태 전환과 행동이 명확히 분리되어 유지보수가 용이합니다.
  
구체 상태(concrete state)는 객체를 활용해서 표현하는 게 일반적이며 태그 클래스보단 sealed class 계층으로 만든다.
또한 이를 불변 객체로 만들고 바꿔야 할 때마다 state 프로퍼티를 변경하게 만든다. 그리고 뷰에서 이런 state의 변화를 observe한다.

```kotlin
private var state: WorkoutState by 
    Delegates.observable(states.first()) { _, _, _ ->
        updateView()
    }
```

### 정리
코틀린에서는 태그 클래스보다 타입 계층을 사용하는 것이 좋다. 그리고 일반적으로 이러한 타입 계층을 만들 때는 sealed 클래스를 사용한다.
타입 계층과 상태 패턴은 실질적으로 함께 사용하는 협력 관계라고 할 수 있다. 하나의 뷰를 가지는 경우보다는 여러 개의 상태로 구분할 수 있는 뷰를 가질 때 많이 활용된다.