package com.hedvig.app.feature.marketpicker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.hedvig.app.BaseActivity
import com.hedvig.app.MockActivity
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.marketPickerModule
import com.hedvig.app.util.extensions.getMarket
import com.hedvig.app.util.extensions.setMarket
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class MarketPickerMockActivity : MockActivity() {
    override val original = listOf(marketPickerModule)
    override val mocks = listOf(module {
        viewModel<MarketPickerViewModel> { MockMarketPickerViewModel(baseContext) }
    })
    private var originalMarket: Market? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalMarket = getMarket()
    }

    override fun adapter() = genericDevelopmentAdapter {
        header("Market Picker")
        clickableItem("Market: SE /w Swedish") {
            setMarket(Market.SE)
            setLanguage(Language.SV_SE)
            startActivity(
                MarketingActivity.newInstance(this@MarketPickerMockActivity)
            )
        }
        clickableItem("Market: NO /w Norwegian") {
            setMarket(Market.NO)
            MockMarketPickerViewModel.AVAILABLE_GEO_MARKET = true
            setLanguage(Language.NB_NO)
            startActivity(
                MarketingActivity.newInstance(this@MarketPickerMockActivity)
            )
        }
        clickableItem("Market: not selected") {
            setMarket(null)
            setLanguage(Language.EN_SE)
            MockMarketPickerViewModel.AVAILABLE_GEO_MARKET = true
            startActivity(
                MarketingActivity.newInstance(this@MarketPickerMockActivity)
            )
        }
        clickableItem("Preselected geo market not avalible") {
            setMarket(null)
            setLanguage(Language.EN_SE)
            MockMarketPickerViewModel.AVAILABLE_GEO_MARKET = false
            startActivity(
                MarketingActivity.newInstance(this@MarketPickerMockActivity)
            )
        }
    }

    @SuppressLint("ApplySharedPref") // Needed
    fun setLanguage(language: Language) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences
            .edit()
            .putString(SettingsActivity.SETTING_LANGUAGE, language.toString())
            .commit()

        LocalBroadcastManager
            .getInstance(this)
            .sendBroadcast(Intent(BaseActivity.LOCALE_BROADCAST))
    }

    override fun onDestroy() {
        super.onDestroy()
        setMarket(originalMarket)
    }
}
