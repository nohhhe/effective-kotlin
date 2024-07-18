아이템 27. 변화로부터 코드를 보호하려면 추상화를 사용하라
=========================
함수와 클래스 등의 추상화로 실질적인 코드를 숨기면, 사용자가 세부사항을 알지 못해도 괜찮다는 장점이 있다. 그리고 이후에 실질적인 코드를 원흔내돌 수정할 수도 있다.

### 상수
리터럴은 아무것도 설명하지 않는다. 따라서 반복적으로 등장할때 문제가 된다.

```kotlin
fun isPassWordValid(text: String) : Boolean {
    if (text.length < 7) return false
    ///...
}

// 상수로 뺀 경우
const val MIN_PASSWORD_LENGTH = 7

fun isPassWordValid(text: String) : Boolean {
    if (text.length < MIN_PASSWORD_LENGTH) return false
    ///...
}
```

#### 상수로 추출 했을 경우 장점
* 이름을 붙일 수 있다.
* 나중에 해당 값을 쉽게 변경할 수 있다.

### 함수
```kotlin
// 토스트 출력 함수
fun Context.toast(
    message : String, 
    duration : Int = Toast.LENGTH_LONG
) { 
    Toast.makeText(this, message, duration).show()
}
```

토스트가 아나리 스낵바라도 변경하고 싶다면, 함수를 변경하면 된다.

```kotlin
fun Context.snackbar(
    message :String, 
    length : Int = Toast.LENGTH_LONG
) {
    ///...
}
```

하지만 이런 해결법은 매우 좋지 않다. 함수의 이름을 직접 바꾸는 것은 위험할 수 있다. 다른 모듈이 이 함수를 의존하고 있다면, 다른 모듈에 큰 문제가 발생한다.
따라서 메세지를 출력하는 더 높은레벨의 함수로 옮기는 것이 좋다.

```kotlin
fun Context.showMessage(
    message : String, 
    duration : MessageLength = MessageLength.Long
) { 
    val toastDuration = when(duration) {
        SHORT -> Length.LENGTH_SHORT
        LONG -> Length.LEGTH_LONG
    }
    
    Toast.makeText(this, message, toastDuration).show()
}

enum class MessageLength { SHORT , LONG }
```

함수는 매우 단순한 추상화지만, 제한이 많다. 예를 들어 함수는 상태를 유지하지 않는다. 또한 함숭 시그니처(이름 등)를 변경하면 프로그램 전체에 큰 영향을 줄 수 있다.

### 클래스
```kotlin
class MessageDisplay(val context: Context) {
    fun show(
        message: String,
        duration: MessageLength = MessageLEngth.LONG
    ) {
        val toastDuration = when(duration) {
            SHORT -> MessageLength.LENGTH_SHORT
            LONG -> MessageLength.LENGTH_LONG
        }
        
        Toast.makeText(context, message, toastDuration).show()
    }
}

enum class MessageLength { SHORT, LONG }

// 사용
val messageDisplay = MessageDisplay(context)
messageDisplay.show("Message")
```

클래스는 상태를 가질 수 있으며, 많은 함수를 가질 수 있어서 강력하다.(클래스 멤버 함수를 메서드라고 부른다.)

* 함수는 상태를 유지하지 않는다 -> 함수를 호출할 때마다 독립적으로 작동하며 이전 호출의 값을 기억하지 않음 
* 클래스는 상태를 유지한다 -> 클래스의 인스턴스 내 변수는 값을 저장하여 메서드 호출 간 상태를 유지

```kotlin
// 의존성 주입 프레임워크를 사용하면, 클래스 위임할 수도 있다.
@Inject lateinit var messageDisplay: MessageDisplay

// mock 객체를 활용하여 해당 클래스에 의존하는 다른 클래스이 기능을 테스트 할 수 있다.
val messageDisplay: MessageDisplay = mock()
```

#### 의존성 객체 자동 주입 방법
* @Autowired: 스프링 전용, 타입 기준 주입, @Primary와 @Qualifier와 함께 사용 가능.
* @Resource: JSR-250 표준, 이름 기준 주입, 주로 필드 이름을 사용.
* @Inject: JSR-330 표준, 타입 기준 주입, @Named와 함께 사용 가능.

혼재해서 사용하는 것은 각기 다른 상황에 최적화된 의존성 주입을 가능하게 해주지만, 코드의 복잡성을 증가시키고, 일관성을 해칠 수 있는 위험이 있다.

더 많은 자유를 얻으려면, 더 추상적이게 만들면 된다. 인터페이스 뒤에 클래스를 숨기는 방법이 있다.

