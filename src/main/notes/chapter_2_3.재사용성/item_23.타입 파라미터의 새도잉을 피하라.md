아이템 23. 타입 파라미터의 새도잉을 피하라
=========================
프로퍼티, 파라미터가 같은 이름을 가질 수 있다. 이렇게 되면 지역 파라미터가 외부 스코프에 있는 프로퍼티를 가린다. 이를 섀도잉(shadowing)이라고 부른다.

```kotlin
class Forest(val name: String) {
    fun addTree(name: String) {
        // ...
    }
}
```

이런 섀도잉 현상은 클래스 타입 파라미터와 함수 타입 파라미터 사이에서도 발생한다. 개발자가 제네릭을 제대로 이해하지 못할 때 이와 관련된 다양한 문제들이 발생한다.

```kotlin
interface Tree
class Birch: Tree
class Spruce: Tree

class Forest<T: Tree> {
    fun <T: Tree> addTree(tree: T) {
        // ...
    }
}

val forest = Forest<Birch>()
forest.addTree(Birch())
forest.addTree(Spruce())
```

이렇게 코드를 작성하면 Forest, addTree의 타입 파라미터가 독립적으로 동작한다. 
코드만 봐선 둘이 독립적으로 동작한다는 걸 빠르게 알아내기 힘들다. 따라서 addTree가 클래스 타입 파라미터인 T를 쓰게 하는 게 좋다.

```kotlin
interface Tree
class Birch: Tree
class Spruce: Tree

class Forest<T: Tree> {
    fun addTree(tree: T) {
        // ..
    }
}

val forest = Forest<Birch>()
forest.addTree(Birch())
forest.addTree(Spruce())    // Type mismatch
```

독립적인 타입 파라미터를 의도했다면 이름을 아예 다르게 다는 것이 좋다. 참고로 타입 파라미터를 사용해서 다른 타입 파라미터에 제한을 줄 수도 있다.
```kotlin
class Forest<T: Tree> {
    fun <ST: T> addTree(tree: ST) {
        // ...
    }
}
```

### 정리
타입 파라미터 섀도잉을 피해라. 타입 파라미터 섀도잉이 발생한 코드는 이해하기 어려울 수 있고 문제를 발생할 수 있다.