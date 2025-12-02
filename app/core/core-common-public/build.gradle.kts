import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilation
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
      common {
        group("jvmAndAndroid") {
          withAndroidLibraryTarget()
          withJvm()
        }
      }
    }
    commonMain.dependencies {
      implementation(libs.coroutines.core)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.datetime)
    }

    val jvmAndAndroidMain by getting {
      dependencies {
        api(libs.okhttp.core)
      }
    }

    androidMain.apply {
      dependencies {
        implementation(libs.androidx.other.core)
        implementation(projects.apolloOctopusPublic)
        implementation(projects.coreResources)
      }
    }
  }
}
