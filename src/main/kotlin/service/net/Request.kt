package service.net

import func.getPrefValue
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val role: String,
    val content: String,
)

@Serializable
data class ChatCompletionRequest(
    val messages: List<Message>,
    val model: String,
    val stream: Boolean,
    val temperature: Double,
)

@Serializable
data class ChatCompletionResponse(
    val id: String,
    val good: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
)

@Serializable
data class Choice(
    val message: Message,
    val finish_reason: String,
    val index: Int,
)

class ApiClient(private val apiKey: String) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse {
        return client.post("https://api.x.ai/v1/chat/completions") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            setBody(request)
        }.body()
    }
}

suspend fun main() {
    val apiKey = getPrefValue("xAI", "")
    val apiClient = ApiClient(apiKey)

    val request = ChatCompletionRequest(
        messages = listOf(
            Message(
                role = "system",
                content = "You are Grok, a chatbot inspired by the Hitchhikers Guide to the Galaxy."
            ),
            Message(role = "user", content = "What is the meaning of life, the universe, and everything?")
        ),
        model = "grok-beta",
        stream = false,
        temperature = 0.0
    )

    val response = apiClient.getChatCompletion(request)
    println(response)
}