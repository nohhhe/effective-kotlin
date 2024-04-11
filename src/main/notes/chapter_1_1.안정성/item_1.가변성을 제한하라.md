아이템 1.가변성을 제한하라
==========================

```kotlin
class BankAccount {
    var balance = 0.0
        private set

    fun deposit(depositAmount: Double) {
        balance += depositAmount
    }

    @Throws(InsufficientFunds::class)
    fun withdraw(withdrawAmount: Double) {
        if (balance < withdrawAmount) {
            throw InsufficientFunds()
        }
        balance -= withdrawAmount
    }
}

class InsufficientFunds : Exception()

fun main() {
    val account = BankAccount()

    println(account.balance) // 0.0
    account.deposit(100.0)
    println(account.balance) // 100.0
    account.withdraw(50.0)
    println(account.balance)// 50.0
}
```
위 코드의 BankAccount에는 계좌에 돈이 얼마나 있는지 나타내는 상태가 있다.
상태를 갖게 하는 것은 양날의 검이다.
시간의 변화에 따라서 변하는 요소를 표현할 수 있다는 것은 유용하지만, 상태를 적절하게 관리하는 것이 생각보다 어렵다.

#### 상태를 관리하는 어려운 점
1. 프로그램을 이해하고 디버그하기 어려우며, 클래스가 예상하지 못하는 상황 또는 오류를 발생시키는 경우 큰 문제가 된다.
2. 가변성이 있으면, 코드의 실행을 추론하기가 어렵다.
3. 멀티스레드 프로그램이 때는 적절한 동기화가 필요하다.
4. 모든 상태를 테스트해야 되기 때문에 테스트하기 어렵다.
5. 상태 변경이 일어날 때, 이러한 변경을 다른 부분에 알려야 하는 경우가 있다.

변할 수 있는 지점은 줄일수록 좋다.
가변성을 생각보다 단점이 많아서 이를 완전하게 제한하는 프로그래밍 언어인 순수 함수형 언어가 있다.
하지만 이러한 프로그래밍 언어는 가변성에 너무 많은 제한이 걸려서 프로그램을 작성하기가 굉장이 어렵다.

코틀린에서 가변성 제한하기
-------------
코틀린은 가변성을 제한할 수 있게 설계되어 있다.
그래서 immutable(불변) 객체를 만들거나, 프로퍼티를 변경할 수 없게 마는 것이 굉장히 쉽다.

#### 가변성 제한 방법
* 읽기 전용 프로퍼티(val)
* 가변 컬렉션과 읽기 전용 컬렉션 구분하기
* 데이터 클래스의 copy

### 읽기 전용 프로퍼티(val)
코틀린은 val을 사용해 읽기 전용 프로퍼티를 만들 수 있다.(읽고 쓸 수 있는 프로퍼티는 var)
```kotlin
val a= 10
a = 20 // 오류
```

읽기 전용 프로퍼티가 mutable 객체를 담고 있다면, 내부적으로 변할 수 있다.
```kotlin
val list = mutableListOf(1, 2, 3)
list.add(4) // 가능

print(list) // [1, 2, 3, 4]
```

읽기 전용 프로퍼티는 다른 프로퍼를 활용하는 사용자 정의 게터로도 정의할 수 있다.
이렇게 var 프로퍼티를 사용하는 val 프로퍼티는 var 프로퍼티가 변할 때 변할 수 있다.
```kotlin
var name: Stirng = "Marcin"
var surname: String = "Moskala"
val fullName: String
    get() = "$name $surname"

fun main() {
    println(fullName) // Marcin Moskala
    name = "Maja"
    println(fullName) // Maja Moskala
}
```

var은 게터와 세터를 모두 제공하지만, val은 변경이 불가능하므로 게터만 제공한다.
그래서 val을 var로 오버라이드할 수 있다.
```kotlin
interface Element {
    val actvie: Boolean
}
    
class ActiveElement : Element {
    override var active: Boolean = false
}
```

