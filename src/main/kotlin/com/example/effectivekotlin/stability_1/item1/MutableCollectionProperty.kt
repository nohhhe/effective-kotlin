package com.example.effectivekotlin.stability_1.item1

import kotlin.concurrent.thread

class MutableCollectionProperty {
    val list1: MutableList<Int> = mutableListOf()
    var list2: List<Int> = listOf()
        set(value) {
            field = value
            println("list2 set to $value")
        }
}

fun main() {
    val mutableCollectionProperty = MutableCollectionProperty()
    mutableCollectionProperty.list1.add(1)
    mutableCollectionProperty.list2 = mutableCollectionProperty.list2 + 1

    mutableCollectionProperty.list1 += 2
    mutableCollectionProperty.list2 += 2

    println(mutableCollectionProperty.list1) // [1]
    println(mutableCollectionProperty.list2) // [1]

    val list1 = mutableListOf<Int>()

    for (i in 1..1000) {
        thread {
            list1.add(i)
        }
    }

    Thread.sleep(1000)
    println(list1.size)

    var list2 = listOf<Int>()

    for (i in 1..1000) {
        thread {
            list2 = list2 + i
        }
    }

    Thread.sleep(1000)
    println(list2.size)
}