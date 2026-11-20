package com.hedvig.android.network.clients.di

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
  fun `the time zone is the device's zone identifier`() {
    assertEquals(NSTimeZone.localTimeZone.name, deviceTimeZoneId())
  }
}
