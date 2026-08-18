package com.example.prosodidownapp4

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _isDarkMode = MutableStateFlow<Boolean?>(false) // default: mode terang (false)
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    private val _shouldOpenDrawer = MutableStateFlow(false)
    val shouldOpenDrawer: StateFlow<Boolean> = _shouldOpenDrawer.asStateFlow()

    fun triggerDrawerOnce() {
        _shouldOpenDrawer.value = true
    }

    fun onDrawerOpened() {
        _shouldOpenDrawer.value = false
    }

    fun toggleDarkMode(currentSystemDark: Boolean) {
        val current = _isDarkMode.value ?: currentSystemDark
        _isDarkMode.value = !current
    }
}
