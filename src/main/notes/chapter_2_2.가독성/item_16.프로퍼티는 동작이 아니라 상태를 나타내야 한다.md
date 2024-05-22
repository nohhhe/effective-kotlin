아이템 16. 프로퍼티는 동작이 아니라 상태를 나타내야 한다
=========================
코틀린의 프로퍼티는 자바의 필드와 비슷해보이지만 사실 전혀 다른 개념이다. 코틀린의 프로퍼티는 사용자 정의 게터/세터를 가질 수 있다.

```kotlin
var name: String? = null
	get() = field?.toUpperCase()
    set(value) {
    	if(!value.isNullOrBlank()) {
        	field = value
        }
    }
```

field라는 식별자를 확인할 수 있는데 이는 프로퍼티의 데이터를 저장해 두는 백킹 필드(backing field)에 대한 래퍼런스이다. 이러한 백킹 필드는 세터와 게터의 디폴트 구현에 사용되므로, 따로 만들지 않아도 디폴트로 생성된다. val을 사용해 읽기 전용 프로퍼티를 만들 때는 field가 만들어지지 않는다.

```kotlin
val fullName: String
    get() = "$name $surname"
```

#### 백킹 필드와 백킹 프로퍼티
* 백킹 필드: 프로퍼티의 값을 저장하기 위한 실제 저장소로, field 키워드를 통해 접근할 수 있다.
* 백킹 프로퍼티: 접근 제어를 위해 별도의 프라이빗 프로퍼티를 사용하여 실제 값을 저장하고, 공개 프로퍼티를 통해 간접적으로 접근하는 방식이다.
* https://yeongunheo.tistory.com/entry/%EC%BD%94%ED%8B%80%EB%A6%B0-Backing-Field%EC%99%80-Backing-Properties

```kotlin
// 백킹 프로퍼티 예제
class User {
    private var _email: String? = null
    val email: String
        get() {
            if (_email == null) _email = "default@example.com"
            return _email!!
        }
}

fun main() {
    val user = User()
    println(user.email) // default@example.com
}
```

var을 사용해서 만든 읽고 쓸 수 있는 프로퍼티는 게터와 세터를 정의할 수 있다. 이러한 프로퍼티를 파생 프로퍼티(derived property)라고 부르며 자주 사용된다.

이처럼 코틀린의 모든 프로퍼티는 디폴트로 캡슐화되어 있다. 만약 자바에서 Date를 활용해 객체에 날짜를 저장해서 많이 활용한 상황을 가정했을 때 데이터를 millis라는 별도의 프로퍼티로 옮기고, 이를 활용해서 date 프로퍼티에 데이터를 저장하지 않고 랩/언랩하도록 코드만 변경하기만 하면 된다.

```kotlin
var date: Date
	get() = Date(millis)
    set(value) {
    	millis = value.time
    }
```

프로퍼티는 필드가 필요없다 오히려 접근자를 나타낸다. 프로퍼티는 개념적으로 접근자(val의 경우 세터, var의 경우 게터와 세터)를 나타낸다. 코틀린은 인터페이스에도 프로퍼티를 정의할 수 있다. 또한 게터에 대한 오버라이드도 할 수 있다.

```kotlin
// 인터페이스에서 프로퍼티 정의
interface Person {
	val name: String
}

// 오버라이드 예제
open class Supercomputer {
    open val theAnswer: Long = 42
}

class AppleComputer : Supercomputer() {
    override val theAnswer: Long = 1_800_275_2273
}
```

마찬가지로 프로퍼티를 위임할 수도 있다.

```kotlin
val db: Database by lazy { connectToDb() }
```

프로퍼티는 본질적으로 함수이므로, 확장 프로퍼티를 만들 수도 있다.
```kotlin
val Context.Preferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

val Context.inflater: LayoutInflater
    get() = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
```

프로퍼티는 필드가 아닌 접근자를 나타낸다. 이처럼 프로퍼티를 함수 대신 쓸 수도 있지만 완전히 대체해서 쓰는 건 좋지 않다. 예를 들어 프로퍼티로 알고리즘 동작을 나타내는 건 좋지 않다.

```kotlin
val Tree<Int>.sum: Int
    get() = when (this) {
        is Leaf -> value
        is Node -> left.sum + right.sum
    }
```

이런 처리는 프로퍼티가 아닌 함수로 구현해야 한다.

```kotlin
fun Tree<Int>.sum(): Int = when (this) {
    is Leaf -> value
    is Node -> left.sum() + right.sum()
}
```

원칙적으로 프로퍼티는 상태를 나타내거나 설정하는 목적으로만 쓰는 게 좋고 다른 로직 등을 포함하지 않아야 한다.

#### 프로퍼티 대신 함수를 사용하는 것이 좋은 경우
* 연산 비용이 높거나 복잡도가 O(1)보다 큰 경우 
* 비즈니스 로직(앱의 동작)을 포함하는 경우 
* 결정적이지 않은 경우: 같은 동작을 연속적으로 두 번 했는데 다른 값이 나올 수 있다면, 함수를 사용하는 것이 좋다
* 변환의 경우: 변환은 관습적으로 Int.toDouble()과 같은 변환 함수로 이루어진다. 따라서 이러한 변환을 프로퍼티로 만들면, 오해를 불러 일으킬 수 있다.
* 게터에서 프로퍼티의 상태 변경이 일어나야 하는 경우

반대로 상태를 추출/설정할 때는 프로퍼티를 사용해야한다. 특별한 이유가 없다면 함수를 사용하면 안된다.

```kotlin
// 이렇게 하지 마세요!
class UserIncorrect {
	private var name: String = ""
    
    fun getName() = name
    
    fun setName(name: String) {
    	this.name = name
    }
}

class UserCorrect {
	var name: String = ""
}
```

프로퍼티는 상태 집합을 나태내고, 함수는 행동을 나타낸다.