package com.smart.accounting.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.smart.accounting.data.model.PersonEntity
import com.smart.accounting.data.model.TransactionEntity

@Database(
    entities = [TransactionEntity::class, PersonEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): TransactionDao
    abstract fun personDao(): PersonDao

    companion object {
        const val DB_NAME = "smart_accounting.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(ctx: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                ctx.applicationContext,
                AppDatabase::class.java,
                DB_NAME
            ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }

        fun close() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}