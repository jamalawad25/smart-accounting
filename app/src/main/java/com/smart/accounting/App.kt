package com.smart.accounting

import android.app.Application
import com.smart.accounting.data.db.AppDatabase
import com.smart.accounting.data.repo.AccountRepository
import com.smart.accounting.data.repo.TransactionRepository

class App : Application() {
    private val db by lazy { AppDatabase.get(this) }
    val accountRepo by lazy { AccountRepository(db.accountDao()) }
    val transactionRepo by lazy { TransactionRepository(db.transactionDao()) }
}