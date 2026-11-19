package com.hedvig.android.network.clients.di

import io.ktor.http.HeadersBuilder
import platform.Foundation.NSTimeZone
import platform.Foundation.localTimeZone

internal actual fun HeadersBuilder.appendPlatformHeaders() {
  append("Hedvig-TimeZone", NSTimeZone.localTimeZone.name)
}

internal actual fun platformClientName(appPackageId: String): String = "iOS:$appPackageId"
