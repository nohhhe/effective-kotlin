아이템 52.mutable 컬렉션 사용을 고려하라 
=========================
immutable 컬렉션보다 mutable 컬렉션이 좋은 점은 성능적인 측면에서 더 빠르다. immutable에 요소를 추가하려면 새로운 컬렉션을 만들면서 여기에 추가해야 한다.
```kotlin
public operator fun <T> Collection<T>.plus(elements: Iterable<T>): List<T> {
    if (elements is Collection) {
        val result = ArrayList<T>(this.size + elements.size)
        result.addAll(this)
        result.addAll(elements)
        return result
    } else {
        val result = ArrayList<T>(this)
        result.addAll(elements)
        return result
    }
}

public operator fun <T> Iterable<T>.plus(elements: Sequence<T>): List<T> {
    val result = ArrayList<T>()
    result.addAll(this)
    result.addAll(elements)
    return result
}
```

컬렉션을 복제하는 처리는 비용이 굉장히 많이 드는 처리이다. 그래서 복제 처리를 하지 않는 mutable 컬렉션이 성능적 관점에서 좋다.
일반적인 지역 변수는 이때 언급했던 동기화와 캡슐화 문제가 될 수 있는 경우에 해당되지 않는다. 따라서 지역변수로 사용할 때는 mutable 컬렉션이 더 합리적이다.
그래서 표준 라이브러리도 내부적으로 어떤 처리를 할때는 mutable 컬렉션을 사용하도록 구현되어 있다.
```kotlin
public inline fun <T, R> Iterable<T>.map(transform: (T) -> R): List<R> {
    return mapTo(ArrayList<R>(collectionSizeOrDefault(10)), transform)
}

@PublishedApi
internal fun <T> Iterable<T>.collectionSizeOrDefault(default: Int): Int = if (this is Collection<*>) this.size else default

public inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.mapTo(destination: C, transform: (T) -> R): C {
    for (item in this)
        destination.add(transform(item))
    return destination
}
```

#### PublishedApi 어노테이션
가진 함수나 프로퍼티를 다른 모듈에서도 사용할 수 있도록 하는 데 사용됩니다. 이 어노테이션은 주로 인라인 함수와 함께 사용됩니다.
```kotlin
// @PublishedApi가 없다면 initializeList를 호출하는 createList 함수는 다른 모듈에서 사용할 수 없다.
inline fun <reified T> createList(): List<T> {
    return mutableListOf<T>().apply { 
        initializeList(this)
    }
}

@PublishedApi
internal fun <T> initializeList(list: MutableList<T>) {
    // 내부 초기화 로직
}
```