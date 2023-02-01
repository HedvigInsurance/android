import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.hedvig.android.configureKotlinAndroid
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the

class ApplicationConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      with(pluginManager) {
        apply(libs.plugins.androidApplication.get().pluginId)
        apply(libs.plugins.kotlin.get().pluginId)
        apply(libs.plugins.cacheFix.get().pluginId)
      }

      extensions.configure<BaseAppModuleExtension> {
        configureKotlinAndroid(this)
        defaultConfig.targetSdk = libs.versions.targetSdkVersion.get().toInt()
      }
    }
  }
}
