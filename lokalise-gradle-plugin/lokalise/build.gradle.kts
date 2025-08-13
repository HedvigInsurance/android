import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
  `embedded-kotlin`
  `java-gradle-plugin`
}

dependencies {
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp.core)
  implementation(libs.okio)
}

java {
  sourceCompatibility = JavaVersion.VERSION_23
  targetCompatibility = JavaVersion.VERSION_23
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_23)
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
