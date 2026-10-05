package com.smart.accounting.data.db

import androidx.room.*
import com.smart.accounting.data.model.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert suspend fun insert(account: AccountEntity): Long
    @Update suspend fun update(account: AccountEntity)
    @Delete suspend fun delete(account: AccountEntity)

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE category = :category ORDER BY name ASC")
    fun observeByCategory(category: String): Flow<List<AccountEntity>>
}