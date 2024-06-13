아이템 24. 제네릭 타입과 variance 한정자를 활용하라
=========================
다음과 같은 제네릭 클래스가 있다.

```kotlin
class Cup<T>
```

위의 코드에서 타입 파라미터 T는 variance 한정자(out 또는 in)가 없으므로, 기본적으로 invariant(불공변성)이다.
invariant(불공변성)라는 것은 제네릭 타입으로 만들어지는 타입들이 서로 관련성이 없다는 의미이다.

```kotlin
fun main() {
    val anys: Cup<Any> = Cup<Int>() // Error: type mismatch
    val nothing: Cup<Nothing> = Cup<Int>() // Error: type mismatch
}
```

만약에 어떤 관련성을 원한다면, out 또는 in이라는 variance 한정자를 붙인다. out은 타입 파라미터를 covariant(공변성)로 만든다.

```kotlin
class Cup<out T>
open class Dog
class Puppy: Dog()

fun main() {
    val b: Cup<Dog> = Cup<Puppy>() // OK
    val a: Cup<Puppy> = Cup<Dog>() // Error
    val anys: Cup<Any> = Cup<Int>() // OK
    val nothings: Cup<Nothing> = Cup<Int>() // Error
}
```

in 한정자는 반대 의미이다. in 한정자는 타입 파라미터를 contravariance(반공변성)으로 만든다.

```kotlin
class Cup<in T>
open class Dog
class Puppy: Dog()

fun main() {
    val b: Cup<Dog> = Cup<Puppy>() // Error
    val a: Cup<Puppy> = Cup<Dog>() // OK
    val anys: Cup<Any> = Cup<Int>() // Error
    val nothings: Cup<Nothing> = Cup<Int>() // OK
}
```

### 함수 타입
함수 타입은 파라미터 유형과 리턴 타입에 따라서 서로 어떤 관계를 갖는다.

```kotlin
fun printProcessedNumber(transition: (Int)->Any) {
    print(transition(42))
}
```

(Int)->Any 타입(Int를 받고, Any를 리턴하는 함수)의 함수는 (Int)->Number, (Number)->Any, (Number)->Number, (Number)->Int 등으로도 작동한다.

```kotlin
val intToDouble: (Int) -> Number = { it.toDouble() }
val numberAsText: (Number) -> Any = { it.toShort() }
val identity: (Number) -> Number = { it }
val numberToInt: (Number) -> Int = { it.toInt() }
val numberHash: (Any) -> Number = { it.hashCode() }

printProcessedNumber(intToDouble)
printProcessedNumber(numberAsText)
printProcessedNumber(identity)
printProcessedNumber(numberToInt)
printProcessedNumber(numberHash)
```

코틀린 함수의 타입의 모든 파라미터 타입은 contravariant(반공변성)이다. 또한 모든 리턴 타입은 covariant(공변성)이다.

함수 타입을 사용할 때는 자동으로 variance 한정자가 사용된다. 코틀린에서 자주 사용되는 것으로는 convariant(out 한정자)를 가진 List가 있다.

### variance 한정자의 안정성
자바의 배열은 convariant입니다. 배열을 기반으로 제네릭 연산자는 정렬 함수 등을 만들기 위해서이다.
그러나 자바 배열이 convariant(공변성)라는 속성을 갖기 때문에 문제가 발생합니다.

```java
Integer[] numbers = {1, 2, 3, 4};
Object[] objs = numbers;
object[2] = "B"; // Runtime에 ArrayStoreException이 발생
```

이 코드는 컴파일 중에 아무런 문제도 없지만 런타임 오류가 발생한다. numbers를 Object[]로 캐스팅해도 구조 내부에서 사용되고 있는 실질적인 타입이 바뀌는 것은 아니며, 여전히 Interger이다.
틀린은 이러한 결함을 해결하기 위해서 Array(IntArray, CharArray 등)를 invariant(무공변성)로 만들었다. (따라서 Array<Int>를 Array<Any> 등으로 바꿀 수 없다.)
파라미터 타입을 예측할 수 있다면, 어떤 서브타입이라도 전달할 수 있습니다.

파라미터 타입을 예측할 수 있다면, 어떤 서브타입이라도 전달할 수 있다. 따라서 아규먼트를 전달할 때, 암묵적으로 업캐스팅할 수 있다.

```kotlin
open class Dog
class Puppy: Dog()
class Hound: Dog()

fun takeDog(dog: Dog) { }

takeDog(Dog())
takeDog(Puppy())
takeDog(Hound())
```

