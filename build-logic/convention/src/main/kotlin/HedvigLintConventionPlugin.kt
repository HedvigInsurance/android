import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import java.io.File
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.project
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
      val lintBaselineJvmFile: File = rootProject.projectDir
        .resolve("hedvig-lint")
        .resolve("lint-baseline")
        .resolve("lint-baseline-$moduleName-jvm.xml")
      val lintXmlPath: File = rootProject.projectDir.resolve("hedvig-lint").resolve("lint.xml")
      val lintPluginId = libs.plugins.lintGradlePlugin.get().pluginId

      // Every setup below reacts to a plugin being present, never to one being absent, so it does not
      // depend on the order a module applies its plugins in.
      //
      // A plain JVM module gets the standalone `com.android.lint` plugin, which runs on its main source set.
      pluginManager.withPlugin(libs.plugins.kotlinJvm.get().pluginId) {
        pluginManager.apply(lintPluginId)
        configure<Lint> { configure(lintXmlPath, lintBaselineFile) }
      }

      // A KMP module also gets the standalone plugin. It adds a `lintJvm` pass, which only sees jvmMain and
      // is configured by the standalone plugin's `lint` extension. When the module also has the KMP-android
      // plugin, that plugin only registers its Android Lint tasks (`lintAndroidMain`,
      // `updateLintBaselineAndroidMain`, ...) because `com.android.lint` is applied, whichever of the two
      // comes first. The JVM pass has a baseline file of its own, so `updateLintBaselineJvm` does not
      // overwrite the Android pass's baseline.
      pluginManager.withPlugin(libs.plugins.kotlinMultiplatform.get().pluginId) {
        pluginManager.apply(lintPluginId)
        configure<Lint> { configure(lintXmlPath, lintBaselineJvmFile) }
      }

      // The KMP-android pass analyses commonMain, jvmAndAndroidMain and androidMain, and is configured by
      // the `lint {}` block in `finalizeDsl`.
      pluginManager.withPlugin(libs.plugins.androidLibraryMultiplatform.get().pluginId) {
        configure<KotlinMultiplatformAndroidComponentsExtension> {
          finalizeDsl {
            it.lint { configure(lintXmlPath, lintBaselineFile) }
          }
        }
      }

      pluginManager.withPlugin(libs.plugins.androidApplication.get().pluginId) {
        configure<ApplicationExtension> { lint { configure(lintXmlPath, lintBaselineFile) } }
      }

      pluginManager.withPlugin(libs.plugins.androidLibrary.get().pluginId) {
        configure<LibraryExtension> { lint { configure(lintXmlPath, lintBaselineFile) } }
      }

      // `lintChecks` is created by whichever of the plugins above sets up lint, so the custom checks are
      // added as soon as it exists.
      configurations.matching { it.name == "lintChecks" }.configureEach {
        target.dependencies.add(name, target.dependencies.project(":hedvig-lint"))
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
