package com.example.prosodidownapp4.data.repository

import com.example.prosodidownapp4.data.local.db.SessionDao
import com.example.prosodidownapp4.data.local.db.SessionEntity
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {
    val allSessions: Flow<List<SessionEntity>> = sessionDao.getAllSessions()
    val availableYears: Flow<List<String>> = sessionDao.getAvailableYears()

    suspend fun saveSession(session: SessionEntity) {
        sessionDao.insertSession(session)
    }

    suspend fun clearHistory() {
        sessionDao.clearAll()
    }
}
