plugins {
  `kotlin-dsl`
}

group = "com.hedvig.android.buildlogic"

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  implementation(libs.android.gradlePlugin)
  implementation(libs.kotlin.gradlePlugin)
  implementation(libs.ktlint.gradlePlugin)
}

gradlePlugin {
  plugins {
    register("hedvigAndroidApplicationCompose") {
      id = "hedvig.android.application.compose"
      implementationClass = "ApplicationComposeConventionPlugin"
    }
    register("hedvigAndroidApplication") {
      id = "hedvig.android.application"
      implementationClass = "ApplicationConventionPlugin"
    }
    register("hedvigAndroidLibraryCompose") {
      id = "hedvig.android.library.compose"
      implementationClass = "LibraryComposeConventionPlugin"
    }
    register("hedvigAndroidLibrary") {
      id = "hedvig.android.library"
      implementationClass = "LibraryConventionPlugin"
    }
    register("hedvigAndroidKtlint") {
      id = "hedvig.android.ktlint"
      implementationClass = "KtlintConventionPlugin"
    }
  }
}
