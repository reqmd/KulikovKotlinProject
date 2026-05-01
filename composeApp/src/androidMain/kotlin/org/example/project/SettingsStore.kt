package org.example.project

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) : ThemeRepository {
    override val isDarkTheme: Flow<Boolean?> = context.dataStore.data
        .map { prefs -> prefs[DARK_THEME_KEY] }

    override suspend fun setDarkTheme(value: Boolean?) {
        context.dataStore.edit { prefs ->
            if (value == null) prefs.remove(DARK_THEME_KEY)
            else prefs[DARK_THEME_KEY] = value
        }
    }

    companion object {
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    }
}