package com.hedvig.android

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.Test

class SharedCommonMainGuardTest {
  @Test
  fun `flags Compose UI, design system, resources, Coil, paging and nav3 UI imports`() {
    val source = """
      package a
      import androidx.compose.ui.Modifier
      import androidx.compose.foundation.layout.Column
      import androidx.compose.material3.Text
      import com.hedvig.android.design.system.hedvig.HedvigText
      import hedvig.resources.Res
      import org.jetbrains.compose.resources.StringResource
      import coil3.compose.AsyncImage
      import androidx.paging.compose.LazyPagingItems
      import androidx.navigation3.ui.NavDisplay
      import androidx.compose.animation.AnimatedContent
      import androidx.compose.runtime.Composable
      import androidx.compose.runtime.saveable.rememberSaveable
      import androidx.lifecycle.viewmodel.compose.viewModel
      import androidx.lifecycle.ViewModel
      import com.hedvig.android.molecule.public.MoleculeViewModel
    """.trimIndent()
    assertThat(findForbiddenImports(source, forbiddenNames = forbiddenSharedCommonMainNames)).containsExactly(
      2 to "androidx.compose.ui.Modifier",
      3 to "androidx.compose.foundation.layout.Column",
      4 to "androidx.compose.material3.Text",
      5 to "com.hedvig.android.design.system.hedvig.HedvigText",
      6 to "hedvig.resources.Res",
      7 to "org.jetbrains.compose.resources.StringResource",
      8 to "coil3.compose.AsyncImage",
      9 to "androidx.paging.compose.LazyPagingItems",
      10 to "androidx.navigation3.ui.NavDisplay",
      11 to "androidx.compose.animation.AnimatedContent",
    )
  }

  @Test
  fun `flags UI dependencies and allows the shared layer`() {
    val forbidden = listOf(
      ":design-system-hedvig",
      ":core-resources",
      ":navigation-compose",
      "org.jetbrains.compose.ui:ui-tooling-preview",
      "org.jetbrains.compose.components:components-resources",
      "io.coil-kt.coil3:coil-compose",
      "androidx.paging:paging-compose",
      "org.jetbrains.compose.animation:animation",
      "androidx.compose.ui:ui-tooling-preview",
      "androidx.compose.foundation:foundation-layout",
      "androidx.compose.material:material-ripple",
      "androidx.compose.material3:material3",
      "androidx.compose.animation:animation-core",
      "org.jetbrains.androidx.navigationevent:navigationevent-compose",
      "dev.zacsweers.metro:metrox-viewmodel-compose",
    )
    for (dependency in forbidden) {
      assertThat(isForbiddenSharedCommonMainDependency(dependency), dependency).isTrue()
    }
    assertThat(isForbiddenSharedCommonMainDependency(":cross-sells", "app/ui/cross-sells")).isTrue()
    val allowed = listOf(
      ":molecule-public",
      ":apollo-core",
      ":core-common-public",
      ":navigation-common",
      "org.jetbrains.compose.runtime:runtime",
      "com.apollographql.apollo:apollo-runtime",
      "androidx.compose:compose-bom",
      "androidx.compose.runtime:runtime-retain",
      "org.jetbrains.compose.runtime:runtime-saveable",
      "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose",
      "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose",
      "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3",
      "dev.zacsweers.metro:metrox-viewmodel",
    )
    for (dependency in allowed) {
      assertThat(isForbiddenSharedCommonMainDependency(dependency), dependency).isFalse()
    }
  }
}
