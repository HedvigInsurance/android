package com.hedvig.authlib.internal

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

internal actual fun httpClientEngineFactory(): HttpClientEngineFactory<*> = Darwin
