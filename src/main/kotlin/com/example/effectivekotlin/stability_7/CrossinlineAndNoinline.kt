package com.example.effectivekotlin.stability_7

inline fun exampleCrossinlineFunction(crossinline action: () -> Unit) {
    println("Before block execution")
    run {
        action() // block에서 return을 사용할 수 없음
    }
    println("After block execution")
}

inline fun exampleFunction(inlineLambda: () -> Unit, noinline noinlineLambda: () -> Unit) {
    println("Before inline lambda")
    inlineLambda()
    println("After inline lambda")

    println("Before noinline lambda")
    noinlineLambda()
    println("After noinline lambda")
}

fun main() {
    exampleCrossinlineFunction {
        println("This is a crossinline block")
        // return 사용 불가
    }

    exampleFunction(
        inlineLambda = { println("This is an inline lambda") },
        noinlineLambda = { println("This is a noinline lambda") }
    )
}