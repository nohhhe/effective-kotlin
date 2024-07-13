package com.example.effectivekotlin.stability_3

import com.example.effectivekotlin.stability_3.GenericTypeVariance.*
class GenericTypeVariance {
    class Cup<out T>
    open class Dog
    class Puppy: Dog()
    class Hound: Dog()

    class Box<out T> {
        private var value: T? = null

        fun set(value: @UnsafeVariance T) {
            this.value = value
        }

        fun get(): T = value ?: error("value not set")
    }

    /*
    class Box2<out T> {
        var value: T? = null // Type parameter T is declared as 'out' but occurs in 'in' position in type T

        fun set(value: T) {
            this.value = value // Type parameter T is declared as 'out' but occurs in 'in' position in type T
        }

        fun get(): T = value ?: error("value not set")
    }
    */

    class Box3<out T> {
        private var value: T? = null

        private fun set(value: T) {
            this.value = value
        }

        fun get(): T = value ?: error("value not set")
    }

    fun append(list: MutableList<Any>) {
        list.add(42)
    }

    /*class Box4<in T> {
        var value: T? = null // Type parameter T is declared as 'in' but occurs in 'invariant' position in type T?

        fun set(value: T) {
            this.value = value
        }

        fun get(): T = value // Type parameter T is declared as 'in' but occurs in 'invariant' position in type T?
            ?: error("Value not set")
    }*/
}

fun main() {
    /*
    val b: Cup<Dog> = Cup<Puppy>() // OK
    val a: Cup<Puppy> = Cup<Dog>() // Error
    val anys: Cup<Any> = Cup<Int>() // OK
    val nothings: Cup<Nothing> = Cup<Int>() // Error
    */

    val puppyBox = Box<Puppy>()
    val dogBox: Box<Dog> = puppyBox
    dogBox.set(Hound()) // Puppy를 위한 공간이므로 문제 발생
    println(dogBox.get())

    val dogHouse = Box<Dog>()
    val box: Box<Any> = dogHouse
    box.set("Some String") // Dog를 위한 공간이므로 문제 발생
    println(box.get())
    box.set(42) // Dog를 위한 공간이므로 문제 발생
    println(box.get())

    /*val strs = mutableListOf<String>("A","B","C")
    GenericTypeVariance().append(strs) // Type mismatch. Required:MutableList<Any> Found:MutableList<String>
    val str: String = strs[3]
    print(str)*/
}