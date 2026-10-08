package com.example.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AIRecordDao {

    @Query("SELECT * FROM ai_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AIRecord>>

    @Query("SELECT * FROM ai_records WHERE taskType = :taskType ORDER BY timestamp DESC")
    fun getRecordsByType(taskType: String): Flow<List<AIRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AIRecord): Long

    @Delete
    suspend fun deleteRecord(record: AIRecord)

    @Query("DELETE FROM ai_records")
    suspend fun clearAll()
}
