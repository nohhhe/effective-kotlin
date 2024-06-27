아이템 28. API 안정성을 확인하라
=========================
프로그래밍은 안정적이고 표준적인 API를 선호한다.
* API가 변경되고 개발자가 업데이트했다면, 변경에 대응하기가 곤란해진다.
* 사용자가 새로운 API를 배워야 한다.

'API'또는 'API의 일부'가 불안정하다면, 이를 명확하게 알려 줘야 한다. 일반적으로 버전을 활용해서 라이브러리와 모듈의 안정성을 나타냅니다.
일반적으로 시멘틱 버저닝을 사용한다. 이 시스템은 버전 번호를 세 번호(MAJOR, MINOR, PATCH)로 나누어서 구성합니다.
* MAJOR 버전: 호환되지 않는 수준의 API 변경
* MINOR 버전: 이전 버전과 호환되는 기능을 추가
* PATCH 버전: 간단한 버그 수정

❓github release 브랜치 네이밍 전략 (급 생각이 남)

안정적인 API에 새로운 요소를 추가할 때, 아직 해당 요소가 안정적이지 않다면, Experimental 메타 어노테이션을 사용해서 사용자들에게 아직 해당 요소가 안정적이지 않다는 것을 알려 주는 것이 좋다.
Experimental 어노테이션을 붙이면, 사용할 때 경고 또는 오류(설정된 레벨에 따라서 다름)가 출력된다.
```kotlin
@Experimental(level = Experimental.Level.Warning)
annotaion class ExperimentalNewApi

@ExperimentalNewApi
suspend fun getUsers() : List<User> {
    //...
}
```

안정적인 API 일부를 변경해야 한다면, 전환하는 데 시간을 두고 Deprecated 어노테이션을 활용해서 사용자에게 미리 알려줄 수 있다.

```kotlin
@Deprecated("Use suspending getUsers instead")
fun getUsers(callback : (List<User>) -> Unit) {
    //...
}
```

또한 직접적인 대안이 있다면, IDE가 자동 전환을 할 수 있게 ReplaceWith를 붙여주는 것도 좋습니다.

```kotlin
@Deprecated("Use suspending getUsers instead", ReplaceWith("getUsers()"))
fun getUsers(callback : (List<User>) -> Unit) {
    //...
}
```