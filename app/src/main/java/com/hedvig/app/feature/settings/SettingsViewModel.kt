package com.hedvig.app.feature.settings

import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.feature.marketpicker.LanguageRepository
import com.hedvig.app.feature.marketpicker.LocaleBroadcastManager
import com.hedvig.hanalytics.HAnalytics

class SettingsViewModel(
    private val repository: LanguageRepository,
    private val localeBroadcastManager: LocaleBroadcastManager,
    hAnalytics: HAnalytics,
) : ViewModel() {
    init {
        hAnalytics.screenViewAppSettings()
    }

    fun save(acceptLanguage: String, locale: Locale) {
        repository.uploadLanguage(acceptLanguage, locale)
        localeBroadcastManager.sendBroadcast(recreate = true)
    }
}
