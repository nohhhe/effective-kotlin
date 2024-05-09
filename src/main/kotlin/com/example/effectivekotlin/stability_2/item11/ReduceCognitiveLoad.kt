package com.example.effectivekotlin.stability_2.item11

data class Person(val name: String, val age: Int) {
    val isAdult: Boolean
        get() = age >= 18
}

interface View {
    fun showPerson(person: Person): String?
    fun hideProgressWithSuccess()
    fun showError()
    fun hideProgress()
}

val view = object : View {
    override fun showPerson(person: Person): String? {
        //return "name: ${person.name}, age: ${person.age}"
        return null
    }

    override fun hideProgressWithSuccess() {
        println("hideProgressWithSuccess")
    }

    override fun showError() {
        println("showError")
    }

    override fun hideProgress() {
        println("hideProgress")
    }
}

class ReduceCognitiveLoad {
    private val person: Person? = Person("Alice", 19)

    fun showPerson() {
        person?.takeIf { it.isAdult }
            ?.let {
                view.showPerson(it)
                view.hideProgressWithSuccess()
            } ?: run {
                view.showError()
                view.hideProgress()
            }
    }
}

fun main() {
    val reduceCognitiveLoad = ReduceCognitiveLoad()

    reduceCognitiveLoad.showPerson()
}