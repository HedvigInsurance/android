package com.hedvig.android.language

import android.content.Context
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.preference.PreferenceManager
import com.hedvig.android.core.common.preferences.PreferenceKey
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import java.util.Locale

class LanguageService(
  private val context: Context,
  private val marketManager: MarketManager,
) {
  private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

  /**
   * Sets the language, and as a side effect, restarts all running activities.
   * Only safe to call from the Main Thread.
   */
  @MainThread
  fun setLanguage(language: Language) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.toString()))
  }

  fun getLanguage(): Language {
    return Language.from(getLocale().toLanguageTag())
  }

  fun getLocale(): Locale {
    val localeList = AppCompatDelegate.getApplicationLocales()
    if (localeList.isEmpty) {
      return Locale("en", "SE")
    }
    return localeList[0]!!
  }

  fun getGraphQLLocale(): com.hedvig.android.apollo.graphql.type.Locale {
    return when (getLocale().toString()) {
      "en_NO" -> com.hedvig.android.apollo.graphql.type.Locale.en_NO
      "nb_NO" -> com.hedvig.android.apollo.graphql.type.Locale.nb_NO
      "sv_SE" -> com.hedvig.android.apollo.graphql.type.Locale.sv_SE
      "en_SE" -> com.hedvig.android.apollo.graphql.type.Locale.en_SE
      "da_DK" -> com.hedvig.android.apollo.graphql.type.Locale.da_DK
      "en_DK" -> com.hedvig.android.apollo.graphql.type.Locale.en_DK
      else -> com.hedvig.android.apollo.graphql.type.Locale.en_SE
    }
  }

  fun performOnLaunchLanguageCheck() {
    performOneTimeLanguageMigration()
    val currentLanguage = AppCompatDelegate.getApplicationLocales()
    // Always default to English, Sweden, if nothing else is set
    if (currentLanguage.isEmpty) {
      setLanguage(Language.EN_SE)
    }
  }

  private fun performOneTimeLanguageMigration() {
    // We're already migrated, and hence we don't need to do anything
    if (preferences.contains(PreferenceKey.SETTING_LANGUAGE).not()) {
      return
    }
    // Retrieve the language from settings
    val storedLanguage = languageFromSettings(context, marketManager.market)
    // Set it using official APIs
    setLanguage(storedLanguage)
    // Clear it out from storage
    preferences.edit {
      remove(PreferenceKey.SETTING_LANGUAGE)
    }
  }

  private fun languageFromSettings(context: Context, market: Market?): Language = when (market) {
    null -> Language.from(Language.SETTING_EN_SE)
    else -> getLanguageFromSharedPreferences(context, market)
  }

  private fun getLanguageFromSharedPreferences(context: Context, market: Market): Language {
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    val firstAvailableLanguage = Language.getAvailableLanguages(market).first().toString()
    val selectedLanguage = sharedPref.getString(
      PreferenceKey.SETTING_LANGUAGE,
      firstAvailableLanguage,
    ) ?: firstAvailableLanguage

    return if (selectedLanguage == Language.SETTING_SYSTEM_DEFAULT) {
      Language.from(firstAvailableLanguage)
    } else {
      Language.from(selectedLanguage)
    }
  }
}
