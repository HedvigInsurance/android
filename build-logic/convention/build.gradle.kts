plugins {
    `kotlin-dsl`
}

group = "com.hedvig.android.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("hedvigAndroidApplicationCompose") {
            id = "hedvig.android.application.compose"
            implementationClass = "HedvigAndroidApplicationComposeConventionPlugin"
        }
        register("hedvigAndroidApplication") {
            id = "hedvig.android.application"
            implementationClass = "HedvigAndroidApplicationConventionPlugin"
        }
        register("hedvigAndroidLibraryCompose") {
            id = "nowinandroid.android.library.compose"
            implementationClass = "HedvigAndroidLibraryComposeConventionPlugin"
        }
        register("hedvigAndroidLibrary") {
            id = "hedvig.android.library"
            implementationClass = "HedvigAndroidLibraryConventionPlugin"
        }
    }
}
