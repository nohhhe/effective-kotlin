package com.example.effectivekotlin.stability_7

fun main() {
    val items: List<Any> = listOf(1, "Hello", 2.5, "World", 3)

    // String 타입의 요소만 필터링
    val strings: List<String> = items.filterIsInstance<String>()

    println(strings)  // 출력: [Hello, World]
}
