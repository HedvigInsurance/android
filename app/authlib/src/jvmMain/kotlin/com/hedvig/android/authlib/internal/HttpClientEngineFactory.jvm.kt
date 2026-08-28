package com.hedvig.android.authlib.internal

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun httpClientEngineFactory(): HttpClientEngineFactory<*> = OkHttp
