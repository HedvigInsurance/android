import com.android.build.gradle.LibraryExtension
import com.hedvig.android.configureKotlinAndroid
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the

class LibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      with(pluginManager) {
        apply(libs.plugins.androidLibrary.get().pluginId)
        apply(libs.plugins.kotlin.get().pluginId)
        apply(libs.plugins.cacheFix.get().pluginId)
      }

      extensions.configure<LibraryExtension> {
        configureKotlinAndroid(this, addStandardBuildTypes = true)
        @Suppress("MISSING_DEPENDENCY_SUPERCLASS")
        defaultConfig.targetSdk = libs.versions.targetSdkVersion.get().toInt()
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
