plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

hedvig {
  room { resolve("app/database/schemas") }
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.room.paging)
  implementation(libs.room.runtime)
  implementation(projects.dataChat)
}
