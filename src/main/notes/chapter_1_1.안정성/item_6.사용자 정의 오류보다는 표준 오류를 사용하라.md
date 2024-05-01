아이템 6. 사용자 정의 오류보다는 표준 오류를 사용하라
=========================
require, check, assert 함수를 사용하면, 대부분의 코틀린 오류를 처리할 수 있다. 하지만 이외에도 예측하지 못한 상황을 나타내야 하는 경우가 있다.

```kotlin
inline fun <reified T> String.readOjbect(): T {
    // ...
    if (incorrectSign) {
        throw JsonParsingException()
    }
    // ...
    return result
}
```

표준 라이브러리에는 이를 나타내는 적절한 오류가 없으므로, 사용자 정의 오류를 사용했지만 직접 오류를 정의하는 것보다는 최대한 표준 라이브러리의 오류를 사용하는 것이 좋다.

표준 라이브러리의 오류는 많은 개발자가 알고 있으므로, 이를 재사용하는 것이 좋다.

#### 일반적으로 사용되는 예외
* IllegalArgumentException, IllegalStateException : require, check를 써서 throw 할 수 있는 예외 
* IndexOutOfBoundsException : 인덱스 파라미터 값이 범위를 벗어났다는 걸 나타낸다. 일반적으로 컬렉션 또는 배열과 같이 쓴다 
* ConcurrentModificationException : 동시 수정(concurrent modification)을 금지했는데 발생했다는 걸 나타낸다 
* UnsupportedOperationException : 사용자가 사용하려고 했던 메서드가 현재 객체에선 사용할 수 없다는 걸 나타낸다. 기본적으로 사용할 수 없는 메서드는 클래스에 없는 게 좋다 
* NoSuchElementException : 사용자가 사용하려고 했던 요소가 존재하지 않음을 나타낸다