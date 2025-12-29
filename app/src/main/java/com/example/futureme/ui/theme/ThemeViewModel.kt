package com.example.futureme.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futureme.data.preferences.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val prefs: ThemePreferences
) : ViewModel() {

    val isDark: StateFlow<Boolean> = prefs.isDarkTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true
    )

    fun setDark(enabled: Boolean) {
        viewModelScope.launch { prefs.setDarkTheme(enabled) }
    }

    fun toggle() {
        setDark(!isDark.value)
    }
}
