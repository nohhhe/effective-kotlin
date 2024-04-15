아이템 4. inferred 타입으로 리턴하지 말라
=========================
코틀린의 타입 추론 (type inference)은 JVM 세계에서 가장 널리 알려진 코틀린의 특징이다. (자바도 자바 10부터 코틀린을 따라 도입했지만 코틀린과 비교하면 제약이 있다.)

다만 타입 추론을 사용할 때는 몇 가지 위험한 부분들이 있다. 이런 위험한 부분을 피하려면, inferred 타입은 정확하게 오른쪽 피연산자에 맞게 설정을 해야한다. 슈퍼클래스 또는 인터페이스로는 절대 설정되지 않는다.

```kotlin
open class Animal
class Zebra: Animal()

fun main() {
    var animal = Zebra()
    animal = Animal() // 오류: type mismatch
}
```

원하는 타입보다 제한된 타입이 설정되었다면, 타입을 명시적으로 지정해서 이러한 문제를 해결할 수 있다.

```kotlin
open class Animal
class Zebra: Animal()

fun main() {
    var animal: Animal = Zebra()
    animal = Animal()
}
```

하지만 직접 라이브러리(또는 모듈)를 조작할 수 없는 경우에는 이러한 문제를 간단하게 해결할 수 없다. 그리고 이러한 경우에는 inferred 타입을 노출하면 위험하다.

```kotlin
interface CarFactory {
    fun produce(): Car
}

val DEFAULT_CAR: Car = Fiat126P()
```

CarFactory 인터페이스가 존재하고 대부분의 공장에서 Fiat126P를 생산하므로, 이를 디폴트로 두었다고 가정하자.

코드를 작성하다보니 DEFAULT_CAR는 Car로 명시적으로 지정되어 있어서 함수의 리턴 타입을 제거했다고 하자.

```kotlin
interface CarFactory {
    fun produce() = DEFAULT_CAR
}
```

그 이후에 DEFAULT_CAR는 타입 추론에 의해 자동으로 타입이 지정될 것으로, Car를 명시적으로 지정하지 않아도 된다고 생각하여 Car 타입을 삭제해보자.

```kotlin
val DEFAULT_CAR = Fiat126P()
```

CarFactory 인터페이스는 이제 Fiat126P 이외의 자동차를 생산하지 못한다. 해당 문제가 외부 API에서 발생 한다면 쉽게 해결할 수 없다.

❗️리턴 타입은 API를 잘 모르는 사람에게 전달해 줄 수 있는 중요한 정보이다. 따라서 리턴 타입은 외부에서 확인할 수 있게 명시적으로 지정해줘야 한다.

### 정리
1. 타입을 확실하게 지정해야 하는 경우에는 명시적으로 타입을 지정해야 한다. (중요한 정보이므로 숨기지 말자.)
2. 외부 API를 만들 때는 반드시 타입을 지정하고, 특별할 이유와 확실한 확인 없이는 제거하지 말아야 한다.
3. inferred 타입은 프로젝트가 진전될 때, 제한이 너무 많아지거나 예측하지 못한 결과를 낼 수 있다.