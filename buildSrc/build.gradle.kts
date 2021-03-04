plugins {
    `kotlin-dsl`
}
repositories {
    jcenter()
    google()
}
dependencies {
    implementation("com.android.tools.build:gradle:4.1.2")
    implementation(kotlin("gradle-plugin", version = "1.4.10"))
}
