package com.example.effectivekotlin.stability_6

class Proper(val name: String) {
    override fun equals(other: Any?): Boolean {
        equalsCounter++
        return other is Proper && other.name == name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    companion object {
        var equalsCounter = 0
    }
}

class Terrible(val name: String) {
    override fun equals(other: Any?): Boolean {
        equalsCounter++
        return other is Terrible && other.name == name
    }

    override fun hashCode() = 0

    companion object {
        var equalsCounter = 0
    }
}

fun main() {
    val properSet = List(10000) { Proper("$it") }.toSet()
    println(Proper.equalsCounter) // 0
    val terribleSet = List(10000) { Terrible("$it") }.toSet()
    println(Terrible.equalsCounter) // 50116683

    Proper.equalsCounter = 0
    println(Proper("9999") in properSet) // true
    println(Proper.equalsCounter) // 1

    Proper.equalsCounter = 0
    println(Proper("A") in properSet) // false
    println(Proper.equalsCounter) // 0

    Terrible.equalsCounter = 0
    println(Terrible("9999") in terribleSet) // true
    println(Terrible.equalsCounter) // 4324

    Terrible.equalsCounter = 0
    println(Terrible("A") in terribleSet) // false
    println(Terrible.equalsCounter) // 10001, 같은 버킷 내 10000개 존재하기 때문에 모두 비교 필요
}