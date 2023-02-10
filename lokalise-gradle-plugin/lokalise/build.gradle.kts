plugins {
  `embedded-kotlin`
  `java-gradle-plugin`
}

dependencies {
  implementation(libs.okio)
  implementation(libs.okhttp.core)
  implementation(libs.serialization.json)
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

gradlePlugin {
  plugins {
    create("hedvigAndroidLokalise") {
      id = "hedvig.android.lokalise"
      implementationClass = "com.hedvig.android.lokalise.HedvigLokalisePlugin"
    }
  }
}
