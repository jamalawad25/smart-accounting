package com.smart.accounting.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val section: String,
    val title: String,
    val amount: Double,
    val currency: String,
    val type: String,
    val paymentMethod: String,
    val walletName: String? = null,
    val category: String? = null,
    val personId: Long? = null,
    val personName: String? = null,
    val dueDate: String? = null,
    val dateText: String,
    val timestamp: Long,
    val notes: String? = null,
    val imagePath: String? = null
)