package com.smart.accounting.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.accounting.data.model.PersonEntity
import com.smart.accounting.data.model.TransactionEntity
import com.smart.accounting.data.repo.PersonRepository
import com.smart.accounting.data.repo.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CurrencySummary(
    val currency: String,
    val balance: Double,
    val income: Double,
    val expense: Double
)

class MainViewModel(
    private val repo: TransactionRepository,
    private val personRepo: PersonRepository
) : ViewModel() {

    private val _section = MutableStateFlow("CASH")
    val section: StateFlow<String> = _section

    val items: StateFlow<List<TransactionEntity>> = _section
        .flatMapLatest { sec ->
            if (sec == "PERSONS") flowOf(emptyList()) else repo.bySection(sec)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val persons: StateFlow<List<PersonEntity>> = personRepo.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summaryYer = buildSummary("YER")
    val summarySar = buildSummary("SAR")
    val summaryUsd = buildSummary("USD")

    private fun buildSummary(cur: String): StateFlow<CurrencySummary> =
        combine(
            repo.cashIn(cur),
            repo.cashOut(cur),
            repo.expenseTotal(cur),
            repo.debtNet(cur)
        ) { inc, exp, ex, debt ->
            CurrencySummary(cur, inc - exp - ex + debt, inc + debt, exp + ex)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CurrencySummary(cur, 0.0, 0.0, 0.0)
        )

    fun setSection(s: String) { _section.value = s }

    fun save(t: TransactionEntity) = viewModelScope.launch {
        if (t.id == 0L) repo.insert(t) else repo.update(t)
    }

    fun delete(id: Long) = viewModelScope.launch { repo.delete(id) }

    fun addPerson(p: PersonEntity) = viewModelScope.launch { personRepo.insert(p) }
    fun deletePerson(p: PersonEntity) = viewModelScope.launch { personRepo.delete(p) }
    fun transactionsForPerson(personId: Long) = repo.byPerson(personId)
    fun balanceForPerson(personId: Long) = repo.balanceForPerson(personId)
}