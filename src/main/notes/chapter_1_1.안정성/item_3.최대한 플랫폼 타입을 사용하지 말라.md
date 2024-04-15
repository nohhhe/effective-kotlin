아이템 3. 최대한 플랫폼 타입을 사용하지 말라
=========================
코틀린의 등장과 함께 소개된 널 안정성(null-safety)은 코틀린의 주요 기능 중 하나이다.
하지만 null-safety 메커니즘이 없는 자바, C 등의 프로그래밍 언어와 코틀린을 연결해서 사용할 때는 예외가 발생할 수 있다.
#### 자바에서 코틀린으로 넘어가는 경우
* @Nullable 어노테이션이 있다면, nullable (ex.String?)
* @NotNull 어노테이션이 있다면, non-null (ex.String)
* 어노테이션이 붙지 않을 경우, 코틀린은 플랫폼 타입(platform type)으로 추정

❗️플랫폼 타입: 다른 프로그래밍 언어에서 전달되어서 nullable인지 아닌지 알 수 없는 타입

플랫폼 타입은 String!처럼 타입 뒤에 ! 기호를 붙여서 표기합니다. 직접적으로 코드에 나타내지는 않는다.

```java
// 자바
public class UserRepo {
    public User getUser() {
        //...
    }
}
```
```kotlin
// 코틀린
val repo = UserRepo()
val user1 = repo.user        // user1의 타입은 User!
val user2: User = repo.user  // user2의 타입은 User
val user3: User? = repo.user // user3의 타입은 User?

val users: List<User> = UserRepo().users
val users: List<List<User>> = UserRepo().groupedUsers
```

문제는 null이 아니라고 생각되는 것이 null일 가능성이 있으므로, 여전히 위험하다.
자바를 코틀린과 함께 사용할 때 자바 코드를 직접 조작할 수 있다면, 가능한 @Nullable, @NotNull 어노테이션을 사용하는 것이 좋다.

```java
// 자바 
import org.jetbrains.annotations.NotNull;

public class UserRepo {
    public @NotNull User getUser() {
        //...
    }
}
```

플랫폼 타입은 안전하지 않으므로, 최대한 빨리 제거해야 한다.

```java
// 자바
public class JavaClass {
    public String getValue() {
        return null;
    }
}
``` 
```kotlin
// 코틀린
fun statedType() {
    val value: String = JavaClass().value // NPE 발생
    // ...
    println(value.length)
}

fun platformType() {
    val value = JavaClass().value
    // ...
    println(value.length) // NPE 발생
}
```

두 가지 모두 NPE(Null-Pointer Exception)가 발생한다.
* statedType: 자바에서 값을 가져오는 위치에서 NPE 발생
* platformType: 값을 활용할 때 NPE 발생

플랫폼 타입으로 지정된 변수는 nullable일 수도 있고, 아닐 수도 있다. 언제든지 NPE를 발생시킬 가능성이 있기 때문에 플랫폼 타입은 더 많은 위험을 가지고 있다.

```kotlin
interface UserRepo {
    fun getUserName() = JavaClass().value
}

class RepoImpl: UserRepo {
    override fun getUserName(): String? {
        return null
    }
}

fun main() {
    val repo: UserRepo = RepoImpl()
    
    val text: String = repo.getUserName() // 런타임 때 NPE
    print("User name length is ${text.length}")
}
```

userRepo.getUserName()은 inferred 타입(추론된 타입)이 플랫폼 타입이 되며, 사용하는 사람이 nullable이 아닐 거라고 사용하면 런타임에 NPE가 발생할 수 있다.

### 정리
1. 다른 프로그래밍 언어에서 와서 nullable 여부를 알 수 없는 값을 플랫폼 타입이라고 한다.
2. 플랫폼 타입은 사용하는 코드 뿐만 아니라 활용하는 곳까지 영향을 줄 수 있는 위험한 코드이며 빨리 해당 코드를 제거하는게 좋다.
3. 연결되어 있는 자바 생성자, 메서드, 필드에 nullable 여부를 지정하는 어노테이션을 사용하는 것이 좋다. (즉, 플랫폼 타입을 사용하지 않도록 한다.)