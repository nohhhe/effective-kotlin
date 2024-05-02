아이템 8. 적절하게 null을 처리하라
=========================
프로퍼티가 null이라는 것은 값이 설정되지 않았거나, 제거되었다는 것을 나타낸다. 
함수가 null을 리턴한다는 것은 함수에 따라서 여러 의미를 가질 수 있다.
* String.toIntOrNull()은 String을 Int로 적절하게 변환할 수 없을 경우 null을 리턴한다.
* Iterable<T>.firstOrNull(() -> Boolean)은 주어진 조건에 맞는 요소가 없을 경우 null을 리턴한다.

null은 최대한 명확한 의미를 갖는 게 좋다. nullable 값을 처리해야 하기 때문이다.

```kotlin
val printer: Printer? = getPrinter()
printer.print() // 컴파일 오류

printer?.print() // 안전 호출
if (printer != null) printer.print() // 스마트 캐스팅
printer!!.print()   // not-null assertion
```
기본적으로 nullable 타입은 3가지 방법으로 처리한다.
* safe call(?.), 스마트 캐스팅, 엘비스 연산자 등을 활용해서 안전하게 처리한다
* 오류를 throw한다
* 함수 또는 프로퍼티를 리팩토링해서 nullable 타입이 안 나오게 바꾼다

### null을 안전하게 처리하기
null을 안전하게 처리하는 방법 중 안전 호출(safe call), 스마트 캐스팅이 있다.

```kotlin
printer?.print() // safe call
if (printer != null) printer.print() // 스마트 캐스팅
```

코틀린은 nullable 변수와 관련된 처리를 광범위하게 지원한다. 대표적인 방법은 Elvis 연산자를 사용하는 것이다.

```kotlin
val printerName1 = printer?.name ?: "Unnamed"
val printerName2 = printer?.name ?: return
val printerName3 = printer?.name ?: throw Error("Printer must be named")
```

많은 객체가 nullable과 관련된 처리를 지원한다. 예를 들어 Collection<T>.orEmpty() 확장 함수를 쓰면 nullable이 아닌 List<T>를 리턴받는다.

스마트 캐스팅은 코틀린의 규약 기능(contracts feature)을 지원한다.

```kotlin
println("What is your name?")
val name = readLine()
if (!name.isNullOrBlank()) {
    println("Hello ${name.toUpperCase()}")
}

val news: List<News>? = getNews()
if (!news.isNullOrEmpty()) {
    news.forEach { notifyUser(it) }
}
```

#### 방어적 프로그래밍과 공격적 프로그래밍
* 방어적 프로그래밍: 모든 가능성을 올바른 방식으로 처리하는 것(예를 들어 null일 떄는 출력하지 않기 등)
* 공격적 프로그래밍: 모든 상황을 안전하게 처리하는 것은 불가능하기 때문에 문제를 개발자에게 알려서 수정하게 만드는 것(require, check, assert가 공격적 프로그래밍을 위한 도구)

### 오류 throw하기
문제가 발생할 경우 개발자에게 오류를 강제로 발생시키는 게 좋다. 오류를 강제 발생시킬 때는 throw, !!, requireNotNull, checkNotNull 등을 활용한다.

```kotlin
fun process(user: User) {
    requireNotNull(user.name)
    val context = checkNotNull(context)
    val networkService = getNetworkService(context) ?: throw NoInternetConnection()
    networkService.getData { data, userData -> 
        show(data!!, userData!!) 
    }
}
```

### not-null assertion(!!)과 관련된 문제
nullable을 처리하는 가장 간단한 방법은 not-null assertion(!!)을 쓰는 것이다. 그런데 !!을 쓰면 자바에서 nullable을 처리할 때 발생할 수 있는 문제가 똑같이 발생한다. 예외가 발생할 때 어떤 설명도 없는 제네릭 예외가 발생한다. !!타입은 nullable이지만 null이 안 나온다는 것이 거의 확실한 상황에서 많이 쓰인다.

```kotlin
fun largestOf(a: Int, b: Int, c: Int, d: Int): Int = 
    listOf(a, b, c, d).max()!!
```

이런 간단한 함수에서도 !!는 NPE로 이어질 수 있다.

```kotlin
fun largestOf(vararg nums: Int): Int = 
    nums.max()!!

largestOf() // NPE
```

nullability(null일 수 있는지)와 관련된 정보는 숨겨져 있으므로 쉽게 놓칠 수 있다. 변수를 null로 선언하고 이후에 !! 연산자를 쓰는 것은 좋은 방법이 아니다.

```kotlin
class UserControllerTest {
    private var dao: UserDao? = null
    private var controller: UserController? = null
    
    @BeforeEach
    fun init() {
        dao = mockk()
        controller = UserController(dao!!)
    }
    
    @Test
    fun test() {
        controller!!.doSomeThing()
    }
    
}
```

이렇게 코드를 짜면 이후 프로퍼티를 계속 언팩해야 하므로 쓰기 귀찮다. 또한 해당 프로퍼티가 실제로 이후에 의미 있는 null 값을 가질 가능성 자체를 차단한다. 이런 코드를 작성하는 올바른 방법은 lateinit 또는 Delegates.notNull을 쓰는 것이다.

