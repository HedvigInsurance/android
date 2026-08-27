import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("hedvig.gradle.plugin")
  `java-library`
  id("org.jetbrains.kotlin.jvm")
  id("com.android.lint")
}

dependencies {
  compileOnly(libs.lintApi)
  testImplementation(libs.junit)
  testImplementation(libs.lintApi)
  testImplementation(libs.lintTests)
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
