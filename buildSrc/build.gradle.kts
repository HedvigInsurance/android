plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(kotlin("gradle-plugin", version = "1.6.10"))
}