!! 연산자를 쓰거나 명시적으로 예외를 발생시키는 형태로 설계하면 해당 코드가 오류를 발생시킬 수 있다는 걸 염두에 둬야 한다. 예외는 예상하지 못한 잘못된 부분을 알려주기 위해 쓰는 것이다.

하지만 명시적 오류는 제네릭 NPE보단 더 많은 정보를 제공해줄 수 있어서 !! 연산자를 쓰는 것보단 훨씬 좋다.

### 의미 없는 nullability 피하기
nullability는 어떻게든 적절하게 처리해야 해서 추가 비용이 발생한다. 따라서 필요한 경우가 아니면 nullability 자체를 피하는 게 좋다.

#### nullability를 피하는 방법
* 클래스에서 nullability에 따라 여러 함수를 만들어 제공할 수도 있다. 대표적인 예로 List<T>와 get, getOrNull()이 있다.
* 어떤 값이 클래스 생성 이후에 확실하게 설정된단 보장이 있으면 lateinit 프로퍼티와 notNull 델리게이트를 써라.
* 빈 컬렉션 대신 null을 리턴하지 마라. List<Int>?와 Set<String?>과 같은 컬렉션을 빈 컬렉션으로 둘 때와 null로 둘 때는 의미가 다르다. null은 컬렉션 자체가 없다는 걸 나타낸다. 요소가 부족하다는 걸 나타내려면 빈 컬렉션을 써라.
* nullable enum, None enum 값은 완전히 다른 의미다. null enum은 별도 처리해야 하지만 None enum 정의에 없으므로 필요한 경우에 사용하는 쪽에서 추가해서 활용할 수 있단 의미다.

### lateinit 프로퍼티와 notNull 델리게이트
```kotlin
class UserControllerTest {
    private var dao: UserDao? = null
    private var controller: UserController? = null

    @BeforeEach
    fun init() {
        dao = mockk()
        controller = UserController(dao!!)
    }

    @Test
    fun test() {
        controller!!.doSomething()
    }

}
```

프로퍼티를 쓸 때마다 nullable에서 null이 아닌 것으로 타입 변환하는 것은 바람직하지 않다. 이런 코드에 대한 바람직한 해결책은 나중에 속성을 초기화할 수 있는 lateinit 한정자를 쓰는 것이다. lateinit 한정자는 프로퍼티가 이후에 설정될 것임을 명시하는 한정자이다.

```kotlin
class UserControllerTest {
    private lateinit var dao: UserDao?
    private lateinit var controller: UserController?

    @BeforeEach
    fun init() {
        dao = mockk()
        controller = UserController(dao)
    }

    @Test
    fun test() {
        controller.doSomething()
    }

}
```

lateinit을 쓸 때도 비용이 발생한다. 초기화 전에 값을 쓰려고 하면 예외가 발생한다.

#### lateinit와 nullable 차이점
* !! 연산자로 언팩하지 않아도 된다.
* 이후 어떤 의미를 나타내기 위해 null을 쓰고 싶을 때 nullable로 만들 수도 있다.
* 프로퍼티 초기화 이후에는 초기화되지 않은 상태로 돌아갈 수 없다.

lateinit은 프로퍼티를 처음 쓰기 전에 반드시 초기화될 거라고 예상되는 상황에 활용한다. 이러한 상황으로는 라이플 사이클을 갖는 클래스처럼 메서드 호출에 명확한 순서가 있을 경우가 있다. 안드로이드 액티비티의 onCreate, iOS UIViewController의 viewDidAppear, 리액트 React.Component의 componentDidMount 등이 대표적인 예이다.

반대로 lateinit를 사용할 수 없는 경우도 있다. JVM에서 Int, Long, Double, Boolean과 같은 기본형과 연결된 타입으로 프로퍼티를 초기화해야 하는 경우이다. 이런 경우에는 lateinit보다는 느리지만 Deletages.notNull을 사용한다.

```kotlin
class DoctorActivity: Activity() {
    private var doctorId: Int by Delegates.notNull()
    private var fromNotification: Boolean by Delegates.notNull()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doctorId = intent.extras.getInt(DOCTOR_ID_ARG)
        fromNotification = intent.extras.getBoolean(FROM_NOTIFICATION_ARG)
    }
}
```
#### lateinit와 Deletages.notNull 비교
* 공통
  * var 프로퍼티에만 사용할 수 있다.
  * null이 될 수 없는 타입에만 사용할 수 있다.
* lateinit
  * 기본 타입(int, long 등)에는 사용할 수 없다. 
  * 프로퍼티가 초기화되기 전에 접근하려고 하면 UninitializedPropertyAccessException이 발생한다. 
  * 리플렉션을 통해 프로퍼티가 초기화되었는지 확인할 수 있다.(::프로퍼티명.isInitialized)
* Delegates.notNull<T>()
  * 기본 타입에도 사용할 수 있다.
  * 프로퍼티가 초기화되기 전에 접근하려고 하면 IllegalStateException이 발생한다.

onCreate때 초기화하는 프로퍼티는 지연 초기화하는 형태로 다음과 같이 프로퍼티 위임을 쓸 수도 있다.

```kotlin
class DoctorActivity: Activity() {
    private var doctorId: Int by arg(DOCTOR_ID_ARG)
    private var fromNotification: Boolean arg(FROM_NOTIFICATION_ARG)
}
```

프로퍼티 위임을 사용하면, nullability로 발생하는 여러 문제를 안전하게 처리할 수 있다.