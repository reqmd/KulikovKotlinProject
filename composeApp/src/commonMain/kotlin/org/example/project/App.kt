package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val shoppingItems = listOf(
    ShoppingItem("Молоко", 2, "л"),
    ShoppingItem("Хлеб", 1, "шт"),
    ShoppingItem("Яблоки", 1, "кг"),
    ShoppingItem("Яйца", 10, "шт"),
    ShoppingItem("Сыр", 300, "г")
)

@Composable
fun App() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🛒 Список покупок",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn {
                items(shoppingItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.name)
                            Text("${item.quantity} ${item.unit}")
                        }
                    }
                }
            }
        }
    }
}