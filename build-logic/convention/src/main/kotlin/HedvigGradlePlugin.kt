import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import com.hedvig.android.HedvigGradlePluginExtension.Companion.configureHedvigPlugin
import java.io.File
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
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
      configureLint(libs)
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

private fun Project.configureLint(libs: LibrariesForLibs) {
  fun Lint.configure(lintXmlFile: File, lintBaselineFile: File) {
    baseline = lintBaselineFile
    lintConfig = lintXmlFile
    xmlReport = true
    disable.add("UnsafeOptInUsageError") // https://issuetracker.google.com/issues/328279054
  }

  val moduleName = this.name
  val lintBaselineFile: File = rootProject.projectDir
    .resolve("hedvig-lint")
    .resolve("lint-baseline")
    .resolve("lint-baseline-$moduleName.xml")
  val lintXmlPath: File = rootProject.projectDir.resolve("hedvig-lint").resolve("lint.xml")
  var didConfigureLint = false
  pluginManager.withPlugin(libs.plugins.androidApplication.get().pluginId) {
    configure<ApplicationExtension> { lint { configure(lintXmlPath, lintBaselineFile) } }
    didConfigureLint = true
  }
  pluginManager.withPlugin(libs.plugins.androidLibrary.get().pluginId) {
    configure<LibraryExtension> { lint { configure(lintXmlPath, lintBaselineFile) } }
    didConfigureLint = true
  }
  // Here we need to do this `afterEvaluate` so that we don't apply the jvm lintGradlePlugin before we know that
  // android gradle plugins is for sure not applied. Those ones apply the lint plugin internally automatically
  // so if it's already applied, the build just crashes.
  // To solve this, there are two options:
  // 1. Move the decision of if we are in an android module or not *inside* the HGP so that we can branch here knowing
  //  this information before-hands
  // 2. Create two separate plugins, and conditionally apply them on if we are in an android module or not. The one
  //  which is non-android should apply the lint plugin and configure it. The Android one should just configure it.
  // TODO: Link to the discussion here
  afterEvaluate {
    if (!didConfigureLint) {
      pluginManager.apply(libs.plugins.lintGradlePlugin.get().pluginId)
      configure<Lint> { configure(lintXmlPath, lintBaselineFile) }
    }
    dependencies {
      add("lintChecks", project(":hedvig-lint"))
    }
  }
}
