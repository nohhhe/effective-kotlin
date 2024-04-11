package com.example.effectivekotlin.stability_1.item1

data class User(var name: String)

class MutableUser {
    val user: User = User("Marcin")

    fun get(): User {
        return user.copy()
    }
}

fun main() {
    val mutableUser = MutableUser()
    val copyUser = mutableUser.get()

    copyUser.name = "Moskala"

    println(mutableUser.user) // User(name=Marcin)
    println(copyUser) // User(name=Moskala)
}