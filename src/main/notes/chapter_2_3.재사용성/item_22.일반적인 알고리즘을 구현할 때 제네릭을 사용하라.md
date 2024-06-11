아이템 21. 일반적인 알고리즘을 구현할 때 제너릭을 사용하라
=========================
아규먼트로 함수에 값을 전달할 수 있는 것처럼 타입 아규먼트를 사용하면 함수에 타입을 전달할수 있다. 타입 아규먼트를 사용하는 함수를 제네릭 함수라고 부른다.

```kotlin
// stdlib에서 제공하는 filter 제네릭 함수 
inline fun <T> Iterable<T>.filter() : List<T> {
    val destination = ArrayList<T>
    for (element in this) {
        if (predicate(element)) {
            destination.add(element)
        }
    }
    return destination
}
```

타입 파라미터는 컴파일러에 타입과 관련된 정보를 제공해 컴파일러가 타입을 조금이라도 더 정확하게 추측할 수 있게 해준다.

### 제네릭 제한
타입 파라미터의 중요한 기능 중 하나는 구체적인 타입의 서브타입만 사용하게 타입을 제한하는 것이다.

콜론 뒤에 슈퍼타입을 설정해 타입에 제한을 건다.
```kotlin
fun <T: Comparable<T>> Iterable<T>.sorted(): List<T> {
    /*...*/
}

fun <T, C: MutableCollection<in T>> Iterable<T>.toCollection(destination: C): C {
    /*...*/
}

class ListAdapter<T: ItemAdapter>(/*...*/) { /*...*/ }
```

많이 사용하는 제한 중 Any도 있다. 이는 nullable이 아닌 타입을 나타낸다.

```kotlin
inline fun <T, R: Any> Iterable<T>.mapNotNull(
    transform: (T) -> R?
): List<R> {
    return mapNotNullTo(ArrayList<R>(), transform)
}
```

둘 이상의 제한을 걸수도 있다. 둘 이상의 제약을 걸때는 where 키워드를 사용한다.
```kotlin
fun <T: Animal> pet(animal: T) where T: GoodTempered {
    /*...*/
}

fun <T> pet(animal: T) where T: Animal, T: GoodTempered {
    /*...*/
}
```

### 정리
일반적으로 타입 파라미터를 사용해서 type-safe 제네릭 알고리즘과 제네릭 객체를 구현한다.
타입 파라미터는 구체적인 자료형(concrete type)의 서브타입을 제한할 수 있으며 특정 자료형이 제공하는 메소드를 안전하게 사용할 수 있다.