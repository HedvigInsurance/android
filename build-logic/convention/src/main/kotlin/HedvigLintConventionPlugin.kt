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
      // The KMP-android plugin only registers its Android Lint tasks (`lintAndroidMain`,
      // `updateLintBaselineAndroidMain`, ...) when `com.android.lint` is also applied, so it is applied here.
      // That androidMain pass analyses commonMain, jvmAndAndroidMain and androidMain, and is configured by
      // the `lint {}` block below. The same plugin also adds a `lintJvm` pass, which only sees jvmMain and
      // keeps lint's defaults. The `lint {}` block in `finalizeDsl`, and so `lint.xml` and the module's
      // baseline, applies to the Android pass only.
      pluginManager.withPlugin(libs.plugins.androidLibraryMultiplatform.get().pluginId) {
        if (moduleName !in kmpModulesWithoutAndroidLint) {
          pluginManager.apply(libs.plugins.lintGradlePlugin.get().pluginId)
        }
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

// These modules are left without Android lint because applying the plugin there creates a Gradle task
// cycle. apollo-octopus-public's commonMain depends on core-build-constants and core-common-public,
// core-common-public's androidMain depends on apollo-octopus-public, and core-build-constants' commonMain
// depends on language-core and core-common-public.
private val kmpModulesWithoutAndroidLint = setOf(
  "core-build-constants",
  "core-common-public",
  "language-core",
)

private fun Lint.configure(lintXmlFile: File, lintBaselineFile: File) {
  // Lint aborts the build whenever it has to create a baseline, even when it found nothing to put in
  // one, and the baseline it leaves behind then suppresses real errors on the next run. A module opts
  // in by committing the file: create it empty, then fill it with `updateLintBaseline`.
  baseline = lintBaselineFile.takeIf(File::exists)
  lintConfig = lintXmlFile
  xmlReport = true
}
