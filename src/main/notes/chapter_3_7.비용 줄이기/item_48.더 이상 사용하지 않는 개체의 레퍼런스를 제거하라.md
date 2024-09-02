아이템 48.더 이상 사용하지 않는 개체의 레퍼런스를 제거하라 
=========================
메모리 관리를 자동으로 해주는 프로그래밍 언어에 익숙한 개발자는 객체 해제를 따로 생각하지 않는다. 그렇다고 메모리 관리를 완전히 무시하면 메모리 누수가 발생해서 상황에 따라 OOM이 발생하기도 한다.
따라서 '더 이상 쓰지 않는 객체의 레퍼런스를 유지하면 안 된다'라는 규칙 정도는 지키는 게 좋다. 특히 어떤 객체가 메모리를 많이 차지하거나 객체가 많이 생성될 경우에는 규칙을 꼭 지켜야 한다.

객체에 대한 참조를 companion(또는 static)으로 유지해 버리면 가비지 컬렉터가 해당 객체에 대한 메모리 해제를 할 수 없다.
개선할 수 있는 방법이 몇 가지 있는데 일단 이러한 리소스를 정적으로 유지하지 않는 것이 가장 좋다. 의존 관계를 정적으로 저장하지 말고, 다른 방법을 활용해서 적절하게 관리하기 바란다.
또한 객체에 대한 레퍼런스를 다른 곳에 저장할 때는 메모리 누수가 발생할 가능성을 언제나 염두에 두기 바란다.
```kotlin
class MainActivity : Activity() {
	
    override fun onCreate(savedInstanceState: Bundle?) {
    	super.onCreate(svaedInstanceState)
        // ...
        
        // this에 대한 레퍼런스 누수가 발생
        logError = { Log.e(this::class.simpleName, it.message) }
    }
    
    // ...
    
    companion object {
    	// 메모리 누수가 발생
        val logError: ((Throwable) -> Unit)? = null
    }
}
```

다음 소스는 배열 위의 요소릃 ㅐ제하는 부분이 없어 pop으로 사이즈가 감소하더라도 가비지 컬렉터가 이를 해제하지 못해 메모리 누수가 발생한다. 더 이상 객체를 사용하지 않을 때 그 레퍼런스에 null을 설정하면 된다.
```kotlin
class Stack {
    companion object {
        private const val DEFAULT_INITIAL_CAPACITY = 16
    }
    private var elements: Array<Any?> = arrayOfNulls(DEFAULT_INITIAL_CAPACITY)
    private var size = 0
    
    fun push(e: Any) {
        ensureCapacity()
        elements[size++] = e
    }
    
    fun pop(): Any? {
        if (size == 0) {
            throw EmptyStackException()
        }
        return elements[--size]
    }
    
    private fun ensureCapacity() {
        if (elements.size == size) {
            elements = elements.copyOf(2 * size + 1)
        }
    }
}
```

다음은 mutableLazy 프로퍼티 델리게이트를 구현한 소스이다. 이 소스에서는 initializer가 사용된 후에도 해제가 되지 않는다는 문제가 있다.
```kotlin
fun <T> mutableLazy(initializer: () -> T): ReadWriteProperty<Any?, T> = mutableLazy(initializer)

private class MutableLazy<T>(
    val initializer: () -> T
): ReadWriteProperty<Any?, T> {
    
    private var value: T? = null
    private var initialized = false
    
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        synchronized(this) {
            if (!initialized) {
                value = initializer()
                initialized = true
            }
            
            return value as T
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        synchronized(this) {
            this.value = value
            initialized = true
        }
    }
}
```

거의 사용되지 않는 객체까지 이런 것을 신경 쓰는 것은 오히려 좋지 않을 수 있다. 하지만 오브젝트에 null을 설정하는 것은 그렇게 어려운 일이 아니므로, 무조건 하는 것이 좋다.
특히 많은 변수를 캡처할 수 있는 함수타입, Any 또는 제네릭 타입과 같은 미지의 클래스읠 때는 이러한 처리가 중요하다. 라이브러리를 만들 때는 이런 최적화가 더 중요하다.
코틀린 stdlib에 구현되어 있는 lazy 델리게이트는 사용 후에 모두 initializer를 null로 초기화한다.
```kotlin
private class SynchronizedLazyImpl<out T>(initializer: () -> T, lock: Any? = null) : Lazy<T>, Serializable {
    private var initializer: (() -> T)? = initializer
    @Volatile private var _value: Any? = UNINITIALIZED_VALUE
    private val lock = lock ?: this

    override val value: T
        get() {
            val _v1 = _value
            if (_v1 !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST")
                return _v1 as T
            }

            return synchronized(lock) {
                val _v2 = _value
                if (_v2 !== UNINITIALIZED_VALUE) {
                    @Suppress("UNCHECKED_CAST") (_v2 as T)
                } else {
                    val typedValue = initializer!!()
                    _value = typedValue
                    initializer = null
                    typedValue
                }
            }
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE
    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."
    private fun writeReplace(): Any = InitializedLazyImpl(value)
}
```

