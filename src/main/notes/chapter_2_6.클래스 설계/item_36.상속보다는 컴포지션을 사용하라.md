아이템 36. 상속보다는 컴포지션을 사용하라
=========================
상속은 굉장히 강력한 기능으로 "is-a" 관계의 객체 계층 구조를 만들기 위해 설계됐다. 상속은 관계가 불명확할 때 쓰면 여러 문제가 발생할 수 있다. 
따라서 단순하게 코드 추출 또는 재사용을 위해 상속을 하려고 한다면 좀 더 신중하게 생각해야 한다. 일반적으로 이런 경우에는 상속보다 컴포지션을 쓰는 게 좋다.

### 간단한 행위 재사용
#### 상속의 단점
* 상속은 하나의 클래스만을 대상으로 할 수 있다. 상속을 써서 행위를 추출하다 보면 많은 함수를 갖는 거대한 BaseXXX 클래스를 만들게 되고, 굉장히 깊고 복잡한 계층 구조가 만들어진다.
* 상속은 클래스의 모든 걸 가져오게 된다. 따라서 불필요한 함수를 갖는 클래스가 만들어질 수 있다. 인터페이스 분리 원칙을 위반하게 된다.
* 상속은 이해하기 어렵다. 일반적으로 개발자가 메서드를 읽고 메서드 작동 방식을 이해하기 위해 슈퍼클래스를 여러 번 확인해야 한다면 문제가 있는 것이다.

대안 방법으로는 컴포지션이 있다. 컴포지션을 사용한다는 것은 객체를 프로퍼티로 갖고, 함수를 호출하는 형태로 재사용하는 것을 의미한다.

```kotlin
class Progress {
    fun showProgress() { /* 프로그레스 바 표시 */ }
    fun hideProgress() { /* 프로그레스 바 숨김 */ }
}

class ProfileLoader {
    val progress = Progress()
    
    fun load() {
        progress.showProgress()
        // 프로필 읽어들임
        progress.hideProgress()
    }
}

class ImageLoader {
    val progress = Progress()
    
    fun load() {
        progress.showProgress()
        // 이미지 읽어들임
        progress.hideProgress()
    }
}
```

컴포지션을 활용하며 명확하게 예측할 수 있고, 자유롭게 사용할 수 있다. 또한 하나의 클래스 내부에서 여러 기능을 재사용할 수 있다.

```kotlin
class ImageLoader {
    private val progress = Progress()
    private val finishedAlert = FinishedAlert() // 예시로 작성한 코드라 복붙 시 컴파일 에러 발생

    fun load() {
        progress.showProgress()
        // 이미지 읽어들임
        progress.hideProgress()
        finishedAlert.show()
    }
}
```

하나 이상의 클래스를 상속할 수 없다. 따라서 상속으로 구현하려면, 두 기능을 하나의 슈퍼클래스에 배치해야 한다. 이 때문에 클래스들에 복잡한 계층 구조가 만들어 질 수 있다.

```kotlin
abstract class InternetLoader(val showAlert: Boolean) {
    fun load() {
        // 프로그레스 바 표시
        innerLoad()
        if (showAlert) {
            // 경고창 출력
        }
    }

    abstract fun innerLoad()
}

class ProfileLoader: InternetLoader(showAlert = true) {
    override fun innerLoad() {
        // 프로필 읽어들임
    }
}

class ImageLoader: InternetLoader(showAlert = true) {
    override fun innerLoad() {
        // 이미지 읽어들임
    }
}
```

위의 소스는 서브클래스가 필요하지도 않은 기능을 갖고, 단순하게 이를 차단할 뿐이고 제대로 차단하지 못하면 문제를 발생할 수 있다.

### 모든 것을 가져올 수밖에 없는 상속
상속은 슈퍼클래스의 메서드, 제약, 행위 등 모든 걸 가져온다. 따라서 상속은 객체의 계층 구조를 나타낼 때 굉장히 좋은 도구다.
하지만 일부분을 재사용하기 위한 목적으론 적합하지 않다. 일부분만 재사용하고 싶다면 컴포지션을 쓰는 게 좋다.

