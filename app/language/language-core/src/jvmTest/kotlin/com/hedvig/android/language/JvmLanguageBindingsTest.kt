package com.hedvig.android.language

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.DesktopDataDirectory
import com.hedvig.android.logger.TestLogcatLoggingRule
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertIs
import org.junit.Rule

@DependencyGraph(AppScope::class)
internal interface LanguageTestGraph {
  val languageService: LanguageService

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides @DesktopDataDirectory dataDirectory: File,
    ): LanguageTestGraph
  }
}

class JvmLanguageBindingsTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `the language service resolves to the JVM implementation`() {
    val graph = createGraphFactory<LanguageTestGraph.Factory>().create(createTempDirectory("language-graph").toFile())

    assertIs<JvmLanguageService>(graph.languageService)
  }
}
