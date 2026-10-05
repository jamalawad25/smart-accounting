package com.smart.accounting.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val category: String = "عميل",       // عميل | مورد | موظف | أخرى
    val initialBalance: Double = 0.0,   // الرصيد الافتتاحي
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)