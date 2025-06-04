import com.hedvig.android.configureKotlinCompilerOptions
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KotlinMultiplatformLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      with(pluginManager) {
        apply(libs.plugins.kotlinMultiplatform.get().pluginId)
        apply<HedvigLintConventionPlugin>() // todo consider moving this into Hedvig gradle plugin instead
      }

      configureKotlinMultiplatform()

      // todo consider moving this into Hedvig gradle plugin instead
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

/**
 * Configure base Kotlin Multiplatform libraries for all the standard targets
 */
private fun Project.configureKotlinMultiplatform() {
  val project = this@configureKotlinMultiplatform
  val libs = the<LibrariesForLibs>()

  project.configure<KotlinMultiplatformExtension> {
//    compilerOptions.configureKotlinCompilerOptions()
//    val xcfName = "design-showcake-desktop-kit"
    listOf(
      iosX64(),
      iosArm64(),
      iosSimulatorArm64(),
    ).forEach {
//      it.binaries.framework { baseName = xcfName }
    }
    jvm {
      compilerOptions {
        this.configureKotlinCompilerOptions()
      }
    }
    applyDefaultHierarchyTemplate()

    sourceSets.getByName("commonMain") {
      dependencies {
        implementation(libs.kotlin.stdlib)
      }
    }
    sourceSets.getByName("commonTest") {
      dependencies {
        implementation(libs.kotlin.test)
      }
    }
  }
}
