package com.example.prosodidownapp4.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long, // Link to UserEntity.id
    val timestamp: Long,
    val totalDurationSeconds: Int,
    val dominantEmotion: String,
    val logJson: String // Format: [{"s": 10, "e": "SENANG"}, ...]
)
