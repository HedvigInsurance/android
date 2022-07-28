import com.android.build.gradle.LibraryExtension
import com.hedvig.android.configureKotlinAndroid
import com.hedvig.android.targetSdkVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class LibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("com.android.library")
        apply("org.jetbrains.kotlin.android")
      }

      val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

      extensions.configure<LibraryExtension> {
        configureKotlinAndroid(this, addStandardBuildTypes = true)
        @Suppress("MISSING_DEPENDENCY_SUPERCLASS")
        defaultConfig.targetSdk = libs.targetSdkVersion
        @Suppress("UnstableApiUsage")
        buildFeatures {
          buildConfig = false
          aidl = false
          renderScript = false
          resValues = false
          shaders = false
        }
      }
    }
  }
}
