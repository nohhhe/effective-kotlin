아이템 41. hashCode의 규약을 지켜라
=========================
오버라이드할 수 있는 Any의 메서드로는 hashCode가 있다. hashCode 함수는 수많은 컬렉션과 알고리즘에 사용되는 자료 구조인 해시 테이블을 구축할 떄 사용된다.

### 해시 테이블
컬렉션에 요소를 빠르게 추가하고 추출해야 할 때 사용할 수 있는 컬렉션은 Set, Map이 있다. 이 둘은 중복을 허용하지 않는다. 따라서 요소를 추가할 때 동일한 요소가 이미 들어 있는지 확인해야 한다. 
배열 또는 링크드 리스트 기반으로 만들어진 컬렉션은 요소가 포함돼 있는지 확인하는 성능이 좋지 않다. 요소가 포함되어 있는지 확인할 때 모든 요소와 비교해야 하기 때문이다. 

성능을 좋게 만드는 해결법이 해시 테이블이다. 해시 테이블은 각 요소에 숫자를 할당하는 함수가 필요하다. 이 함수를 해시 함수라고 부르며 같은 요소라면 항상 같은 숫자를 리턴한다.

#### 해시 함수 갖으면 좋은 특성
* 빠르다
* 충돌이 적다(다른 값이면 최대한 다른 숫자를 리턴한다)

해시 함수는 각각의 요소에 특정 숫자를 할당하고 이를 기반으로 요소를 다른 버킷(bucket)에 넣는다. 또한 해시 함수의 기본 조건(같은 요소면 항상 같은 숫자 리턴)에 의해 같은 요소는 항상 동일한 버킷에 넣게 된다.
버킷은 버킷 수와 같은 크기의 배열인 해시 테이블에 보관된다. 요소를 추가하는 경우에는 해시 함수로 배치할 버킷을 계산하고 이 버킷 안에 요소를 추가한다. 
해시 함수의 속도는 빨라야 하므로 이 처리는 굉장히 빨리 이뤄진다. 요소를 찾는 경우에도 해시 함수로 만들어지는 숫자를 활용해 버킷을 찾은 뒤 버킷 내부에서 원하는 요소를 찾는다. 
해시 함수는 같은 요소라면 같은 값을 리턴하므로 다른 버킷을 확인할 필요 없이 바로 원하는 게 들어 있는 버킷을 찾을 수 있다.
아래 문자열이 있고 4개의 버킷으로 분할되는 해시 함수가 있다고 가정한다

| 텍스트 | 해시 코드 |
|-------|-------|
| How much wood would a woodchunk chunk | 3     |
| Peter piper picked a peck | 2     |
| SBetty bought a bit of butter | 1     |
| She sells seashells | 2     |

| 인덱스 | 해시 테이블이 가리키는 객체 코드 |
|-----|-------|
| 0   | []     |
| 1   | ["Betty bought a bit of butter"]     |
| 2   | ["Peter piper picked a peck", "She sells seashells"]     |
| 3   | ["How much wood would a woodchunk chunk"]     |

### 가변성과 관련된 문제
요소가 추가될 때만 해시 코드를 계산한다. 요소가 바뀌어도 해시 코드는 계산되지 않고 버킷 재배치도 이뤄지지 않는다. 
그래서 기본적인 LinkedHashSet과 LinkedHashMap의 키는 한 번 추가한 요소를 바꿀 수 없다. 따라서 Set, Map의 키로 mutable 요소를 쓰면 안 되며, 사용하더라도 요소를 바꿔선 안 된다. 이러한 이유로 immutable 객체를 많이 사용한다.

```kotlin
data class FullName(
    var name: String,
    var surname: String
)

val person = FullName("Maja", "Markiewicz")
val s = mutableSetOf<FullName>()
s.add(person)
person.surname = "Moskala"
print(person) // FullName(name=Maja, surnam=Moskala)
print(person in s) // false, 해시코드가 바뀌었기 때문에 찾을 수 없다.
print(s.first() == person) // true
```

