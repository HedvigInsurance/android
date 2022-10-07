import com.hedvig.android.configureKotlin
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

/**
 * Defines a Kotlin library which does not know anything about Android
 */
class KotlinLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = the<LibrariesForLibs>()
      with(pluginManager) {
        apply(libs.plugins.kotlinJvm.get().pluginId)
      }

      tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
        configureKotlin(this)
      }
    }
  }
}
