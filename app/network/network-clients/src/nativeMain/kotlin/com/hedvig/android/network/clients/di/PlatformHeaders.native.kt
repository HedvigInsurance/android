package com.hedvig.android.network.clients.di

import io.ktor.http.HeadersBuilder
import kotlinx.datetime.TimeZone

internal actual fun HeadersBuilder.appendPlatformHeaders() {
  append("Hedvig-TimeZone", TimeZone.currentSystemDefault().id)
}
