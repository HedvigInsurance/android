import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.hedvig.android.configureKotlinAndroid
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.the

class ApplicationConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      with(pluginManager) {
        apply(libs.plugins.androidApplication.get().pluginId)
        apply(libs.plugins.cacheFix.get().pluginId)
        apply(libs.plugins.kotlin.get().pluginId)
      }

      extensions.configure<BaseAppModuleExtension> {
        val extension = extensions.getByType<ApplicationExtension>()
        configureKotlinAndroid(extension)
        defaultConfig.targetSdk = libs.versions.targetSdkVersion.get().toInt()
        // Libraries don't build debug so fall back to release.
        buildTypes.getByName("debug") {
          matchingFallbacks += "release"
        }
      }
      extensions.configure<ApplicationAndroidComponentsExtension> {
        beforeVariants(selector().withBuildType("release")) { applicationVariantBuilder ->
          @Suppress("DEPRECATION")
          applicationVariantBuilder.enableUnitTest = false
        }
        beforeVariants(selector().withBuildType("staging")) { applicationVariantBuilder ->
          @Suppress("DEPRECATION")
          applicationVariantBuilder.enableUnitTest = false
        }
      }
    }
  }
}
