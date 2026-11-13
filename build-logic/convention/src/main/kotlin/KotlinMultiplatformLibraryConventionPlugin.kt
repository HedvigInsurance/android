import com.hedvig.android.configureKotlinCompilerOptions
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.UsesKotlinJavaToolchain

/**
 * A Kotlin Multiplatform library with a JVM target, plus an Android target when
 * `hedvig.multiplatform.library.android` is also applied. Never part of the iOS umbrella.
 */
class KotlinMultiplatformLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.applyKotlinMultiplatform(withIos = false)
  }
}

/**
 * A Kotlin Multiplatform library that compiles for iOS so `:umbrella` can export it, directly or
 * transitively. Adds the iOS targets on top of the JVM and Android ones, so every `expect` in the
 * module needs a native actual. A module keeps this plugin even while `:umbrella` does not currently
 * reach it, so it stays exportable without a porting effort.
 */
class KotlinMultiplatformIosLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.applyKotlinMultiplatform(withIos = true)
  }
}

private fun Project.applyKotlinMultiplatform(withIos: Boolean) {
  val libs = the<LibrariesForLibs>()
  with(pluginManager) {
    apply(libs.plugins.kotlinMultiplatform.get().pluginId)
  }
  configureKotlinMultiplatform(withIos)
  registerTestLifecycleTask()
}

/**
 * Gives the module a `test` task, so `./gradlew test` and `./gradlew :module:test` run its unit tests like they do
 * in Android and JVM modules. The Kotlin plugin names the JVM test task `jvmTest`, and the Android host tests run
 * as `testAndroidHostTest` in modules that enable them with `withHostTest {}`.
 */
private fun Project.registerTestLifecycleTask() {
  tasks.register("test") {
    group = "verification"
    description = "Runs the JVM unit tests, and the Android host tests when the module has them."
    dependsOn("jvmTest")
    dependsOn(tasks.named { it == "testAndroidHostTest" })
  }
}

/**
 * Configure base Kotlin Multiplatform libraries for all the standard targets
 */
private fun Project.configureKotlinMultiplatform(withIos: Boolean) {
  val project = this@configureKotlinMultiplatform
  val libs = the<LibrariesForLibs>()

  project.configure<KotlinMultiplatformExtension> {
    // Until Nov 2026 every native binary carried
    //   freeCompilerArgs += "-Xdisable-phases=DevirtualizationAnalysis,DCEPhase,RemoveRedundantCallsToStaticInitializersPhase"
    // as a workaround for the release XCFramework link running out of memory on CI (KT-76047, KT-78518).
    // Kotlin 2.4 cut the devirtualization memory use (KT-80367) and the umbrella stopped exporting
    // Compose, so the link fits again. If the umbrella grows and CI OOMs on the link once more, put the
    // flag back here, starting with DevirtualizationAnalysis alone since that is the phase that blows up.
    // The size cost of the flag was measured at ~24 KB, so it is a safe lever. Check with the umbrella
    // workflow's dry_run input before merging an export change.
    if (withIos) {
      iosArm64()
      iosSimulatorArm64()
    }
    jvm {
      compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
      }
      // The JDK 21 toolchain and release that `hedvig.jvm.library` gives a plain JVM module, set on the JVM target's
      // tasks only, so the Android target's compilations are left to AGP.
      val javaToolchains = project.extensions.getByType<JavaToolchainService>()
      val jdk21 = JavaLanguageVersion.of(JavaVersion.VERSION_21.majorVersion)
      compilations.configureEach {
        compileTaskProvider.configure {
          (this as UsesKotlinJavaToolchain).kotlinJavaToolchain.toolchain.use(
            javaToolchains.launcherFor { languageVersion.set(jdk21) },
          )
        }
        compileJavaTaskProvider?.configure {
          javaCompiler.set(javaToolchains.compilerFor { languageVersion.set(jdk21) })
          options.release.set(JavaVersion.VERSION_21.majorVersion.toInt())
        }
      }
      testRuns.configureEach {
        executionTask.configure {
          javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(jdk21) })
        }
      }
    }
    compilerOptions {
      configureKotlinCompilerOptions()
    }
    applyDefaultHierarchyTemplate()

    sourceSets.getByName("commonMain") {
      dependencies {
        implementation(libs.kotlin.stdlib)
      }
    }
    sourceSets.getByName("commonTest") {
      dependencies {
        implementation(libs.kotlin.test)
      }
    }
  }
}
