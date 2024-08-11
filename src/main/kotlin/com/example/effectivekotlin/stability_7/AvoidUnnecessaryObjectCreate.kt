package com.example.effectivekotlin.stability_7

class AvoidUnnecessaryObjectCreate {}

fun main() {
    val str1 = "Lorem"
    val str2 = "Lorem"

    println(str1 == str2) // true
    println(str1 === str2) // true

    var i1: Int? = 1
    var i2: Int? = 1

    println(i1 == i2) // true
    println(i1 === i2) // true

    i1 = 3
    println(i1 === i2) // false
    i1 = 1
    println(i1 === i2) // true

    // -128 ~ 127 범위를 벗어나면 새로운 객체를 생성한다.
    val j1: Int? = 1024
    val j2: Int? = 1024

    println(j1 == j2) // true
    println(j1 === j2) // false

    // 기본 Int 타입 (Java의 원시 타입 int로 컴파일)
    val nonNullableInt: Int = 42
    println("nonNullableInt: ${nonNullableInt::class.java}") // 출력: int

    // Nullable Int 타입 (Java의 Integer로 컴파일)
    val nullableInt: Int? = 42
    println("nullableInt: ${nullableInt!!::class.java}") // 출력: class java.lang.Integer

    emptyList<String>()
}