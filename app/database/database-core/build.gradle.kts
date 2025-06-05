plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
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
