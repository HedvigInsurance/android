package com.hedvig.android

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

internal fun KotlinCommonCompilerOptions.configureKotlinCompilerOptions() {
  when (this) {
    is KotlinJvmCompilerOptions -> {
      configureCommonKotlinCompilerOptions(listOf("-Xjvm-default=all"))
      jvmTarget.set(JvmTarget.JVM_21)
    }
    else -> {
      configureCommonKotlinCompilerOptions()
    }
  }
}

private fun KotlinCommonCompilerOptions.configureCommonKotlinCompilerOptions(
  extraFreeCompilerArgs: List<String> = emptyList(),
) {
  apiVersion.set(KotlinVersion.KOTLIN_2_0)
  languageVersion.set(KotlinVersion.KOTLIN_2_0)
  freeCompilerArgs.addAll(commonFreeCompilerArgs().plus(extraFreeCompilerArgs))
}

private fun commonFreeCompilerArgs(): List<String> {
  return buildList {
    addAll(
      listOf(
        "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
        "-opt-in=androidx.compose.animation.ExperimentalSharedTransitionApi",
        "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
        "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
        "-opt-in=androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi",
        "-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi",
        "-opt-in=kotlin.Experimental",
        "-opt-in=kotlin.RequiresOptIn",
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-opt-in=kotlinx.coroutines.FlowPreview",
        "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
      ),
    )
  }
}
