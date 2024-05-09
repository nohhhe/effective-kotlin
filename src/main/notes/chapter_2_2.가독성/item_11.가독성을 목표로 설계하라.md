아이템 11. 가독성을 목표로 설계하라
=========================
프로그래밍은 쓰기보다 읽기가 중요하다. 항상 가독성을 생각하면서 코드를 작성해야 한다.

### 인식 부하 감소
```kotlin
// 구현 A
if (person != null && person.isAdult) {
  view.shopwPerson(person)
} else {
  view.showError()
}

// 구현 B
person?.takeIf { it.isAdult }
  ?.let(view::showPerson)
  ?: view.showError()
```

구현 A는 일반적인 관용구(if / else, &&, 메소드 호출)을 사용하고 있어 초보자가 더 이해하기 쉽다.

구현 B는 관용구(안정 호출?.*, takeIf, let, Elvis 연산자, 제한된 함수 레퍼런스 view::showPerson)를 사용하고 있어 숙련된 개발자라면 쉽게 읽지만 좋은 코드는 아니다.

```kotlin
// 구현 A
if (person != null && person.isAdult) {
  view.showPerson(person)
  view.hideProgressWithSuccess()
} else {
  view.showError()
  view.hideProgress()
}

// 구현 B
person?.takeIf { it.isAdult }
  ?.let {
    view.showPerson(it)
    view.hideProgressWithSuccess()
  } ?: run {
    view.showError()
    view.hideProgress()
  }
```

구현 A는 수정하기도 쉽고 디버깅도 간단하다. 또한 위의 소스는 실행 결과가 다르다.

구현 B는 showPerson이 null을 리턴하면 showError도 호출한다.(수정 전 소스를 의미하는 것인가❓) 익숙하지 않은 구조를 사용하면 잘못된 동작을 코드를 보면서 확인하기가 어렵다.

기본적으로 인지 부하를 줄이는 방향으로 코드를 작성해야 빠르게 이해할 수 있다.

### 극단적이 되지 않기
let으로 인해 예상치 못한 결과가 나오는 경우를 보고 let을 절대로 쓰면 안된다고 이해하는 경우가 있다.

let은 좋은 코드를 만들기 위해서 다양하게 활용된다.
```kotlin
// 스마트 캐스팅할 때
class Person(val name: String)
var person: Person? = null

fun printName() {
    person?.let {
        print(it.name) 
    }
}

// 연산을 아규먼트 처리 후로 이동 시킬 때
students
    .filter { it.result >= 50 }
    .joinToString(separator = "\n") {
        "${it.name} ${it.surname}, ${it.result}"
    }
    .let(::print)

// 데코레이터를 사용해서 객체를 랩할 때
var obj = FileInputStream("/file.gz")
    .let(::BufferedInputStream)
    .let(::ZipInputStream)
    .let(::ObjectInputStream)
    .readObject() as SomeObject
```

위의 코드는 디버그하기 어렵고 이해하는데 비용이 발생하지만 지불할 만한 가치가 있으므로 사용해도 괜찮다.
문제가 되는 경우는 비용을 지불할 만한 가치가 없는 코드에 비용을 지불하는 경우이다.(이유 없이 복잡성을 추가할 때)

### 컨벤션
개발할 때 함수 이름을 어떻게 지어야 하는지, 어떤 것이 명시적이야 하는지, 어떤 것이 암묵적이어야 하는지, 어떤 관용구를 사용해야 하는지 등을 토론하고 이를 위해 이해하고 기억해야 하는 규칙들이 존재한다.

```kotlin
val abc = "A" { "B" } and "C"
print(abc)

operator fun String.invoke(f: () -> String): String = this + f()
infix fun String.and(s: String) = this + s
```

위의 코드는 수많은 규칙들을 위반한다.
* 연산자는 의미에 맞게 사용해야 한다. invoke를 이러한 형태로 사용하면 안된다.
* 람다를 마지막 아규먼트로 사용한다라는 컨벤션을 여기에 적용하면 코드가 복잡해진다.
* 현재 코드에서 and라는 함수 이름이 실제 함수 내부에서 이루어지는 처리와 맞지 않다.
* 문자열을 결합하는 기능은 이미 언어에 내장되어 있어 이미 있는 것들 다시 만들 필요는 없다.