일반적인 규칙은 상태를 유지할 때는 메모리 관리를 염두에 두어야 한다는 것이다. 코드를 작성할 때는 '메모리와 성능'뿐만 아니라 '가독성과 확장성'을 항상 고려해야 한다.
일반적으로 가독성과 확장성을 중시하지만 예외적으로 라이브러리를 구현할 때는 메모리와 성능이 더 중요하다.
* 라이브러리는 많은 코드에 의해 사용된다.
* 성능과 메모리 효율성이 전체 시스템에 영향을 미친다.
* 라이브러리는 안정성과 일관성이 중요하다.

메모리 누수가 발생하는 케이스 중 하나는 절대 사용되지 않는 객체를 캐시해서 저장해 두는 경우이다. 해결 방법은 소프트 레퍼런스를 사용하는 것이다.(item 45.불필요한 객체 생성을 피하라 참고)
메모리 누수는 예측하기가 어렵다. 애플리케이션이 크래시되기 전까지 있는지 확인하기가 힘들 수 있다.
별도의 크래시 도구들을 활용해서 메모리 누수를 찾는 것도 좋은 방법이다. 가장 기본적인 도구로는 힙 프로파일러가 있다. 또한 메모리 누수 탐색에 도움이 되는 LeakCanary와 같은 라이브러리도 있다.(item 45.불필요한 객체 생성을 피하라 참고, BE에서 사용할만한 프로파일러 정리) 

일반적으로 스코프를 벗어나면서, 레퍼런스가 제거될 때 객체는 자동으로 해지되기 때문에 수동으로 해지해야 하는 경우는 드물다.
따라서 메모리와 관련된 문제를 피하는 가장 좋은 방법은 변수를 지역 스코프에 정의하고, 톱레벨 프로퍼티 또는 객체 선언(companion object)으로 큰 데이터를 저장하지 않는 것이다.

#### 델리게이트 종류
* lazy: 프로퍼티를 지연 초기화(lazy initialization)할 때 사용
* observable: 프로퍼티의 값이 변경될 때마다 특정 동작을 수행할 때 사용
* vetoable: 프로퍼티의 값이 변경되기 전에 특정 동작을 수행할 때 사용
* notNull: lateinit 대신 사용될 수 있으며, null 값이 들어갈 수 없는 변수를 나중에 초기화해야 할 때 사용
* 사용자 정의 델리게이트: getValue와 setValue 연산자를 오버로드하여 자신만의 델리게이트를 정의할 수 있다.
```kotlin
// lazy
val lazyValue: String by lazy {
    println("Computed!")
    "Hello, Lazy!"
}

fun main() {
    println("Before accessing lazyValue")
    println(lazyValue)  // 처음 접근할 때 초기화가 수행됨
    println(lazyValue)  // 이미 초기화된 값을 사용
}

// observable
var name: String by Delegates.observable("Initial Name") { prop, old, new ->
    println("Name changed from $old to $new")
}

fun main() {
    name = "Alice" // Name changed from Initial Name to Alice
    name = "Bob" // Name changed from Alice to Bob
}

// vetoable
var age: Int by Delegates.vetoable(0) { prop, old, new ->
    new >= 0  // 나이가 음수가 되지 않도록 막음
}

fun main() {
    age = 25
    println(age)  // 25 출력

    age = -5
    println(age)  // 이전 값 25가 유지됨
}

// 사용자 정의 델리게이트(LoggerDelegator)
class LoggerDelegator : ReadOnlyProperty<Any?, Logger> {

    companion object {
        private fun <T> createLogger(clazz: Class<T>): Logger {
            return LoggerFactory.getLogger(clazz)
        }
    }

    private var logger: Logger? = null

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Logger {
        if (logger == null) {
            logger = createLogger(thisRef!!.javaClass)
        }
        return logger!!
    }
}
```