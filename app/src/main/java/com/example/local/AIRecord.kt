package com.example.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_records")
data class AIRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskType: String, // "SUMMARY", "SENTIMENT", "CHAT", "CODE", etc.
    val prompt: String,
    val result: String,
    val modelName: String,
    val executionTimeMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val sentimentScore: Float? = null, // e.g. -1.0 to 1.0 or confidence
    val sentimentLabel: String? = null // POSITIVE, NEGATIVE, NEUTRAL
)
