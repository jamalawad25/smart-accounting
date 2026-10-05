package com.smart.accounting.data.repo

import com.smart.accounting.data.db.PersonDao
import com.smart.accounting.data.model.PersonEntity
import kotlinx.coroutines.flow.Flow

class PersonRepository(private val dao: PersonDao) {
    fun all(): Flow<List<PersonEntity>> = dao.observeAll()
    suspend fun insert(p: PersonEntity) = dao.insert(p)
    suspend fun update(p: PersonEntity) = dao.update(p)
    suspend fun delete(p: PersonEntity) = dao.delete(p)
    suspend fun getById(id: Long) = dao.getById(id)
}