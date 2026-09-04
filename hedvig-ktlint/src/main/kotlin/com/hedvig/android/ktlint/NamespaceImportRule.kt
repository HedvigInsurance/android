package com.hedvig.android.ktlint

import com.pinterest.ktlint.rule.engine.core.api.ElementType
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Reports imports that shorten a qualified reference past the point where the short name still says
 * what it is, such as `import hedvig.resources.Res.string` turning `Res.string.FOO` into `string.FOO`.
 *
 * This runs through kotlinter on every source set, `commonMain` and `iosMain` included, which is why
 * the rule lives here rather than as an Android Lint check: AGP's KMP library plugin registers no
 * task that runs Android Lint (https://issuetracker.google.com/issues/246751841), so a lint check
 * cannot see a KMP module at all, and the design system is one.
 *
 * The cost of that reach is precision. ktlint has no type resolution, so an owner is recognized by
 * the shape of the import path rather than by resolving it, and [CAPITALIZED_PACKAGES] carries the
 * exceptions. Trading the heuristic for a resolved owner across the whole codebase needs an analyzer
 * that both resolves types and runs on every source set: Android Lint, once the issue above is
 * fixed, or detekt with type resolution.
 */
internal class NamespaceImportRule :
  Rule(
    ruleId = RuleId("$CUSTOM_RULE_SET_ID:namespace-import"),
    about = About(
      maintainer = "Hedvig",
      repositoryUrl = "https://github.com/HedvigInsurance/android",
      issueTrackerUrl = "https://github.com/HedvigInsurance/android/issues",
    ),
  ) {
  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
  ) {
    if (node.elementType != ElementType.IMPORT_DIRECTIVE) return
    val text = node.text
    // An alias is a deliberate act of renaming, and gives the use site a name of its own.
    if (text.contains(" as ")) return
    val qualifiedName = text.removePrefix("import").trim()
    if (qualifiedName.isEmpty() || qualifiedName.endsWith("*")) return

    val importedName = qualifiedName.substringAfterLast('.')
    val ownerPath = qualifiedName.substringBeforeLast('.', "")
    val ownerName = ownerPath.substringAfterLast('.')
    if (importedName.isEmpty() || ownerName.isEmpty()) return
    if (!ownerName.first().isUpperCase()) return
    // `Duration.Companion.seconds` and friends exist to enable the `5.seconds` receiver idiom.
    if (ownerName == "Companion") return
    if (CAPITALIZED_PACKAGES.any { qualifiedName.startsWith(it) }) return

    val importsAMember = importedName.first().isLowerCase()
    if (!importsAMember && qualifiedName !in DENIED_IMPORTS) return

    emit(
      node.startOffset,
      "Import $ownerName and write $ownerName.$importedName at the use site. " +
        "On its own, $importedName no longer says what it is.",
      false,
    )
  }

  private companion object {
    /**
     * Kotlin/Native interop packages are capitalized after the framework they bind, so their
     * top-level declarations look identical to members of a class.
     */
    val CAPITALIZED_PACKAGES = listOf("platform.")

    /** Imports that read as a type but still leave nothing meaningful at the use site. */
    val DENIED_IMPORTS = setOf("kotlin.time.Clock.System")
  }
}
