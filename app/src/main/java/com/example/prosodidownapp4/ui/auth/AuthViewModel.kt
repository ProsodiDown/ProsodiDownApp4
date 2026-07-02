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

    /** Login / register berhasil */
    data class Success(val user: User) : AuthState()

    /** Ada error — message ditampilkan di UI */
    data class Error(val message: String) : AuthState()
}

// ─────────────────────────────────────────────────────────────────────────────
// AuthViewModel
// ─────────────────────────────────────────────────────────────────────────────

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    // ── Login ──────────────────────────────────────────────────────────────
    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _state.value = AuthState.Error("Username dan password tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading

            // TODO: ganti dengan API call ke backend kamu
            // Contoh sementara: simulasi delay + validasi dummy
            kotlinx.coroutines.delay(1000L)

            // ── Ganti blok ini dengan hasil response API ──────────────────
            if (username == "admin" && password == "admin123") {
                _state.value = AuthState.Success(
                    user = User(username = username, role = "admin")
                )
            } else if (password.length >= 4) {
                // Sementara: login berhasil jika password >= 4 karakter
                _state.value = AuthState.Success(
                    user = User(username = username, role = "user")
                )
            } else {
                _state.value = AuthState.Error("Username atau password salah")
            }
            // ─────────────────────────────────────────────────────────────
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

                // TODO: ganti dengan API call register ke backend kamu
                kotlinx.coroutines.delay(1000L)

                // Sementara selalu berhasil
                _state.value = AuthState.Error("Pendaftaran berhasil! Silakan masuk.")
            }
        }
    }

    // ── Reset state ke Idle ────────────────────────────────────────────────
    fun resetState() {
        _state.value = AuthState.Idle
    }
}