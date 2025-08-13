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
  sourceCompatibility = JavaVersion.VERSION_23
  targetCompatibility = JavaVersion.VERSION_23
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_23)
  }
}
