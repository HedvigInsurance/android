package com.hedvig.android

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
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
}
