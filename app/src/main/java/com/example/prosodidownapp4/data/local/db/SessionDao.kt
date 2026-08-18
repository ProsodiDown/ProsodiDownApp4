package com.example.prosodidownapp4.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getSessionsByUser(userId: Long): Flow<List<SessionEntity>>

    @Query("SELECT DISTINCT strftime('%Y', datetime(timestamp / 1000, 'unixepoch', 'localtime')) FROM sessions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAvailableYearsByUser(userId: Long): Flow<List<String>>

    @Query("DELETE FROM sessions WHERE userId = :userId")
    suspend fun clearAllByUser(userId: Long)
}
