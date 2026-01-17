package com.hedvig.android.design.system.internals

import androidx.compose.material3.CalendarLocale
import com.hedvig.android.core.locale.CommonLocale

internal actual fun CommonLocale.toCalendarLocale(): CalendarLocale {
  return this
}
