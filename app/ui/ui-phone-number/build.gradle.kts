plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  // api: the field's signature exposes TextFieldState, KeyboardActionHandler and
  // HedvigTextFieldDefaults, so a consumer cannot call it without seeing these.
  api(libs.androidx.compose.foundation)
  api(projects.coreCommonPublic)
  api(projects.designSystemHedvig)
}
