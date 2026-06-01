plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  room { resolve("app/database/schemas") }
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.room.paging)
  api(libs.room.runtime)
  implementation(projects.coreCommonPublic)
  api(projects.dataChat)
}
