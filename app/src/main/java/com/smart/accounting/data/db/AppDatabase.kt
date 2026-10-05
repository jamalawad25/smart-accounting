package com.smart.accounting.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.smart.accounting.data.model.AccountEntity
import com.smart.accounting.data.model.TransactionEntity

@Database(
    entities = [AccountEntity::class, TransactionEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DB_NAME = "smart_accounting.db"

        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(ctx: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                ctx.applicationContext,
                AppDatabase::class.java,
                DB_NAME
            ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }

        fun close() { INSTANCE?.close(); INSTANCE = null }
    }
}