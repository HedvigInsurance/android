import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Action
import org.gradle.api.JavaVersion

data class AndroidVersions(
    val compileSdk: Int = 31,
    val minSdk: Int = 23,
    val targetSdk: Int = 30,
)

fun LibraryExtension.commonConfig(androidVersions: AndroidVersions = AndroidVersions()) {
    compileSdk = androidVersions.compileSdk

    defaultConfig {
        minSdk = androidVersions.minSdk
        targetSdk = androidVersions.compileSdk
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        maybeCreate("staging")
        maybeCreate("pullrequest")

        named("debug") {}
        named("staging") {}
        named("pullrequest") {}
        named("release") {}
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

fun BaseAppModuleExtension.commonConfig(androidVersions: AndroidVersions = AndroidVersions()) {
    compileSdk = androidVersions.compileSdk

    defaultConfig {
        minSdk = androidVersions.minSdk
        targetSdk = androidVersions.compileSdk
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

fun LibraryExtension.kotlinOptions(
    configure: Action<org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions>
) {
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("kotlinOptions", configure)
}

fun BaseAppModuleExtension.kotlinOptions(
    configure: Action<org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions>
) {
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("kotlinOptions", configure)
}
