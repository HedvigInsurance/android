import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import java.io.File
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the

class HedvigLintConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      val moduleName = target.name
      val lintBaselineFile: File = rootProject.projectDir
        .resolve("hedvig-lint")
        .resolve("lint-baseline")
        .resolve("lint-baseline-$moduleName.xml")
      val lintXmlPath: File = rootProject.projectDir.resolve("hedvig-lint").resolve("lint.xml")
      when {
        pluginManager.hasPlugin(libs.plugins.androidApplication.get().pluginId) -> {
          configure<ApplicationExtension> { lint { configure(lintXmlPath, lintBaselineFile) } }
        }

        pluginManager.hasPlugin(libs.plugins.androidLibrary.get().pluginId) -> {
          configure<LibraryExtension> { lint { configure(lintXmlPath, lintBaselineFile) } }
        }

        else -> {
          pluginManager.apply(libs.plugins.lintGradlePlugin.get().pluginId)
          configure<Lint> { configure(lintXmlPath, lintBaselineFile) }
        }
      }
    }
  }
}

private fun Lint.configure(lintXmlFile: File, lintBaselineFile: File) {
  baseline = lintBaselineFile
  lintConfig = lintXmlFile
  xmlReport = true
  disable.add("UnsafeOptInUsageError") // https://issuetracker.google.com/issues/328279054
}
