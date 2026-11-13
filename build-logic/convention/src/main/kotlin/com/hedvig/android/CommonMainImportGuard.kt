package com.hedvig.android

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin

/**
 * Packages, and single declarations of otherwise multiplatform packages, that `commonMain` must not use. `java`,
 * `javax` and `android` are JVM or Android API. The `androidx` and Accompanist entries are artifacts that publish
 * only an Android variant, or declarations that exist only in the Android variant of a multiplatform artifact.
 */
val forbiddenCommonMainNames: List<String> = listOf(
  "java",
  "javax",
  "android",
  "androidx.activity",
  "androidx.appcompat",
  "androidx.browser",
  // The whole prefix is forbidden because every Google artifact in the `androidx.core` group (`core`, `core-ktx`,
  // `core-splashscreen`, `core-viewtree` and the rest) publishes only an Android variant. JetBrains publishes
  // multiplatform artifacts under the same import path: `org.jetbrains.androidx.core:core-bundle` (package
  // `androidx.core.bundle`) and `org.jetbrains.androidx.core:core-uri` (package `androidx.core.uri`), each with
  // Android, JVM, iOS, other native, JS and Wasm variants (checked on Maven Central on 2026-11-13, latest
  // 1.1.0-alpha03). An import from one of those is valid in commonMain, and adopting one means adding its
  // sub-package to [allowedCommonMainSubPackages], which is the correct and expected fix.
  "androidx.core",
  "androidx.exifinterface",
  "androidx.fragment",
  "androidx.media3",
  "androidx.work",
  "com.google.accompanist",
  "androidx.annotation.Keep",
  "androidx.compose.ui.platform.LocalConfiguration",
  "androidx.compose.ui.platform.LocalContext",
  "androidx.compose.ui.platform.LocalLifecycleOwner",
  "androidx.compose.ui.platform.LocalResources",
  "androidx.compose.ui.platform.LocalSavedStateRegistryOwner",
  "androidx.compose.ui.platform.LocalView",
  "androidx.compose.ui.res",
  "androidx.compose.ui.viewinterop.AndroidView",
)

/**
 * Sub-packages inside a [forbiddenCommonMainNames] entry that `commonMain` may use, because a multiplatform artifact
 * publishes them. For example, adopting `org.jetbrains.androidx.core:core-bundle` means adding
 * `"androidx.core.bundle"` here.
 */
val allowedCommonMainSubPackages: List<String> = emptyList()

/**
 * Returns the entry of [forbiddenCommonMainNames] that [name] is, or lies inside, or null. A name inside one of
 * [allowedSubPackages] is never forbidden.
 */
fun forbiddenEntryOf(name: String, allowedSubPackages: List<String> = allowedCommonMainSubPackages): String? {
  val plain = name.replace("`", "")
  fun String.isOrIsInside(prefix: String) = this == prefix || startsWith("$prefix.")
  if (allowedSubPackages.any { plain.isOrIsInside(it) }) return null
  return forbiddenCommonMainNames.firstOrNull { plain.isOrIsInside(it) }
}

private val importedName = Regex("""^\s*import\s+([\w.`*]+)""")

/**
 * Returns the 1-based line number and imported name of every import in [source] that names a
 * [forbiddenCommonMainNames] entry or something inside it.
 */
fun findForbiddenImports(
  source: String,
  allowedSubPackages: List<String> = allowedCommonMainSubPackages,
): List<Pair<Int, String>> {
  return source.lineSequence().mapIndexedNotNull { index, line ->
    importedName.find(line)?.groupValues?.get(1)
      ?.takeIf { forbiddenEntryOf(it, allowedSubPackages) != null }
      ?.let { index + 1 to it }
  }.toList()
}

private val fullyQualifiedReference =
  Regex("""(?<![\w.$])(?:java|javax|android|androidx|com)(?:\.[A-Za-z]\w*)(?:\.\w+)*""")

/**
 * Returns the 1-based line number and dotted name of every fully qualified reference in [source] to a
 * [forbiddenCommonMainNames] entry, outside `import` and `package` lines, comments, string literals and backticked
 * names. Known misses: code inside string templates (`"${java.util.UUID.randomUUID()}"`), and a reference that is
 * split across lines or written with spaces around its dots.
 */
fun findFullyQualifiedReferences(source: String): List<Pair<Int, String>> {
  return blankCommentsAndLiterals(source).lineSequence().flatMapIndexed { index, line ->
    val trimmed = line.trimStart()
    if (trimmed.startsWith("import ") || trimmed.startsWith("package ")) {
      emptySequence()
    } else {
      fullyQualifiedReference.findAll(line)
        .filter { forbiddenEntryOf(it.value) != null }
        .map { index + 1 to it.value }
    }
  }.toList()
}

/**
 * Replaces every character inside comments, string and char literals and backticked names with a space, keeping
 * line breaks so line numbers stay the same.
 */
