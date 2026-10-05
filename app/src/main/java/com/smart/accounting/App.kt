package com.smart.accounting

import android.app.Application
import com.smart.accounting.data.db.AppDatabase
import com.smart.accounting.data.repo.PersonRepository
import com.smart.accounting.data.repo.TransactionRepository

class App : Application() {
    val repository: TransactionRepository by lazy {
        TransactionRepository(AppDatabase.get(this).dao())
    }
    val personRepository: PersonRepository by lazy {
        PersonRepository(AppDatabase.get(this).personDao())
    }
}