package com.smart.accounting.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smart.accounting.data.repo.PersonRepository
import com.smart.accounting.data.repo.TransactionRepository

class VMFactory(
    private val repo: TransactionRepository,
    private val personRepo: PersonRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MainViewModel(repo, personRepo) as T
}