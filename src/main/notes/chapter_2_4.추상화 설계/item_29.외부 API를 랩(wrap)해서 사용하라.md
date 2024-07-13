아이템 29. 외부 API를 랩(wrap)해서 사용하라
=========================
잠재적으로 불안정하다고 판단되는 외부 라이브러리 API를 랩(wrap)해서 사용하라. 랩을 사용하면 다음과 같은 자유와 안정성을 얻을 수 있다.
* 문제가 있다면 래퍼만 변경하면 되므로, API 변경에 쉽게 대응할 수 있다.
* 프로젝트의 스타일에 맞춰서 API의 형태를 조절할 수 있다.
* 특정 라이브러리에 문제가 발생하면, 래퍼를 수정해서 다른 라이브러리를 사용하도록 쉽게 변경할 수 있다.
* 쉽게 동작을 추가하거나 수정할 수 있다.

단점
* 래퍼를 따로 정의해야 한다.
* 다른 개발자가 프로젝트를 다룰 때, 어떤 래퍼들이 있는지 따로 확인해야 한다.
* 래퍼들은 프로젝트 내부에서만 유효하므로, 문제가 생겨도 질문할 수 없다.

```kotlin
// ExternalApi를 랩핑하는 Wrapper 클래스
class ApiWrapper(private val externalApi: ExternalApi) {
    
    fun getData(): String {
        return try {
            externalApi.fetchData()
        } catch (e: Exception) {
            ///...
        }
    }
}

fun main() {
    val externalApi = ExternalApi()
    val apiWrapper = ApiWrapper(externalApi)

    // 이제 외부 API를 직접 사용하지 않고, 래퍼 클래스를 통해서 사용
    println(apiWrapper.getData())
}
```