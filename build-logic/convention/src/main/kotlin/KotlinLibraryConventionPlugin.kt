import com.hedvig.android.configureKotlin
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the

/**
 * Defines a Kotlin library which does not know anything about Android
 */
class KotlinLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      with(pluginManager) {
        apply(libs.plugins.kotlinJvm.get().pluginId)
        apply("hedvig.lint")
      }

      configureKotlin()

      dependencies {
        val koinBom = libs.koin.bom
        add("implementation", platform(koinBom))

        add("lintChecks", project(":hedvig-lint"))
        if (target.name != "logging-public") {
          add("implementation", project(":logging-public"))
        }
        // Add logging-public and tracking-core to all modules except themselves
        if (!project.isLoggingPublicModule() && !project.isTrackingCoreModule()) {
          add("implementation", project(":logging-public"))
          add("implementation", project(":tracking-core"))
        }
      }
    }
  }
}

private fun Project.isLoggingPublicModule(): Boolean {
  return name == "logging-public"
}

private fun Project.isTrackingCoreModule(): Boolean {
  return name == "tracking-core"
}
