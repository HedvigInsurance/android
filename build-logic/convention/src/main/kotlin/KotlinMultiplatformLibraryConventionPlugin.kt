import com.hedvig.android.configureKotlinCompilerOptions
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KotlinMultiplatformLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      with(pluginManager) {
        apply(libs.plugins.kotlinMultiplatform.get().pluginId)
      }

      configureKotlinMultiplatform()
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
    listOf(
      iosX64(),
      iosArm64(),
      iosSimulatorArm64(),
    )
    jvm {
      compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
      }
    }
    compilerOptions {
      configureKotlinCompilerOptions()
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
