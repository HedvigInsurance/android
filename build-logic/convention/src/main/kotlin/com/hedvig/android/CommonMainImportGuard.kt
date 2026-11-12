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

private val forbiddenImport = Regex("""^\s*import\s+((?:java|javax|android)\.[\w.`*]+)""")

/**
 * Returns the 1-based line number and imported name of every `java.*`, `javax.*` or `android.*` import in [source].
 */
fun findForbiddenImports(source: String): List<Pair<Int, String>> {
  return source.lineSequence().mapIndexedNotNull { index, line ->
    forbiddenImport.find(line)?.let { index + 1 to it.groupValues[1] }
  }.toList()
}

private val fullyQualifiedReference = Regex("""(?<![\w.$])(?:java|javax|android)(?:\.[A-Za-z]\w*)(?:\.\w+)*""")

/**
 * Returns the 1-based line number and dotted name of every fully qualified `java.*`, `javax.*` or `android.*`
 * reference in [source] outside `import` and `package` lines, comments, string literals and backticked names.
 * Known misses: code inside string templates (`"${java.util.UUID.randomUUID()}"`), and a reference that is
 * split across lines or written with spaces around its dots.
 */
fun findFullyQualifiedReferences(source: String): List<Pair<Int, String>> {
  return blankCommentsAndLiterals(source).lineSequence().flatMapIndexed { index, line ->
    val trimmed = line.trimStart()
    if (trimmed.startsWith("import ") || trimmed.startsWith("package ")) {
      emptySequence()
    } else {
      fullyQualifiedReference.findAll(line).map { index + 1 to it.value }
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
 * Registers `checkCommonMainImports` on the root project. `commonMain` of a module with only JVM and Android targets
 * still compiles against JVM API, so this is what keeps such code portable to iOS.
 */
fun Project.registerCommonMainImportGuard() {
  check(this == rootProject) { "checkCommonMainImports is a root project task" }
  tasks.register<CheckCommonMainImportsTask>("checkCommonMainImports") {
    group = "verification"
    description = "Fails when a commonMain Kotlin file imports java.*, javax.* or android.*."
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
}

abstract class CheckCommonMainImportsTask : DefaultTask() {
  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val sources: ConfigurableFileCollection

  /** Root-relative paths, one per line, each preceded by a `#` comment giving the reason. */
  @get:InputFile
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val allowList: RegularFileProperty

  @get:Internal
  abstract val rootDirectory: DirectoryProperty

  @get:OutputFile
  abstract val marker: RegularFileProperty

  @TaskAction
  fun check() {
    val allowed = allowList.get().asFile.readLines()
      .map { it.trim() }
      .filter { it.isNotEmpty() && !it.startsWith("#") }
      .toSet()
    val root = rootDirectory.get().asFile
    val violations = sources.files.sorted().flatMap { file ->
      val path = file.relativeTo(root).invariantSeparatorsPath
      if (path in allowed) return@flatMap emptyList()
      val text = file.readText()
      (findForbiddenImports(text) + findFullyQualifiedReferences(text))
        .sortedBy { it.first }
        .map { (line, reference) -> "$path:$line: $reference" }
    }
    if (violations.isNotEmpty()) {
      throw GradleException(
        "commonMain must not import or reference java.*, javax.* or android.*. Move the code behind a platform " +
          "seam, or add the file to config/commonMain-import-allowlist.txt with a reason.\n" +
          violations.joinToString("\n"),
      )
    }
    marker.get().asFile.writeText("OK\n")
  }
}
