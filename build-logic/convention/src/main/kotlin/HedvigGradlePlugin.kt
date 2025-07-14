import com.hedvig.android.HedvigGradlePluginExtension.Companion.configureHedvigPlugin
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jmailen.gradle.kotlinter.KotlinterExtension
import org.jmailen.gradle.kotlinter.support.ReporterType

class HedvigGradlePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      val libs = the<LibrariesForLibs>()
      configureHedvigPlugin()
      configureFeatureModuleGuidelines()
      configureKtlint(libs)
      configureCommonDependencies(libs)
      apply<HedvigLintConventionPlugin>()
      with(pluginManager) {
        apply(libs.plugins.dependencyAnalysis.get().pluginId)
        apply(libs.plugins.squareSortDependencies.get().pluginId)
      }
    }
  }
}

private fun Project.configureKtlint(libs: LibrariesForLibs) {
  pluginManager.apply(libs.plugins.kotlinter.get().pluginId)

  extensions.configure<KotlinterExtension> {
    reporters = arrayOf(ReporterType.checkstyle.name)
  }

  tasks.withType<org.jmailen.gradle.kotlinter.tasks.LintTask>().configureEach {
    exclude { it.file.path.contains("generated/") }
    reports.set(
      mapOf(
        "checkstyle" to rootDir.resolve("build/reports/ktlint/${project.path}.xml"),
      ),
    )
  }
  tasks.withType<org.jmailen.gradle.kotlinter.tasks.FormatTask>().configureEach {
    exclude { it.file.path.contains("generated/") }
    report.set(rootDir.resolve("build/reports/ktlint/${project.path}.xml"))
  }

  tasks.register("ktlintCheck") {
    dependsOn(tasks.withType<org.jmailen.gradle.kotlinter.tasks.LintTask>())
  }

  tasks.register("ktlintFormat") {
    dependsOn(tasks.withType<org.jmailen.gradle.kotlinter.tasks.FormatTask>())
  }
}

private fun Project.configureFeatureModuleGuidelines() {
  fun String.isFeatureModule(): Boolean {
    return startsWith("feature-") && !startsWith("feature-flags")
  }

  val thisModuleName = this.name
  if (!thisModuleName.isFeatureModule()) return
  configurations.configureEach {
    resolutionStrategy {
      eachDependency {
        if (requested.group != "hedvigandroid") return@eachDependency // Only check for our own modules
        if (requested.name == thisModuleName) return@eachDependency // Only check deps to other modules
        val requestedModuleIsAFeatureModule = requested.name.isFeatureModule()
        require(!requestedModuleIsAFeatureModule) {
          "Hedvig build error on a module marked as featureModule() in HGP." +
            "\nYou are trying to depend on another feature module from a feature module." +
            "\nThis is not allowed as it breaks our ability to properly share code between modules." +
            "\nIn particular, $thisModuleName is trying to depend on ${requested.name}." +
            "\nIf you need to share code between feature modules, consider moving the shared code to a library module."
        }
      }
    }
  }
}

private fun Project.configureCommonDependencies(libs: LibrariesForLibs) {
  pluginManager.withPlugin(libs.plugins.kotlinMultiplatform.get().pluginId) {
    project.extensions.configure<KotlinMultiplatformExtension> {
      sourceSets.configureEach {
        dependencies {
          configureCommonDependencies(project, libs)
        }
      }
    }
  }
  pluginManager.withPlugin(libs.plugins.kotlinJvm.get().pluginId) {
    dependencies {
      configureCommonDependencies(project, libs)
    }
  }
  pluginManager.withPlugin(libs.plugins.kotlin.get().pluginId) {
    dependencies {
      configureCommonDependencies(project, libs)
    }
  }
}

@Suppress("UnusedReceiverParameter")
private fun KotlinDependencyHandler.configureCommonDependencies(project: Project, libs: LibrariesForLibs) {
  project.configureCommonDependencies(libs, "commonMainImplementation")
}

@Suppress("UnusedReceiverParameter")
private fun DependencyHandlerScope.configureCommonDependencies(project: Project, libs: LibrariesForLibs) {
  project.configureCommonDependencies(libs, "implementation")
}

private fun Project.configureCommonDependencies(libs: LibrariesForLibs, configurationName: String) {
  val koinBom = libs.koin.bom
  val composeBom = libs.androidx.compose.bom
  dependencies {
    add(configurationName, platform(koinBom))
    add(configurationName, platform(composeBom))

    if (project.name != "logging-public") {
      add(configurationName, project(":logging-public"))
    }
    // Add logging-public and tracking-core to all modules except themselves
    if (!project.isLoggingPublicModule() && !project.isTrackingCoreModule()) {
      add(configurationName, project(":logging-public"))
      add(configurationName, project(":tracking-core"))
    }
  }
}

fun Project.isLoggingPublicModule(): Boolean {
  return name == "logging-public"
}

fun Project.isTrackingCoreModule(): Boolean {
  return name == "tracking-core"
}
