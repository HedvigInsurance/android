import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
}

group = "com.hedvig.android.buildlogic"

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_21)
  }
}

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.androidMultiplatform.gradlePlugin)
  compileOnly(libs.apollo.gradlePlugin)
  compileOnly(libs.compose.compilerGradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
  compileOnly(libs.kotlinter.gradlePlugin)
  compileOnly(libs.room.gradlePlugin)

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
    createPlugin("hedvig.android.library", "LibraryConventionPlugin")
    createPlugin("hedvig.jvm.library", "KotlinLibraryConventionPlugin")
    createPlugin("hedvig.multiplatform.library", "KotlinMultiplatformLibraryConventionPlugin")
    createPlugin("hedvig.multiplatform.library.android", "KotlinMultiplatformAndroidLibraryConventionPlugin")
    createPlugin("hedvig.gradle.plugin", "HedvigGradlePlugin")
  }
}
