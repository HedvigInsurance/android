package com.hedvig.authlib.internal

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
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
