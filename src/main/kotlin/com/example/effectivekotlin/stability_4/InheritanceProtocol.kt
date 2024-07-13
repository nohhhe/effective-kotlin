package com.example.effectivekotlin.stability_4

class InheritanceProtocol(val id: Int) {
        override fun equals(other: Any?): Boolean =
            other is InheritanceProtocol && other.id == id
}

fun main() {
    val set = mutableSetOf(InheritanceProtocol(1))
    set.add(InheritanceProtocol(1))
    set.add(InheritanceProtocol(1))
    println(set.size) // 3
}