val은 읽기 전용 프로퍼티지만, 변경할 수 없음(불변, immutable)을 의미하는 것은 아니다. 완전히 변경할 필요가 없다면, final 프로퍼티를 사요하는 것이 좋다.
val는 스마트 캐스트 등의 추가적인 기능을 활용할 수 있다.
```kotlin
val name: String = "Marcin"
val surname: String = "Moskala"

val fullName: String?
    get() = name?.let { "$it $surname" }

val fullName2: String? = name?.let { "$it $surname" }

fun main() {
    if (fullName != null) {
        println(fullName.length) // 오류
    }
    
    if (fullName2 != null) {
        println(fullName2.length) // Marcin Moskala
    }
}
```
fullName은 게터로 정의했으므로 스마트 캐스트를 사용할 수 없다. 게터를 활용하므로, 값을 사용하는 시점에 name에 따라서 다른 결과가 나올 수 있기 때문이다.

### 가변 컬렉션과 읽기 전용 컬렉션 구분하기
* 읽기 전용(이미지 왼쪽): Iterable, Collection, Set, List 인터페이스
* 가변(이미지 오른쪽): Mutablelterable, MutableCollection, MutableSet, MutableList 인터페이스
![스크린샷 2024-04-10 21.39.37.png](..%2F..%2F..%2F..%2F..%2F..%2FDesktop%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-04-10%2021.39.37.png)

읽기 전용 컬렉션이 내부의 값을 변경할 수 없다는 의미는 아니다. 변경할 수는 있지만 읽기 전용 인터페이스가 이를 지원하지 않으므로 변경할 수 없습니다. (예, Iterable<T>.map과 Iterable<T>.
filter 함수는 ArrayList를 리턴)
```kotlin
inline fun <T, R> Iterable<T>.map(transform: (T) -> R): List<R> {
    val result = ArrayList<R>()
    for (item in this) {
        result.add(transform(item))
    }
    return result
}
```

읽기 전용 컬렉션을 다운캐스팅을 하면 추상화를 무시하는 행위이며 안전하지 않고 예측하지 못한 결과를 초래할 수 있다.
```kotlin
val list: List<Int> = listOf(1, 2, 3)

if(list is MutableList) {
    list.add(4) // 오류
}
```

읽기 전용 컬렉션을 mutable 컬렉션으로 변경하려면 새로운 mutable 컬렉션을 만드는 toMutableList() 함수를 사용하면 된다.
```kotlin
val list: List<Int> = listOf(1, 2, 3)
val mutableList = list.toMutableList()
mutableList.add(4)
```

### 데이터 클래스의 copy
#### immutable 객체 사용 장점
1. 한 번 정의된 상태가 유지되므로, 코드를 이해하기 쉽다.
2. 공유했을 때도 충돌이 따로 이루어지지 않으므로, 병렬처리를 안전하게 할 수 있다.
3. 참조는 변경되지 않으므로, 쉽게 캐시할 수 있다.
4. 방어적 복사본(defensive copy)을 만들 필요가 없다. 또한 객체를 복사할 때 깊은 복사를 따로 하지 않아도 된다.
5. 다른 객체(mutable 또는 immutable 객체)를 만들 때 활용하기 좋다.
6. ‘세트(set)’ 또는 ‘맵(map)의 키’로 사용할 수 있다.
```kotlin
val names: SortedSet<FullName> = TreeSet()
val person = FullName("AAA", "AAA")
names.add(person)
names.add(FullName("Jordan", "Hansen"))
names.add(FullName("David", "Blanc"))

print(names) // [AAA AAA, David Blanc, Jordan Hansen]
print(person in names) // true

person.name = "ZZZ"
print(names) // [ZZZ AAA, David Blanc, Jordan Hansen]
print(person in names) // false
```

mutable 객체는 예측하기 어려우며 위험하다는 단점이 있고 immutable 객체는 변경할 수 없다는 단점이 있다.
2가지를 모두 보안하기 위해서는 immutable 객체를 사용하고 자신의 일부를 수정한 새로운 객체를 만들어내는 메서드를 가져야 한다.
```kotlin
class User (
   val name:String, 
   val surname: String
) {
   fun withSurname(surname: String) = User(name, usrname)
}

var user = User("Maja", "Markiewicz")
user = user.withSurname("Moskala")
print(user) // User(name=Maja, surname=Moskala)
```

