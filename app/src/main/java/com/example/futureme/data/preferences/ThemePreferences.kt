package com.example.futureme.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemePreferences(private val context: Context) {

    private val KEY_DARK = booleanPreferencesKey("dark_theme_enabled")

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK] ?: true // por defecto DARK
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DARK] = enabled
        }
    }
}
