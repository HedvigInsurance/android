package com.hedvig.android.network.clients.di

import io.ktor.http.HeadersBuilder

/**
 * Appends the headers only one platform sends, after the headers every platform sends.
 */
internal expect fun HeadersBuilder.appendPlatformHeaders()
