package com.smart.accounting.data.repo

import com.smart.accounting.data.db.AccountDao
import com.smart.accounting.data.model.AccountEntity
import kotlinx.coroutines.flow.Flow

class AccountRepository(private val dao: AccountDao) {
    fun all(): Flow<List<AccountEntity>> = dao.observeAll()
    suspend fun insert(a: AccountEntity) = dao.insert(a)
    suspend fun update(a: AccountEntity) = dao.update(a)
    suspend fun delete(a: AccountEntity) = dao.delete(a)
    suspend fun getById(id: Long) = dao.getById(id)
}