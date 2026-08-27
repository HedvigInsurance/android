package com.hedvig.android.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.Test

class NamespaceImportDetectorTest : LintDetectorTest() {
  override fun getDetector(): Detector = NamespaceImportDetector()

  override fun getIssues(): List<Issue> = listOf(NamespaceImportDetector.ISSUE)

  private val resources = kotlin(
    """
    package com.example.res
    object Res {
      object string {
        const val GREETING = "hi"
      }
      object drawable
    }
    """,
  ).indented()

  private val designSystem = kotlin(
    """
    package com.example.ds
    object TooltipDefaults {
      val defaultStyle: Int = 0
    }
    """,
  ).indented()

  private val uiState = kotlin(
    """
    package com.example.ui
    sealed interface HomeUiState {
      object Success : HomeUiState
      object Loading : HomeUiState
    }
    """,
  ).indented()

  private val duration = kotlin(
    """
    package com.example.time
    class Duration {
      companion object {
        val seconds: Int = 1
      }
    }
    """,
  ).indented()

  // Kotlin/Native interop packages are capitalized, which a purely textual check misreads as a class.
  private val capitalizedPackage = kotlin(
    """
    package platform.Foundation
    fun systemLocale(): String = ""
    """,
  ).indented()

  @Test
  fun testReportsLowercaseMemberImport() {
    lint()
      .files(
        resources,
        kotlin(
          """
          package com.example.app
          import com.example.res.Res.string
          fun greet() = string.GREETING
          """,
        ).indented(),
      )
      .issues(NamespaceImportDetector.ISSUE)
      .run()
      .expectErrorCount(1)
      .expectContains("Import Res and write Res.string at the use site")
  }

  @Test
  fun testReportsEveryOffendingImportInAFile() {
    lint()
      .files(
        resources,
        designSystem,
        kotlin(
          """
          package com.example.app
          import com.example.ds.TooltipDefaults.defaultStyle
          import com.example.res.Res.drawable
          import com.example.res.Res.string
          fun use() = listOf(string, drawable, defaultStyle)
          """,
        ).indented(),
      )
      .issues(NamespaceImportDetector.ISSUE)
      .run()
      .expectErrorCount(3)
  }

  @Test
  fun testAllowsSealedSubclassImport() {
    lint()
      .files(
        uiState,
        kotlin(
          """
          package com.example.app
          import com.example.ui.HomeUiState
          import com.example.ui.HomeUiState.Loading
          import com.example.ui.HomeUiState.Success
          fun describe(state: HomeUiState) = when (state) {
            Success -> "ok"
            Loading -> "wait"
          }
          """,
        ).indented(),
      )
      .issues(NamespaceImportDetector.ISSUE)
      .run()
      .expectClean()
  }

  @Test
  fun testAllowsCompanionExtensionImport() {
    lint()
      .files(
        duration,
        kotlin(
          """
          package com.example.app
          import com.example.time.Duration
          import com.example.time.Duration.Companion.seconds
          fun timeout() = Duration.seconds
          """,
        ).indented(),
      )
      .issues(NamespaceImportDetector.ISSUE)
      .run()
      .expectClean()
  }

  @Test
  fun testIgnoresCapitalizedPackage() {
    lint()
      .files(
        capitalizedPackage,
        kotlin(
          """
          package com.example.app
          import platform.Foundation.systemLocale
          fun locale() = systemLocale()
          """,
        ).indented(),
      )
      .issues(NamespaceImportDetector.ISSUE)
      .run()
      .expectClean()
  }

  @Test
  fun testAllowsAliasedImport() {
    lint()
      .files(
        resources,
        kotlin(
          """
          package com.example.app
          import com.example.res.Res.string as StringResources
          fun greet() = StringResources.GREETING
          """,
        ).indented(),
      )
      .issues(NamespaceImportDetector.ISSUE)
      .run()
      .expectClean()
  }

  @Test
  fun testIgnoresWildcardImport() {
    lint()
      .files(
        resources,
        kotlin(
          """
          package com.example.app
          import com.example.res.*
          fun greet() = Res.string.GREETING
          """,
        ).indented(),
      )
      .issues(NamespaceImportDetector.ISSUE)
      .run()
      .expectClean()
  }
}
