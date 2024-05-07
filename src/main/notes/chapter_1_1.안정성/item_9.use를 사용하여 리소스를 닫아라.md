아이템 9. use를 사용하여 리소스를 닫아라
=========================
#### 코틀린/JVM에서 close 메소드를 활용하여 명시적으로 닫아야 하는 리소스
* InputStream과 OutputStream 
* java.sql.Connection 
* java.io.Reader(FileReader, BufferedReader, CSSParser)
* java.new.Socket과 java.util.Scanner

이러한 리소스들은 AutoCloseable을 상속받는 Closeable인터페이스를 구현하고 있다. 
이러한 모든 리소스는 최종적으로 리소스에 대한 레퍼런스가 없어질 때 가비지 컬렉터가 처리한다.
하지만 굉장히 느리며 그동안 리소스 유지비용이 많이 들어간다. 따라서 더 이상 필요하지 않을 때 close메소드를 호출해 주는 것이 좋다.

```kotlin
fun countCharactersInFile(path: String): Int {
    val reader = BufferedReader(FileReader(path))
    try {
        return reader.lineSequence().sumBy { it.length }  // sumBy는 코틀린 1.5부터 사용되지 않기 때문에 sumOf를 사용하자
    } finally {
        reader.close()
    }
}
```

보통 이러한 리소스는 try-finally 블록을 사용해서 처리했지만 리소스를 닫을 때 예외가 발생할 수 있는데 이러한 예외는 따로 처리하지 않는다.
또한 try블록과 finally블록 내부에서 오류가 발생하면 둘 중 하나만 전파된다.

표준 라이브러리에는 이러한 리소스를 처리하는 use라는 확장 함수가 있고 모든 Closeable 객체에 사용할 수 있다.

```kotlin
@InlineOnly
public inline fun <T : Closeable?, R> T.use(block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        when {
            apiVersionIsAtLeast(1, 1, 0) -> this.closeFinally(exception)
            this == null -> {}
            exception == null -> close()
            else ->
                try {
                    close()
                } catch (closeException: Throwable) {
                    // cause.addSuppressed(closeException) // ignored here
                }
        }
    }
}
```

```kotlin
fun countCharactersInFile(path : File) : Int { 
    val reader = BufferdReader(FileReader(path))
    readers.use{
    	return reader.lineSequence().sumBy { it.length }
    }
}

// 람다 매개변수로 리시버가 전달되는 형태
fun countCharactersInFile(path: String): Int {
    BufferedReader(FileReader(path)).use { reader ->
        return reader.lineSequence().sumBy { it.length }
    }
}
``` 

파일을 한 줄씩 처리할 때 활용할 수 있는 useLines함수도 제공한다. 다만 파일의 줄을 한 번만 사용할 수 있다는 단점이 있다.

```kotlin
public inline fun <T> Reader.useLines(block: (Sequence<String>) -> T): T =
    buffered().use { block(it.lineSequence()) }
```

```kotlin
fun countCharactersInFile(path: String): Int {
    File(path).useLines { lines ->
        return lines.sumBy { it.length }
    }
}

// return 생략
fun countCharactersInFile(path: String): Int =
    File(path).useLines { lines ->
        lines.sumBy { it.length }
    }
```

### 정리
* use를 사용하면 Closeable/AutoColseable을 구현한 객체를 쉽고 안전하게 처리할 수 있다.
* 파일을 처리할 때는 파일을 한 줄씩 읽어 들이는 useLines를 사용하면 좋다.