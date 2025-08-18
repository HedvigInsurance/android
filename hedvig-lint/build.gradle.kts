import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("hedvig.gradle.plugin")
  `java-library`
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.lintGradlePlugin)
}

dependencies {
  compileOnly(libs.lintApi)
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_21)
  }
}
