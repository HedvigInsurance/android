package com.hedvig.android.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.StringOption
import com.android.tools.lint.detector.api.TextFormat
import com.android.tools.lint.detector.api.isKotlin
import com.hedvig.android.lint.config.Priorities
import com.hedvig.android.lint.util.OptionLoadingDetector
import com.hedvig.android.lint.util.StringSetLintOption
import com.hedvig.android.lint.util.sourceImplementation
import com.intellij.psi.PsiNamedElement
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UResolvable

private const val Material2DetectorIssueId = "ComposeM2Api"

/**
 * Checks and reports any usage of Material2 for compose. Material3 should be used everywhere instead.
 */
internal class Material2Detector
@JvmOverloads
constructor(
  private val allowList: StringSetLintOption = StringSetLintOption(ALLOW_LIST),
) : OptionLoadingDetector(allowList), SourceCodeScanner {

  override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf<Class<out UElement>>(
    UCallExpression::class.java,
    UQualifiedReferenceExpression::class.java,
  )

  override fun createUastHandler(context: JavaContext): UElementHandler? {
    // Only applicable to Kotlin files
    if (!isKotlin(context.uastFile?.lang)) return null
    return object : UElementHandler() {
      override fun visitCallExpression(node: UCallExpression) = checkNode(node)
      override fun visitQualifiedReferenceExpression(node: UQualifiedReferenceExpression) = checkNode(node)

      private fun checkNode(node: UResolvable) {
        val resolved = node.resolve() ?: return
        val packageName = context.evaluator.getPackage(resolved)?.qualifiedName ?: return
        if (packageName == M2Package) {
          // https://github.com/slackhq/compose-lints/issues/167
          // https://issuetracker.google.com/issues/297544175
          val resolvedName = (resolved as? PsiNamedElement)?.name
          val unmangledResolvedName = resolvedName?.substringBefore("-")
          if (unmangledResolvedName in allowList.value) {
            // Ignore any in the allow-list.
            return
          }
          context.report(
            issue = ISSUE,
            location = context.getLocation(node),
            message = ISSUE.getExplanation(TextFormat.TEXT),
          )
        }
      }
    }
  }

  companion object {
    private const val M2Package = "androidx.compose.material"

    internal val ALLOW_LIST = StringOption(
      "allowed-m2-apis",
      "A comma-separated list of APIs in androidx.compose.material that should be allowed.",
      null,
      "This property should define a comma-separated list of APIs in androidx.compose.material that should be allowed.", // ktlint-disable max-line-length
    )

    val ISSUE = Issue.create(
      id = Material2DetectorIssueId,
      briefDescription = "Using a Compose M2 API is not recommended",
      explanation = """
        Compose Material 2 (M2) is succeeded by Material 3 (M3). Please use M3 APIs.
        See https://slackhq.github.io/compose-lints/rules/#use-material-3 for more information.
      """,
      category = Category.CORRECTNESS,
      priority = Priorities.NORMAL,
      severity = Severity.ERROR,
      implementation = sourceImplementation<Material2Detector>(),
    )
      .setOptions(listOf(ALLOW_LIST))
      .setEnabledByDefault(true)
  }
}
