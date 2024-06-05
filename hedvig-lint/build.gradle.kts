import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `java-library`
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.lintGradlePlugin)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  compileOnly(libs.lintApi)
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
  }
}
