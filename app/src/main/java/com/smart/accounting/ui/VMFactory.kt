package com.smart.accounting.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smart.accounting.data.repo.AccountRepository
import com.smart.accounting.data.repo.TransactionRepository

class VMFactory(
    private val accountRepo: AccountRepository,
    private val transactionRepo: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MainViewModel(accountRepo, transactionRepo) as T
}