package com.hedvig.android

import androidx.room.gradle.RoomExtension
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.apollographql.apollo.gradle.api.ApolloExtension
import com.apollographql.apollo.gradle.api.Service
import com.apollographql.apollo.gradle.internal.ApolloDownloadSchemaTask
import java.io.File
import javax.inject.Inject
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
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

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
  private val navKeysHandler: NavKeysHandler =
    project.objects.newInstance<NavKeysHandler>()
  private val viewModelsHandler: ViewModelsHandler =
    project.objects.newInstance<ViewModelsHandler>()

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

  fun androidResources(resourcesNamespace: String? = null, publicRes: Boolean = false) {
    androidResHandler.configure(project, resourcesNamespace, publicRes)
  }

  fun room(isTestOnly: Boolean = false, resolveSchemaRelativeToRootDir: File.() -> File) {
    roomHandler.configure(project, pluginManager, libs, isTestOnly, resolveSchemaRelativeToRootDir)
  }

  /**
   * Wires the [:navigation-keys-processor] KSP processor into this module. The processor scans for
   * concrete `@Serializable HedvigNavKey` subclasses and generates a Metro `@ContributesTo(AppScope)`
   * provider that registers them all into the polymorphic [HedvigNavKey] serializers module, so the
   * Nav3 back stack survives process death without any hand-written per-module provider.
   */
  fun navKeys() {
    navKeysHandler.configure(project, pluginManager, libs)
  }

  /**
   * Wires the [:viewmodel-processor] KSP processor into this module. The processor scans for
   * `@HedvigViewModel`-annotated ViewModels and generates their Metro map contribution (no-arg) or
   * assisted factory, so no VM hand-writes `@ViewModelKey` / `@ContributesIntoMap` / a nested factory.
   *
   * For KMP modules both `kspCommonMainMetadata` (commonMain VMs — generated DI must be visible to
   * every target, including iOS via IosGraph) and `kspAndroid` (androidMain-only VMs, e.g. a screen
   * iOS never renders) are wired, so a single module may freely mix the two. See [ViewModelsHandler]
   * for how the resulting double-emission of commonMain VMs is suppressed.
   */
  fun viewModels() {
    viewModelsHandler.configure(project, pluginManager, libs)
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
    project.pluginManager.apply(libs.plugins.composeKotlinCompilerGradlePlugin.get().pluginId)
    project.pluginManager.apply(libs.plugins.composeJetbrainsCompilerGradlePlugin.get().pluginId)
    // consider enabling this again if we are interested in these reports
//    project.extensions.configure<ComposeCompilerGradlePluginExtension> {
//      configureComposeCompilerMetrics(project)
//    }
    project.configureIfPresent<LibraryExtension> {
      buildFeatures.compose = true
    }
    project.configureIfPresent<ApplicationExtension> {
      buildFeatures.compose = true
    }
    val isAndroidLibrary = project.extensions.findByType<LibraryExtension>() != null
    val isAndroidApp = project.extensions.findByType<ApplicationExtension>() != null
    val isAndroidMultiplatformLibrary =
      project.extensions.findByType<KotlinMultiplatformAndroidComponentsExtension>() != null
    project.dependencies {
      if (isAndroidLibrary || isAndroidApp || isAndroidMultiplatformLibrary) {
        val bom = libs.androidx.compose.bom
        if (isAndroidMultiplatformLibrary) {
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
  fun configure(project: Project, resourcesNamespace: String?, publicRes: Boolean) {
    project.configureIfPresent<LibraryExtension> {
      androidResources.enable = true
    }
    // For KMP android library targets, configure android resources via the KMP extension's targets
    project.configureIfPresent<KotlinMultiplatformExtension> {
      targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java) {
        @Suppress("UnstableApiUsage")
        androidResources.enable = true
      }
    }
    project.configureIfPresent<ComposeExtension> {
      extensions.configure<ResourcesExtension> {
        generateResClass = ResourcesExtension.ResourceClassGeneration.Always
        if (resourcesNamespace != null) {
          packageOfResClass = resourcesNamespace
        }
        publicResClass = publicRes
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
    // Room's KSP-generated code calls internal Room APIs annotated with @RestrictTo(LIBRARY_GROUP),
    // which triggers RestrictedApi lint errors. Since this is generated code we don't control,
    // allow modules to disable specific lint checks when using Room.
    project.afterEvaluate {
      project.extensions.findByType<Lint>()?.apply {
        disable.add("RestrictedApi")
      }
    }
  }
}

private abstract class NavKeysHandler {
  fun configure(project: Project, pluginManager: PluginManager, libs: LibrariesForLibs) {
    pluginManager.apply(libs.plugins.ksp.get().pluginId)
    // A KMP module's android-target KSP (kspAndroid) sees commonMain symbols too, so attaching the
    // processor to the android compilation alone covers main/androidMain/commonMain keys in one pass
    // and avoids the double-emission that per-target wiring would cause.
    val isMultiplatform = project.extensions.findByType<KotlinMultiplatformExtension>() != null
    val kspConfiguration = if (isMultiplatform) "kspAndroid" else "ksp"
    project.dependencies {
      add(kspConfiguration, project.project(":navigation-keys-processor"))
    }
  }
}

private abstract class ViewModelsHandler {
  fun configure(project: Project, pluginManager: PluginManager, libs: LibrariesForLibs) {
    pluginManager.apply(libs.plugins.ksp.get().pluginId)
    val processor = project.project(":viewmodel-processor")
    val isMultiplatform = project.extensions.findByType<KotlinMultiplatformExtension>() != null
    if (!isMultiplatform) {
      project.dependencies {
        add("ksp", processor)
      }
      return
    }
    // commonMain VMs must be visible to every target (notably iOS via IosGraph), so they are
    // generated into commonMain through kspCommonMainMetadata. androidMain-only VMs are invisible to
    // the metadata pass, so kspAndroid is ALSO wired to cover them. kspAndroid also re-sees commonMain
    // VMs and would double-emit them, but the processor detects the single-target leaf pass and skips
    // commonMain symbols there, so only the metadata pass emits them.
    project.dependencies {
      add("kspCommonMainMetadata", processor)
      add("kspAndroid", processor)
    }
    project.extensions.configure<KotlinMultiplatformExtension> {
      sourceSets.getByName("commonMain").kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }
    // The generated dir is a commonMain source root, so every per-target compile *and* every other
    // per-target KSP task (e.g. the nav-keys kspAndroidMain) reads it. Gradle won't infer that
    // ordering, so make all of them run after the metadata generation. The metadata task itself is
    // excluded to avoid a self-cycle.
    project.tasks.configureEach {
      if (name != "kspCommonMainKotlinMetadata" && (name.startsWith("compile") || name.startsWith("ksp"))) {
        dependsOn("kspCommonMainKotlinMetadata")
      }
    }
  }
}

private inline fun <reified T : Any> Project.configureIfPresent(noinline configure: T.() -> Unit) {
  if (project.extensions.findByType<T>() != null) {
    project.extensions.configure<T>(configure)
  }
}
