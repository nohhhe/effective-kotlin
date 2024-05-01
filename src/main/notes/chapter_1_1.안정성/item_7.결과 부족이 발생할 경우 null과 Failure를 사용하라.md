아이템 5. 결과 부족이 발생할 경우 null과 Failure를 사용하라
=========================
함수가 원하는 결과를 만들어 낼 수 없을 경우가 있다.
* 서버로부터 데이터를 읽어 들이려고 했는데, 인터넷 연결 문제로 읽어 들이지 못한 경우 
* 조건에 맞는 첫 번째 요소를 찾으려 했는데, 조건에 맞는 요소가 없는 경우 
* 텍스트를 파싱해서 객체를 만들려고 했는데, 텍스트의 형식이 맞지 않는 경우

이러한 상황을 처리하는 방법은 두 가지가 있다.
* null 또는 실패를 나타내는 sealed 클래스(일반적으로 Failure라는 이름을 붙임)를 리턴한다.
* 예외를 throw 한다.

예외는 정보를 전달하는 방법으로 사용되선 안되고 예외적인 상황이 발생했을 때 사용하는 것이 좋다. 이유는 아래와 같다.
* 많은 개발자가 예외가 전파되는 과정을 제대로 추적하지 못한다.
* 코틀린의 모든 예외는 unchecked 예외다. 따라서 사용자가 예외를 처리하지 않을 수도 있으며 이와 관련된 내용은 문서에도 제대로 드러나지 않는다. 실제로 API를 쓸 때 예외 관련 사항을 단순하게 메서드 등을 사용하면서 파악하기 힘들다.
  * unchecked: 처리하지 않아도 실행에 문제 없는 예외
  * checked: 반드시 처리하게 강제되는 예외
* 예외는 예외적인 상황을 처리하기 위해 만들어졌으므로 명시적인 테스트(explicit test)만큼 빠르게 동작하지 않는다.
* try-catch 블록 안에 코드를 배치하면 컴파일러가 할 수 있는 최적화가 제한된다.
  * 제어 흐름 복잡성 증가: 컴파일러가 실행 흐름을 분석하면서 고려해야 할 경로가 늘어나게 되므로, 코드의 복잡성과 예측 불가능성이 증가 
  * 인라이닝(inlining) 작업 제한: 인라이닝은 함수 호출 오버헤드를 줄이기 위해 함수 호출을 함수의 본문으로 대체하는 컴파일러 최적화 기법이다. 하지만, try-catch 블록 내부에 코드가 있으면, 예외가 발생할 수 있는 가능성 때문에 인라이닝을 수행하기 어렵다. 
  * 예외 생성 비용: 예외가 발생하면 종종 예외 객체를 생성하고 스택 트레이스 정보를 채워야 한다. 이 과정은 상당한 시스템 자원을 소비할 수 있으며, 컴파일러가 성능 측면에서 고려해야 할 추가적인 부담을 만든다. 
  * 코드의 해석 가능성 감소: 예외를 발생시킬 수 있는 여부를 컴파일러가 정확하게 판단하기 어려워, 컴파일러는 보수적인 방식으로 최적화를 수행한다. 이는 특정 최적화를 수행하지 않거나, 전체적으로 최적화의 범위가 좁아질 수 있음을 의미한다.

반면 null, Failure는 예상되는 오류를 표현할 때 굉장히 좋다. 이는 명시적이고 효율적이며 간단한 방법으로 처리 가능하다. 따라서 충분히 예측할 수 있는 범위의 오류는 null, Failure를 쓰고 예측하기 어려운 예외적인 범위의 오류는 예외를 throw해서 처리하는 게 좋다.

```kotlin
inline fun <reified T> String.readObjectOrNull(): T? {
  // ...
  if (incorrectSign) {
    return null
  }
  // ...
  return result
}

inline fun <reified T> String.readObject(): Result<T> {
  // ...
  if (incorrectSign) {
    return Failure(JsonParsingException())
  }
  // ...
  return Success(result)
}

sealed class Result<out T>
class Success<out T>(val result: T): Result<T>()
class Failure(val throwable: Throwable): Result<Nothing>()

class JsonParsingException: Exception()
```

null을 처리해야 한다면 사용자는 safe call 또는 엘비스 연산자 같은 다양한 null-safety 기능을 활용한다.

```kotlin
val age = userText.readObjectOrNull<Person>()?.age ?: -1
```

Result 같은 공용체(union type)를 리턴하기로 했다면 when 표현식을 써서 처리할 수 있다.

```kotlin
val person = userText.readObjectOrNull<Person>()
val age = when(person) {
  is Success -> person.age
  is Failure -> -1
}
```

이러한 오류 처리 방식은 try-catch 블록보다 효율적이고 사용하기 쉽고 더 명확하다. 예외는 놓칠 수도 있고 전체 애플리케이션을 중지시킬 수도 있다. null 값과 sealed Result 클래스는 명시적으로 처리해야 하며 애플리케이션의 흐름을 중지하지도 않는다.

추가적인 정보를 전달해야 한다면 sealed result 클래스를 쓰고 그게 아니면 null을 쓰는 게 일반적이다. Failure는 처리할 때 필요한 정보를 가질 수 있다.

일반적으로 2가지 형태의 함수를 쓴다. 하나는 예상할 수 있을 때, 다른 하나는 예상할 수 없을 때 사용한다.
* get : 특정 위치의 요소를 추출할 때 사용. 요소가 해당 위치에 없으면 IndexOutOfBoundsException 발생 
* getOrNull : out of range 오류가 발생할 수 있는 경우 사용. 발생 시 null 리턴

이외에도 일부 상황에 유용한 getOrDefault 같은 다른 선택지도 있다. 하지만 일반적으로 getOrNull 또는 엘비스 연산자를 쓰는 게 쉽다.

❗개발자는 항상 자신이 요소를 안전하게 추출할 거라 생각한다. 따라서 nullable을 리턴하면 안 된다. 개발자에게 null이 발생할 수 있다는 경고를 주려면 getOrNull을 써서 뭐가 리턴되는지 예측할 수 있게 하는 게 좋다.

