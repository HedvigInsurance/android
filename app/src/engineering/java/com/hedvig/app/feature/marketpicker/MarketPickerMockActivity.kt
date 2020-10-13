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
import com.hedvig.app.util.extensions.storeBoolean
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class MarketPickerMockActivity : MockActivity() {
    override val original = listOf(marketPickerModule)
    override val mocks = listOf(module {
        viewModel<MarketPickerViewModel> { MockMarketPickerViewModel(get()) }
    })
    private var originalMarket: Market? = null
    private var originalShouldOpenMarketSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        originalMarket = getMarket()
        originalShouldOpenMarketSelected =
            pref.getBoolean(MarketingActivity.SHOULD_OPEN_MARKET_SELECTED, false)
    }

    @SuppressLint("ApplySharedPref") // Needed
    override fun adapter() = genericDevelopmentAdapter {
        val pref = this@MarketPickerMockActivity.getSharedPreferences("hedvig_shared_preference", MODE_PRIVATE)
        header("Market Picker")
        clickableItem("Market: SE /w Swedish") {
            setMarket(Market.SE)
            setLanguage(Language.SV_SE)
            storeBoolean(MarketingActivity.SHOULD_OPEN_MARKET_SELECTED, true)
            startActivity(
                MarketingActivity.newInstance(this@MarketPickerMockActivity)
            )
        }
        clickableItem("Market: NO /w Norwegian") {
            setMarket(Market.NO)
            MockMarketPickerViewModel.AVAILABLE_GEO_MARKET = true
            storeBoolean(MarketingActivity.SHOULD_OPEN_MARKET_SELECTED, true)
            setLanguage(Language.NB_NO)
            startActivity(
                MarketingActivity.newInstance(this@MarketPickerMockActivity)
            )
        }
        clickableItem("Market: not selected. SE IP Address") {
            setMarket(null)
            removeLanguage()
            pref.edit().remove(MarketingActivity.SHOULD_OPEN_MARKET_SELECTED).commit()
            MockMarketPickerViewModel.AVAILABLE_GEO_MARKET = true
            startActivity(
                MarketingActivity.newInstance(this@MarketPickerMockActivity)
            )
        }
        clickableItem("Preselected geo market not avalible") {
            setMarket(null)
            removeLanguage()
            pref.edit().remove(MarketingActivity.SHOULD_OPEN_MARKET_SELECTED).commit()
            MockMarketPickerViewModel.AVAILABLE_GEO_MARKET = false
            startActivity(
                MarketingActivity.newInstance(this@MarketPickerMockActivity)
            )
        }
    }

    @SuppressLint("ApplySharedPref") // Needed
    private fun setLanguage(language: Language) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences
            .edit()
            .putString(SettingsActivity.SETTING_LANGUAGE, language.toString())
            .commit()

        LocalBroadcastManager
            .getInstance(this)
            .sendBroadcast(Intent(BaseActivity.LOCALE_BROADCAST))
    }

    @SuppressLint("ApplySharedPref") // Needed
    private fun removeLanguage() {
        val sharedPreferences = this.getSharedPreferences("hedvig_shared_preference", MODE_PRIVATE)
        sharedPreferences
            .edit()
            .remove(SettingsActivity.SETTING_LANGUAGE)
            .commit()

        LocalBroadcastManager
            .getInstance(this)
            .sendBroadcast(Intent(BaseActivity.LOCALE_BROADCAST))
    }

    override fun onDestroy() {
        super.onDestroy()
        setMarket(originalMarket)
        storeBoolean(
            MarketingActivity.SHOULD_OPEN_MARKET_SELECTED,
            originalShouldOpenMarketSelected
        )
    }
}
