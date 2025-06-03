package com.hedvig.android

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

fun Project.configureKotlinMultiplatform() {
  val project = this@configureKotlinMultiplatform
  val libs = the<LibrariesForLibs>()

  configureKotlinAndroidMultiplatform()
//  extensions.configure<KotlinMultiplatformAndroidLibraryExtension> {
//  }
}
