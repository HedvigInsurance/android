package com.hedvig.android.core.common.android

import android.content.Context
import android.content.res.Configuration

val Context.isDarkThemeActive: Boolean
  get() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