### 인터페이스
코틀린 표준 라이브러리는 거의 모든 것이 인터페이스로 표현된다.
* listOf 함수는 List를 리턴한다. List는 인터페이스이며, listOf는 팩토리 메서드라고 할 수 있다.
* 컬렉션 처리 함수는 Iterable 또는 Collection의 확장 함수로서, List, Map 등을 리턴한다.
* 프로퍼티 위임은 ReadOnlyProperty, ReadWriteProperty 뒤에 숨겨진다. 함수 lazy는 Lazy 인터페이스를 리턴한다.

라이브러리를 만드는 사람은 내부 클래스와 가시성을 제한하고, 인터페이스를 통해 이를 노출하는 코드를 많이 사용한다.
인터페이스 뒤에 객체를 숨김으로써 실질적인 구현을 추사항화하고, 사용자가 추상화된 것에만 의존하게 만들 수 있는 것이다. 즉, 결합을 줄일 수 있다.

코틀린은 멀티 플래폼 언어이다. 따라서 listOf가 코틀린/JVM, 코틀린/JS, 코틀린/네이티브에 따라서 구현이 다른 리스트를 리턴한다.
다른 리스트를 사용하는 이유는 최적화 떄문이다. 각 플랫폼의 네이티브 리스트를 사용해서 속도를 높이는 것이다.
어떤 플랫폼을 사용해도 List 인터페이스에 맞춰져 있으므로, 차이 없이 사용할 수 있다.

```kotlin
// 클래스를 인터페이스 뒤에 숨긴다는 예제
interface MessageDisplay {
    fun show (
        message: String,
        duration: MessageLength = LONG
    )
}

class ToastDisplay(val context: Context): MessageDisplay {
    override fun show(
        message: String,
        duration: MessageLength
    ) {
        val toastDuration = when(duration) {
            SHORT -> MessageLength.SHORT
            LONG -> MessageLength.LONG
        }
        Toast.makeText(context, message, toastDuration).show()
    }
}

enum class MessageLength { SHORT, LONG }
```

또 다른 장점은 테스트할 때 인터페이스 페이킹이 클래스 모킹보다 간단하므로, 별도의 모킹 라이브러리를 사용하지 않아도 된다는 것이다.

#### 인터페이스 페이킹 (Interface Faking)
실제 구현체 대신 테스트용으로 간단한 구현체(페이크)를 만들어 사용하는 기법

장점
* 간단한 테스트: 복잡한 동작을 구현할 필요 없이, 간단한 페이크를 통해 빠르고 쉽게 테스트할 수 있다,
* 독립적인 테스트: 다른 컴포넌트에 의존하지 않고 해당 컴포넌트를 독립적으로 테스트할 수 있다.
* 성능: 페이크는 간단한 동작만 수행하기 때문에, 성능적으로 더 가볍고 빠르다.

```kotlin
interface UserService { 
    fun getUser(id: String): User
}

class FakeUserService : UserService { 
    override fun getUser(id: String): User { 
        return User(id, "Fake User")
    }
}
```

#### 클래스 모킹 (Class Mocking)
클래스 모킹은 실제 객체 대신 모킹 라이브러리를 사용하여 동작을 시뮬레이션하는 가짜 객체(모크)를 만들어 사용하는 기법(우리가 많이 사용하는 방법)

장점
* 세밀한 제어: 특정 메서드 호출 시 반환값을 지정하거나 예외를 발생시켜, 다양한 시나리오를 테스트할 수 있다.
* 의존성 격리: 모킹을 통해 다른 클래스나 컴포넌트에 대한 의존성을 제거하여, 테스트 대상 클래스만 집중적으로 테스트할 수 있다.
* 복잡한 동작: 복잡한 비즈니스 로직이나 외부 시스템과의 상호작용을 모킹을 통해 단순화할 수 있다.

### ID 만들기(nextId)
```kotlin
var nextId: Int = 0

// 사용
val newId = nextId++
```

위 소스의 문제점
* ID는 무조건 0부터 시작
* 스레드-안전(thread-safe)하지 않다.

```kotlin
// 함수 사용법
private var nextId: Int = 0
fun getNextId(): Int = nextId++

// 사용
val newId = getNextId()

// 클래스 사용법
data class Id(private val id: Int)

private var nextId: Int = 0
fun getNextId(): Id = Id(nextId++)
```

### 추상화가 주는 자유
* 상수로 추출한다.
* 동작을 함수로 래핑한다.
* 함수를 클래스로 래핑한다.
* 인터페이스 뒤에 클래스를 숨긴다.
* 보편적인 객체를 특수한 객체로 래핑한다.

#### 추상화를 구현하기 위한 도구
* 제네릭 타입 파라미터를 사용한다.
* 내부 클래스를 추출한다.
* 생성을 제한한다(팩토리 함수로만 객체를 생성할 수 있게 만드는 등).

### 추상화의 문제
추상화가 너무 많으면 코드를 이해하기가 어렵다. 추상화는 많은 것을 숨길 수 있는 테크닉이다.
