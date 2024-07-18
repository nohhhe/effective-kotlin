아이템 37. 데이터 집합 표현에 data 한정자를 사용하라
=========================
데이터를 한꺼번에 전달해야 할 때는 data 클래스를 사용하자.

```kotlin
data class Player(
    val id: Int,
    val name: String,
    val points: Int
)

val player = Player(0, "Gecko", 9999)
```

#### data 한정자를 붙이면 생성되는 함수
* toString: 클래스이 이름과 기본 생성자 형태로 모든 프로퍼티와 값을 출력
* equals: 기본 생성자의 프로퍼티가 같은지 확인
* hashCode: equals와 같은 결과
* copy: 기본 생성자 프로퍼티가 같은 새로운 객체를 얕은 복사, 깊은 복사가 필요 없는 immutable 객체를 만들 때 유용하다.
* componentN: 위치를 기반으로 객체를 해제

```kotlin
// componentN 예제
val (id, name, pts) = player

val id: Int = player.component1()
val name: String = player.component2()
val pts: Int = player.component3()

val visited = listOf("China", "Russia", "India")
val (first, second, third) = visited
println("$first, $second, $third") // China, Russia, India

val trip = mapOf(
    "China" to "Tianjin",
    "Russia" to "Petersburg",
    "India" to "Rishikesh"
)
for ((country, city) in trip) {
    println("we loved $city in $country")
}

data class FullName(
    val firstName: String,
    val secondName: String,
    val lastName: String
)
val elon = FullName("Elon", "Reeve", "Musk")
val (name, surname) = elon
print("It is $name $surname") // It is Elon Reeve
```

### 튜플 대신 데이터 클래스 사용하기
구체적으로 코틀린의 튜플은 Serializable 기반으로 만들어지며, toString을 사용할 수 있는 제네릭 데이터 클래스이다.

#### 튜플을 사용하는 케이스
* 값에 간단하게 이름을 붙일 떄
* 미리 알 수 없는 집합을 표현할 때
 
```kotlin
// 값에 간단하게 이름을 붙일 때
val (description, color) = when {
    degrees < 5 -> "cold" to Color.BLUE
    degrees < 23 -> "mild" to Color.YELLOW
    else -> "hot" to Color.RED
}

// 미리 알 수 없는 집합을 표현할 때
val (odd, even) = numbers.partition { it % 2 == 1 }
val map = mapOf(1 to "San Francisco", 2 to "Amsterdam")
```

#### 데이터 클래스를 사용할 때 장점
* 함수의 리턴 타입이 더 명확해진다.
* 리턴 타입이 더 짧아지며, 전달하기 쉬워진다.
* 사용자가 데이터 클래스에 적혀 있는 것과 다른 이름을 활용해 변수를 해제하면, 경고가 출력된다.

클래스가 좁은 스코프를 갖게하고 싶다면, 일반적인 클래스와 같은 형태로 가시성에 제한을 걸어 두면 된다.

#### 데이터 클래스의 문제점
* 엔티티 클래스로는 사용안하는 것을 권장
  * 1:N 관계에서 순환 참조로 인해 StackOverFlow Error 발생할 수 있다. 
    * 해결 방안: toString, equals, hashCode를 오버라이드해서 순환 참조를 방지한다. 또는 한쪽에서 삭제한다.
    * https://stackoverflow.com/questions/48926704/kotlin-data-class-entity-throws-stackoverflowerror
  * copy의 얕은 복사로 인해 참조가 복사되어 문제가 발생할 수 있다.
  * Hibernate의 Lazy Loading을 사용하기 위해서는 데이터 클래스를 사용할 수 없다. (프록시 객체 문제)
    * https://velog.io/@donghyunele/kotlin-jpa-2-%EC%9A%B0%EC%95%84%ED%95%9C%ED%98%95%EC%A0%9C%EB%93%A4-%EA%B8%B0%EC%88%A0%EB%B8%94%EB%A1%9C%EA%B7%B8
* 자동으로 만들어주는 함수 때문에 용량이 크다. (App의 경우는 용량에 대한 이슈가 있을 수 있다.)