package com.hedvig.app.feature.settings

import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.feature.marketpicker.LanguageRepository

class SettingsViewModel(
    private val repository: LanguageRepository
) : ViewModel() {
    fun save(acceptLanguage: String, locale: Locale) =
        repository.setLanguage(acceptLanguage, locale)
}
