package com.hedvig.android.language

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.TestLogcatLoggingRule
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph
import kotlin.test.Test
import kotlin.test.assertIs
import org.junit.Rule

@DependencyGraph(AppScope::class)
internal interface LanguageTestGraph {
  val languageService: LanguageService
}

class JvmLanguageBindingsTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `the language service resolves to the JVM implementation`() {
    assertIs<JvmLanguageService>(createGraph<LanguageTestGraph>().languageService)
  }
}
