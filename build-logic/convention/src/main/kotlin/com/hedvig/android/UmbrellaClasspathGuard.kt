package com.hedvig.android

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin

private val umbrellaClasspathConfigurations = listOf(
  "iosArm64CompileKlibraries",
  "iosSimulatorArm64CompileKlibraries",
)

/**
 * Returns the `group:name` coordinates in [coordinates] that must not reach the iOS framework: Skiko, which links
 * Skia, ICU and HarfBuzz into the app, and compose resources, which pulls Skiko in through Compose foundation.
 */
fun findForbiddenUmbrellaArtifacts(coordinates: Collection<String>): List<String> = coordinates.filter {
  val group = it.substringBefore(":")
  val name = it.substringAfter(":")
  group == "org.jetbrains.skiko" ||
    (group == "org.jetbrains.compose.components" && name.startsWith("components-resources"))
}.distinct().sorted()

/**
 * Registers `checkUmbrellaClasspath`, which fails when the iOS compile classpath of this module contains an artifact
 * reported by [findForbiddenUmbrellaArtifacts], and makes the module's `check` depend on it.
 */
fun Project.registerUmbrellaClasspathGuard() {
  val guard = tasks.register<CheckUmbrellaClasspathTask>("checkUmbrellaClasspath") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Fails when the iOS framework's compile classpath contains Skiko or compose resources."
    for (name in umbrellaClasspathConfigurations) {
      val configuration = configurations.named(name)
      resolvedModules.put(
        name,
        configuration.flatMap { it.incoming.resolutionResult.rootComponent }.map { it.moduleCoordinates() },
      )
    }
    projectPath.set(this@registerUmbrellaClasspathGuard.path)
    marker.set(layout.buildDirectory.file("checkUmbrellaClasspath/ok"))
  }
  tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure { dependsOn(guard) }
}

private fun ResolvedComponentResult.moduleCoordinates(): List<String> {
  val seen = mutableSetOf<ResolvedComponentResult>()
  val pending = ArrayDeque(listOf(this))
  val coordinates = mutableListOf<String>()
  while (pending.isNotEmpty()) {
    val component = pending.removeFirst()
    if (!seen.add(component)) continue
    val id = component.id
    if (id is ModuleComponentIdentifier) coordinates += "${id.group}:${id.module}"
    component.dependencies.filterIsInstance<ResolvedDependencyResult>().forEach { pending += it.selected }
  }
  return coordinates
}

abstract class CheckUmbrellaClasspathTask : DefaultTask() {
  /** Every resolved `group:name` per checked configuration. */
  @get:Input
  abstract val resolvedModules: MapProperty<String, List<String>>

  @get:Internal
  abstract val projectPath: Property<String>

  @get:OutputFile
  abstract val marker: RegularFileProperty

  @TaskAction
  fun check() {
    val violations = resolvedModules.get().flatMap { (configuration, coordinates) ->
      findForbiddenUmbrellaArtifacts(coordinates).map { "$configuration contains $it" }
    }
    if (violations.isNotEmpty()) {
      throw GradleException(
        "The iOS framework must not link Skiko or compose resources. Find the module that brings it in with " +
          "`./gradlew ${projectPath.get()}:dependencies --configuration <configuration>` and keep its UI code out of " +
          "the shared modules.\n" + violations.joinToString("\n"),
      )
    }
    marker.get().asFile.writeText("OK\n")
  }
}
