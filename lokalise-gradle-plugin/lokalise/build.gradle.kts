import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

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
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinJvmCompile>().configureEach {
  kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
}

gradlePlugin {
  plugins {
    create("hedvigAndroidLokalise") {
      id = "hedvig.android.lokalise"
      implementationClass = "com.hedvig.android.lokalise.HedvigLokalisePlugin"
    }
  }
}
