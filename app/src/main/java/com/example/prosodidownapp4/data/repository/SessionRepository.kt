package com.example.prosodidownapp4.data.repository

import com.example.prosodidownapp4.data.local.db.SessionDao
import com.example.prosodidownapp4.data.local.db.SessionEntity
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {
    fun getSessionsByUser(userId: Long): Flow<List<SessionEntity>> = sessionDao.getSessionsByUser(userId)
    fun getAvailableYearsByUser(userId: Long): Flow<List<String>> = sessionDao.getAvailableYearsByUser(userId)

    suspend fun saveSession(session: SessionEntity) {
        sessionDao.insertSession(session)
    }

    suspend fun clearHistoryByUser(userId: Long) {
        sessionDao.clearAllByUser(userId)
    }
}
