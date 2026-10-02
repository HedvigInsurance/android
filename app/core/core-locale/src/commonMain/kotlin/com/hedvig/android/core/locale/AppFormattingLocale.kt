package com.hedvig.android.core.locale

import kotlin.concurrent.Volatile

/**
 * The locale of the language the app is shown in, for formatting done outside of composition. The process default
 * locale follows the phone's region instead, and Android resets it on configuration changes.
 *
 * Kept up to date by the app's language service; null before it starts.
 */
object AppFormattingLocale {
  @Volatile
  var current: CommonLocale? = null
}
