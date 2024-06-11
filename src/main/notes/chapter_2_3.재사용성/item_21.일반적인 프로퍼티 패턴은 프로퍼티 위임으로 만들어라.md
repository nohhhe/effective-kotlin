아이템 21. 일반적인 프로퍼티 패턴은 프로퍼티 위임으로 만들어라
=========================
코틀린은 코드 재사용과 관련해서 프로퍼티 위임이라는 기능을 제공한다. 프로퍼티 위임을 쓰면 일반적인 프로퍼티의 행위를 추출해 재사용할 수 있다.
대표적인 예로 지연 프로퍼티가 있다. lazy 프로퍼티는 이후에 처음 사용하는 요청이 들어올 때 초기화되는 프로퍼티를 의미한다.
코틀린의 stdlib는 lazy 프로퍼티 패턴을 쉽게 구현할 수 있게 lazy 함수를 제공한다.

```kotlin
val value by lazy { createValue() }
```

프로퍼티 위임을 쓰면 이외에도 변화가 있을 때 이를 감지하는 stdlib의 observable 델리게이트를 기반으로 간단하게 구현할 수 있다.

```kotlin
// 내부 데이터가 변경될 때마다 변경된 내용을 출력
val items: List<Item> by Delegates.observable(listOf()) { _, _, _ ->
    notifyDataSetChanged()
}

// 변경 사항을 로그로 출력
val key: String? by Delegates.observable(null) { _, old, new ->
    Log.e("TAG", "key changed from $old to $new")
}
```

일반적으로 프로퍼티 위임 매커니즘을 활용하면 다양한 패턴들을 만들 수 있다. 예로 뷰, 리소스 바인딩, 의존성 주입, 데이터 바인딩 등이 있다.
일반적으로 이런 패턴들을 사용할 때 자바 등에선 어노테이션을 많이 활용해야 한다. 하지만 코틀린은 프로퍼티 위임을 써서 간단하고 type-safe하게 구현할 수 있다.

```kotlin
// 안드로이드에서의 뷰와 리소스 바인딩
private val button: Button by bindView(R.id.button)
private val textSize by bindDimension(R.dimen.font_size)
private val doctor: Doctor by argExtra(DOCTOR_ARG)

// kotlin 에서의 종속성 주입
private val presenter: MainPresenter by inject()
private val repository: NetworkRepository by inject()
private val vm: MainViewModel by viewModel()

// 데이터 바인딩
private val port by bindConfiguration("port")
private val token: String by preferences.bind(TOKEN_KEY)
```

간단한 로그를 출력하는 프로퍼티 델리게이트를 만들어보자. 가장 기본적인 구현 방법은 게터와 세터에서 로그를 출력하는 방법이다.

```kotlin
var token: String? = null
    get() {
        print("token returned value $field")
        return field
    }
    set(value) {
        print("token changed from $field to $value")
        field = value
    }

var attempts: Int = 0
    get() {
        print("attempts returned value $field")
        return field
    }
    set(value) {
        print("attempts changed from $field to $value")
        field = value
    }
```

프로퍼티 위임은 다른 객체의 메서드를 활용해서 프로퍼티의 접근자(게터, 세터)를 만드는 방식이다.
이 때 다른 객체의 메서드명이 중요하다. 게터는 getValue, 세터는 setValue 함수를 써서 만들어야 한다.
객체를 만든 뒤에는 by 키워드를 써서 getValue, setValue를 정의한 클래스와 연결하면 된다.

```kotlin
var token: String? by LoggingProperty(null)
var attempts: Int by LoggingProperty(0)

private class LoggingProperty<T>(var value: T) {
    operator fun getValue(
        thisRef: Any?,
        prop: KProperty<*>
    ): T {
        print("${prop.name} returned value $value")
        return value
    }

    operator fun setValue(
        thisRef: Any?,
        prop: KProperty<*>,
        newValue: T
    ) {
        val name = prop.name
        print("$name changed from $value to $newValue")
        value = newValue
    }
}
```

프로퍼티 위임이 어떻게 동작하는지 이해하려면, by가 어떻게 컴파일되는지 보는 것이 좋다. token 프로퍼티는 다음과 비슷한 형태로 컴파일된다.

```kotlin
JvmField
private val 'token$delegate' = LoggingProperty<String>(null)
var token: String?
    get() = 'token$delegate'.getValue(this, ::token)
    set(value) {
        'token$delegate'.setValue(this, ::token, value)
    }
```

getValue와 setValue는 단순하게 값만 처리하도록 바뀌는 것이 아니라, 컨텍스트(this)와 프로퍼티 레퍼런스의 경계도 함께 사용하는 형태로 바뀐다.
프로퍼티에 대한 레퍼런스는 이름, 어노테이션과 관련된 정보 등을 얻을 때 사용된다. 그리고 컨텍스트는 함수가 어떤 위치에서 사용되는지와 관련된 정보를 제공해준다.

이러한 정보로 인해서 getValue와 setValue메서드가 여러 개 있어도 문제가 없습니다.
getValue와 setValue 메서드가 여러개 있어도 컨텍스트를 활용하므로, 상황에 따라서 적절한 메서드가 선택됩니다.

```kotlin
class SwipeRefreshBinderDelegate(val id: Int) {
    private var cache: SwipeRefreshLayout? = null

    operator fun getValue(
        activity: Activity,
        prop: KProperty<*>,
    ): SwipeRefreshLayout {
    return cache?: activity
        .findViewById<SwipeRefreshLayout>(id)
        .also { cache = it }
    }

    operator fun getValue(
        fragment: Fragment,
        prop: KProperty<*>
    ): SwipeRefreshLayout {
        return cache?: fragment.view
        .findViewById<SwipeRefreshLayout>(id)
        .also { cache = it }
    }
}
```

객체를 프로퍼티 위임하려면 val의 경우 getValue 연산, var의 경우 getValue와 setValue 연산이 필요하다.  이러한 연산은 확장 함수로도 만들 수 있다.

```kotlin
val map: Map<String, Any> = mapOf(
    "name" to "Marcin",
    "kotlinProgrammer" to true
)
val name by map
print(name) // Marcin

// stdlib에서 제공되는 확장함수 정의
inline operator fun <V, V1 : V> Map<in String, V>
        .getValue(thisRef: Any?, property: KProperty<*>): V1 =
    getOrImplicitDefault(property.name) as V1
```

코틀린 stdlib에서 다음과 같은 프로퍼티 델리게이터를 알아 두면 좋다.
* lazy: 지연 초기화
* Delegates.observable: 값 변경 감지
* Delegates.vetoable: 값 변경 감지하여 변경을 할 것인지 결정
* Delegates.notNull: null이 아님을 보장

### 정리
프로퍼티 델리게이트는 프로퍼티와 관련된 다양한 조작을 할 수 있으며, 컨텍스트와 관련된 대부분의 정보를 갖는다.
이러한 특징으로 인해서 다양한 프로퍼티의 동작을 추출해서 재사용할 수 있다.