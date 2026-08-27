package com.hedvig.android.app

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.Test

internal class NavigationStateBridgeTest {
  private val thirtyMinutesMs = 30L * 60L * 1000L

  @Test
  fun `a missing stash timestamp is treated as not fresh`() {
    assertThat(NavigationStateBridge.isPendingDeepLinkStashTimeFresh(null, nowEpochMs = 1_000_000L))
      .isFalse()
  }

  @Test
  fun `a just-stashed pending link is fresh`() {
    val now = 1_000_000L
    assertThat(NavigationStateBridge.isPendingDeepLinkStashTimeFresh(now, nowEpochMs = now)).isTrue()
  }

  @Test
  fun `a link stashed just under the limit is fresh`() {
    val now = 5_000_000L
    val stashedAt = now - (thirtyMinutesMs - 1)
    assertThat(NavigationStateBridge.isPendingDeepLinkStashTimeFresh(stashedAt, nowEpochMs = now)).isTrue()
  }

  @Test
  fun `a link stashed exactly at the limit is still fresh`() {
    val now = 5_000_000L
    val stashedAt = now - thirtyMinutesMs
    assertThat(NavigationStateBridge.isPendingDeepLinkStashTimeFresh(stashedAt, nowEpochMs = now)).isTrue()
  }

  @Test
  fun `a link stashed past the limit is stale`() {
    val now = 5_000_000L
    val stashedAt = now - (thirtyMinutesMs + 1)
    assertThat(NavigationStateBridge.isPendingDeepLinkStashTimeFresh(stashedAt, nowEpochMs = now)).isFalse()
  }

  @Test
  fun `a negative age from a backwards clock is treated as not fresh`() {
    val now = 5_000_000L
    val stashedAt = now + 60_000L // stashed "in the future" relative to now
    assertThat(NavigationStateBridge.isPendingDeepLinkStashTimeFresh(stashedAt, nowEpochMs = now)).isFalse()
  }
}
