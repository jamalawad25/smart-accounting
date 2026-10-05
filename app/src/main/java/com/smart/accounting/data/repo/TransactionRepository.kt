package com.smart.accounting.data.repo

import com.smart.accounting.data.db.TransactionDao
import com.smart.accounting.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {
    fun byAccount(accountId: Long): Flow<List<TransactionEntity>> = dao.observeByAccount(accountId)
    fun bySection(section: String): Flow<List<TransactionEntity>> = dao.observeBySection(section)
    suspend fun insert(t: TransactionEntity) = dao.insert(t)
    suspend fun update(t: TransactionEntity) = dao.update(t)
    suspend fun delete(id: Long) = dao.deleteById(id)
    fun balanceForAccount(accountId: Long) = dao.balanceForAccount(accountId)
    fun totalDebit(accountId: Long) = dao.totalDebit(accountId)
    fun totalCredit(accountId: Long) = dao.totalCredit(accountId)
    fun cashIn(c: String) = dao.cashIn(c)
    fun cashOut(c: String) = dao.cashOut(c)
    fun expenseTotal(c: String) = dao.expenseTotal(c)
}