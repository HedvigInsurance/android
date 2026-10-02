package com.hedvig.android.language

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test

class LanguageResolutionTest {
  @Test
  fun `with nothing stored the phone's first language decides`() {
    assertThat(
      resolveLanguage(storedLocaleTags = emptyList(), phoneLocaleTags = listOf("sv-SE")),
    ).isEqualTo(Language.SV_SE)
    assertThat(resolveLanguage(emptyList(), listOf("sv-FI"))).isEqualTo(Language.SV_SE)
    assertThat(resolveLanguage(emptyList(), listOf("sv"))).isEqualTo(Language.SV_SE)
    assertThat(resolveLanguage(emptyList(), listOf("en-US", "sv-SE"))).isEqualTo(Language.EN_SE)
  }

  @Test
  fun `a phone whose first language is neither Swedish nor English gets English`() {
    assertThat(resolveLanguage(emptyList(), listOf("de-DE"))).isEqualTo(Language.EN_SE)
    assertThat(resolveLanguage(emptyList(), listOf("de-DE", "sv-SE"))).isEqualTo(Language.EN_SE)
    assertThat(resolveLanguage(emptyList(), emptyList())).isEqualTo(Language.EN_SE)
  }

  @Test
  fun `a stored choice wins over the phone and counts by its language`() {
    assertThat(resolveLanguage(listOf("sv-SE"), listOf("en-US"))).isEqualTo(Language.SV_SE)
    assertThat(resolveLanguage(listOf("sv-FI"), listOf("en-US"))).isEqualTo(Language.SV_SE)
    assertThat(resolveLanguage(listOf("en-GB"), listOf("sv-SE"))).isEqualTo(Language.EN_SE)
    assertThat(resolveLanguage(listOf("nb-NO"), listOf("sv-SE"))).isEqualTo(Language.EN_SE)
  }

  @Test
  fun `picking the language the phone resolves to stores nothing`() {
    assertThat(storedLocaleTagsForPick(Language.EN_SE, phoneLocaleTags = listOf("en-US"))).isEmpty()
    assertThat(storedLocaleTagsForPick(Language.SV_SE, phoneLocaleTags = listOf("sv-FI", "en-US"))).isEmpty()
    assertThat(storedLocaleTagsForPick(Language.EN_SE, phoneLocaleTags = listOf("de-DE", "sv-SE"))).isEmpty()
  }

  @Test
  fun `picking a language other than the phone's stores it`() {
    assertThat(storedLocaleTagsForPick(Language.SV_SE, listOf("en-US"))).isEqualTo(listOf("sv-SE"))
    assertThat(storedLocaleTagsForPick(Language.EN_SE, listOf("sv-SE"))).isEqualTo(listOf("en-SE"))
  }

  @Test
  fun `migration clears what matches the phone or is unsupported and keeps a real choice`() {
    assertThat(storedLanguageMigration(emptyList(), listOf("sv-SE"))).isEqualTo(StoredLanguageMigration.Keep)
    assertThat(storedLanguageMigration(listOf("en-SE"), listOf("en-US"))).isEqualTo(StoredLanguageMigration.Clear)
    assertThat(storedLanguageMigration(listOf("sv-SE"), listOf("sv-FI"))).isEqualTo(StoredLanguageMigration.Clear)
    assertThat(
      storedLanguageMigration(listOf("en-SE"), listOf("de-DE", "sv-SE")),
    ).isEqualTo(StoredLanguageMigration.Clear)
    assertThat(storedLanguageMigration(listOf("nb-NO"), listOf("en-US"))).isEqualTo(StoredLanguageMigration.Clear)
    assertThat(storedLanguageMigration(listOf("sv-SE"), listOf("en-US"))).isEqualTo(StoredLanguageMigration.Keep)
    assertThat(storedLanguageMigration(listOf("en-SE"), listOf("sv-SE"))).isEqualTo(StoredLanguageMigration.Keep)
  }
}
