package com.hedvig.android

import assertk.assertThat
import assertk.assertions.containsExactly
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
}
