package com.example.prosodidownapp4.data.local.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "prosodi_session"
        private const val KEY_USER_ID = "user_id"
    }

    fun saveUserId(userId: Long) {
        prefs.edit { putLong(KEY_USER_ID, userId) }
    }

    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1L)
    }

    fun clearSession() {
        prefs.edit { remove(KEY_USER_ID) }
    }
}
