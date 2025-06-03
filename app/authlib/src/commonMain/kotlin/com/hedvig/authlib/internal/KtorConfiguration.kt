package com.hedvig.authlib.internal

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal fun buildKtorClient(
  httpClientEngine: HttpClientEngine?,
  additionalHttpHeadersProvider: () -> Map<String, String>,
): HttpClient {
  val httpClientConfig: HttpClientConfig<*>.() -> Unit = {
    commonKtorConfiguration(additionalHttpHeadersProvider).invoke(this)
  }
  return if (httpClientEngine == null) {
    HttpClient {
      httpClientConfig()
    }
  } else {
    HttpClient(httpClientEngine) {
      httpClientConfig()
    }
  }
}

private fun commonKtorConfiguration(
  additionalHttpHeadersProvider: () -> Map<String, String>,
): HttpClientConfig<*>.() -> Unit = {
  install(ContentNegotiation) {
    json(
      Json {
        allowSpecialFloatingPointValues = true
        isLenient = true
        allowStructuredMapKeys = true
        ignoreUnknownKeys = true
      },
    )
  }
  install(Logging) {
    logger = Logger.DEFAULT
    level = LogLevel.INFO
  }
  defaultRequest {
    additionalHttpHeadersProvider().forEach { entry ->
      header(entry.key, entry.value)
    }
  }
}
