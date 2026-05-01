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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class ShoppingItem(
    val name: String,
    val quantity: Int,
    val unit: String,
    val bought: Boolean = false
)

val LightColors = lightColorScheme()
val DarkColors = darkColorScheme()

object Routes {
    const val LIST = "list"
    const val ADD = "add"
    const val SETTINGS = "settings"  // ← новый маршрут
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(settingsStore: ThemeRepository) {
    // Читаем сохранённую тему из DataStore
    val savedTheme by settingsStore.isDarkTheme.collectAsState(initial = null)
    val systemDark = isSystemInDarkTheme()
    val isDark = savedTheme ?: systemDark  // если не задано — системная

    val colors = if (isDark) DarkColors else LightColors

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

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    val navController = rememberNavController()

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
                        }) { Text("Удалить") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearDialog = false }) { Text("Отмена") }
                    }
                )
            }

            NavHost(navController = navController, startDestination = Routes.LIST) {
                composable(Routes.LIST) {
                    ListScreen(
                        items = items,
                        snackbarHostState = snackbarHostState,
                        onToggle = { item ->
                            items = items.map { i ->
                                if (i == item) i.copy(bought = !i.bought) else i
                            }
                        },
                        onClearClick = { showClearDialog = true },
                        onAddClick = { navController.navigate(Routes.ADD) },
                        onSettingsClick = { navController.navigate(Routes.SETTINGS) }
                    )
                }
                composable(Routes.ADD) {
                    AddScreen(
                        snackbarHostState = snackbarHostState,
                        onAdd = { name, quantity, unit ->
                            items = items + ShoppingItem(name, quantity, unit)
                            scope.launch {
                                snackbarHostState.showSnackbar("$name добавлен")
                            }
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        isDarkTheme = savedTheme,
                        onThemeChange = { value ->
                            scope.launch { settingsStore.setDarkTheme(value) }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────
// Экран 1 — Список
// ─────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    items: List<ShoppingItem>,
    snackbarHostState: SnackbarHostState,
    onToggle: (ShoppingItem) -> Unit,
    onClearClick: () -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🛒 Список покупок") },
                actions = {
                    TextButton(onClick = onClearClick) { Text("Очистить") }
                    // Кнопка настроек
                    IconButton(onClick = onSettingsClick) {
                        Text("⚙️")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            val isExpanded = maxWidth > 600.dp
            if (isExpanded) {
                Row(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    ShoppingList(
                        items = items.filter { !it.bought },
                        onToggle = onToggle,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    ShoppingList(
                        items = items.filter { it.bought },
                        onToggle = onToggle,
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    )
                }
            } else {
                ShoppingList(
                    items = items,
                    onToggle = onToggle,
                    modifier = Modifier.padding(16.dp).fillMaxSize()
                )
            }
        }
    }
}

// ─────────────────────────────────────────
// Экран 2 — Добавление с поиском
// ─────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    snackbarHostState: SnackbarHostState,
    onAdd: (String, Int, String) -> Unit,
    onBack: () -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var newQuantity by remember { mutableStateOf("") }
    var newUnit by remember { mutableStateOf("шт") }

    var searchResults by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить товар") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Назад") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = {
                        newName = it
                        errorMessage = null
                        searchResults = emptyList()
                    },
                    label = { Text("Название") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    isError = errorMessage != null
                )
                Button(
                    onClick = {
                        if (newName.isBlank()) {
                            errorMessage = "Введите название для поиска"
                            return@Button
                        }
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            val result = NetworkService.searchProducts(newName)
                            isLoading = false
                            result
                                .onSuccess { products ->
                                    searchResults = products
                                    if (products.isEmpty()) errorMessage = "Ничего не найдено"
                                }
                                .onFailure { e ->
                                    errorMessage = when {
                                        e.message?.contains("Unable to resolve host") == true ->
                                            "Нет подключения к интернету"
                                        e.message?.contains("timeout") == true ->
                                            "Превышено время ожидания"
                                        else -> "Ошибка: ${e.message}"
                                    }
                                }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("Найти")
                }
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }

            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "⚠️ $error",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (searchResults.isNotEmpty()) {
                Text(
                    text = "Результаты поиска:",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(searchResults) { product ->
                        TextButton(
                            onClick = { newName = product.product_name },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = product.product_name, modifier = Modifier.fillMaxWidth())
                        }
                        HorizontalDivider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
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
                        onAdd(newName.trim(), qty, newUnit.trim())
                    } else {
                        errorMessage = "Введите название товара"
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("+ Добавить в список")
            }
        }
    }
}

// ─────────────────────────────────────────
// Компонент списка
// ─────────────────────────────────────────
@Composable
fun ShoppingList(
    items: List<ShoppingItem>,
    onToggle: (ShoppingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(items) { item ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
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