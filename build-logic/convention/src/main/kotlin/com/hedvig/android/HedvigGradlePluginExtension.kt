package com.hedvig.android

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import javax.inject.Inject
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.plugins.PluginManager
import org.gradle.kotlin.dsl.assign
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
  internal val apolloSchemaHandler: ApolloSchemaHandler =
    project.objects.newInstance<ApolloSchemaHandler>(project, pluginManager, libs)
  internal val composeHandler: ComposeHandler =
    project.objects.newInstance<ComposeHandler>(project, pluginManager, libs)

  fun apolloSchema() {
    apolloSchemaHandler.configure()
  }

  /**
   * [packageName] is the package that the generated apollo classes will have for this module.
   */
  fun apollo(packageName: String) {
    pluginManager.apply(libs.plugins.apollo.get().pluginId)
    project.extensions.configure<com.apollographql.apollo.gradle.api.ApolloExtension> {
      service("octopus") {
        this.packageName = packageName
        @Suppress("OPT_IN_USAGE")
        dependsOn(project.project(":apollo-octopus-public"), true)
      }
    }
  }

  fun compose() {
    composeHandler.configure()
  }

  fun serialization() {
    pluginManager.apply(libs.plugins.serialization.get().pluginId)
  }

  fun ksp() {
    pluginManager.apply(libs.plugins.ksp.get().pluginId)
  }

  fun room() {
    // todo
  }

  companion object {
    internal fun Project.configureHedvigPlugin(): HedvigGradlePluginExtension {
      return extensions.create<HedvigGradlePluginExtension>(
        "hedvig",
        project,
        with(project) { the<LibrariesForLibs>() },
        project.pluginManager,
      )
    }
  }
}

internal abstract class ApolloSchemaHandler @Inject constructor(
  private val project: Project,
  private val pluginManager: PluginManager,
  private val libs: LibrariesForLibs,
) {
  fun configure() {
    pluginManager.apply(libs.plugins.apollo.get().pluginId)
    project.tasks.withType<com.apollographql.apollo.gradle.internal.ApolloDownloadSchemaTask>().configureEach {
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

internal abstract class ComposeHandler @Inject constructor(
  private val project: Project,
  private val pluginManager: PluginManager,
  private val libs: LibrariesForLibs,
) {
  fun configure() {
    pluginManager.apply(libs.plugins.composeCompilerGradlePlugin.get().pluginId)
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
    project.dependencies {
      val bom = libs.androidx.compose.bom
      add("implementation", platform(bom))
      if (isAndroidLibrary || isAndroidApp) {
        add("androidTestImplementation", platform(bom))
        add("implementation", libs.androidx.compose.uiToolingPreview)
        add("debugImplementation", libs.androidx.compose.uiTooling)
      }
    }
  }

  private fun AndroidCommonExtension.configureComposeAndroidBuildFeature() {
    buildFeatures {
      compose = true
    }
  }
}
