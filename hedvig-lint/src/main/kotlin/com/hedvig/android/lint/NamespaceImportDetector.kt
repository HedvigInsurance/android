package com.hedvig.android.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.StringOption
import com.android.tools.lint.detector.api.isKotlin
import com.hedvig.android.lint.config.Priorities
import com.hedvig.android.lint.util.OptionLoadingDetector
import com.hedvig.android.lint.util.StringSetLintOption
import com.hedvig.android.lint.util.sourceImplementation
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UImportStatement

private const val NamespaceImportDetectorIssueId = "NamespaceImport"

/**
 * Reports imports that shorten a qualified reference past the point where the short name still says
 * what it is, such as `import hedvig.resources.Res.string` turning `Res.string.FOO` into `string.FOO`.
 *
 * Importing a type is fine, so a sealed subclass or enum entry (`HomeUiState.Success`) is left alone.
 * Importing a member off a class owner is not, because the owner is what gave the name its meaning.
 * A capitalized name that is still meaningless on its own, such as `Clock.System`, is covered by
 * [DENY_LIST].
 *
 * The `hedvig:namespace-import` ktlint rule enforces the same policy across every source set and is
 * the mechanism of record. This check is additive: it resolves the owner instead of guessing from the
 * shape of the import path, and it reports inline in the IDE while the import is being typed.
 * [DEFAULT_DENIED_IMPORTS] therefore has to stay in step with that rule's own denied list.
 *
 * It cannot subsume the ktlint rule, because AGP's KMP library plugin registers no task that runs
 * Android Lint (https://issuetracker.google.com/issues/246751841), which puts every KMP module, the
 * design system among them, out of reach. Should that gain a runnable lint task, this check becomes
 * able to cover the whole repository and the ktlint rule becomes the redundant half of the pair.
 */
internal class NamespaceImportDetector
  @JvmOverloads
  constructor(
    private val extraDeniedImports: StringSetLintOption = StringSetLintOption(DENY_LIST),
    private val allowedImports: StringSetLintOption = StringSetLintOption(ALLOW_LIST),
  ) : OptionLoadingDetector(extraDeniedImports, allowedImports), SourceCodeScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf<Class<out UElement>>(
      UImportStatement::class.java,
    )

    override fun createUastHandler(context: JavaContext): UElementHandler? {
      val language = context.uastFile?.lang ?: return null
      if (!isKotlin(language)) return null
      return object : UElementHandler() {
        override fun visitImportStatement(node: UImportStatement) {
          if (node.isOnDemand) return // Wildcards are owned by ktlint's no-wildcard-imports.
          val importText = node.sourcePsi?.text ?: return
          // An alias is a deliberate act of renaming, and gives the use site a name of its own.
          if (importText.contains(" as ")) return
          val qualifiedName = importText.removePrefix("import").trim()

          val importedName = qualifiedName.substringAfterLast('.')
          val ownerPath = qualifiedName.substringBeforeLast('.', "")
          val ownerName = ownerPath.substringAfterLast('.')
          if (importedName.isEmpty() || ownerName.isEmpty()) return
          // A lowercase owner is a package, so this is a plain top-level import.
          if (!ownerName.first().isUpperCase()) return
          // `Duration.Companion.seconds` and friends exist to enable the `5.seconds` receiver idiom.
          if (ownerName == "Companion") return
          // A capitalized path segment is not proof of a class: `platform.Foundation` is a package.
          if (context.evaluator.findClass(ownerPath) == null) return
          if (qualifiedName in allowedImports.value) return

          val importsAMember = importedName.first().isLowerCase()
          val isDeniedByName = qualifiedName in DEFAULT_DENIED_IMPORTS ||
            qualifiedName in extraDeniedImports.value
          if (!importsAMember && !isDeniedByName) return

          context.report(
            issue = ISSUE,
            location = context.getLocation(node),
            message = "Import `$ownerName` and write `$ownerName.$importedName` at the use site. " +
              "On its own, `$importedName` no longer says what it is.",
          )
        }
      }
    }

    companion object {
      /**
       * Imports whose final segment is capitalized, so they read as a type, but which still leave
       * nothing meaningful behind at the use site. Kept in step with `NamespaceImportRule`, which
       * applies the same list where this check cannot run.
       */
      private val DEFAULT_DENIED_IMPORTS = setOf(
        "kotlin.time.Clock.System",
      )

      internal val DENY_LIST = StringOption(
        "denied-member-imports",
        "A comma-separated list of fully qualified imports to reject in addition to the built-in ones.",
        null,
        "This property should define a comma-separated list of fully qualified imports that must " +
          "never be used, even though their final segment is capitalized",
      )

      internal val ALLOW_LIST = StringOption(
        "allowed-member-imports",
        "A comma-separated list of fully qualified member imports that should be allowed.",
        null,
        "This property should define a comma-separated list of fully qualified member imports that " +
          "are allowed to shorten their receiver away",
      )

      val ISSUE = Issue.create(
        id = NamespaceImportDetectorIssueId,
        briefDescription = "Importing a member hides the receiver that carries its meaning",
        explanation = """
        Import the type, never the namespace. An import may shorten a qualified reference only when \
        the short name still says what it is to someone reading that line cold.

        Sealed subclasses and enum entries pass that test, so `HomeUiState.Success` may be imported \
        as `Success`. Members reached through a receiver that carries the meaning do not: \
        `Res.string.FOO` must not become `string.FOO`, and `Clock.System.now()` must not become \
        `System.now()`, which additionally reads as `java.lang.System`.
      """,
        category = Category.CORRECTNESS,
        priority = Priorities.NORMAL,
        severity = Severity.ERROR,
        implementation = sourceImplementation<NamespaceImportDetector>(),
      )
        .setOptions(listOf(DENY_LIST, ALLOW_LIST))
        .setEnabledByDefault(true)
    }
  }
