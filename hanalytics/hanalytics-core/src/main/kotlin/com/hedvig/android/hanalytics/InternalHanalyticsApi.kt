package com.hedvig.android.hanalytics

@RequiresOptIn(
  level = RequiresOptIn.Level.ERROR,
  message = "This is internal API for hanalytics modules, not to be used from other modules",
)
internal annotation class InternalHanalyticsApi
