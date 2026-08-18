package com.hedvig.android.network.clients.di

import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun HttpClientConfig<*>.installDatadogKtorPlugin(hedvigBuildConstants: HedvigBuildConstants) {
  // no-op
}

internal actual fun httpClientEngineFactory(): HttpClientEngineFactory<*> = OkHttp
