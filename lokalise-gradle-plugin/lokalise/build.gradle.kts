plugins {
  `java-gradle-plugin`
  kotlin("jvm") version "1.6.21"
}

dependencies {
  compileOnly("dev.gradleplugins:gradle-api:7.5")
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
  implementation(libs.okio)
  implementation(libs.okhttp.core)
  implementation(libs.serialization)
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

gradlePlugin {
  plugins {
    create("hedvigAndroidLokalise") {
      id = "hedvig.android.lokalise"
      implementationClass = "com.hedvig.android.lokalise.HedvigLokalisePlugin"
    }
  }
}
