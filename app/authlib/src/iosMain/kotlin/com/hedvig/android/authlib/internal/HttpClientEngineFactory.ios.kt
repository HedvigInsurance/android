package com.hedvig.android.authlib.internal

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

internal actual fun httpClientEngineFactory(): HttpClientEngineFactory<*> = Darwin
