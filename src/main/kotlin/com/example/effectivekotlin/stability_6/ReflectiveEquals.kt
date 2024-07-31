package com.example.effectivekotlin.stability_6

sealed class Time
data class TimePoint(val millis: Long): Time()
object Now: Time() {
    val millis: Long
        get() = System.currentTimeMillis()
}

fun main() {
    val now = Now
    val timePoint = TimePoint(System.currentTimeMillis())

    println(now == now) // true
    println(timePoint == timePoint) // true
}