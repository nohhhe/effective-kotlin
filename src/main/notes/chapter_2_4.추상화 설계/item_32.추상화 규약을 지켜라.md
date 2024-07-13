아이템 32. 추상화 규약을 지켜라
=========================
기술적으로 모든 부분에서 규약 위반이 발생할 수 있다. 예를 들어 리플렉션을 활용하면, 열고 사용할 수 있다.

```kotlin
class Employee {
    private val id: Int = 2
    override fun toString(): String = "User(id = $id)"

    private fun privateFunction() = println("Private function called")
}

fun callPrivateFunction(employee: Employee) {
    employee::class.declaredMemberFunctions
        .first { it.name == "privateFunction" }
        .apply { isAccessible = true }
        .call(employee)
}

fun changeEmployeeId(employee: Employee, newId: Int) {
    employee::class.java.getDeclaredField("id")
        .apply { isAccessible = true }
        .set(employee, newId)
}

fun main() {
    val employee = Employee()
    callPrivateFunction(employee) // Private function called
    changeEmployeeId(employee, 1) // User(id = 1)
    print(employee)
}
```

규약을 위반하면, 코드가 작동을 멈췄을 때 문제가 된다.

### 상송된 규약
클래스를 상속하거나, 다른 라이브러리의 인터페이스를 구현할 때는 규악을 반드시 지켜야 한다.

```kotlin
class Id(val id: Int) {
    override fun equals(other: Any?): Boolean = other is Id && other.id == id
}

fun main() {
    val set = mutableSetOf(Id(1))
    set.add(Id(1))
    set.add(Id(1))
    println(set.size) // 3
}
```