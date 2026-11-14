package com.hedvig.android

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin

/**
 * Packages that `commonMain` of a module shared with iOS must not import: Compose UI and the libraries built on it,
 * the design system and compose resources. Their code stays in `jvmAndAndroidMain`, so the iOS framework links none
 * of it. `androidx.compose.runtime` stays allowed, since presenters are written against it.
 */
val forbiddenSharedCommonMainNames: List<String> = listOf(
  "androidx.compose.ui",
  "androidx.compose.foundation",
  "androidx.compose.material",
  "androidx.compose.material3",
  "com.hedvig.android.design.system",
  "hedvig.resources",
  "org.jetbrains.compose.resources",
  "coil",
  "coil3",
  "androidx.paging.compose",
  "androidx.navigation3.ui",
)

private val forbiddenSharedProjectPaths = setOf(
  ":core-resources",
  ":navigation-compose",
  ":compose-ui",
  ":core-icons",
  ":placeholder",
  ":pullrefresh",
)

private val forbiddenSharedGroups = setOf(
  "org.jetbrains.compose.ui",
  "org.jetbrains.compose.foundation",
  "org.jetbrains.compose.material",
  "org.jetbrains.compose.material3",
  "org.jetbrains.compose.components",
  "io.coil-kt",
  "io.coil-kt.coil3",
)

private val forbiddenSharedModules = setOf(
  "androidx.paging:paging-compose",
  "org.jetbrains.androidx.navigation3:navigation3-ui",
)

/**
 * Whether a module shared with iOS may not declare [dependency] in `commonMain`. [dependency] is a project path
 * (`:design-system-hedvig`) or `group:name`, and [projectDirectory] is a project dependency's directory relative to
 * the root project.
 */
fun isForbiddenSharedCommonMainDependency(dependency: String, projectDirectory: String? = null): Boolean {
  if (dependency.startsWith(":")) {
    return dependency in forbiddenSharedProjectPaths ||
      dependency.startsWith(":design-system-") ||
      projectDirectory?.replace('\\', '/')?.startsWith("app/ui/") == true
  }
  if (dependency in forbiddenSharedModules) return true
  return dependency.substringBefore(":") in forbiddenSharedGroups
}

private val sharedCommonMainDependencyConfigurations = listOf("commonMainImplementation", "commonMainApi")

/**
 * Registers this module's `checkCommonMainImports`, which fails on Compose UI imports in `src/commonMain` and on UI
 * dependencies declared for `commonMain`, and makes the module's `check` depend on it. It shares its name with the
 * root task so that `./gradlew checkCommonMainImports` runs both.
 */
internal fun Project.registerSharedCommonMainGuard() {
  val declared = provider {
    sharedCommonMainDependencyConfigurations
      .mapNotNull { configurations.findByName(it) }
      .flatMap { it.dependencies }
      .mapNotNull { dependency ->
        when (dependency) {
          is ProjectDependency -> {
            val directory = rootProject.findProject(dependency.path)?.projectDir
              ?.relativeTo(rootDir)?.invariantSeparatorsPath
            dependency.path to directory.orEmpty()
          }
          is ExternalModuleDependency -> "${dependency.group}:${dependency.name}" to ""
          else -> null
        }
      }
      .distinct()
  }
  val guard = tasks.register<CheckSharedCommonMainTask>("checkCommonMainImports") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Fails when commonMain imports or declares Compose UI, the design system or compose resources."
    sources.from(
      fileTree("src/commonMain") {
        include("**/*.kt")
      },
    )
    declaredDependencies.set(declared.map { pairs -> pairs.map { it.first } })
    projectDirectories.set(declared.map { pairs -> pairs.filter { it.second.isNotEmpty() }.toMap() })
    rootDirectory.set(rootProject.layout.projectDirectory)
    marker.set(layout.buildDirectory.file("checkCommonMainImports/ok"))
  }
  tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure { dependsOn(guard) }
}

abstract class CheckSharedCommonMainTask : DefaultTask() {
  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val sources: ConfigurableFileCollection

  /** Project paths and `group:name` coordinates declared in `commonMainImplementation` and `commonMainApi`. */
  @get:Input
  abstract val declaredDependencies: ListProperty<String>

  /** Root-relative directory of each project path in [declaredDependencies]. */
  @get:Input
  abstract val projectDirectories: MapProperty<String, String>

  @get:Internal
  abstract val rootDirectory: DirectoryProperty

  @get:OutputFile
  abstract val marker: RegularFileProperty

  @TaskAction
  fun check() {
    val root = rootDirectory.get().asFile
    val importViolations = sources.files.sorted().flatMap { file ->
      val path = file.relativeTo(root).invariantSeparatorsPath
      findForbiddenImports(file.readText(), forbiddenNames = forbiddenSharedCommonMainNames)
        .map { (line, import) -> "$path:$line: $import" }
    }
    val directories = projectDirectories.get()
    val dependencyViolations = declaredDependencies.get()
      .filter { isForbiddenSharedCommonMainDependency(it, directories[it]) }
      .map { "commonMain declares $it" }
    val violations = importViolations + dependencyViolations
    if (violations.isNotEmpty()) {
      throw GradleException(
        "commonMain of a module shared with iOS must not use Compose UI, the design system or compose " +
          "resources: move that code and its dependencies to jvmAndAndroidMain.\n" +
          violations.joinToString("\n"),
      )
    }
    marker.get().asFile.writeText("OK\n")
  }
}
