package com.hedvig.android.language

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * A context whose resources resolve in [language], for text built outside of an Activity, like notifications. Below
 * API 33 the application context ignores an in-app language choice, and Android's own resource matching can pick a
 * different language than [resolveLanguage] when the phone's first language is unsupported.
 */
fun Context.withAppLanguage(language: Language): Context {
  val configuration = Configuration(resources.configuration)
  configuration.setLocale(Locale.forLanguageTag(language.toBcp47Format()))
  return createConfigurationContext(configuration)
}
