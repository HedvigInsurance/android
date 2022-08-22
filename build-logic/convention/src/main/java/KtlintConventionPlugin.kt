import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

class KtlintConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      with(pluginManager) {
        apply(libs.plugins.ktlint.get().pluginId)
      }

      extensions.configure<KtlintExtension> {
        version.set(libs.versions.ktlint.get())
        enableExperimentalRules.set(true)
        additionalEditorconfigFile.set(rootProject.file(".editorconfig"))
        outputToConsole.set(true)
        filter {
          exclude("**/generated/**")
          include("**/src/**/*.kt")
          include("**/src/**/*.kts")
          include("**/build.gradle.kts")
        }
        reporters {
          reporter(ReporterType.CHECKSTYLE)
        }
      }

      tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask>().configureEach {
        reportsOutputDirectory.set(
          rootProject.layout.buildDirectory.dir("ktlint-report-in-checkstyle-format"),
        )
      }
    }
  }
}
