package com.hedvig.android.design.system.internals

import com.hedvig.android.design.system.hedvig.api.CommonLocale
import androidx.compose.material3.CalendarLocale

internal actual fun CommonLocale.toCalendarLocale(): CalendarLocale {
  return this
}
