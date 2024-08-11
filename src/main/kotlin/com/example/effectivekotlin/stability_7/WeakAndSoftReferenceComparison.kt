package com.example.effectivekotlin.stability_7

import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

class WeakReferenceExample {
    fun doSomething() {
        println("Doing something...")
    }
}

class SoftReferenceExample {
    fun doSomething() {
        println("Doing something...")
    }
}

fun main() {
    var weakReferenceExample: WeakReferenceExample? = WeakReferenceExample()
    val weakRef = WeakReference(weakReferenceExample)

    val softReferenceExample = SoftReferenceExample()
    val softRef = SoftReference(softReferenceExample)

    println("Before GC (WeakReference): ${weakRef.get()}")
    println("Before GC (SoftReference): ${softRef.get()}")

    System.gc()

    println("After GC (WeakReference): ${weakRef.get()}")
    println("After GC (SoftReference): ${softRef.get()}")

    // 참조 해제
    weakReferenceExample = null

    println("After setting weakReferenceExample to null (WeakReference): ${weakRef.get()}")

    System.gc()

    println("After GC with nullified weakReferenceExample (WeakReference): ${weakRef.get()}")
    println("After GC with nullified weakReferenceExample (SoftReference): ${softRef.get()}")

    // 메모리 압박을 주기 위해 큰 객체 생성
    try {
        val largeArray = Array(100_000_000) { ByteArray(1024) }
    } catch (e: OutOfMemoryError) {
        println("OutOfMemoryError: ${e.message}")
    }

    System.gc()

    println("After memory pressure GC (WeakReference): ${weakRef.get()}")
    println("After memory pressure GC (SoftReference): ${softRef.get()}")
}