```kotlin
abstract class Dog {
    open fun bark() { /*...*/ }
    open fun sniff() { /*...*/ }
}

class Labrador: Dog()

class RobotDog: Dog() {
    override fun sniff() {
        throw Error("지원되지 않는 기능입니다")
        // 인터페이스 분리 원칙에 위반됨
    }
}
```

위의 소스는 인터페이스 분리 원칙에 위반된다. 또한 슈퍼클래스의 동작을 서브클래스에서 깨버리므로 리스코프 치환 원칙에도 위반된다. 코틀린은 다중 상속을 지원하지 않는다.

### 캡슐화를 깨는 상속
내부적인 구현 방법 변경에 의해서 클래스이 캡슐화가 깨질 수 있다.

```kotlin
class CounterSet<T>: HashSet<T>() {
    var elementsAdded: Int = 0
        private set

    override fun add(element: T): Boolean {
        elementsAdded++
        return super.add(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        elementsAdded += elements.size
        return super.addAll(elements)
    }
}

fun main() {
    val counterList = CounterSet<String>()
    counterList.addAll(listOf("A", "B", "C"))
    print(counterList.elementsAdded) // 6
}
```

왜 문제가 발생한 건가? 문제는 HashSet의 addAll 안에서 add를 썼기 때문이다. addAll과 add에서 추가한 요소 개수를 중복해서 세므로 요소 3개를 추가했는데 6이 출력되는 것이다. 간단하게 addAll()을 제거하면 이런 문제가 사라진다.
하지만 HashSet.addAll을 최적화하고 add를 호출하지 않는 방식으로 변경되었다면 문제가 생길 수 있다. 이런 문제를 방지하려면 컴포지션을 사용하는 게 좋다.

```kotlin
class CounterSet<T> {
    private var innerSet = HashSet<T>()
    var elementsAdded: Int = 0
        private set

    fun add(element: T) {
        elementsAdded++
        innerSet.add(element)
    }

    fun addAll(elements: Collection<T>) {
        elementsAdded += elements.size
        innerSet.addAll(elements)
    }
}
```

위의 소스는 다형성이 사라진다는 문제가 있다. CounterSet은 더 이상 Set이 아니다. 만약 이를 유지하고 싶다면 위임 패턴을 사용할 수 있다.
위임 패턴은 클래스가 인터페이스를 상속받게 하고, 포함한 객체의 메서드들을 활용해서 인터페이스에서 정의한 메서드를 구현하는 패턴이다. 이렇게 구현된 메서드를 포워딩 메서드라고 부른다.

```kotlin
class CounterSet<T> (
    private val innerSet: MutableList<T> = mutableListOf()
) : MutableSet<T> by innerSet {
    var elementsAdded: Int = 0
        private set
    
    fun add(element: T) {
        elementsAdded++
        innerSet.add(element)
    }
    
    fun addAll(elements: Collection<T>) {
        elementsAdded += elements.size
        innerSet.addAll(elements)
    }
}

fun main() {
    val counterList = CounterSet<String>()
    counterList.addAll(listOf("A", "B", "C"))
    print(counterList.elementsAdded)    // 3
}
```

### 오버라이딩 제한하기
개발자가 상속용으로 설계되지 않은 클래스를 상속하지 못하게 하려면 final을 쓰면 된다. 그런데 만약 어떤 이유로 상속은 허용하지만 메서드는 오버라이드하지 못하게 만들고 싶을 수 있다. 이런 경우 메서드에 open 키워드를 사용한다. open 클래스는 open 메서드만 오버라이드할 수 있다.

```kotlin
open class Parent {
    fun a() {}
    open fun b() {}
}

class Child: Parent() {
    override fun a() {} // 오류
    override fun b() {}
}
```

메서드를 오버라이드할 때 서브클래스에서 해당 메서드에 final을 붙일 수도 있다.

```kotlin
abstract class InternetLoader {
    open fun loadFromInternet() {
        //
    }
}

open class ProfileLoader: InternetLoader() {
    final override fun loadFromInternet() {
        // 프로파일을 읽어들임
    }
}
```

### 상속은 언제 사용하면 좋을까요?
슈퍼클래스를 상속하는 모든 서브클래스는 슈퍼클래스로도 동작할 수 있어야 한다. 슈퍼클래스의 모든 단위 테스트는 서브클래스로도 통과할 수 있어야 한다는 의미이다.(리스코프 치환 원칙) 
