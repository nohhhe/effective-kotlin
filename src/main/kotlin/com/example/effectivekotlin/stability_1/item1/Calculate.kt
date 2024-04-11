package com.example.effectivekotlin.stability_1.item1

class Calculate {
    private fun calculate(): Int {
        print("Calculating...")
        return 42
    }

    val fizz = calculate()
    val buzz
        get() = calculate()
}

fun main() {
    val calculate = Calculate()
    println(calculate.fizz)
    println(calculate.fizz)
    println(calculate.buzz)
    println(calculate.buzz)
}