import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion

fun LibraryExtension.commonConfig() {
    compileSdk = AndroidVersions.compileSdkVersion

    defaultConfig {
        minSdk = AndroidVersions.minSdkVersion
        targetSdk = AndroidVersions.compileSdkVersion
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        maybeCreate("staging")
        maybeCreate("pullrequest")

        named("debug") {}
        named("staging") {}
        named("pullrequest") {}
        named("release") {}
    }
}

fun BaseAppModuleExtension.commonConfig() {
    compileSdk = AndroidVersions.compileSdkVersion

    defaultConfig {
        minSdk = AndroidVersions.minSdkVersion
        targetSdk = AndroidVersions.compileSdkVersion
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
