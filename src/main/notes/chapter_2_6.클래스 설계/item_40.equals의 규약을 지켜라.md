아이템 40. equals의 규약을 지켜라
=========================
코틀린의 Any에는 잘 설정된 규약을 가진 아래 메서드들이 있다.
* equals
* hashCode
* toString

Any 클래스를 상속받는 모든 메서드는 이런 규약을 잘 지키는 게 좋다.

### 동등성
코틀린에는 두 가지 종류의 동등성 있다.
* 구조적 동등성 : equals 메서드와 이를 기반으로 만들어진 !=, == 연산자로 확인하는 동등성이다. a가 nullable이 아니면 a == b는 a.equals(b)로 변환되고, a가 nullable이면 a?.equals(b) ?: (b === null)로 변환된다.
* 레퍼런스적 동등성 : !==, === 연산자로 확인하는 동등성이다. 두 피연산자가 같은 객체를 가리키면 true를 리턴한다.

equals는 모든 클래스의 슈퍼클래스인 Any에 구현돼 있으므로 모든 객체에서 쓸 수 있다. 다만 연산자를 써서 다른 타입의 두 객체를 비교하는 건 허용되지 않는다.

```kotlin
open class Animal
class Book
Animal() == Book() // 오류
Animal() === Book() // 오류
```

같은 타입을 비교하거나 둘이 상속 관계를 가질 땐 비교할 수 있다.

```kotlin
open class Animal
class Cat: Animal()

Animal() == Cat()
Animal() === Cat()
```

### equals가 필요한 이유
equals는 디폴트로 ===처럼 두 인스턴스가 완전히 같은 객체인지 비교한다. 이는 모든 객체는 디폴트로 유일한 객체라는 걸 의미한다.

동등성을 약간 다른 형태로 표현해야 하는 객체가 있다. 예를 들어 두 객체가 기본 생성자의 프로퍼티가 같다면 같은 객체로 보는 형태가 있을 수 있다. data 한정자를 붙여서 데이터 클래스로 정의하면 자동으로 이런 동등성으로 동작한다.

```kotlin
data class FullName(val name: String, val surname: String)

val name1 = FullName("Marcin", "Moskala")
val name2 = FullName("Marcin", "Moskala")
val name3 = FullName("Maja", "Moskala")

println(name1 == name1) // true
println(name1 == name2) // true (데이터가 같기 때문)
println(name1 == name3) // false

println(name1 === name1) // true
println(name1 === name2) // false
println(name1 === name3) // false
```

데이터 클래스는 내부에 어떤 값을 가졌는지가 중요하므로 이렇게 동작하는 게 좋다. 데이터 클래스의 동등성은 모든 프로퍼티가 아닌 일부 프로퍼티만 비교해야 할 때도 유용하다. 

```kotlin
class DateTime(
    private var millis: Long = 0L,
    private var timeZone: TimeZone? = null
) {
    private var asStringCache = ""
    private var changed = false

    override fun equals(other: Any?): Boolean =
        other is DateTime &&
                other.millis == millis &&
                other.timeZone == timeZone
}
```

기본 생성자에 선언되지 않은 프로퍼티는 copy로 복사되지 않는다.

data 한정자를 기반으로 동등성의 동작을 조작할 수 있으므로 일반적으로 코틀린에선 equals를 직접 구현할 필요가 없다. 
다만 상황에 따라 equals를 직접 구현해야 하는 경우가 있을 수 있다. 또한 일부 프로퍼티만 같은지 확인해야 하는 경우 등이 있을 수 있다.

```kotlin
class User(
    val id: Int,
    val name: String,
    val surname: String
) {
    override fun equals(other: Any?): Boolean =
        other is User && other.id == id

    override fun hashCode(): Int = id
}
```

#### equals를 직접 구현해야 하는 경우
* 기본 제공되는 동작과 다른 동작을 해야 하는 경우
* 일부 프로퍼티만으로 비교해야 하는 경우
* data 한정자를 붙이는 걸 원하지 않거나 비교해야 하는 프로퍼티가 기본 생성자에 없는 경우

### equals의 규약
* 반사적(reflexive) 동작 : x가 null이 아닌 값이면 x.equals(x)는 true를 리턴해야 한다.
```kotlin
class Time(
    val millisArg: Long = -1,
    val isShow: Boolean = false
) {
    val millis: Long
        get() = if (isShow) System.currentTimeMillis() else millisArg

    override fun equals(other: Any?): Boolean =
        other is Time && millis == other.millis
}

val now = Time(isShow = true)
now = now // 때에 따라서 true, false로 나올 수 있다.
List(100_000) { now }.all { it == now } // 대부분 false
```

위의 코드를 수정하기 위해서는 클래스 계층 구조를 사용해서 해결하는 것이 좋다.

```kotlin
sealed class Time
data class TimePoint(val millis: Long): Time()
object Now: Time() {
    val millis: Long
        get() = System.currentTimeMillis()
}

val now = Now
val timePoint = TimePoint(System.currentTimeMillis())

println(now == now) // true
println(timePoint == timePoint) // true
```

* 대칭적(symmetric) 동작 : x, y가 null이 아닌 값이면 x.equals(y)는 y.equals(x)와 같은 결과를 출력해야 한다.
```kotlin
class Complex(
    val real: Double,
    val imaginary: Double
) {
    override fun equals(other: Any?): Boolean {
        if (other is Double) {
            return imaginary == 0.0 && real == other
        }
        return other is Complex && real == other.real && imaginary == other.imaginary
    }
}

println(Complex(1.0, 0.0).equals(1.0))  // true
println(1.0.equals(Complex(1.0, 0.0)))  // false
```

* 연속적(transitive) 동작 : x, y, z가 null이 아닌 값이고 x.equals(y)와 y.equals(z)가 true라면 x.equals(z)도 true여야 한다.
```kotlin
open class Date(
    val year: Int,
    val month: Int,
    val day: Int
) {
    override fun equals(other: Any?): Boolean = when (other) {
        is DateTime -> this == other.date
        is Date -> other.day == day && other.month == month && other.year == year
        else -> false
    }
}

class DateTime(
    val date: Date,
    val hour: Int,
    val minute: Int,
    val second: Int
): Date(date.year, date.month, date.day) {
    override fun equals(other: Any?): Boolean = when (other) {
        is DateTime -> other.date == date && other.hour == hour && other.minute == minute && other.second == second
        is Date -> date == other
        else -> false
    }
}

val o1 = DateTime(Date(1992, 10, 20), 12, 30, 0)
val o2 = Date(1992, 10, 20)
val o3 = DateTime(Date(1992, 10, 20), 14, 45, 30)

println(o1 == o2)   // true
println(o2 == o3)   // true
println(o1 == o3)   // false
```

* 일관적(consistent) 동작 : x, y가 null이 아닌 값이면 x.equals(y)는 비교에 쓰이는 프로퍼티를 변경한 게 아니라면 여러 번 실행하더라도 항상 같은 결과를 반환해야 한다.
* null과 관련된 동작 : x가 null이 아닌 값이면 x.equals(null)은 항상 false를 리턴해야 한다.

### equals 구현하기
특별한 이유가 없는 이상 equals 직접 구현하는 것은 좋지 않다. 직접 구현해야 한다면 반사적, 대칭적, 연속적, 일관적 동작을 하는지 꼭 확인해야한다.
이러한 클래스는 final로 만드는 것이 좋다. 만약 상속한다면 서브클래스에서 equals의 작동 방식을 변경하면 안 된다. 데이터 클래스는 언제나 final이다.