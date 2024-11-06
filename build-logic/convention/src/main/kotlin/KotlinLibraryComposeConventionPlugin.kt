import com.hedvig.android.configureComposeCompilerMetrics
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/**
 * Configures compose compiler on a non-android module
 */
class KotlinLibraryComposeConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      with(pluginManager) {
        apply(libs.plugins.composeCompilerGradlePlugin.get().pluginId)
      }
      extensions.configure<ComposeCompilerGradlePluginExtension> {
        configureComposeCompilerMetrics(this@with)
      }
      dependencies {
        val bom = libs.androidx.compose.bom
        add("implementation", platform(bom))
      }
    }
  }
}
