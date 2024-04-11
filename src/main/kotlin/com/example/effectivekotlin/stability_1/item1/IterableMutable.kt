package com.example.effectivekotlin.stability_1.item1

class IterableMutable {
    private val list: Iterable<Int> = listOf(1, 2, 3, 4, 5)

    fun execution() {
        list.map {
            it * it
        }.forEach {
            println(it)
        }
    }
}

fun main() {
    val iterableMutable = IterableMutable()
    iterableMutable.execution()

    val list: List<Int> = listOf(1, 2, 3)

    if(list is MutableList) {
        list.add(4) // 오류
    }
}