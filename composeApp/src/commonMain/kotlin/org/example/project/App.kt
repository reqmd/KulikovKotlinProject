package org.example.project

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

data class ShoppingItem(
    val name: String,
    val quantity: Int,
    val unit: String,
    val bought: Boolean = false
)

val LightColors = lightColorScheme()
val DarkColors = darkColorScheme()

@Composable
fun App() {
    val darkTheme = isSystemInDarkTheme()
    val colors = if (darkTheme) DarkColors else LightColors

    var items by remember {
        mutableStateOf(
            listOf(
                ShoppingItem("Молоко", 2, "л"),
                ShoppingItem("Хлеб", 1, "шт"),
                ShoppingItem("Яблоки", 1, "кг"),
                ShoppingItem("Яйца", 10, "шт"),
                ShoppingItem("Сыр", 300, "г")
            )
        )
    }

    var newName by remember { mutableStateOf("") }
    var newQuantity by remember { mutableStateOf("") }
    var newUnit by remember { mutableStateOf("шт") }

    MaterialTheme(colorScheme = colors) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .safeDrawingPadding()  // ← отступ под статус бар
                    .padding(16.dp)
            ) {

                Text(
                    text = "🛒 Список покупок",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {

                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Название") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            OutlinedTextField(
                                value = newQuantity,
                                onValueChange = { newQuantity = it },
                                label = { Text("Кол-во") },
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            OutlinedTextField(
                                value = newUnit,
                                onValueChange = { newUnit = it },
                                label = { Text("Ед.") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = {
                                val qty = newQuantity.toIntOrNull() ?: 1
                                if (newName.isNotBlank()) {
                                    items = items + ShoppingItem(newName.trim(), qty, newUnit.trim())
                                    newName = ""
                                    newQuantity = ""
                                    newUnit = "шт"
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("+ Добавить")
                        }
                    }
                }

                LazyColumn {
                    items(items) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.bought,
                                    onCheckedChange = {
                                        items = items.map { i ->
                                            if (i == item) i.copy(bought = !i.bought) else i
                                        }
                                    }
                                )
                                Text(
                                    text = "${item.name} — ${item.quantity} ${item.unit}",
                                    modifier = Modifier.weight(1f),
                                    textDecoration = if (item.bought)
                                        TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (item.bought)
                                        MaterialTheme.colorScheme.outline
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}