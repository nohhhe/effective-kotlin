package com.example.effectivekotlin.stability_1.item1

class BankAccount {
    var balance = 0.0
        private set

    fun deposit(depositAmount: Double) {
        balance += depositAmount
    }

    @Throws(InsufficientFunds::class)
    fun withdraw(withdrawAmount: Double) {
        if (balance < withdrawAmount) {
            throw InsufficientFunds()
        }
        balance -= withdrawAmount
    }
}

class InsufficientFunds : Exception()

fun main() {
    val account = BankAccount()

    println(account.balance) // 0.0
    account.deposit(100.0)
    println(account.balance) // 100.0
    account.withdraw(50.0)
    println(account.balance)// 50.0
}