package com.example.effectivekotlin.stability_2.item12

class NameArgument {
    fun calculate(operation: (Int, Int) -> Int): Int {
        return operation(2, 3)
    }

    fun call(before: () -> Unit = {}, after: () -> Unit = {}) {
        before()
        print("Middle")
        after()
        println()
    }
}

fun main() {
    val nameArgument = NameArgument()

    nameArgument.call({print("CALL")}) //CALLMiddle
    nameArgument.call{print("CALL")} //MiddleCALL

    // 이름 있는 아규먼트를 사용한 경우
    nameArgument.call(before = {print("CALL")}) //CALLMiddle
    nameArgument.call(after = {print("CALL")}) //MiddleCALL

    val sum = nameArgument.calculate { x, y -> x + y } // 람다 표현식, named arguments 사용 불가
    //val sum = nameArgument.calculate { x = 2, y = 3 -> x + y } // 컴파일 오류 발생
    println(sum)
}