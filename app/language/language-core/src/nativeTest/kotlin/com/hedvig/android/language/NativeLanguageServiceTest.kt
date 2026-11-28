package com.hedvig.android.language

import kotlin.test.Test
import kotlin.test.assertEquals

class NativeLanguageServiceTest {
  @Test
  fun `the language follows the iOS app's current language on every read`() {
    var current = Language.SV_SE
    val service = NativeLanguageService(
      object : IosLanguageSource {
        override fun currentLanguage(): Language = current
      },
    )

    val first = service.getLanguage()
    current = Language.EN_SE
    val second = service.getLanguage()

    assertEquals(listOf(Language.SV_SE, Language.EN_SE), listOf(first, second))
    assertEquals(Language.EN_SE, service.language.value)
  }
}
