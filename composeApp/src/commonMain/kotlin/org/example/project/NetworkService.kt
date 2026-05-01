package org.example.project

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Модель ответа от API
@Serializable
data class ProductResponse(
    val products: List<Product> = emptyList()
)

@Serializable
data class Product(
    val product_name: String = "",
    val quantity: String = ""
)

object NetworkService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true  // игнорируем лишние поля
                isLenient = true
            })
        }
    }

    // Поиск продуктов по названию
    suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            val response: ProductResponse = client
                .get("https://world.openfoodfacts.org/cgi/search.pl") {
                    parameter("search_terms", query)
                    parameter("json", "1")
                    parameter("page_size", "5")
                }
                .body()
            Result.success(response.products.filter { it.product_name.isNotBlank() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}