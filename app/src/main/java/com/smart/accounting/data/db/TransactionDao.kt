package com.smart.accounting.data.db

import androidx.room.*
import com.smart.accounting.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert suspend fun insert(t: TransactionEntity): Long
    @Update suspend fun update(t: TransactionEntity)
    @Delete suspend fun delete(t: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    // ==== عمليات حساب معين ====
    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY timestamp ASC")
    fun observeByAccount(accountId: Long): Flow<List<TransactionEntity>>

    // ==== رصيد الحساب (موجب = لنا، سالب = علينا) ====
    @Query("""
        SELECT IFNULL(SUM(
            CASE 
                WHEN type = 'CREDIT' THEN amount 
                WHEN type = 'DEBIT' THEN -amount 
                ELSE 0 
            END
        ), 0) 
        FROM transactions 
        WHERE accountId = :accountId
    """)
    fun balanceForAccount(accountId: Long): Flow<Double>

    // ==== إجمالي المدين (عليه) ====
    @Query("SELECT IFNULL(SUM(amount),0) FROM transactions WHERE accountId = :accountId AND type = 'DEBIT'")
    fun totalDebit(accountId: Long): Flow<Double>

    // ==== إجمالي الدائن (له) ====
    @Query("SELECT IFNULL(SUM(amount),0) FROM transactions WHERE accountId = :accountId AND type = 'CREDIT'")
    fun totalCredit(accountId: Long): Flow<Double>

    // ==== عمليات الأقسام (صندوق/مصروف/ديون) ====
    @Query("SELECT * FROM transactions WHERE section = :section ORDER BY timestamp DESC")
    fun observeBySection(section: String): Flow<List<TransactionEntity>>

    @Query("SELECT IFNULL(SUM(amount),0) FROM transactions WHERE section='CASH' AND currency=:cur AND type='CREDIT'")
    fun cashIn(cur: String): Flow<Double>

    @Query("SELECT IFNULL(SUM(amount),0) FROM transactions WHERE section='CASH' AND currency=:cur AND type='DEBIT'")
    fun cashOut(cur: String): Flow<Double>

    @Query("SELECT IFNULL(SUM(amount),0) FROM transactions WHERE section='EXPENSE' AND currency=:cur")
    fun expenseTotal(cur: String): Flow<Double>
}