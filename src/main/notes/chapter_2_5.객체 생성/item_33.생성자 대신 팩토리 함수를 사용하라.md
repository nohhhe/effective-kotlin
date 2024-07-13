아이템 33. 생성자 대신 팩토리 함수를 사용하라
=========================
클래스의 인스턴스를 만드는 가장 일반적인 방법은 기본 생성자(primary constructor)를 사용하는 방법이다.

```kotlin
class MyLinkedList<T> (
  val head: T,
  val tail: MyLinkedList<T>?
)

val list = MyLinkedList(1, MyLinkedList(2, null))
```

디자인 패턴으로 굉장히 다양한 생성 패턴들이 만들어져 있으며, 일반적으로 이러한 생성 패턴은 객체를 생성자로 생성하지 않고 별도의 함수를 통해 생성한다.
생성자의 역할을 대신 해 주는 함수를 팩토리 함수라고 부르며, 다양한 장점들이 있습니다.

### 팩토리 함수 장점
* 생성자와 다르게, 함수에 이름을 붙일 수 있다.
* 생성자와 다르게, 함수가 원하는 형태의 타입을 리턴할 수 있다.
* 생성자와 다르게, 호출될 때마다 새 객체를 만들 필요가 없다. 함수를 사용해서 싱글턴 패턴처럼 객체를 하나만 생성하게 강제하거나, 캐싱 메커니즘을 사용하거나, 객체를 만들 수 없을 경우 null을 리턴하게 할 수도 있다.
* 팩토리 함수는 아직 존재하지 않는 객체를 리턴할 수도 있다.
* 객체 외부에 팩토리 함수를 만들면, 가시성을 제어할 수 있다. 예를 들어 톱레벨 팩토리 함수를 같은 파일 또는 모듈에서만 접근하게 만들 수 있다.
* 팩토리 함수를 인라인으로 만들 수 있으며, 그 파라미터들을 reified로 만들 수 있다.
* 팩토리 함수는 생성자로 만들기 복잡한 객체도 만들 수 있다.
* 생성자는 즉시 슈퍼클래스 또는 기본 생성자를 호출해야 하지만, 팩토리 함수를 사용하면, 원하는 때에 생성자를 호출할 수 있다.

```kotlin
fun makeListView(config : Config) : ListView{
    val items = ... // config로부터 요소를 읽어 들인다.

    return ListView(items) // 진짜 생성자를 호출한다.

}
```

팩토리 함수로 클래스를 생성할 때는 약간의 제한이 발생한다.
서브클래스 생성에는 슈퍼클래스의 생성자가 필요하기 떄문에, 서브클래스를 만들어낼 수 없다. 하지만 서비스 클래스도 팩토리 함수로 만들면 된다.

```kotlin
class MyLinkedIntList(head: Int, tail: MyLinkedIntList?): MyLinkedList<Int>(head, tail)

fun myLinkedIntListOf(vararg elements: Int): MyLinkedIntList? {
    if (elements.isEmpty()) return null
    val head = elements.first()
    val elementsTail = elements.copyOfRange(1, elements.size)
    val tail = myLinkedIntListOf(*elementsTail)
    
    return MyLinkedIntList(head, tail)
}
```

앞의 생성자는 이전 생성자보다 길지만 유연성, 클래스 독립성, nullable을 리턴하는 등 다양한 특징을 갖는다.
팩토리 함수 안에선 생성자를 써야 한다. 일반적인 자바로 팩토리 패턴을 구현할 땐 생성자를 private로 만들지만, 코틀린에선 그렇게 하는 경우가 거의 없다. 팩토리 함수는 기본 생성자가 아닌 추가적인 생성자와 경쟁 관계다.

#### 팩토리 함수 종류
* companion 객체 팩토리 함수
* 확장 팩토리 함수
* 톱레벨 팩토리 함수
* 가짜 생성자
* 팩토리 클래스의 메서드

### Companion 객체 팩토리 함수
팩토리 함수를 정의하는 가장 일반적인 방법은 companion 객체를 쓰는 것이다.

```kotlin
fun main() {
    val list = MyLinkedList.of(1, 2)
}

open class MyLinkedList<T>(
    val head: T,
    val tail: MyLinkedList<T>?
) {
    companion object {
        fun <T> of(vararg elements: T): MyLinkedList<T>? {
            /*...*/
        }
    }
}
```

기존 자바 개발자라면 이 코드가 static factory function과 같다는 걸 알 수 있을 것이다. C++같은 프로그래밍 언어에선 이를 이름을 가진 생성자(Named Constructor Idiom)라고 부른다.
이름 그대로 생성자 같은 역할을 하면서도 다른 이름이 있기 때문이다. 코틀린에선 이런 접근 방법을 인터페이스에도 구현할 수 있다.

