package com.smart.accounting.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.accounting.data.model.AccountEntity
import com.smart.accounting.data.model.TransactionEntity
import com.smart.accounting.data.repo.AccountRepository
import com.smart.accounting.data.repo.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val accountRepo: AccountRepository,
    private val transactionRepo: TransactionRepository
) : ViewModel() {

    private val _section = MutableStateFlow("ACCOUNTS")
    val section: StateFlow<String> = _section

    val accounts: StateFlow<List<AccountEntity>> = accountRepo.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = _section
        .flatMapLatest { sec ->
            if (sec == "ACCOUNTS") flowOf(emptyList())
            else transactionRepo.bySection(sec)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSection(s: String) { _section.value = s }

    fun addAccount(a: AccountEntity) = viewModelScope.launch { accountRepo.insert(a) }
    fun deleteAccount(a: AccountEntity) = viewModelScope.launch { accountRepo.delete(a) }

    fun saveTransaction(t: TransactionEntity) = viewModelScope.launch {
        if (t.id == 0L) transactionRepo.insert(t) else transactionRepo.update(t)
    }
    fun deleteTransaction(id: Long) = viewModelScope.launch { transactionRepo.delete(id) }
}