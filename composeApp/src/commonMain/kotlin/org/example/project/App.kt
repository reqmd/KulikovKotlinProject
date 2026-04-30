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
import kotlinx.coroutines.launch

data class ShoppingItem(
    val name: String,
    val quantity: Int,
    val unit: String,
    val bought: Boolean = false
)

val LightColors = lightColorScheme()
val DarkColors = darkColorScheme()

@OptIn(ExperimentalMaterial3Api::class)
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

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showClearDialog by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = colors) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (showClearDialog) {
                AlertDialog(
                    onDismissRequest = { showClearDialog = false },
                    title = { Text("Очистить список?") },
                    text = { Text("Все отмеченные товары будут удалены. Это действие нельзя отменить.") },
                    confirmButton = {
                        TextButton(onClick = {
                            items = items.filter { !it.bought }
                            showClearDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Купленные товары удалены")
                            }
                        }) {
                            Text("Удалить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("🛒 Список покупок") },
                        actions = {
                            TextButton(onClick = { showClearDialog = true }) {
                                Text("Очистить")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->

                // --- Адаптивность через BoxWithConstraints ---
                BoxWithConstraints(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    val isExpanded = maxWidth > 600.dp  // планшет если шире 600dp

                    if (isExpanded) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize()
                        ) {
                            AddItemForm(
                                newName = newName,
                                newQuantity = newQuantity,
                                newUnit = newUnit,
                                onNameChange = { newName = it },
                                onQuantityChange = { newQuantity = it },
                                onUnitChange = { newUnit = it },
                                onAdd = {
                                    val qty = newQuantity.toIntOrNull() ?: 1
                                    if (newName.isNotBlank()) {
                                        val addedName = newName.trim()
                                        items = items + ShoppingItem(addedName, qty, newUnit.trim())
                                        newName = ""
                                        newQuantity = ""
                                        newUnit = "шт"
                                        scope.launch {
                                            snackbarHostState.showSnackbar("$addedName добавлен")
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f).padding(end = 16.dp)
                            )
                            ShoppingList(
                                items = items,
                                onToggle = { item ->
                                    items = items.map { i ->
                                        if (i == item) i.copy(bought = !i.bought) else i
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize()
                        ) {
                            AddItemForm(
                                newName = newName,
                                newQuantity = newQuantity,
                                newUnit = newUnit,
                                onNameChange = { newName = it },
                                onQuantityChange = { newQuantity = it },
                                onUnitChange = { newUnit = it },
                                onAdd = {
                                    val qty = newQuantity.toIntOrNull() ?: 1
                                    if (newName.isNotBlank()) {
                                        val addedName = newName.trim()
                                        items = items + ShoppingItem(addedName, qty, newUnit.trim())
                                        newName = ""
                                        newQuantity = ""
                                        newUnit = "шт"
                                        scope.launch {
                                            snackbarHostState.showSnackbar("$addedName добавлен")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            ShoppingList(
                                items = items,
                                onToggle = { item ->
                                    items = items.map { i ->
                                        if (i == item) i.copy(bought = !i.bought) else i
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddItemForm(
    newName: String,
    newQuantity: String,
    newUnit: String,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.padding(bottom = 12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = newName,
                onValueChange = onNameChange,
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedTextField(
                    value = newQuantity,
                    onValueChange = onQuantityChange,
                    label = { Text("Кол-во") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                OutlinedTextField(
                    value = newUnit,
                    onValueChange = onUnitChange,
                    label = { Text("Ед.") },
                    modifier = Modifier.weight(1f)
                )
            }
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("+ Добавить")
            }
        }
    }
}

@Composable
fun ShoppingList(
    items: List<ShoppingItem>,
    onToggle: (ShoppingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
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
                        onCheckedChange = { onToggle(item) }
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