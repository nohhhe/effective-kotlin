아이템 34. 기본 생성자에 이름 있는 옵션 아규먼트를 사용하라
=========================
객체를 정의하고 생성하는 방법을 지정할 때 사용하는 가장 기본적인 방법은 기본 생성자를 사용하는 것이다.

```kotlin
class user(var name: String, var surname: String)
val user = User("Marcin", "Moskata")
```

### 점층적 생성자 패턴
점층적 생성자 패턴은 '여러 가지 종류의 생성자를 사용하는' 굉장히 간단한 패턴이다.

```kotlin
class Pizza{
    val size: String
    val cheese: Int
    val olives: Int
    val bacon: Int

    constructor(size: String, cheese: Int, olives: Int, bacon: Int){
        this.size = size
        this.cheese = cheese
        this.olives = olives
        this.bacon = bacon
    }

    constructor(size: String, cheese: Int, olives: Int) : this(size, cheese, olives, 0)
    constructor(size: String, cheese: Int): this(size, cheese, 0)
    constructor(size: String): this(size, 0)   
}
```

이 코드는 좋은 코드가 아니다. 코틀린에서는 일반적으로 디폴트 아규먼트를 사용한다.

```kotlin
class Pizza(
    val size: String,
    val cheese: Int = 0,
    val olives: Int = 0,
    val bacon: Int = 0
)
```

#### 디폴트 아규먼트가 점층적 생성자보다 좋은 이유
* 파라미터들의 값을 원하는 대로 지정할 수 있다.
* 아규먼트를 원하는 순서로 지정할 수 있다.
* 명시적으로 이름을 붙여서 아규먼트를 지정하므로 의미가 훨씬 명확하다.

이름 있는 아규먼트를 화용해서 명시적으로 이름을 붙여 주면, 의미가 훨씬 명확하다.

```kotlin
val villagePizza = Pizza(
    size = "L",
    cheese = 1,
    olives = 2,
    bacon = 3
)
```

### 빌더 패턴
자바에서는 이름 있는 파라미터와 디폴트 아규먼트를 사용할 수 없다. 그래서 자바는 필터 패턴을 사용한다.

#### 빌더 패턴 장점
* 파라미터에 이름을 붙일 수 있다.
* 파라미터를 원하는 순서로 지정할 수 있다.
* 디폴트 값을 지정할 수 있다.

```kotlin
class Pizza private constructor(
    val size: String,
    val cheese: Int = 0,
    val olive: Int = 0,
    val bacon: Int = 0
) {
    class Builder(private val size: String){
        private var cheese: Int = 0
        private var olive: Int = 0
        private var bacon: Int = 0
        
        fun setCheese(value: Int): Builder = apply{
            cheese = value
        }

        fun setOlive(value: Int): Builder = apply{
            olive = value
        }

        fun setBacon(value: Int): Builder = apply{
            bacon = value
        }
        
        fun build() = Pizza(size, cheese, olive, bacon)
    }
}

val myFavorite = Pizza.Builder("L").setOlives(3).build()

val villagePizza = Pizza.Builder("L")
    .setCheese(1)
    .setOlive(2)
    .setBacon(3)
    .build()
```

#### 빌더 패턴 보다 이름 있는 파라미터를 사용하는 것이 좋은 이유
* 더 짧다.
* 더 명확하다.
* 더 사용하기 쉽다.
* 동시성과 관련된 문제가 없다: 코틀린의 함수 파라미터는 항상 immutable이다. 반면 대부분의 빌더 패턴에서 프로퍼티는 mutable이다. 따라서 빌더 패턴의 빌더 함수를 쓰레드 안전하게 구현하는 것은 어렵다.

무조건 빌더 패턴 대신 기본 생성자를 사용해야 한다는 것은 아니다.

