import com.hedvig.android.HedvigGradlePluginExtension.Companion.configureHedvigPlugin
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jmailen.gradle.kotlinter.KotlinterExtension
import org.jmailen.gradle.kotlinter.support.ReporterType

class HedvigGradlePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      val libs = the<LibrariesForLibs>()
      configureHedvigPlugin()
      configureFeatureModuleGuidelines()
      configureKtlint(libs)
      pluginManager.apply(libs.plugins.dependencyAnalysis.get().pluginId)
      pluginManager.apply(libs.plugins.squareSortDependencies.get().pluginId)
    }
  }
}

private fun Project.configureKtlint(libs: LibrariesForLibs) {
  pluginManager.apply(libs.plugins.kotlinter.get().pluginId)

  extensions.configure<KotlinterExtension> {
    ignoreFailures = false
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
