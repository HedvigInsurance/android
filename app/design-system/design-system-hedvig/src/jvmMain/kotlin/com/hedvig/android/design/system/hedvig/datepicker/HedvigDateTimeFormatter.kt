package com.hedvig.android.design.system.hedvig.datepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.hedvig.android.design.system.hedvig.api.CommonLocale
import java.util.Locale

@Composable
@ReadOnlyComposable
actual fun getLocale(): CommonLocale {
  return Locale.getDefault()
}
