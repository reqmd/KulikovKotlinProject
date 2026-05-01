package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean?,       // null = системная
    onThemeChange: (Boolean?) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Назад") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Оформление",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {

                    // Вариант 1 — системная тема
                    ThemeOption(
                        label = "Как в системе",
                        selected = isDarkTheme == null,
                        onClick = { onThemeChange(null) }
                    )
                    HorizontalDivider()

                    // Вариант 2 — светлая
                    ThemeOption(
                        label = "Светлая",
                        selected = isDarkTheme == false,
                        onClick = { onThemeChange(false) }
                    )
                    HorizontalDivider()

                    // Вариант 3 — тёмная
                    ThemeOption(
                        label = "Тёмная",
                        selected = isDarkTheme == true,
                        onClick = { onThemeChange(true) }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}