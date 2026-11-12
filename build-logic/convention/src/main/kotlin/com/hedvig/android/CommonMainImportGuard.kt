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
      findForbiddenImports(file.readText()).map { (line, import) -> "$path:$line: $import" }
    }
    if (violations.isNotEmpty()) {
      throw GradleException(
        "commonMain must not import java.*, javax.* or android.*. Move the code behind a platform seam, or add the " +
          "file to config/commonMain-import-allowlist.txt with a reason.\n" + violations.joinToString("\n"),
      )
    }
    marker.get().asFile.writeText("OK\n")
  }
}
