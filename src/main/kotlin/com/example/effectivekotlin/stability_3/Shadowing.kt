package com.example.effectivekotlin.stability_3

import com.example.effectivekotlin.stability_3.Shadowing.*

class Shadowing {
    interface Tree
    class Birch : Tree
    class Spruce : Tree

    class Forest<T : Tree> {
        fun <T : Tree> addTree(tree: T) {
            println("Adding tree: $tree")
        }

        fun addTree2(tree: T) {
            println("Adding tree: $tree")
        }

        fun <ST: T> addTree3(tree: ST) {
            println("Adding tree: $tree")
        }
    }
}

fun main() {
    val forest = Forest<Birch>()
    forest.addTree(Birch())
    forest.addTree(Spruce())

    val forest2 = Forest<Birch>()
    forest2.addTree2(Birch())
    //forest.addTree2(Spruce()) // Type mismatch

    val forest3 = Forest<Birch>()
    forest3.addTree(Birch())
    forest3.addTree(Spruce())
}