package com.example.prosodidownapp4

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _isDarkMode = MutableStateFlow<Boolean?>(null) // null means follow system
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    fun setDarkMode(enabled: Boolean?) {
        _isDarkMode.value = enabled
    }
    
    fun toggleDarkMode(currentSystemDark: Boolean) {
        val current = _isDarkMode.value ?: currentSystemDark
        _isDarkMode.value = !current
    }
}
