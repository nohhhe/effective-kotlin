package com.example.effectivekotlin.stability_2.item12

fun Int.factorial(): Int = (1..this).product()

fun Iterable<Int>.product(): Int =
    fold(1) { acc, i -> acc * i }

fun main() {
    println(10 * 6.factorial()) // 120
}