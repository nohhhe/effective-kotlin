package com.example.effectivekotlin.stability_6

class PhoneBookCorrect {
    fun String.isPhoneNumber() =
        length == 7 && all { it.isDigit() }
}

class A {
    val a = 10
}

class B {
    val a = 20
    val b = 30

    fun A.test() = a + b // 40일까? 50일까?
}

class A2 {
    var state = 10
}

class B2 {
    var state = 20

    fun A2.update() = state + 10  // A와 B중에서 어떤 것을 업데이트 할까요?
}

fun main() {
    PhoneBookCorrect().apply {
        println("1234567890".isPhoneNumber())
    }

    B().apply { println(A().test()) } // 정답 : 40
    B2().apply { println(A2().update()) } // 정답 : 20
}