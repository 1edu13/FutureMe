package com.example.futureme.ui.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.futureme.data.preferences.ThemePreferences

class ThemeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ThemeViewModel(ThemePreferences(context.applicationContext)) as T
    }
}
