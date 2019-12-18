package com.hedvig.app.feature.language

import androidx.lifecycle.ViewModel

class LanguageViewModel(
    private val languageRepository: LanguageRepository
) : ViewModel() {
    fun updateLanguage(acceptLanguage: String) {
        languageRepository
            .setLanguage(acceptLanguage)
    }
}
