import com.hedvig.android.configureKotlinCompilerOptions
import kotlin.apply
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

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

      configureKotlin()
    }
  }
}

private fun Project.configureKotlin() {
  kotlinExtension.forEachCompilerOptions {
    configureKotlinCompilerOptions()
  }
  project.extensions.getByType(JavaPluginExtension::class.java).apply {
    toolchain.languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_21.majorVersion))
  }
  project.tasks.withType(JavaCompile::class.java).configureEach {
    options.release.set(JavaVersion.VERSION_21.majorVersion.toInt())
  }
}

private fun KotlinProjectExtension.forEachCompilerOptions(block: KotlinCommonCompilerOptions.() -> Unit) {
  when (this) {
    is KotlinJvmProjectExtension -> compilerOptions.block()
    is KotlinAndroidProjectExtension -> compilerOptions.block()
    is KotlinMultiplatformExtension -> {
      targets.all {
        compilations.all {
          compileTaskProvider.configure {
            compilerOptions {
              block()
            }
          }
        }
      }
    }

    else -> error("Unknown kotlin extension $this")
  }
}
