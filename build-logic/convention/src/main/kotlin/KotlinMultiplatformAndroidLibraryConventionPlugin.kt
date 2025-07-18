import com.android.build.api.dsl.androidLibrary
import com.hedvig.android.configureAutomaticNamespace
import com.hedvig.android.configureKotlinCompilerOptions
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

class KotlinMultiplatformAndroidLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      with(pluginManager) {
        apply(libs.plugins.androidLibraryMultiplatform.get().pluginId)
      }

      configureKotlinAndroidMultiplatform()
    }
  }
}

/**
 * Configure base Kotlin Multiplatform libraries that also need to be an android target
 *
 * See [com.hedvig.android.configureKotlinAndroid] as the source of truth for what must be done here. There are
 * currently no common parent interfaces for android KMP libraries, android libraries and android applications atm.
 * tl;dr [com.hedvig.android.AndroidCommonExtension] does not have a common parent with
 * [com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget] so this contains copy-pasted code
 */
private fun Project.configureKotlinAndroidMultiplatform() {
  val libs = the<LibrariesForLibs>()

  project.configure<KotlinMultiplatformExtension> {
    androidLibrary {
      this.compileSdk = libs.versions.compileSdkVersion.get().toInt()
      this.minSdk = libs.versions.minSdkVersion.get().toInt()
      this.enableCoreLibraryDesugaring = true
      this.compilations.configureEach {
        this.compileTaskProvider.configure {
          this.compilerOptions {
            this.configureKotlinCompilerOptions()
          }
        }
        this.compilerOptions.configure {
          this.jvmTarget.set(JvmTarget.JVM_21)
          this.languageVersion.set(KotlinVersion.KOTLIN_2_1)
        }
      }
      configureAutomaticNamespace(path, this.namespace, { this.namespace = it })
      // https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-resources-setup.html#resources-in-the-androidlibrary-target
      experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
    }
  }

  dependencies {
    add("coreLibraryDesugaring", libs.coreLibraryDesugaring.get())
  }
}
