package com.example.prosodidownapp4.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prosodidownapp4.data.local.db.AppDatabase
import com.example.prosodidownapp4.data.local.db.UserEntity
import com.example.prosodidownapp4.data.local.pref.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

// ─────────────────────────────────────────────────────────────────────────────
// Model data user sederhana
// ─────────────────────────────────────────────────────────────────────────────

data class User(
    val id: Long,
    val username: String,
    val fullName: String = "",
    val email: String = "",
    val absenceNumber: String = "",
    val studentClass: String = "",
)

fun UserEntity.toUser() = User(id, username, fullName, email, absenceNumber, studentClass)

// ─────────────────────────────────────────────────────────────────────────────
// AuthState: semua kemungkinan state autentikasi
// ─────────────────────────────────────────────────────────────────────────────

sealed class AuthState {
    /** Belum ada aksi apapun */
    object Idle : AuthState()

    /** Sedang proses login / register */
    object Loading : AuthState()

    /** Login berhasil */
    data class Success(val user: User) : AuthState()

    /** Pendaftaran berhasil */
    data class RegistrationSuccess(val message: String) : AuthState()

    /** Ada error — message ditampilkan di UI */
    data class Error(val message: String) : AuthState()
}

// ─────────────────────────────────────────────────────────────────────────────
// AuthViewModel
// ─────────────────────────────────────────────────────────────────────────────

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val sessionManager = SessionManager(application)

    init {
        // Inisialisasi data simulasi ke Database jika kosong
        viewModelScope.launch {
            val users = userDao.getAllUsers()
            if (users.isEmpty()) {
                userDao.insertUser(
                    UserEntity(
                        username = "admin",
                        fullName = "Administrator",
                        email = "admin@prosodi.com",
                        absenceNumber = "0",
                        studentClass = "Admin",
                        password = "admin123"
                    )
                )
                userDao.insertUser(
                    UserEntity(
                        username = "user123",
                        fullName = "User Example",
                        email = "user@gmail.com",
                        absenceNumber = "12",
                        studentClass = "5A",
                        password = "user123"
                    )
                )
            }

            // Check session
            val savedUserId = sessionManager.getUserId()
            if (savedUserId != -1L) {
                val userEntity = userDao.getUserById(savedUserId)
                if (userEntity != null) {
                    _state.value = AuthState.Success(userEntity.toUser())
                }
            }
        }
    }

    // ── Login ──────────────────────────────────────────────────────────────
    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _state.value = AuthState.Error("Username dan password tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            kotlinx.coroutines.delay(1000L.milliseconds)

            val userEntity = userDao.getUserByUsername(username)

            if (userEntity != null && userEntity.password == password) {
                sessionManager.saveUserId(userEntity.id)
                _state.value = AuthState.Success(userEntity.toUser())
            } else {
                _state.value = AuthState.Error("Username atau password salah")
            }
        }
    }

    // ── Register ───────────────────────────────────────────────────────────
    fun register(
        username    : String,
        password    : String,
        namaLengkap : String,
        email       : String,
    ) {
        when {
            namaLengkap.isBlank() ->
                _state.value = AuthState.Error("Nama lengkap tidak boleh kosong")
            email.isBlank() || !email.contains("@") ->
                _state.value = AuthState.Error("Email tidak valid")
            username.isBlank() ->
                _state.value = AuthState.Error("Username tidak boleh kosong")
            password.length < 6 ->
                _state.value = AuthState.Error("Password minimal 6 karakter")
            else -> viewModelScope.launch {
                _state.value = AuthState.Loading
                kotlinx.coroutines.delay(1000L.milliseconds)

                if (userDao.getUserByUsername(username) != null) {
                    _state.value = AuthState.Error("Username sudah terdaftar")
                    return@launch
                }

                // Simpan ke database
                userDao.insertUser(
                    UserEntity(
                        username = username,
                        fullName = namaLengkap,
                        email = email,
                        absenceNumber = "",
                        studentClass = "",
                        password = password
                    )
                )

                _state.value = AuthState.RegistrationSuccess("Pendaftaran berhasil! Silakan masuk.")
            }
        }
    }

    // ── Update Profil ──────────────────────────────────────────────────────
    fun updateProfile(
        userId: Long,
        newUsername: String,
        fullName: String,
        email: String,
        absenceNumber: String,
        studentClass: String
    ) {
        viewModelScope.launch {
            val userEntity = userDao.getUserById(userId)
            if (userEntity != null) {
                // Check if username is changing and if the new one is already taken
                if (userEntity.username != newUsername && userDao.getUserByUsername(newUsername) != null) {
                    _state.value = AuthState.Error("Username sudah digunakan")
                    return@launch
                }

                val updatedEntity = userEntity.copy(
                    username = newUsername,
                    fullName = fullName,
                    email = email,
                    absenceNumber = absenceNumber,
                    studentClass = studentClass
                )
                userDao.updateUser(updatedEntity)
                _state.value = AuthState.Success(updatedEntity.toUser())
            }
        }
    }

    // ── Ganti Password ─────────────────────────────────────────────────────
    fun changePassword(userId: Long, oldPass: String, newPass: String): Boolean {
        viewModelScope.launch {
            val userEntity = userDao.getUserById(userId)
            if (userEntity != null && userEntity.password == oldPass) {
                userDao.updateUser(userEntity.copy(password = newPass))
            }
        }
        return true
    }

    // ── Reset state ke Idle (digunakan untuk Logout) ─────────────────────
    fun logout() {
        sessionManager.clearSession()
        _state.value = AuthState.Idle
    }

    fun resetState() {
        _state.value = AuthState.Idle
    }
}