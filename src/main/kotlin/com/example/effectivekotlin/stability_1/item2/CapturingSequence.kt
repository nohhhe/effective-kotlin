package com.example.effectivekotlin.stability_1.item2

class CapturingSequence {
    val primes: Sequence<Int> = sequence {
        var numbers = generateSequence(2) { it + 1 }

        var prime: Int
        while (true) {
            //val prime = numbers.first()
            prime = numbers.first()
            println(prime)
            yield(prime)
            numbers = numbers.drop(1).filter {
                println("it: $it, prime:$prime")
                it % prime != 0
            }
        }
    }
}

fun main() {
    val capturingSequence = CapturingSequence()
    println(capturingSequence.primes.take(4).toList())
}