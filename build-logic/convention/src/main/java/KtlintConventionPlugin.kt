import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jmailen.gradle.kotlinter.KotlinterExtension
import org.jmailen.gradle.kotlinter.support.ReporterType
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

class KtlintConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      with(pluginManager) {
        apply(libs.plugins.kotlinter.get().pluginId)
      }

      extensions.configure<KotlinterExtension> {
        ignoreFailures = false
        @Suppress("MISSING_DEPENDENCY_SUPERCLASS")
        reporters = arrayOf(ReporterType.checkstyle.name)
      }

      tasks.withType<LintTask>().configureEach {
        @Suppress("MISSING_DEPENDENCY_SUPERCLASS")
        exclude { it.file.path.contains("generated/") }
      }
      tasks.withType<FormatTask>().configureEach {
        @Suppress("MISSING_DEPENDENCY_SUPERCLASS")
        exclude { it.file.path.contains("generated/") }
      }

      tasks.register("ktlintCheck") {
        dependsOn(tasks.withType<LintTask>())
      }

      tasks.register("ktlintFormat") {
        dependsOn(tasks.withType<FormatTask>())
      }
    }
  }
}
