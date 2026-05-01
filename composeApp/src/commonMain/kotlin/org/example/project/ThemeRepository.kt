package org.example.project

import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    val isDarkTheme: Flow<Boolean?>
    suspend fun setDarkTheme(value: Boolean?)
}