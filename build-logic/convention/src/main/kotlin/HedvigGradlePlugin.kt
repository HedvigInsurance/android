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
