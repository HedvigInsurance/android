package com.hedvig.android.design.system.internals

import androidx.compose.material3.CalendarLocale
import com.hedvig.android.design.system.hedvig.api.CommonLocale

internal actual fun CommonLocale.toCalendarLocale(): CalendarLocale {
  return this
}
