package com.example.trackify2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppSettingsViewModel : ViewModel() {
    private val _isDarkMode = MutableStateFlow(false) // Default to light mode
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    // Function to toggle dark mode
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // Function to set dark mode explicitly
    fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
    }
}
