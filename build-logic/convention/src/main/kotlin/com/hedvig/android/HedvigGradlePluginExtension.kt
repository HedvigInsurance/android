package com.hedvig.android

import androidx.room.gradle.RoomExtension
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.apollographql.apollo.gradle.api.ApolloExtension
import com.apollographql.apollo.gradle.api.Service
import com.apollographql.apollo.gradle.internal.ApolloDownloadSchemaTask
import java.io.File
import javax.inject.Inject
import kotlin.takeIf
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

abstract class HedvigGradlePluginExtension @Inject constructor(
  private val project: Project,
  private val libs: LibrariesForLibs,
  private val pluginManager: PluginManager,
) {
  private val apolloHandler: ApolloHandler =
    project.objects.newInstance<ApolloHandler>()
  private val apolloSchemaHandler: ApolloSchemaHandler =
    project.objects.newInstance<ApolloSchemaHandler>()
  private val composeHandler: ComposeHandler =
    project.objects.newInstance<ComposeHandler>()
  private val androidResHandler: AndroidResHandler =
    project.objects.newInstance<AndroidResHandler>()
  private val roomHandler: RoomHandler =
    project.objects.newInstance<RoomHandler>()

  fun apolloSchema(apolloServiceAction: Action<Service>) {
    apolloSchemaHandler.configure(project, apolloServiceAction)
  }

  /**
   * [packageName] is the package that the generated apollo classes will have for this module.
   */
  fun apollo(packageName: String, extraConfiguration: Action<Service> = Action {}) {
    apolloHandler.configure(project, pluginManager, libs, packageName, extraConfiguration)
  }

  fun compose() {
    composeHandler.configure(project)
  }

  fun serialization() {
    pluginManager.apply(libs.plugins.serialization.get().pluginId)
  }

  fun androidResources() {
    androidResHandler.configure(project)
  }

  fun room(isTestOnly: Boolean = false, resolveSchemaRelativeToRootDir: File.() -> File) {
    roomHandler.configure(project, pluginManager, libs, isTestOnly, resolveSchemaRelativeToRootDir)
  }

  companion object {
    internal fun Project.configureHedvigPlugin(): HedvigGradlePluginExtension {
      return extensions.create<HedvigGradlePluginExtension>(
        "hedvig",
        project,
        project.the<LibrariesForLibs>(),
        project.pluginManager,
      )
    }
  }
}

private abstract class ApolloSchemaHandler {
  fun configure(project: Project, apolloServiceAction: Action<Service>) {
    with(project) {
      pluginManager.apply(the<LibrariesForLibs>().plugins.apollo.get().pluginId)
    }
    project.extensions.configure<ApolloExtension> {
      service("octopus") {
        apolloServiceAction.execute(this)
      }
    }
    project.tasks.withType<ApolloDownloadSchemaTask>()
      .configureEach {
        doLast {
          val schemaFile = outputFile.get().asFile
          val schemaText = schemaFile.readText()
          val convertedSchema = schemaText.performClientSideChanges()
          schemaFile.writeText(convertedSchema)
        }
      }
  }

  private fun String.performClientSideChanges(): String {
    return this
      .withoutDoubleLineBreaks()
      .makeClaimConversationNullable()
  }

  /**
   * Just so the schema looks more neat and is easier to navigate without all the extra whitespace
   */
  private fun String.withoutDoubleLineBreaks(): String {
    return replace("\n\n", "\n")
  }

  /**
   * Sometimes the backend wrongly sends null for the conversation field
   * Context: https://hedviginsurance.slack.com/archives/C075NGQ600Z/p1721134166246409
   */
  private fun String.makeClaimConversationNullable(): String {
    val oldValue = """
|  ${'"'}""
|  Return the relevant conversation for this claim.
|  These conversations can be one of two kinds:
|  - either it will be a dedicated conversation for this specific claim
|  - or it will be the "legacy conversation", containing the entire old chat history
|  The first one will be given for claims that are newer, created after the release of conversations.
|  The second one for all other (older) claims.
|  ${'"'}""
|  conversation: Conversation!
    """.trimMargin()
    val newValue = """
|  ${'"'}""
|  Return the relevant conversation for this claim.
|  These conversations can be one of two kinds:
|  - either it will be a dedicated conversation for this specific claim
|  - or it will be the "legacy conversation", containing the entire old chat history
|  The first one will be given for claims that are newer, created after the release of conversations.
|  The second one for all other (older) claims.
|  ${'"'}""
|  conversation: Conversation
    """.trimMargin()
    return replace(
      oldValue,
      newValue,
    )
  }
}

