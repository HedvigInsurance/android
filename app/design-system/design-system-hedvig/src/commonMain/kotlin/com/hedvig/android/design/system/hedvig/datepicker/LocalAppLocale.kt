package com.hedvig.android.design.system.hedvig.datepicker

import androidx.compose.runtime.staticCompositionLocalOf
import com.hedvig.android.core.locale.CommonLocale

/**
 * The locale of the language the app is shown in, provided at the app's root, so dates follow the app language.
 * Compose's `Locale.current` and `LocalConfiguration` carry the phone's locale or Android's own pick, en-US for
 * example, which formats dates for another region than the app's.
 *
 * Null outside the app's root composition, such as in previews, where formatting falls back to the configuration.
 */
val LocalAppLocale = staticCompositionLocalOf<CommonLocale?> { null }
