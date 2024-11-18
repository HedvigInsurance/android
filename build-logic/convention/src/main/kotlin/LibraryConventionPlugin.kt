import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import com.hedvig.android.configureKotlinAndroid
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the

/**
 * Defines an Android library
 */
class LibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      with(pluginManager) {
        apply(libs.plugins.androidLibrary.get().pluginId)
        apply(libs.plugins.cacheFix.get().pluginId)
        apply(libs.plugins.kotlin.get().pluginId)
        apply<HedvigLintConventionPlugin>()
      }

      extensions.configure<LibraryExtension> {
        configureKotlinAndroid(this)
        defaultConfig.targetSdk = libs.versions.targetSdkVersion.get().toInt()
        buildFeatures {
          resValues = false
          shaders = false
        }
      }
      extensions.configure<LibraryAndroidComponentsExtension> {
        beforeVariants { libraryVariantBuilder ->
          if (libraryVariantBuilder.buildType == "debug") {
            libraryVariantBuilder.enable = false
          }
        }
      }
    }
  }
}
