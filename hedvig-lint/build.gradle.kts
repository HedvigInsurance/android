import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
  }
}
