package com.hedvig.android.design.system.hedvig.datepicker

import androidx.compose.runtime.staticCompositionLocalOf
import com.hedvig.android.core.locale.CommonLocale

/**
 * The locale of the language the app is shown in, provided at the app root, so dates follow the app language rather
 * than the phone's region.
 */
val LocalAppLocale = staticCompositionLocalOf<CommonLocale?> { null }
