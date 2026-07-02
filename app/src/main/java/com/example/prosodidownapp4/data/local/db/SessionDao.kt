package com.example.prosodidownapp4.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT DISTINCT strftime('%Y', datetime(timestamp / 1000, 'unixepoch', 'localtime')) FROM sessions ORDER BY timestamp DESC")
    fun getAvailableYears(): Flow<List<String>>

    @Query("DELETE FROM sessions")
    suspend fun clearAll()
}
