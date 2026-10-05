package com.hedvig.android.language

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * A context whose resources resolve in [language]. Android otherwise picks the language for its resources itself:
 * below API 33 the application context ignores an in-app choice, and for a phone set to [Deutsch, Svenska] Android
 * picks Swedish where [resolveLanguage] picks English.
 *
 * Only the locale is replaced and the rest of the configuration is copied, so it works the same on every API level.
 * It is not an Activity context, so use it for resources only.
 */
fun Context.withAppLanguage(language: Language): Context {
  val configuration = Configuration(resources.configuration)
  configuration.setLocale(Locale.forLanguageTag(language.toBcp47Format()))
  return createConfigurationContext(configuration)
}