private fun blankCommentsAndLiterals(source: String): String {
  val out = StringBuilder(source.length)
  var i = 0
  fun blank(until: Int) {
    while (i < until && i < source.length) {
      out.append(if (source[i] == '\n') '\n' else ' ')
      i++
    }
  }
  while (i < source.length) {
    when {
      source.startsWith("//", i) -> {
        val end = source.indexOf('\n', i)
        blank(if (end == -1) source.length else end)
      }
      source.startsWith("/*", i) -> {
        var depth = 0
        var j = i
        while (j < source.length) {
          if (source.startsWith("/*", j)) {
            depth++
            j += 2
          } else if (source.startsWith("*/", j)) {
            depth--
            j += 2
            if (depth == 0) break
          } else {
            j++
          }
        }
        blank(j)
      }
      source.startsWith("\"\"\"", i) -> {
        var end = source.indexOf("\"\"\"", i + 3)
        end = if (end == -1) source.length else end + 3
        while (end < source.length && source[end] == '"') end++
        blank(end)
      }
      source[i] == '"' || source[i] == '\'' || source[i] == '`' -> {
        val quote = source[i]
        var j = i + 1
        while (j < source.length && source[j] != quote && source[j] != '\n') {
          j += if (source[j] == '\\' && quote != '`') 2 else 1
        }
        blank(minOf(j + 1, source.length))
      }
      else -> {
        out.append(source[i])
        i++
      }
    }
  }
  return out.toString()
}

/**
 * Registers `checkCommonMainImports` on the root project and makes the root `check` depend on it. `commonMain` of a
 * module with JVM and Android targets compiles against Android API in the Android build, and `java.*` compiles on
 * both, so this text check is what reports such code before a JVM or iOS compile does.
 */
fun Project.registerCommonMainImportGuard() {
  check(this == rootProject) { "checkCommonMainImports is a root project task" }
  val guard = tasks.register<CheckCommonMainImportsTask>("checkCommonMainImports") {
    group = "verification"
    description = "Fails when a commonMain Kotlin file uses java.*, javax.*, android.* or Android-only androidx API."
    for (dir in listOf("app", "micro-apps")) {
      sources.from(
        fileTree(dir) {
          include("**/src/commonMain/**/*.kt")
          exclude("**/build/**")
        },
      )
    }
    allowList.set(layout.projectDirectory.file("config/commonMain-import-allowlist.txt"))
    rootDirectory.set(layout.projectDirectory)
    marker.set(layout.buildDirectory.file("checkCommonMainImports/ok"))
  }
  val check = if (LifecycleBasePlugin.CHECK_TASK_NAME in tasks.names) {
    tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME)
  } else {
    tasks.register(LifecycleBasePlugin.CHECK_TASK_NAME) {
      group = LifecycleBasePlugin.VERIFICATION_GROUP
      description = "Runs all checks."
    }
  }
  check.configure { dependsOn(guard) }
}

abstract class CheckCommonMainImportsTask : DefaultTask() {
  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val sources: ConfigurableFileCollection

  /**
   * One entry per line, each preceded by a `#` comment giving the reason: a root-relative path skips the whole file,
   * and a path followed by a [forbiddenCommonMainNames] entry allows only that entry in the file.
   */
  @get:InputFile
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val allowList: RegularFileProperty

  @get:Internal
  abstract val rootDirectory: DirectoryProperty

  @get:OutputFile
  abstract val marker: RegularFileProperty

  @TaskAction
  fun check() {
    val entries = allowList.get().asFile.readLines()
      .map { it.trim() }
      .filter { it.isNotEmpty() && !it.startsWith("#") }
      .map { it.split(Regex("""\s+"""), limit = 2) }
    val wholeFiles = entries.filter { it.size == 1 }.map { it[0] }.toSet()
    val allowedNames = entries.filter { it.size == 2 }.groupBy({ it[0] }, { it[1] })
    val root = rootDirectory.get().asFile
    val violations: List<Pair<String, String>> = sources.files.sorted().flatMap { file ->
      val path = file.relativeTo(root).invariantSeparatorsPath
      if (path in wholeFiles) return@flatMap emptyList()
      val allowedHere = allowedNames[path].orEmpty()
      val text = file.readText()
      (findForbiddenImports(text) + findFullyQualifiedReferences(text))
        .filter { (_, reference) -> forbiddenEntryOf(reference) !in allowedHere }
        .sortedBy { it.first }
        .map { (line, reference) -> "$path:$line" to reference }
    }
    if (violations.isNotEmpty()) {
      throw GradleException(commonMainFailureMessage(violations))
    }
    marker.get().asFile.writeText("OK\n")
  }
}

/**
 * Returns the failure message for [violations], each a `path:line` location and the reference found there. When a
 * reference is under `androidx.core`, the message also points at the sub-package exemption for multiplatform
 * artifacts that share that import path.
 */
fun commonMainFailureMessage(violations: List<Pair<String, String>>): String {
  val androidxCoreHint = if (violations.any { (_, reference) -> forbiddenEntryOf(reference) == "androidx.core" }) {
    "If an androidx.core import comes from a multiplatform artifact (such as the JetBrains " +
      "org.jetbrains.androidx.core ports), the fix is to add its sub-package to allowedCommonMainSubPackages in " +
      "CommonMainImportGuard.kt, not to move the code.\n"
  } else {
    ""
  }
  return "commonMain must not use JVM or Android-only API: move the code to androidMain (or jvmAndAndroidMain) " +
    "behind an expect declaration or an interface bound per platform, as the \"Critical Metro KMP rule\" " +
    "under \"Dependency Injection\" in CLAUDE.md describes, or list it in " +
    "config/commonMain-import-allowlist.txt with a reason.\n" +
    androidxCoreHint +
    violations.joinToString("\n") { (location, reference) -> "$location: $reference" }
}
