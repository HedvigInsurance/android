import com.hedvig.android.configureKotlinCompilerOptions
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * A Kotlin Multiplatform library with a JVM target, plus an Android target when
 * `hedvig.multiplatform.library.android` is also applied. Never part of the iOS umbrella.
 */
class KotlinMultiplatformLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.applyKotlinMultiplatform(withIos = false)
  }
}

/**
 * A Kotlin Multiplatform library that compiles for iOS so `:umbrella` can export it, directly or
 * transitively. Adds the iOS targets on top of the JVM and Android ones, so every `expect` in the
 * module needs a native actual. A module keeps this plugin even while `:umbrella` does not currently
 * reach it, so it stays exportable without a porting effort.
 */
class KotlinMultiplatformIosLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.applyKotlinMultiplatform(withIos = true)
  }
}

private fun Project.applyKotlinMultiplatform(withIos: Boolean) {
  val libs = the<LibrariesForLibs>()
  with(pluginManager) {
    apply(libs.plugins.kotlinMultiplatform.get().pluginId)
  }
  configureKotlinMultiplatform(withIos)
}

/**
 * Configure base Kotlin Multiplatform libraries for all the standard targets
 */
private fun Project.configureKotlinMultiplatform(withIos: Boolean) {
  val project = this@configureKotlinMultiplatform
  val libs = the<LibrariesForLibs>()

  project.configure<KotlinMultiplatformExtension> {
    if (withIos) {
      iosArm64()
      iosSimulatorArm64()
    }
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
