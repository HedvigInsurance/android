package com.hedvig.android.network.clients.di

import kotlin.test.Test
import kotlin.test.assertEquals

class NativePlatformHeadersTest {
  @Test
  fun `the client name matches the iOS format`() {
    assertEquals("iOS:com.hedvig.test", platformClientName("com.hedvig.test"))
  }
}
