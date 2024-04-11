package com.example.effectivekotlin.stability_1.item1

class ValSmartCast {
    val name: String = "Marcin"
    val surname: String = "Moskala"

    val fullName: String?
        get() = name?.let { "$it $surname" }

    val fullName2: String? = name?.let { "$it $surname" }

    fun main() {
        if (fullName != null) {
            //println(fullName.length) // 오류
        }

        if (fullName2 != null) {
            println(fullName2.length) // Marcin Moskala
        }
    }
}