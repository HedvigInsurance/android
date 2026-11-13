package com.hedvig.android

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.Test

class CommonMainImportGuardTest {
  @Test
  fun `flags java javax and android imports only`() {
    val source = """
      package a
      import java.util.Locale
      import javax.inject.Inject
      import android.content.Context
      import androidx.compose.runtime.Composable
      import kotlin.time.Clock
      import javafoo.Bar
    """.trimIndent()
    assertThat(findForbiddenImports(source)).containsExactly(
      2 to "java.util.Locale",
      3 to "javax.inject.Inject",
      4 to "android.content.Context",
    )
  }

  @Test
  fun `flags fully qualified java javax and android references`() {
    val source = """
      package a
      fun f() {
        java.time.LocalDate.now()
        val id = java.util.UUID.randomUUID()
      }
      val x: java.io.File = TODO()
      val uri = g(android.net.Uri.parse("x"))
      @javax.inject.Inject
      val list: List<java.util.Locale> = listOf(a, java.util.Locale.ROOT)
      fun h(file:java.io.File) = Unit
    """.trimIndent()
    assertThat(findFullyQualifiedReferences(source)).containsExactly(
      3 to "java.time.LocalDate.now",
      4 to "java.util.UUID.randomUUID",
      6 to "java.io.File",
      7 to "android.net.Uri.parse",
      8 to "javax.inject.Inject",
      9 to "java.util.Locale",
      9 to "java.util.Locale.ROOT",
      10 to "java.io.File",
    )
  }

  @Test
  fun `ignores look-alikes, members, comments and string literals`() {
    val tripleQuote = "\"\"\""
    val source = """
      package com.hedvig.android.feature.claim.chat
      import java.util.Locale
      import androidx.compose.runtime.Composable
      val a = androidx.compose.ui.Modifier
      val b = com.hedvig.core.common.android.Thing
      val c = com.hedvig.android.Other
      val d = hedvig.resources.Res.string
      val e = javaClass.name
      val f = foo.java.name
      val g = SomeClass::class.java
      val h = "Preview text mentioning java.util.Locale and android.net.Uri"
      val i = 'j'
      // java.time.LocalDate.now()
      /* android.net.Uri.parse("x") /* nested javax.inject.Inject */ java.io.File */
      /**
       * See [java.util.Locale] and android.content.Context.
       */
      val j = ${tripleQuote}
        java.time.LocalDate.now()
      ${tripleQuote}
      val k = "escaped \" java.io.File"
      fun `name with java.io.File`() = Unit
    """.trimIndent()
    assertThat(findFullyQualifiedReferences(source)).isEmpty()
  }

  @Test
  fun `flags imports of Android-only androidx packages`() {
    val source = """
      package a
      import androidx.activity.compose.BackHandler
      import androidx.core.net.toUri
      import androidx.appcompat.app.AppCompatActivity
      import androidx.browser.customtabs.CustomTabsIntent
      import androidx.media3.exoplayer.ExoPlayer
      import androidx.work.WorkManager
      import androidx.fragment.app.Fragment
      import androidx.exifinterface.media.ExifInterface
      import com.google.accompanist.permissions.rememberPermissionState
      import androidx.compose.ui.res.stringResource
      import androidx.lifecycle.compose.LocalLifecycleOwner
      import androidx.coreutils.Thing
      import androidx.activityx.Thing
    """.trimIndent()
    assertThat(findForbiddenImports(source)).containsExactly(
      2 to "androidx.activity.compose.BackHandler",
      3 to "androidx.core.net.toUri",
      4 to "androidx.appcompat.app.AppCompatActivity",
      5 to "androidx.browser.customtabs.CustomTabsIntent",
      6 to "androidx.media3.exoplayer.ExoPlayer",
      7 to "androidx.work.WorkManager",
      8 to "androidx.fragment.app.Fragment",
      9 to "androidx.exifinterface.media.ExifInterface",
      10 to "com.google.accompanist.permissions.rememberPermissionState",
      11 to "androidx.compose.ui.res.stringResource",
    )
  }

  @Test
  fun `flags single Android-only members of multiplatform packages and nothing else in them`() {
    val source = """
      package a
      import androidx.annotation.Keep
      import androidx.annotation.VisibleForTesting
      import androidx.compose.ui.platform.LocalContext
      import androidx.compose.ui.platform.LocalConfiguration
      import androidx.compose.ui.platform.LocalView
      import androidx.compose.ui.platform.LocalDensity
      import androidx.compose.ui.platform.LocalContextMenuRepresentation
      import androidx.compose.ui.platform.*
      import androidx.compose.ui.viewinterop.AndroidView
      @androidx.annotation.Keep
      class K
      val c = androidx.compose.ui.platform.LocalContext.current
      val d = androidx.compose.ui.platform.LocalDensity.current
    """.trimIndent()
    assertThat(findForbiddenImports(source)).containsExactly(
      2 to "androidx.annotation.Keep",
      4 to "androidx.compose.ui.platform.LocalContext",
      5 to "androidx.compose.ui.platform.LocalConfiguration",
      6 to "androidx.compose.ui.platform.LocalView",
      10 to "androidx.compose.ui.viewinterop.AndroidView",
    )
    assertThat(findFullyQualifiedReferences(source)).containsExactly(
      11 to "androidx.annotation.Keep",
      13 to "androidx.compose.ui.platform.LocalContext.current",
    )
  }

  @Test
  fun `maps a reference to the entry that forbids it`() {
    assertThat(forbiddenEntryOf("androidx.annotation.Keep")).isEqualTo("androidx.annotation.Keep")
    assertThat(forbiddenEntryOf("androidx.core.net.toUri")).isEqualTo("androidx.core")
    assertThat(forbiddenEntryOf("java.util.UUID.randomUUID")).isEqualTo("java")
    assertThat(forbiddenEntryOf("androidx.annotation.KeepKmp")).isNull()
  }
}
