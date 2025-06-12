package com.hedvig.authlib

import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.engine.okhttp.OkHttpEngine
import okhttp3.OkHttpClient

@Suppress("FunctionName")
public fun OkHttpNetworkAuthRepository(
  environment: AuthEnvironment,
  additionalHttpHeadersProvider: () -> Map<String, String>,
  okHttpClientBuilder: OkHttpClient.Builder,
): AuthRepository {
  return NetworkAuthRepository(
    environment = environment,
    additionalHttpHeadersProvider = additionalHttpHeadersProvider,
    httpClientEngine = OkHttpEngine(
      OkHttpConfig().apply { preconfigured = okHttpClientBuilder.build() },
    ),
  )
}