```kotlin
fun main() {
    val list = MyList.of(1, 2)
}

open class MyLinkedList<T>(
    val head: T,
    val tail: MyLinkedList<T>?
) {
    companion object {
        fun <T> of(vararg elements: T): MyLinkedList<T>? {
            /*...*/
        }
    }
}

interface MyList<T> {
    // ...
    
    companion object {
        fun <T> of(vararg elements: T): MyList<T>? {
            // ...
        }
    }
}
```

함수명만 보면 뭐 하는 함수인지 모를 수도 있지만 대부분 개발자는 자바에서 온 규칙 덕분에 이미 이 이름에 익숙할 것이므로 큰 문제 없이 이해할 수 있을 것이다. 이외에도 다양한 이름들이 많이 사용된다.
* from : 파라미터를 하나 받고 같은 타입의 인스턴스 하나를 리턴하는 타입 변환 함수를 나타낸다.
* of : 파라미터를 여럿 받고 이를 통합해 인스턴스를 만들어주는 함수를 나타낸다.
* valueOf : from 또는 of와 비슷한 기능을 하면서도 의미를 좀 더 쉽게 읽을 수 있게 이름을 붙인 함수이다.
* instance 또는 getInstance : 싱글턴으로 인스턴스 하나를 리턴하는 함수. 파라미터가 있을 경우 아규먼트를 기반으로 하는 인스턴스를 리턴한다. 일반적으로 같은 아규먼트를 넣으면 같은 인스턴스를 리턴하는 형태로 작동한다
* createInstance 또는 newInstance : getInstance처럼 동작하지만 싱글턴이 적용되지 않아서 함수 호출 시마다 새 인스턴스를 만들어 리턴한다.
* getType : getInstance처럼 동작하지만 팩토리 함수가 다른 클래스에 있을 때 쓰는 이름이다. 타입은 팩토리 함수에서 리턴하는 타입이다.
* newType : newInstance처럼 동작하지만 팩토리 함수가 다른 클래스에 있을 때 쓰는 이름이다. 타입은 팩토리 함수에서 리턴하는 타입이다.

companion 객체는 인터페이스를 구현할 수 있고 클래스를 상속받을 수도 있다.

```kotlin
fun main() {
    val intent = MainActivity.getIntent(context)
    MainActivity.start()
    MainActivity.startForResult(activity, requestCode)
}

abstract class ActivityFactory {
    abstract fun getIntent(context: Context): Intent

    fun start(context: Context) {
        val intent = getIntent(context)
        context.startActivity(intent)
    }

    fun startForResult(activity: Activity, requestCode: Int) {
        val intent = getIntent(activity)
        activity.startActivityForResult(intent, requestCode)
    }
}

class MainActivity : AppCompatActivity() {
    // ...

    companion object: ActivityFactory() {
        override fun getIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java)
    }
}
```

추상 companion 객체 팩토리는 값을 가질 수 있다. 따라서 캐싱을 구현하거나 테스트를 위한 가짜 객체 생성(fake creation)을 할 수 있다.

### 확장 팩토리 함수
이미 companion 객체가 존재할 때 이 객체의 함수처럼 사용할 수 있는 팩토리 함수를 만들어야 할 때가 있다. 이런 경우 확장 함수를 활용하면 된다.

```kotlin
interface Tool {
    companion object { /*...*/ }
}

fun Tool.Companion.createBigTool(/*...*/): BigTool {
    // ...
}

Tool.createBigTool()
```

이런 코드를 활용하면 팩토리 메서드를 만들어서 외부 라이브러리를 확장할 수 있다. 다만 companion 객체를 확장하려면 적어도 비어 있는 컴패니언 객체가 필요하다.

### 톱레벨 팩토리 함수
톱레벨 팩토리 함수의 대표적인 예로 listOf, setOf, mapOf가 있다. 안드로이드에선 액티비티를 시작하기 위해 인텐트를 만드는 함수를 정의해서 사용한다.

```kotlin
class MainActivity : AppCompatActivity() {
    
    companion object {
        fun getIntent(context: Context) =
            Intent(context, MainActivity::class.java)
    }
}
```

코틀린 Anko 라이브러리를 쓰면 reified 타입을 활용해 intentFor라는 톱레벨 함수를 쓰는 코드를 작성할 수 있다. 또한 아규먼트를 전달할 떄도 사용할 수 있다.

```kotlin
intentFor<MainActivity>("page" to 2, "row" to 10)
```

public 톱레벨 함수는 모든 곳에서 쓸 수 있으므로 IDE가 제공하는 팁을 복잡하게 만드는 단점이 있다. 톱레벨 함수를 만들 땐 꼭 이름을 신중하게 생각해서 잘 지정해야 한다.

