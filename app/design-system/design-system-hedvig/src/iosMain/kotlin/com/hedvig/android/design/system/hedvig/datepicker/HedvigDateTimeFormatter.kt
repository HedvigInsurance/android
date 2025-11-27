package com.hedvig.android.design.system.hedvig.datepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.hedvig.android.design.system.hedvig.api.CommonLocale
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

@Composable
@ReadOnlyComposable
actual fun getLocale(): CommonLocale {
  return NSLocale.currentLocale
}
