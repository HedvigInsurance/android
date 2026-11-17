package com.hedvig.android

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import org.junit.Test

class UmbrellaClasspathGuardTest {
  @Test
  fun `flags Skiko and compose resources`() {
    val coordinates = listOf(
      "org.jetbrains.skiko:skiko",
      "org.jetbrains.skiko:skiko-iosarm64",
      "org.jetbrains.compose.components:components-resources",
      "org.jetbrains.compose.components:components-resources-iossimulatorarm64",
      "org.jetbrains.compose.runtime:runtime",
    )
    assertThat(findForbiddenUmbrellaArtifacts(coordinates)).containsExactly(
      "org.jetbrains.compose.components:components-resources",
      "org.jetbrains.compose.components:components-resources-iossimulatorarm64",
      "org.jetbrains.skiko:skiko",
      "org.jetbrains.skiko:skiko-iosarm64",
    )
  }

  @Test
  fun `allows the shared layer`() {
    val coordinates = listOf(
      "org.jetbrains.compose.runtime:runtime",
      "org.jetbrains.compose.runtime:runtime-saveable",
      "org.jetbrains.compose.components:components-ui-tooling-preview",
      "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel",
      "com.apollographql.apollo:apollo-runtime",
      "dev.zacsweers.metro:runtime",
    )
    assertThat(findForbiddenUmbrellaArtifacts(coordinates)).isEmpty()
  }
}
