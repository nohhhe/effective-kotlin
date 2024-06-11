아이템 20. 일반적인 알고리즘을 반복해서 구현하지 말라
=========================
이미 있는 알고리즘을 활용하면 단순하게 코드가 짧아진다는 것 이외에도 다양한 장점이 있다.
* 코드 작성 속도가 빨라진다
* 구현을 따로 읽지 않아도 함수명만 보고도 뭘 하는지 확실하게 알 수 있다
* 직접 구현 시 발생할 수 있는 실수를 줄일 수 있다
* 한 번만 최적화하면 이런 함수를 활용하는 모든 곳이 최적화의 혜택을 받을 수 있다

```kotlin
override fun saveCallResult(item: SourceResponse) {
    var sourceList = ArrayList<SourceEntry>()
    item.sources.forEach {
        var sourceEntity = SourceEntity()
        sourceEntity.id = it.id
        sourceEntity.category = it.category
        sourceEntity.country = it.country
        sourceEntity.description = it.description
        sourceList.add(sourceEntity)
    }
    db.insertSources(sourceList)
}
```

앞의 코드에서 forEach를 쓰는 건 사실 좋지 않다. 어떤 자료형을 다른 자료형으로 매핑하는 처리를 하기 때문에 map()을 쓰면 된다.
또한 SourceEntity를 설정하는 부분은 코틀린 코드에선 더 이상 볼 수 없는 자바빈 패턴이다. 이런 형태보다는 팩토리 메서드를 활용하거나 기본 생성자를 활용하는 게 좋다.
그래도 위와 같은 패턴을 써야겠다면 최소한 apply를 활용해 모든 단일 객체들의 프로퍼티를 암묵적으로 설정하는 게 좋다.

```kotlin
override fun saveCallResult(item: SourceResponse) {
    val sourceEntities = item.sources.map(::sourceToEntry)
    db.insertSources(sourceList)
}

private fun sourceToEntry(source: Source) = SourceEntity()
    .apply {
        id = source.id
        category = source.category
        country = source.country
        description = source.description
    }
```

### 나만의 유틸리티 구현하기
컬렉션에 있는 모든 숫자의 곱을 계산하는 라이브러리가 필요하다면 널리 알려진 추상화이므로 범용 유틸리티 함수로 정의하는 게 좋다.

```kotlin
fun Iterable<Int>.product() = fold(1) { acc, i -> acc * i }
```

여러 번 쓰이지 않아도 이렇게 만드는 게 좋다. 이후 다른 개발자가 필요할 때 사용할 수 있다.
필요 없는 함수를 중복해서 만들지 않게 기존에 관련된 함수가 있는지 탐색하는 과정이 필요하다.
많이 쓰이는 알고리즘을 추출하는 방법으론 톱레벨 함수, 프로퍼티 위임, 클래스 등이 있다. 확장 함수는 이런 방법들과 비교해서 아래와 같은 장점이 있다.
* 함수는 상태를 유지하지 않으므로 행위를 나타내기 좋다
* 톱레벨 함수와 비교해서 확장 함수는 구체적인 타입이 있는 객체에만 사용을 제한할 수 있어서 좋다
* 수정할 객체를 아규먼트로 전달받아 쓰는 것보다는 확장 리시버로 사용하는 게 가독성 측면에서 좋다
* 확장 함수는 객체에 정의한 함수보다 객체를 사용할 때 자동완성 기능 등으로 제안이 이뤄져 쉽게 찾을 수 있다

### 정리
알고리즘을 반복해서 만들지말자. 대부분 stdlib에 이미 정의되어 있을 가능성이 높다. 없는 일반적인 알고리즘이 필요하거나 특정 알고리즘을 반복해서 사용해야 하는 경우 프로젝트 내부에 직접 확장 함수로 정의하는 것이 좋다.