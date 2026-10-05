package com.smart.accounting.data.repo

import com.smart.accounting.data.db.TransactionDao
import com.smart.accounting.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {
    fun bySection(section: String): Flow<List<TransactionEntity>> = dao.observeBySection(section)
    fun byPerson(personId: Long): Flow<List<TransactionEntity>> = dao.observeByPerson(personId)
    suspend fun insert(t: TransactionEntity) = dao.insert(t)
    suspend fun update(t: TransactionEntity) = dao.update(t)
    suspend fun delete(id: Long) = dao.deleteById(id)
    fun cashIn(c: String) = dao.cashIn(c)
    fun cashOut(c: String) = dao.cashOut(c)
    fun expenseTotal(c: String) = dao.expenseTotal(c)
    fun debtNet(c: String) = dao.debtNet(c)
    fun balanceForPerson(personId: Long) = dao.balanceForPerson(personId)
}