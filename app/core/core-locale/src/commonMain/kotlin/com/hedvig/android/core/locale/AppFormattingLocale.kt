package com.hedvig.android.core.locale

import kotlin.concurrent.Volatile

/**
 * The locale of the language the app is shown in, for formatting done outside of composition, like
 * `UiMoney.toString()`. The process default locale follows the phone's region instead, so an en-US phone would show
 * "99.5 kr" where the app shows "99,5 kr", and Android resets it on configuration changes.
 *
 * The app's language service writes it on the main thread and formatting reads it from any thread, hence volatile.
 * Null before the service starts.
 */
object AppFormattingLocale {
  @Volatile
  var current: CommonLocale? = null
}
