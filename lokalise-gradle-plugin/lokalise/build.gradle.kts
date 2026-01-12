import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
  `embedded-kotlin`
  `java-gradle-plugin`
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.ktor.client.cio)
  implementation(libs.ktor.client.contentNegotiation)
  implementation(libs.ktor.client.core)
  implementation(libs.ktor.client.json)
  implementation(libs.okio)
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_21)
    languageVersion = KotlinVersion.KOTLIN_2_2
  }
}

gradlePlugin {
  plugins {
    create("hedvigAndroidLokalise") {
      id = "hedvig.android.lokalise"
      implementationClass = "com.hedvig.android.lokalise.HedvigLokalisePlugin"
    }
  }
}