### 가짜 생성자
코틀린의 생성자는 톱레벨 함수와 같은 형태로 사용된다. 따라서 아래처럼 톱레벨 함수처럼 참조될 수 있다. 생성자 레퍼런스는 함수 인터페이스로 구현한다.

```kotlin
val reference: () -> A = ::A
```

인터페이스를 위한 생성자를 만들 수 있는 케이스로 List, MutableList가 있다.

```kotlin
public inline fun <T> List(
    size: Int,
    init: (index: Int) -> T
): List<T> = MutableList(size, init)

public inline fun <T> MutableList(size: Int, init: (index: Int) -> T): MutableList<T> {
    val list = ArrayList<T>(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}
```

이런 톱레벨 함수는 생성자처럼 보이고 생성자처럼 작동한다. 하지만 팩토리 함수와 같은 모든 장점을 갖는다. 이 함수가 톱레벨 함수인지 잘 모른다. 그래서 이걸 가짜 생성자(fake constructor)라고 부른다.

#### 가짜 생성자를 만드는 이유
* 인터페이스를 위한 생성자를 만들고 싶을 때
* reified 타입 아규먼트를 갖게 하고 싶을 때

가짜 생성자는 진짜 생성자처럼 동작해야 한다. 생성자처럼 보여야 하고 생성자와 같은 동작을 해야 한다. 캐싱, nullable 타입 리턴, 서브클래스 리턴 등의 기능까지 포함해 객체를 만들고 싶다면 companion 객체 팩토리 메서드처럼 다른 이름을 가진 팩토리 함수를 쓰는 게 좋다.

가짜 생성자를 선언하는 또 다른 방법이 있다. invoke 연산자를 갖는 companion 객체를 쓰면 비슷한 결과를 얻을 수 있다.

```kotlin
fun main() {
    Tree(10) { "$it" }
}

class Tree<T> {
    companion object {
        operator fun <T> invoke(
            size: Int,
            generator: (Int) -> T
        ): Tree<T> {
            // ...
        }
    }
}
```

다만 이런 방식은 거의 쓰이지 않고 필자도 추천하지 않는다. 연산자 오버로드를 할 때는 의미에 맞게 하는 것에 위배 되기 때문이다.

리플렉션을 보면 지금까지 살펴보았던 생성자, 가짜 생성자, invoke 함수의 복잡성을 확인할 수 있다.
```kotlin
// 생성자
val f: () -> Tree = ::Tree

// 가짜 생성자
val f2: () -> Tree = ::Tree

// invoke()를 갖는 companion 객체
val f3: () -> Tree = Tree.Companion::invoke
```

가짜 생성자는 톱레벨 함수를 쓰는 게 좋다. 기본 생성자를 만들 수 없는 상황 또는 생성자가 제공하지 않는 기능(reified 타입 파라미터 등)으로 생성자를 만들어야 하는 상황에만 가짜 생성자를 쓰는 게 좋다.

### 팩토리 클래스이 메서드
팩토리 클래스와 관련된 추상 팩토리, 프로토타입 등의 수많은 생성 패턴이 있다.
이런 패턴 중 일부는 코틀린에선 적합하지 않다. 예를 들어 점층적 생성자 패턴, 빌더 패턴은 코틀린에선 의미가 없다.
팩토리 클래스는 클래스의 상태를 가질 수 있다는 특징 때문에 팩토리 함수보다 다양한 기능을 가진다.

```kotlin
data class Student(
    val id: Int,
    val name: String,
    val surName: String
)

class StudentFactory {
    var nextId = 0
    fun next(name: String, surName: String) =
        Student(nextId++, name, surName)
}

val factory = StudentFactory()
val s1 = factory.next(name = "Marcin", surName = "Moskala")
println(s1) // Student(id=0, name=Marcin, surName=Moskala)
val s2 = factory.next(name = "Igor", surName = "Wojda")
println(s2) // Student(id=1, name=Igor, surName=Wojda)
```

팩토리 클래스는 프로퍼티를 가질 수 있다. 이를 활용하면 다양한 종류로 최적화하고, 다양한 기능을 도입할 수 있다.
예를 들어 캐싱을 활용하거나, 이전에 만든 객체를 복제해서 객체를 생성하는 방법으로 객체 생성 속도를 높일 수 있다.

### 정리
코틀린은 팩토리 함수를 만들 수 있는 다양한 방법들은 제공한다.
가짜 생성자, 톱레벨 팩토리 함수, 확장 팩토리 함수 등 일부는 신중하게 사용해야 한다.
팩토리 함수를 정의하는 가장 일반적인 방법은 companion 객체를 사용하는 것이다.