이는 convariant(공변성)하지 않다. covariant 타입 파라미터(out 한정자)가 in 한정자 위치(예를 들어 타입 파라미터)에 있다면, convariant(공변성)와 업캐스팅을 연결해서, 우리가 원하는 타입을 아무것이나 전달할 수 있다.

```kotlin
open class Dog
class Puppy: Dog()
class Hound: Dog()

class Box<out T> {

  private var value: T? = null

  fun set(value: T) {
    this.value = value
  }

  fun get(): T = value ?: error("value not set")
}

val puppyBox = Box<Puppy>()
val dogBox: Box<Dog> = puppyBox
dogBox.set(Hound()) // Puppy를 위한 공간이므로 문제 발생

val dogHouse = Box<Dog>()
val box: Box<Any> = dogHouse
box.set("Some String") // Dog를 위한 공간이므로 문제 발생
box.set(42) // Dog를 위한 공간이므로 문제 발생
```

캐스팅 후에 실질적인 객체가 그대로 유지되고, 타이핑 시스템에서만 다르게 처리되기 때문에 위의 코드는 안전하지 않다.

코틀린은 public in 한정자 위치에 covariant 타입 파라미터(out 한정자)가 오는 것을 금지하여 이러한 상황을 막는다.

```kotlin
class Box<out T> {
    var value: T? = null // Type parameter T is declared as 'out' but occurs in 'in' position in type T

    fun set(value: T) { // Type parameter T is declared as 'out' but occurs in 'in' position in type T
      this.value = value 
    }

    fun get(): T = value ?: error("value not set")
}
```

가시성을 private로 제한하면, 오류가 발생하지 않는다. 객체 내부에서는 업캐스트 객체에 convariant(out 한정자)를 사용할 수 없기 때문이다.

```kotlin
class Box<out T> {
    private var value: T? = null

    private fun set(value: T) {
        this.value = value
    }

    fun get(): T = value ?: error("value not set")
}
```

❗️여기서 중요한 점은 out 한정자가 붙은 타입 파라미터는 "읽기 전용" 용도로만 사용할 수 있다는 것이다. 즉, out T는 메서드 파라미터로 사용할 수 없고, 오직 반환 타입으로만 사용할 수 있다.

covariant(out 한정자)는 public out 한정자 위치에서도 안전하므로 따로 제한되지 않습니다.
이러한 안정성의 이유로 생성되거나 노출되는 타입에만 covariant(out 한정자)를 사용하는 것입니다.

예로 T는 covariant인 List<T>와 Response가 있다. 함수의 파라미터가 List<Any?>로 예측된다면, 모든 종류를 파라미터로 전달할 수 있다.
다만, MutableList<T>에서 T는 in 한정자 위치에서 사용되며, 안전하지 않으므로 invariant 이다.

```kotlin
fun append(list: MutableList<Any>) {
    list.add(42)
}

fun main() {
    val strs = mutableListOf<String>("A","B","C")
    append(strs) //Type mismatch. Required:MutableList<Any> Found:MutableList<String>
    val str: String = strs[3]
    print(str)
}
```

Response에 사용하면 다양한 이득을 얻을 수 있다. variance 한정자 덕분에 아래 내용은 모두 참이 된다.

* Response<T>라면 T의 모든 서브타입이 허용된다. Response<Any>가 허용된다면 Response<Int>, Response<String>이 허용된다
* Response<T1, T2>라면 T1, T2의 모든 서브타입이 허용된다
* Failure<T>라면 T의 모든 서브타입 Failure가 허용된다. Failure<Number>라면 Failure<Int>, Failure<Double>이 모두 허용된다
* Failure<Any>라면 Failure<Int>, Failure<String>이 모두 허용된다

아래 코드는 covariant와 Nothing 타입으로 인해 Failure는 오류 타입을 지정하지 않아도 되고 Success는 잠재적인 값을 지정하지 않아도 된다.

```kotlin
sealed class Response<out R, out E>
class Failure<out E>(val error: E): Response<Nothing, E>()
class Success<out R>(val value: R): Response<R, Nothing>()
```

covariant와 public in 위치 같은 문제는 contravariant 타입 파라미터(in 한정자)와 public out 위치(함수 리턴 타입 또는 프로퍼티 타입)에서도 발생한다. out 위치는 암묵적인 업캐스팅을 허용한다.

```kotlin
open class Car
interface Boat
class Amphibious: Car(), Boat

fun getAmphibious(): Amphibious = Amphibious()

val car: Car = getAmphibious()
val boat: Boat = getAmphibious()
```

