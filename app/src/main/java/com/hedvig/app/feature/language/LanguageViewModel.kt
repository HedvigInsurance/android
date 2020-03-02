package com.hedvig.app.feature.language

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.settings.Language

class LanguageViewModel(
    private val languageRepository: LanguageRepository
) : ViewModel() {
    val selectedLanguage = MutableLiveData<Language>()

    fun selectLanguage(language: Language) {
        selectedLanguage.postValue(language)
    }

    fun updateLanguage(acceptLanguage: String) {
        languageRepository
            .setLanguage(acceptLanguage)
    }
}
