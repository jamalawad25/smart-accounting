package com.smart.accounting.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.smart.accounting.data.model.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Insert
    suspend fun insert(person: PersonEntity): Long

    @Update
    suspend fun update(person: PersonEntity)

    @Delete
    suspend fun delete(person: PersonEntity)

    @Query("SELECT * FROM persons ORDER BY name ASC")
    fun observeAll(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM persons WHERE id = :id")
    suspend fun getById(id: Long): PersonEntity?
}