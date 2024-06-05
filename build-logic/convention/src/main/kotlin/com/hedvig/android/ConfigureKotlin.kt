package com.hedvig.android

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

/**
 * Configure base Kotlin without Android options
 */
internal fun Project.configureKotlin() {
  kotlinExtension.forEachCompilerOptions {
    configureKotlinCompilerOptions(this@configureKotlin)
  }
  project.extensions.getByType(JavaPluginExtension::class.java).apply {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
  }
  project.tasks.withType(JavaCompile::class.java).configureEach {
    options.release.set(17)
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