이런 함수를 모두 만드는 것은 귀찮은 일이다. 데이터 클래스는 copy라는 메서드를 만들어주고 이런 일을 대신해준다.
```kotlin
data class User (
   val name:String, 
   val surname: String
)

var user = User("Maja", "Markiewicz")
user = user.copy(surname = "Moskala")
print(user) // User(name=Maja, surname=Moskala)
```

### 다른 종류의 변경 가능 지점
#### 가변 리스트를 만드는 방법
* mutable 컬렉션템
* mutable 프로퍼티(var)
```kotlin
val list1: MutableList<Int> = mutableListOf()
var list2: List<Int> = listOf()

list1.add(1)
list2 = list2 + 1

// 위의 코드는 += 연산자를 사용해도 된다.
list1 += 1 // list1.plusAssign(1)로 변경됨
list2 += 1 // list2 = list2.plus(1)로 변경됨
```
️❓   
첫 번째 코드는 구체적인 리스트 구현 내부에 변경 가능 지점이 있다. 멀티스레드 처리가 이루어질 경우, 내부적으로 적절한 동기화가 되어 있는지 확실하게 알 수 없으므로 위험하다.   
두 번째 코드는 프로퍼티 자체가 변경 가능 지점입니다. 따라서 멀티스레드 처리의 안정성이 더 좋다고 할 수 있다.

mutable 프로퍼티는 사용자 정의 세터(또는 이를 사용하는 델리게이트)를 활용해서 변경을 추적할 수 있다.
```kotlin
var list: List<Int> = listOf()
    set(value) {
        field = value
        println("List changed to $value")
    }

list = list + 1 // List changed to [1]
```

참고로 최악의 방식은 프로퍼티와 컬렉션을 모두 변경 가능한 지점으로 만드는 것
```kotlin
var list: MutableList<Int> = mutableListOf()
```

### 변경 가능 지점 노출하지 말기
mutable 객체를 외부에 노출할 경우 돌발적인 수정이 일어날 수 있고 이로 인해 예상치 못한 결과가 발생할 수 있다.
```kotlin
data class User(val name: String)

class UserRepository {
    private val storedUsers: MutableMap<Int, String> = mutableMapOf()

    fun loadAll(): MutableMap<Int, String> {
        return storedllsers
    }
}

val repository = UserRepository()
val storedUsers = repository.loadAll()
storedUsers[1] = "Maja"
print(repository.loadAll()) // {1=Maja}
```

#### 해결 방안
1. 방어적 복제
```kotlin
data class MutableUser(val name: String)

class UserHolder {
    private val user: MutableUser
            
    fun get(): MatableUser {
        return user.copy()
    }
}
```
2. 가변성 제한
```kotlin
data class User(val name: String)

class UserRepository {
    private val storedUsers: MutableMap<Int, String> = mutableMapOf()

    fun loadAll(): Map<Int, String> {
        return storedUsers
    }
}
```

### 정리
1. var보다는 val을 사용하는 것이 좋다.
2. mutable 프로퍼티보다는 immutable 프로퍼티를 사용하는 것이 좋다.
3. mutable 객체와 클래스보다는 immutable 객체와 클래스를 사용하는 것이 좋다. 
4. 변경이 필요한 대상을 만들어야 한다면, immutable 데이터 클래스로 만들고 copy를 활용하는 것이 좋다.
5. 컬렉션에 상태를 저장해야 한다면, mutable 컬렉션보다는 읽기 전용 컬렉션을 사용하는 것이 좋다. 
6. 변이 지점을 적절하게 설계하고, 불필요한 변이 지점은 만들지 않는 것이 좋다. 
7. mutable 객체를 외부에 노출하지 않는 것이 좋습니다. 
8. mutable 객체를 사용할 때는, 멀티스레드에 더 많은 주의를 기울여야 한다.

#### 예외, 가끔 효율성 때문에 immutable 객체보다 mutable 객체를 사용해야 하는 경우가 있다.
1. 대규모 데이터 구조 변경: 대규모 데이터 구조를 변경할 때는, 변경이 필요한 부분만 변경하는 것이 효율적이다.
2. 메모리 사용 최적화: 객체를 복사할 때, 매번 새로운 객체를 생성하니 메모리를 많이 사용하는 경우가 있다.