### hashCode의 규약
hashCode는 명확한 규약이 있다.
* 어떤 객체를 바꾸지 않았다면(equals에서 비교에 쓰인 정보가 수정되지 않는 이상) hashCode는 여러 번 호출해도 그 결과가 항상 같아야 한다.
* equals의 실행 결과로 두 객체가 같다고 나온다면 hashCode의 결과도 같다고 나와야 한다.

첫 번째 요구사항은 일관성 유지를 위해 hashCode가 필요하다는 것이다. 두 번째 요구사항은 hashCode와 equals은 같이 일관성 있게 동작해야 한다.
즉 같은 요소는 반드시 같은 해시 코드를 가져야 한다. 그래서 코틀린은 equals 구현을 오버라이드할 때 hashCode도 같이 오버라이드하는 걸 추천한다.

```kotlin
data class FullName(
    var name: String,
    var surname: String
) {
    override fun equals(other: Any?): Boolean  =
        other is FullName && other.name == name && other.surname == surname
}

val s = mutableSetOf<FullName>()
s.add(FullName("Marcin", "Moskala"))
val p = FullName("Marcin", "Moskala")
print(p in s) // false
print(p == s.first()) // true
```

필수 요구 사항은 아니지만 hashCode는 최대한 요소를 넓게 퍼뜨려야 한다. 다른 요소라면 최대한 다른 해시 값을 갖는 것이 좋다.
```kotlin
class Proper(val name: String) {
    override fun equals(other: Any?): Boolean {
        equalsCounter++
        return other is Proper && other.name == name
    }
    
    override fun hashCode(): Int {
        return name.hashCode()
    }
    
    companion object {
        var equalsCounter = 0
    }
}

class Terrible(val name: String) {
    override fun equals(other: Any?): Boolean {
        equalsCounter++
        return other is Terrible && other.name == name
    }
    
    override fun hashCode() = 0
    
    companion object {
        var equalsCounter = 0
    }
}

val properSet = List(10000) { Proper("$it") }.toSet()
println(Proper.equalsCounter) // 0
val terribleSet = List(10000) { Terrible("$it") }.toSet()
println(Terrible.equalsCounter) // 50116683, 모든 요소가 같은 버킷에 들어가기 때문에 값을 넣을 때마다 모든 요소를 비교해야 한다.

Proper.equalsCounter = 0
println(Proper("9999") in properSet) // true
println(Proper.equalsCounter) // 1

Proper.equalsCounter = 0
println(Proper("A") in properSet) // false
println(Proper.equalsCounter) // 0

Terrible.equalsCounter = 0
println(Terrible("9999") in terribleSet) // true
println(Terrible.equalsCounter) // 4324

Terrible.equalsCounter = 0
println(Terrible("A") in terribleSet) // false
println(Terrible.equalsCounter) // 10001, 같은 버킷 내 10000개 존재하기 때문에 모두 비교 필요
```

### hashCode 구현하기
일반적으로 data 한정자를 붙이면 코틀린이 알아서 적당한 equals, hashCode를 정의하므로 직접 정의할 일은 거의 없다. 다만 equals를 따로 정의했다면 반드시 hashCode도 함께 정의해야 한다. equals로 같은 요소라고 판정되는 요소는 hashCode가 반드시 같은 값을 리턴해야 한다.

hashCode는 기본적으로 equlas에서 비교에 쓰이는 프로퍼티를 기반으로 해시 코드를 만들어야 한다.
해시 코드를 만들 때는 일반적으로 모든 해시 코드의 값을 더하고 더하는 과정마다 이전까지의 결과에 31을 곱한 뒤에 더해준다. 물론 31일 필요는 없지만, 관례적으로 31을 많이 사용한다. data 한정자를 붙일 떄도 이렇게 구현된다.

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
           
    override fun hashCode(): Int {
        var result = millis.hashCode()
        result = result * 31 + timeZone.hashCode() // 관례적으로 31을 사용한다.
        return result
    }   
}
```

hashCode 구현 시 가장 중요한 규칙은 언제나 equals와 일관된 결과가 나와야 한다는 것이다. 같은 객체면 언제나 같은 값을 리턴하게 만들어야 한다.