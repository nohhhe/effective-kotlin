아이템 47.인라인 클래스의 사용을 고려하라 
=========================
기본 생성자 프로퍼티가 하나인 클래스 앞에 inline을 붙이면, 해당 객체를 사용하는 위치가 모두 해당 프로퍼티로 교체된다. inline 클래스의 메서드는 모두 정적 메서드로 만들어집니다.
```kotlin
inline class Name(private val value: String) {
    // ...
    fun greet() {
      print("Hello, I am $value")
    }
}

// 코드
val name: Name = Name("Marcin")

// 컴파일 때 다음과 같은 형태로 바뀐다.
val name: String = "Marcin"

// 코드
val name: Name = Name("Marcin")
name.greet()

// 컴파일 때 다음과 같은 형태로 바뀐다.
val name: String = "Marcin"
Name.`greet-impl`(name)
```

인라인 클래스는 다른 자료형을 래핑해서 새로운 자료형을 만들 때 많이 사용되며, 어떠한 오버헤드도 발생하지 않는다.

#### inline 클래스가 사용되는 경우
* 측정 단위를 표현할 때
* 타입 오용으로 발생하는 문제를 막을 때

### 측정 단위를 표현할 때
측정 단위 혼동은 굉장히 큰 문제를 초래할 수 있다. 해결 방법은 타입에 제한을 거는 것이다. 제한을 걸면 제네릭 유형을 잘못 사용하는 문제를 줄일 수 있다. 코드를 더 효율적으로 만들려면, 인라인 클래스를 활용한다.
```kotlin
inline class Minutes(val minutes: Int) {
    fun toMillis(): Millis = Millis(minutes * 60 * 1000)
}

inline class Millis(val milliseconds: Int) {
    // ...
}

interface User {
    fun decideAboutTime(): Minutes
    fun wakeUp()
}

interface Timer {
    fun callAfter(timeMillis: Millis, callback: ()->Unit)
}

fun setUpUserWakeUpUser(user: User, timer: Timer) {
    val time: Minutes = user.decideAboutTime()
    timer.callAfter(time) { // 오류: Type mismatch
        user.wakeUp()
    }
}
```

객체 생성을 위해서 DSL-like 확장 프로퍼티를 만들어 두어도 좋다. (DSL-like: DSL과 유사한 형태로 만들어진 것)
```kotlin
val Int.min get() = Minutes(this)
val Int.ms get() = Millis(this)

val time: Minutes = 10.min
```

### 타입 오용으로 발생하는 문제를 막을 때
SQL 데이터베이스는 일반적으로 ID를 사용해서 요소를 식별한다. 모든 ID가 같은 자료형일 경우 실수로 잘못된 값을 넣으면 문제가 발생했을 때 오류가 발생하지 않으므로 디버깅이 어렵다. 이런 문제를 해결하기 위해 인라인 클래스를 사용할 수 있다.
```kotlin
inline class StudentId(val id: Int)
inline class TeacherId(val id: Int)
inline class SchoolId(val id: Int)

@Entity(tableName = "grades")
class Grades(
    @ColumnInfo(name = "studentId")
    val studentId: StudentId,
    @ColumnInfo(name = "teacherId")
    val teacherId: TeacherId,
    @ColumnInfo(name = "schoolId")
    val schoolId: SchoolId
    //...
)
```

### 인라인 클래스와 인터페이스
인라인 클래스도 다른 클래스와 마찬가지로 인터페이스를 구현할 수는 있지만, inline으로 동작하지 않는다. 인터페이스를 통해서 타입을 나타내려면, 객체를 래핑해서 사용해야하기 때문이다.

### typealias
여러 typealias를 혼용해서 입력하더라도 오류가 발생하지 않는다. 단위 등을 표현하려면 인라인 클래스를 사용하는 것이 좋다.
```kotlin
typealias Seconds = Int
typealias Millis = Int

fun getTime(): Millis = 10
fun setUpTimer(time: Seconds) {}

fun main() {
    val seconds: Seconds = 10
    val millis: Millis = seconds // 컴파일 오류가 발생하지 않는다.
    
    setUpTimer(getTime())
}
```

### 정리
인라인 클래스를 사용하면 성능적인 오버헤드 없이 타입을 래핑할 수 있다. 인라인 클래스는 타입 시스템을 통해 실수로 코드를 잘못 작성하는 것을 막아주므로, 코드의 안정성을 향상시켜 준다.
으미가 명확하지 않은 타입, 특히 여러 측정 단위들을 함께 사용하는 경우에는 인라인 클래스를 활용하자.