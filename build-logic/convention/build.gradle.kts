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
  implementation(libs.kotlinter.gradlePlugin)

  // Enables using type-safe accessors to reference plugins from the [plugins] block defined in version catalogs.
  // Context: https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
  plugins {
    fun createPlugin(id: String, className: String) {
      plugins.create(id) {
        this.id = id
        implementationClass = className
      }
    }
    createPlugin("hedvig.android.application", "ApplicationConventionPlugin")
    createPlugin("hedvig.android.application.compose", "ApplicationComposeConventionPlugin")
    createPlugin("hedvig.android.ktlint", "KtlintConventionPlugin")
    createPlugin("hedvig.android.library", "LibraryConventionPlugin")
    createPlugin("hedvig.android.library.compose", "LibraryComposeConventionPlugin")
    createPlugin("hedvig.kotlin.library", "KotlinLibraryConventionPlugin")
  }
}
