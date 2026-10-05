package com.smart.accounting.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.smart.accounting.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(t: TransactionEntity): Long

    @Update
    suspend fun update(t: TransactionEntity)

    @Delete
    suspend fun delete(t: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions WHERE section = :section ORDER BY timestamp DESC")
    fun observeBySection(section: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE personId = :personId ORDER BY timestamp DESC")
    fun observeByPerson(personId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT IFNULL(SUM(amount),0) FROM transactions WHERE section='CASH' AND currency=:cur AND type='IN'")
    fun cashIn(cur: String): Flow<Double>

    @Query("SELECT IFNULL(SUM(amount),0) FROM transactions WHERE section='CASH' AND currency=:cur AND type='OUT'")
    fun cashOut(cur: String): Flow<Double>

    @Query("SELECT IFNULL(SUM(amount),0) FROM transactions WHERE section='EXPENSE' AND currency=:cur")
    fun expenseTotal(cur: String): Flow<Double>

    @Query("SELECT IFNULL(SUM(CASE WHEN type='RECEIVABLE' THEN amount WHEN type='PAYABLE' THEN -amount ELSE 0 END),0) FROM transactions WHERE section='DEBT' AND currency=:cur")
    fun debtNet(cur: String): Flow<Double>

    @Query("SELECT IFNULL(SUM(CASE WHEN type='RECEIVABLE' THEN amount WHEN type='PAYABLE' THEN -amount ELSE 0 END),0) FROM transactions WHERE personId = :personId")
    fun balanceForPerson(personId: Long): Flow<Double>
}