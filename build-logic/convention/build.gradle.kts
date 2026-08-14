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
    compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")
  }
}

dependencies {
  // build-logic owns the whole build's plugin classpath. Every plugin the build applies is an
  // `implementation` dependency here (not `compileOnly`), so it lives on this classloader: the
  // `hedvig.settings` precompiled settings plugin loads them, the convention plugins' handler code
  // (ApolloHandler, ComposeHandler, ...) can reference their types at runtime, and both settings and
  // project scripts apply them by id without a version, keeping the catalog the single source of truth.
  // Kotlin compiler plugins (metro, kmpNativeCoroutines, ksp) must sit on the same classloader as the
  // Kotlin plugin, which is exactly here. Add new build plugins to this list. AGP carries the
  // com.android.{application,library,lint,kotlin.multiplatform.library} ids and KGP carries
  // org.jetbrains.kotlin.{android,jvm,multiplatform}, so only the remaining ids need their own marker.
  implementation(libs.android.gradlePlugin)
  implementation(libs.kotlin.gradlePlugin)
  implementation(libs.compose.compilerGradlePlugin)
  implementation(libs.compose.gradlePlugin)
  implementation(libs.kotlinSerialization.gradlePlugin)
  implementation(libs.apollo.gradlePlugin)
  implementation(libs.kotlinter.gradlePlugin)
  implementation(libs.room.gradlePlugin)
  implementation(libs.dedebug.gradlePlugin)
  implementation(libs.gradleDevelocity.gradlePlugin)
  implementation(libs.ksp.gradlePlugin)
  implementation(libs.metro.gradlePlugin)
  implementation(libs.kmpNativeCoroutines.gradlePlugin)
  implementation(libs.cacheFix.gradlePlugin)
  implementation(libs.crashlytics.gradlePlugin)
  implementation(libs.datadog.gradlePlugin)
  implementation(libs.googleServices.gradlePlugin)
  implementation(libs.license.gradlePlugin)
  implementation(libs.appIconBannerGenerator.gradlePlugin)
  implementation(libs.squareSortDependencies.gradlePlugin)

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
