import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the

class HedvigLintConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      when {
        pluginManager.hasPlugin(libs.plugins.androidApplication.get().pluginId) -> {
          configure<ApplicationExtension> { lint(Lint::configure) }
        }
        pluginManager.hasPlugin(libs.plugins.androidLibrary.get().pluginId) -> {
          configure<LibraryExtension> { lint(Lint::configure) }
        }
        else -> {
          pluginManager.apply(libs.plugins.lintGradlePlugin.get().pluginId)
          configure<Lint>(Lint::configure)
        }
      }
    }
  }
}

private fun Lint.configure() {
  xmlReport = true
  checkDependencies = true
}
