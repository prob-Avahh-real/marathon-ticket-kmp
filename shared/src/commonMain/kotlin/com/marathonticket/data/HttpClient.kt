// HttpClient.kt — 马拉松抢票 API 客户端 (Ktor, KMP common)
package com.marathonticket.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.encoding.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class MarathonHttpClient private constructor() {
    private var token: String? = null

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    val client = HttpClient {
        install(ContentNegotiation) {
            json(this@MarathonHttpClient.json)
        }
        install(ContentEncoding) {
            gzip()
            deflate()
        }
        defaultRequest {
            url("https://api.marathon.example.com/v1/")
            contentType(ContentType.Application.Json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 2)
            retryOnException(maxRetries = 3)
            exponentialDelay()
        }
    }

    suspend inline fun <reified T> get(path: String, params: Map<String, String> = emptyMap()): T {
        return client.get(path) {
            token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            params.forEach { (k, v) -> parameter(k, v) }
        }.body()
    }

    suspend inline fun <reified T> post(path: String, body: Any? = null): T {
        return client.post(path) {
            token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            if (body != null) setBody(body)
        }.body()
    }

    suspend fun warmup() {
        try { client.get("/health").body<Unit>() } catch (_: Exception) { }
    }

    companion object {
        val shared: MarathonHttpClient by lazy { MarathonHttpClient() }
    }
}
