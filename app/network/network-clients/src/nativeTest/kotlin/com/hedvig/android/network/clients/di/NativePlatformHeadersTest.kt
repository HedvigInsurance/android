package com.hedvig.android.network.clients.di

import io.ktor.http.HeadersBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import platform.Foundation.NSTimeZone
import platform.Foundation.localTimeZone

class NativePlatformHeadersTest {
  @Test
  fun `the client name matches the iOS format`() {
    assertEquals("iOS:com.hedvig.test", platformClientName("com.hedvig.test"))
  }

  @Test
  fun `the time zone header is the device's zone identifier`() {
    val headers = HeadersBuilder().apply { appendPlatformHeaders() }.build()

    assertEquals(NSTimeZone.localTimeZone.name, headers["Hedvig-TimeZone"])
  }
}
