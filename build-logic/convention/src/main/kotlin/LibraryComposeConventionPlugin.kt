import com.android.build.gradle.LibraryExtension
import com.hedvig.android.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class LibraryComposeConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      extensions.configure<LibraryExtension> {
        configureAndroidCompose(this)
      }
    }
  }
}
