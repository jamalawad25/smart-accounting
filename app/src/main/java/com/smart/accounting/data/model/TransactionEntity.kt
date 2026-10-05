package com.smart.accounting.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("accountId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,                // الحساب المرتبط (إلزامي)
    val type: String,                   // DEBIT (مدين/عليه) | CREDIT (دائن/له)
    val amount: Double,
    val currency: String = "YER",       // YER | SAR | USD
    val title: String,                  // البيان
    val dateText: String,               // "الأحد، 04/10/2026"
    val timestamp: Long,
    val notes: String? = null,
    val imagePath: String? = null,
    val section: String = "ACCOUNT"     // ACCOUNT | CASH | EXPENSE
)