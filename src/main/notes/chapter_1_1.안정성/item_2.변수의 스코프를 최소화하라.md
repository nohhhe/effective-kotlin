아이템 2. 변수의 스코프를 최소화하라
=========================
* 프로퍼티보다는 지역 변수를 사용하는 것이 좋습니다.
* 최대한 좁은 스코프를 갖게 변수를 사용합니다

```kotlin
// 나쁜 예
var user: User
for (i in users.indices) {
    user = users[i]
    print("User at $i is $user")
}

// 조금 더 좋은 예
for (i in users.indices) {
    val user = users[i]
    print("User at $i is $user")
}

// 제일 좋은 예
for ((i, user) in users.withIndex()) {
    print("User at $i is $user")
}
```

스코프를 좁게 만드는 것은 프로그램을 추적하고 관리하기 쉽기 때문이다.
변수는 일기 전용 또는 읽고 쓰기 전용 여부와 상관없이, 변수를 정의할 때 초기화되는 것이 좋다.
if, when, try-catch, Elvis 표현식 등을 활용하면, 최대한 변수를 정의할 때 초기화할 수 있다
```kotlin
// 나쁜 예
val user: User
if (hasValue) {
    user = getValue()
} else {
    user = User()
}

// 조금 더 좋은 예
val user: User = if (hasValue) {
    getValue()
} else {
    User()
}
```

여러 프로퍼티를 한꺼번에 설정해야 하는 경우에는 구조분해 선언을 활용하는 것이 좋다.
```kotlin
// 나쁜 예
fun updateWeather(degrees: Int) {
    val description: String
    val color: Int
    if (degrees < 5) {
        description = "cold"
        color = Color.BLUE
    } else if (degrees < 23) {
        description = "mild"
        color = Color.YELLOW
    } else {
        description = "hot"
        color = Color.RED
    }
    // ...
}

// 조금 더 좋은 예
fun updateWeather(degrees: Int) {
    val (description, color) = when {
        degrees < 5 -> "cold" to Color.BLUE
        degrees < 23 -> "mild" to Color.YELLOW
        else -> "hot" to Color.RED
    }
    // ...
}
```

### 캡처링
에라토스테네스의 체(소수를 구하는 알고리즘)
```kotlin
// 간단한 구현
var numbers = (2..100).toList()
val primes = mutableListOf<Int>()
while(numbers.isNotEmpty()){
    val prime = numbers.first()
    primes.add(prime)
    numbers = numbers.filter { it%prime != 0 }
}

print(primes) // // [2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97]



// 시퀀스 활용 예제
val primes: Sequence<Int> = sequence {
    var numbers = generateSequence(2) { it + 1 }
    
    while (true) {
        val prime = numbers.first()
        yield(prime)
        numbers = numbers.drop(1).filter {
            it % prime != 0
        }
    }
}

print(primes.take(10).toList()) // [2,3,5,7,11,13,17,19,23,29]


// 캡처링 오류
val primes: Sequence<Int> = sequence {
    var numbers = generateSequence(2) { it + 1 }

    var prime: Int
    while (true) {
        prime = numbers.first()
        yield(prime)
        numbers = numbers.drop(1).filter {
            it % prime != 0
        }
    }
}

print(primes.take(10).toList()) // [2,3,5,6,7,8,9,10,11,12]
```


prime 변수를 캡처했기 때문에 최종적인 prime 값으로만 필터링되어 prime이 2로 설정되어 있을 때 필터링된 4를 제외하면, drop만 동작하므로 그냥 연속된 숫자가 나온다.

❗️설명이 잘못된 게 아닌가? 아래 동작 방식을 확인해보자

#### 시퀀스 활용 예제 동작 방식
1. first()로 numbers의 첫 번째 값을 가져온다.
2. yield()로 첫 번째 prime 값(2)을 반환 및 중단한다.
3. 두 번째 루프 때 first()를 만나면 중단된 부분부터 다시 실행된다.
4. ❗️numbers를 drop(1)로 첫 번째 값을 제외하고 filter로 prime의 배수를 제외하는 부분을 numbers에 캡처링 한다.❗️
5. first()로 numbers의 두 번째 값을 가져온다.
6. yield()로 두 번째 prime 값(3)을 반환 및 중단한다.
7. 세 번째 루프 때 first()를 만나면 중단된 부분부터 다시 실행된다.
8. ❗️캡처링된 numbers를 기준으로 4번 동작을 반복한다. (즉, numbers.drop(1).filter { it % prime(2) != 0 }.drop(1).filter { it % prime(3) != 0 })❗️
9. 이런 식으로 계속 반복된다.

#### 캡처링 오류 동작 방식
1. first()로 numbers의 첫 번째 값을 가져온다.
2. yield()로 첫 번째 prime 값(2)을 반환 및 중단한다.
3. 두 번째 루프 때 first()를 만나면 중단된 부분부터 다시 실행된다.
4. numbers를 drop(1)로 첫 번째 값을 제외하고 filter로 prime의 배수를 제외하는 부분을 numbers에 캡처링 한다.
5. first()로 numbers의 두 번째 값을 가져온다.
6. yield()로 두 번째 prime 값(3)을 반환 및 중단한다.
7. 세 번째 루프 때 first()를 만나면 중단된 부분부터 다시 실행된다.
8. ❗️캡처링된 numbers를 기준으로 4번 동작을 반복하지만 prime 변수가 캡처링 되기 때문에 마지막 prime 값으로만 필터링 된다. (즉, numbers.drop(1).filter { it % prime(3) != 0 }.drop(1).filter { it % prime(3) != 0 })❗️
9. 이런 식으로 계속 반복된다.

#### 설명이 틀린 부분
숫자 4는 prime이 3일 때 numbers.drop(1).filter { it % prime(3) != 0 }.drop(1).filter { it % prime(3) != 0 }으로 인해서 2번째 drop에 의해서 삭제가 되는 것이지 필터링되는 것은 아닌 것 같다.

### 정리
1. 변수의 스코프는 최대한 좁게 만드는 것이 좋다.
2. 람다에서는 변수를 캡처할 수 있으므로 주의해야 한다.