private abstract class ApolloHandler {
  fun configure(
    project: Project,
    pluginManager: PluginManager,
    libs: LibrariesForLibs,
    packageName: String,
    extraConfiguration: Action<Service>,
  ) {
    pluginManager.apply(libs.plugins.apollo.get().pluginId)
    project.extensions.configure<ApolloExtension> {
      service("octopus") {
        this.packageName.set(packageName)

        @Suppress("OPT_IN_USAGE")
        dependsOn(project.project(":apollo-octopus-public"), true)
        extraConfiguration.execute(this)
      }
    }
  }
}

private abstract class ComposeHandler {
  fun configure(project: Project) {
    val libs = project.the<LibrariesForLibs>()
    project.pluginManager.apply(libs.plugins.composeCompilerGradlePlugin.get().pluginId)
    project.extensions.configure<ComposeCompilerGradlePluginExtension> {
      configureComposeCompilerMetrics(project)
    }
    val isAndroidLibrary = project.extensions.findByType<LibraryExtension>() != null
    if (isAndroidLibrary) {
      project.extensions.configure<LibraryExtension> {
        configureComposeAndroidBuildFeature()
      }
    }
    val isAndroidApp = project.extensions.findByType<BaseAppModuleExtension>() != null
    if (isAndroidApp) {
      project.extensions.configure<BaseAppModuleExtension> {
        configureComposeAndroidBuildFeature()
      }
    }
    val isAndroidMulitplatformLibrary =
      project.extensions.findByType<KotlinMultiplatformAndroidComponentsExtension>() != null
    project.dependencies {
      if (isAndroidLibrary || isAndroidApp || isAndroidMulitplatformLibrary) {
        val bom = libs.androidx.compose.bom
        if (isAndroidMulitplatformLibrary) {
          add("androidMainImplementation", libs.androidx.compose.uiToolingPreview)
          add("androidMainImplementation", libs.androidx.compose.uiTooling)
        } else {
          add("androidTestImplementation", platform(bom))
          add("implementation", libs.androidx.compose.uiToolingPreview)
          add("implementation", libs.androidx.compose.uiTooling)
        }
      }
    }
  }

  private fun AndroidCommonExtension.configureComposeAndroidBuildFeature() {
    buildFeatures {
      compose = true
    }
  }

  private fun ComposeCompilerGradlePluginExtension.configureComposeCompilerMetrics(project: Project) {
    with<Project, Unit>(project) {
      fun Provider<String>.onlyIfTrue() = flatMap { provider { it.takeIf(String::toBoolean) } }

      fun Provider<*>.relativeToRootProject(dir: String): Provider<Directory> = flatMap {
        rootProject.layout.buildDirectory.dir(projectDir.toRelativeString(rootDir))
      }.map { it.dir(dir) }

      // Get compose metrics with `./gradlew :app:assembleRelease -Pcom.hedvig.app.enableComposeCompilerReports=true`
      project.providers.gradleProperty("com.hedvig.app.enableComposeCompilerReports")
        .onlyIfTrue()
        .relativeToRootProject("compose-metrics")
        .let(metricsDestination::set)

      project.providers.gradleProperty("com.hedvig.app.enableComposeCompilerReports")
        .onlyIfTrue()
        .relativeToRootProject("compose-reports")
        .let(reportsDestination::set)
    }
  }
}

private abstract class AndroidResHandler {
  fun configure(project: Project) {
    project.extensions.configure<LibraryExtension> {
      buildFeatures {
        androidResources = true
      }
    }
  }
}

private abstract class RoomHandler {
  fun configure(
    project: Project,
    pluginManager: PluginManager,
    libs: LibrariesForLibs,
    isTestOnly: Boolean,
    resolveSchemaRelativeToRootDir: File.() -> File,
  ) {
    with(pluginManager) {
      apply(libs.plugins.room.get().pluginId)
      apply(libs.plugins.ksp.get().pluginId)
    }
    project.dependencies {
      val ksp = if (isTestOnly) "kspTest" else "ksp"
      add(ksp, libs.room.ksp)
    }
    project.extensions.configure<RoomExtension> {
      schemaDirectory(project.rootDir.resolveSchemaRelativeToRootDir().absolutePath)
    }
  }
}
