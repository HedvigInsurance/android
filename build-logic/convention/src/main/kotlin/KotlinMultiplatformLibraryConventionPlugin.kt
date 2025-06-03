import com.hedvig.android.configureKotlinMultiplatform
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the

class KotlinMultiplatformLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      with(pluginManager) {
        apply(libs.plugins.kotlinMultiplatform.get().pluginId)
        apply(libs.plugins.androidLibraryMultiplatform.get().pluginId)
        apply<HedvigLintConventionPlugin>()
      }

      configureKotlinMultiplatform()

//      dependencies {
//        val koinBom = libs.koin.bom
//        add("implementation", platform(koinBom))
//
//        add("lintChecks", project(":hedvig-lint"))
//        if (target.name != "logging-public") {
//          add("implementation", project(":logging-public"))
//        }
//        // Add logging-public and tracking-core to all modules except themselves
//        if (!project.isLoggingPublicModule() && !project.isTrackingCoreModule()) {
//          add("implementation", project(":logging-public"))
//          add("implementation", project(":tracking-core"))
//        }
//      }
    }
  }
}
