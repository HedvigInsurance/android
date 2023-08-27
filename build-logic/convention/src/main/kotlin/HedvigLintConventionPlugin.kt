import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import java.io.File

class HedvigLintConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      val lintXmlPath: File = rootProject.projectDir.resolve("hedvig-lint").resolve("lint.xml")
      when {
        pluginManager.hasPlugin(libs.plugins.androidApplication.get().pluginId) -> {
          configure<ApplicationExtension> { lint { configure(lintXmlPath) } }
        }
        pluginManager.hasPlugin(libs.plugins.androidLibrary.get().pluginId) -> {
          configure<LibraryExtension> { lint { configure(lintXmlPath) } }
        }
        else -> {
          pluginManager.apply(libs.plugins.lintGradlePlugin.get().pluginId)
          configure<Lint> { configure(lintXmlPath) }
        }
      }
    }
  }
}

private fun Lint.configure(lintXmlFile: File) {
  lintConfig = lintXmlFile
  xmlReport = true
  checkDependencies = true
}