```kotlin
// 값의 의미를 묶어서 지정
val dialog = AlertDialog.Builder(context)
    .setMessage(R.string.fire_missiles)
    .setPositiveButton(R.string.fire, { d, id ->
        // 미사일 발사
    })
    .setNegativeButton(R.string.cancel, { d, id ->
        // 사용자가 대화상자에서 취소를 누른 경우
    })
    .create()

// 특정 값을 누적
val router = Router.Builder()
    .addRoute(path= "/home", ::showHome)
    .addRoute(path= "/users", ::showUsers)
    .build()
```

빌더 패턴을 사용하지 않고 구현하려면, 추가적인 타입을 만들고 활용해야 한다. 코드가 복잡해진다.

```kotlin
// 값의 의미를 묶어서 지정
val dialog = AlertDialog(context,
    message = R.string.fire_missiles,
    positiveButtonDescription = 
        ButtonDescription(R.string.fire, { d, id ->
            // 미사일 발사
        }),
    negativeButtonDiscription = 
        ButtonDiscription(R.string.cancle, { d, id ->
            // 사용자가 대화상자에서 취소를 누른 경우
        })
)

// 특정 값을 누적
val router = Router(
    router = listOf(
        Route("/home", ::showHome),
        Route("/users", ::showUsers)
    )
)
```

일반적으로 이런 코드는 DSL 빌더를 사용한다.

```kotlin
// 값의 의미를 묶어서 지정
var dialog = context.alert(R.string.fire_missiles) {
    positiveButton(R.string.fire) {
        // 미사일 발사
    }
    nagativeButton(R.string.cancel) {
        // 사용자가 대화상자에서 취소를 누른 경우
    }
}

// 특정 값을 누적
val route = router {
    "/home" directTo :: showHome
    "/users" directTo :: showUsers
}
```

고전적인 빌더 패턴의 또 다른 장점으로는 팩토리로 사용할 수 있다는 것이다.

```kotlin
fun Context.makeDefaultDialogBuilder() =
    Alertdialog.Builder(this)
        .setIcon(R.drawable.ic_dialog)
        .setTitle(R.string.dialog_title)
        .setOnCancelListener { it.cancel() }
```

팩토리 메서드를 기본 생성자처럼 사용하게 만들려면 커링(currying)을 활용해야 한다. 하지만 코틀린은 커링을 지원하지 않는다. 대신 객체 설정을 데이터 클래스로 만들고, 데이터 클래스로 객체를 만들어 두고, 이를 copy로 복제한 뒤 필요한 설정들을 일부 수정해서 사용하는 형태로 만든다.

```kotlin
data class DialogConfig(
    val icon: Int = -1,
    val title: Int = -1,
    val onCancelListener: (() -> Unit)? = null
    // ...
)

fun makeDefaultDialogConfig() = DialogConfig(
    icon = R.drawable.ic_dialog,
    title = R.string.dialog_title,
    onCancelListener = { it.cancel() }
)
```

코틀린에선 빌더 패턴을 거의 쓰지 않는다. 빌더 패턴은 아래와 같은 경우에만 쓴다.
* 빌더 패턴을 쓰는 다른 언어로 작성된 라이브러리를 그대로 옮길 때
* 디폴트 아규먼트, DSL을 지원하지 않는 다른 언어에서 쉽게 쓸 수 있게 API를 설계할 때

이를 제외하면 빌더 패턴 대신 디폴트 아규먼트를 갖는 기본 생성자 또는 DSL을 쓰는 게 좋다.

❓ 스코프 함수를 쓰는 것은 다른 방법인가?

### 정리
* 일반적으로 기본 생성자를 사용해 객체를 만든다.
* 코틀린에서는 점층적 생성자 패턴을 사용하지 않는다.
* 디폴트 아규먼트를 사용하는 것이 좋다.
* 빌더 패턴도 마찬가지로 거의 사용하지 않는다.
* 빌더 패턴 대신 기본 생성자를 사용하는 코드로 바꾸거나, DSL을 활용하는 것이 좋다.