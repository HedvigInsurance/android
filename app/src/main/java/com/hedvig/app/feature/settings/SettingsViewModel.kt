package com.hedvig.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.type.Locale
import com.hedvig.app.feature.marketpicker.LanguageRepository
import com.hedvig.app.feature.marketpicker.LocaleBroadcastManager
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: LanguageRepository,
    private val localeBroadcastManager: LocaleBroadcastManager,
    hAnalytics: HAnalytics,
) : ViewModel() {
    init {
        hAnalytics.screenView(AppScreen.APP_SETTINGS)
    }

    fun save(acceptLanguage: String, locale: Locale) {
        viewModelScope.launch {
            repository.uploadLanguage(acceptLanguage, locale)
        }
        localeBroadcastManager.sendBroadcast(recreate = true)
    }
}
