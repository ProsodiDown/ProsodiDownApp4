package com.example.prosodidownapp4.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Model data user sederhana
// ─────────────────────────────────────────────────────────────────────────────

data class User(
    val username: String,
    val role: String = "user",
)

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

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    // ── Database Simulasi (In-Memory) ───────────────────────────────────────
    // Dalam aplikasi asli, data ini akan diambil dari Database (Room) atau API
    private val registeredUsers = mutableListOf(
        User("admin", "admin"), // username, role
        User("user123", "user")
    )
    
    // Password disimpan terpisah untuk simulasi keamanan sederhana
    private val userPasswords = mutableMapOf(
        "admin" to "admin123",
        "user123" to "user123"
    )

    // ── Login ──────────────────────────────────────────────────────────────
    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _state.value = AuthState.Error("Username dan password tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            kotlinx.coroutines.delay(1000L)

            val user = registeredUsers.find { it.username == username }
            val storedPassword = userPasswords[username]

            if (user != null && storedPassword == password) {
                _state.value = AuthState.Success(user)
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
            registeredUsers.any { it.username == username } ->
                _state.value = AuthState.Error("Username sudah terdaftar")
            password.length < 6 ->
                _state.value = AuthState.Error("Password minimal 6 karakter")
            else -> viewModelScope.launch {
                _state.value = AuthState.Loading
                kotlinx.coroutines.delay(1000L)

                // Simpan ke database simulasi
                registeredUsers.add(User(username, "user"))
                userPasswords[username] = password

                _state.value = AuthState.RegistrationSuccess("Pendaftaran berhasil! Silakan masuk.")
            }
        }
    }

    // ── Reset state ke Idle ────────────────────────────────────────────────
    fun resetState() {
        _state.value = AuthState.Idle
    }
}