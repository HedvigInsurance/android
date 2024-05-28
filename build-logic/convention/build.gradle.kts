import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  `kotlin-dsl`
}

group = "com.hedvig.android.buildlogic"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinJvmCompile>().configureEach {
  kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
}

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
  compileOnly(libs.kotlinter.gradlePlugin)
  // Not sure why this can't be compileOnly. Not a big deal, but might figure it out in the future
  implementation(libs.apollo.gradlePlugin)

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
    createPlugin("hedvig.android.apollo", "ApolloConventionPlugin")
    createPlugin("hedvig.android.application", "ApplicationConventionPlugin")
    createPlugin("hedvig.android.application.compose", "ApplicationComposeConventionPlugin")
    createPlugin("hedvig.android.feature", "FeatureConventionPlugin")
    createPlugin("hedvig.android.ktlint", "KtlintConventionPlugin")
    createPlugin("hedvig.android.library", "LibraryConventionPlugin")
    createPlugin("hedvig.android.library.compose", "LibraryComposeConventionPlugin")
    createPlugin("hedvig.kotlin.library", "KotlinLibraryConventionPlugin")
    createPlugin("hedvig.lint", "HedvigLintConventionPlugin")
  }
}
