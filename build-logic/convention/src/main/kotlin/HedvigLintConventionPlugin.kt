import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import java.io.File
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the

class HedvigLintConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      val moduleName = target.name
      if (moduleName == "hedvig-lint") return
      val lintBaselineFile: File = rootProject.projectDir
        .resolve("hedvig-lint")
        .resolve("lint-baseline")
        .resolve("lint-baseline-$moduleName.xml")
      val lintXmlPath: File = rootProject.projectDir.resolve("hedvig-lint").resolve("lint.xml")
      var didConfigureLint = false
      // The KMP-android variant registers no runnable Android Lint task (only `lintKotlin*`, which is
      // ktlint), so this lint configuration is inert: `./gradlew lint` does not lint these modules and
      // nothing produces or consumes their baseline file. Any lint-baseline-<module>.xml for a KMP-android
      // module is therefore not enforced and cannot be regenerated via `updateLintBaseline`.
      pluginManager.withPlugin(libs.plugins.androidLibraryMultiplatform.get().pluginId) {
        configure<KotlinMultiplatformAndroidComponentsExtension> {
          finalizeDsl {
            it.lint { configure(lintXmlPath, lintBaselineFile) }
          }
        }
        didConfigureLint = true
      }

      pluginManager.withPlugin(libs.plugins.androidApplication.get().pluginId) {
        configure<ApplicationExtension> { lint { configure(lintXmlPath, lintBaselineFile) } }
        didConfigureLint = true
      }

      pluginManager.withPlugin(libs.plugins.androidLibrary.get().pluginId) {
        configure<LibraryExtension> { lint { configure(lintXmlPath, lintBaselineFile) } }
        didConfigureLint = true
      }

      if (!didConfigureLint) {
        pluginManager.apply(libs.plugins.lintGradlePlugin.get().pluginId)
        configure<Lint> { configure(lintXmlPath, lintBaselineFile) }
      }
      dependencies {
        add("lintChecks", project(":hedvig-lint"))
      }
    }
  }
}

private fun Lint.configure(lintXmlFile: File, lintBaselineFile: File) {
  // Lint aborts the build whenever it has to create a baseline, even when it found nothing to put in
  // one, and the baseline it leaves behind then suppresses real errors on the next run. A module opts
  // in by committing the file: create it empty, then fill it with `updateLintBaseline`.
  baseline = lintBaselineFile.takeIf(File::exists)
  lintConfig = lintXmlFile
  xmlReport = true
}