이는 contravariant(in 한정자)에 맞는 동작이 아니다.

```kotlin
class Box<in T> {
    // 코틀린에서 쓸 수 없는 코드
    val value: T
}

val garage: Box<Car> = Box(Car())
val amphibiousSpot: Box<Amphibious> = garage
val boat: Boat = garage.value // Car를 위한 공간

val noSpot: Box<Nothing> = Box<Car>(Car())
val boat: Nothing = noSpot.value
// 아무것도 만들 수 없음
```

이런 상황을 막기 위해 코틀린은 contravariant 타입 파라미터(in 한정자)를 public out 한정자 위치에 쓰는 걸 금지하고 있다.

```kotlin
class Box<in T> {
    var value: T? = null    // 오류
    
    fun set(value: T) {
        this.value = value
    }
    
    fun get(): T = value    // 오류
        ?: error("Value not set")
}
```

이번에도 요소가 private면 아무런 문제가 없다.

```kotlin
class Box<in T> {
    private var value: T? = null
    
    fun set(value: T) {
        this.value = value
    }

    private fun get(): T = value
        ?: error("Value not set")
}
```

❗️변수 value의 경우 별도의 variance 한정자가 없기 때문에 invariant(불공변성)이다. 따라서 in, out 모두 사용할 수 없다.

이런 형태로 타입 파라미터에 contravariant(in 한정자)를 사용한다. 추가적으로 많이 사용되는 예로는 kotlin.coroutines.Continuation이 있다.

```kotlin
public interface Continuation<in T> {
    public val context: CoroutineContext
    public fun resumeWith(result: Result<T>)
}
```

### variance 한정자의 위치
variance 한정자는 크게 두 위치에 쓸 수 있다. 첫 번째는 선언 부분이다. 이 위치에서 사용하면 클래스와 인터페이스 선언에 한정자가 적용되기 때문에 클래스, 인터페이스가 쓰이는 모든 곳에 영향을 준다.

```kotlin
// 선언 쪽의 variance 한정자
class Box<out T>(val value: T)
val boxStr: Box<String> = Box("string")
val boxAny: Box<Any> = boxStr
```

두 번째는 클래스와 인터페이스를 활용하는 위치이다. 이 위치에 variance 한정자를 쓰면 특정한 변수에만 variance 한정자가 적용된다.

```kotlin
class Box<T>(val value: T)
val boxStr: Box<String> = Box("string")
// 사용하는 쪽의 variance 한정자
val boxAny: Box<out Any> = boxStr
```

모든 인스턴스에 variance 한정자를 적용하면 안 되고, 특정 인스턴스에만 적용해야 할 때 이런 코드를 사용한다.
예를 들어 MutableList는 in 한정자를 포함하면, 요소를 리턴할 수 없으므로 in 한정자를 붙이지 않는다.
하지만 단일 파라미터 타입에 in 한정자를 붙여서 contravariant를 가지게 할 수 있다.

```kotlin
interface Dog
interface Cutie
data class Puppy(val name: String): Dog, Cutie
data class Hound(val name: String): Dog
data class Cat(val name: String): Cutie

fun fillWithPuppies(list: MutableList<in Puppy>) {
    list.add(Puppy("Jim"))
    list.add(Puppy("Beam"))
}

fun main() {
    val dogs = mutableListOf<Dog>(Hound("Pluto"))
    fillWithPuppies(dogs)
    println(dogs)

    val animals = mutableListOf<Cutie>(Cat("Felix"))
    fillWithPuppies(animals)
    println(animals)
}
```

### 정리
* 타입 파라미터의 기본적인 variance의 동작은 invariant이다.
* out 한정자는 타입 파라미터를 covariant하게 만든다.
* in 한정자는 타입 파라미터를 contravariant하게 만든다.

코틀린에서 variance 한정자
* List와 Set의 타입 파라미터는 covariant(out 한정자)이다. 또한 Map에서 값의 타입을 나타내는 타입 파라미터는 covariant(out 한정자)이다. Array, MutableList, MutableSet, MutableMap의 타입 파라미터는 invariant(한정자 지정 없음)이다.
* 함수 타입의 파라미터 타입은 contravariant(in 한정자)이다. 그리고 리턴타입은 covariant(out 한정자)이다.
* 리턴만 되는 타입에는 covariant(out 한정자)를 사용한다.
* 허용만 되는 타입에는 contravariant(in 한정자)를 사용한다.