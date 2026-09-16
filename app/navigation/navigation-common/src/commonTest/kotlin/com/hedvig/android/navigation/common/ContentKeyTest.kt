package com.hedvig.android.navigation.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlinx.serialization.Serializable

/**
 * [contentKey] is the identity behind saved-state and `ViewModel` retention, so two different
 * destinations must never share one.
 */
class ContentKeyTest {
  @Serializable
  private data object SubmitFailureKey : HedvigNavKey

  @Serializable
  private data class DetailKey(val id: String) : HedvigNavKey

  /** Mirrors the real clash: `feature-choose-tier` and `feature-addon-purchase` both ship one. */
  @Serializable
  private data object OtherSubmitFailureKey : HedvigNavKey {
    override fun toString() = "SubmitFailureKey"
  }

  @Test
  fun `two destinations sharing a toString do not share a content key`() {
    assertEquals(SubmitFailureKey.toString(), OtherSubmitFailureKey.toString())
    assertNotEquals(SubmitFailureKey.contentKey(), OtherSubmitFailureKey.contentKey())
  }

  @Test
  fun `the same destination always derives the same content key`() {
    assertEquals(DetailKey("a").contentKey(), DetailKey("a").contentKey())
  }

  @Test
  fun `the same type with different arguments derives different content keys`() {
    assertNotEquals(DetailKey("a").contentKey(), DetailKey("b").contentKey())
  }
}
