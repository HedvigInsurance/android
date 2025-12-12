package com.hedvig.android.design.system.hedvig.datepicker

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import com.hedvig.android.core.locale.CommonLocale
import kotlin.time.Instant

fun formatInstantForTalkBack(context: Context, instant: Instant): String {
  val timeInMillis = instant.toEpochMilliseconds()
  return DateUtils.formatDateTime(
    context,
    timeInMillis,
    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_YEAR,
  )
}

@Composable
@ReadOnlyComposable
actual fun getLocale(): CommonLocale {
  val configuration = LocalConfiguration.current
  return ConfigurationCompat.getLocales(configuration).get(0) ?: LocaleListCompat.getAdjustedDefault()[0]!!
}
