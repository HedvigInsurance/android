plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      // api: the field's signature exposes TextFieldState, KeyboardActionHandler and
      // HedvigTextFieldDefaults, so a consumer cannot call it without seeing these.
      api(libs.jetbrains.compose.foundation)
      api(projects.coreCommonPublic)
      api(projects.designSystemHedvig)
    }
  }
}
