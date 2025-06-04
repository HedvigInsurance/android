import com.android.build.api.dsl.androidLibrary
import com.hedvig.android.configureAutomaticNamespace
import com.hedvig.android.configureKotlinCompilerOptions
import com.hedvig.android.isLoggingPublicModule
import com.hedvig.android.isTrackingCoreModule
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
 */
private fun Project.configureKotlinAndroidMultiplatform() {
  val project = this@configureKotlinAndroidMultiplatform
  val libs = the<LibrariesForLibs>()

  project.configure<KotlinMultiplatformExtension> {
    androidLibrary {
      configureAutomaticNamespace(
        path = path,
        namespace = namespace,
        setNameSpace = { namespace = it },
      )
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
    }
  }

  dependencies {
    val koinBom = libs.koin.bom
//    implementation(platform(koinBom))

    add("coreLibraryDesugaring", libs.coreLibraryDesugaring.get())
//    add("lintChecks", project(":hedvig-lint"))
    // Add logging-public and tracking-core to all modules except themselves
    if (!project.isLoggingPublicModule() && !project.isTrackingCoreModule()) {
//      implementation(project(":logging-public"))
//      implementation(project(":tracking-core"))
    }
  }
}
