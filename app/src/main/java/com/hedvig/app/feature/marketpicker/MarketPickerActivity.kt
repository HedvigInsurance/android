package com.hedvig.app.feature.marketpicker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.language.LanguageSelectionTracker
import com.hedvig.app.feature.language.LanguageViewModel
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.makeLocaleString
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.activity_market_picker.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class MarketPickerActivity : BaseActivity(R.layout.activity_market_picker) {
    private val countryModel: MarketPickerViewModelImpl by viewModel()
    private val languageViewModel: LanguageViewModel by viewModel()
    private val tracker: LanguageSelectionTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languageList.adapter = LanguageAdapterNew(languageViewModel, Country.SV)
        countryModel.selectedMarket.observe(this) { market ->
            market?.let {
                languageList.adapter = LanguageAdapterNew(languageViewModel, market)
            }
        }
        countryList.adapter = MarketAdapter(countryModel)

        save.setHapticClickListener {
            val language = languageViewModel.selectedLanguage.value
            language?.let {
                setLanguage(language, languageViewModel)
            }
            goToMarketingActivity()
        }
    }

    @SuppressLint("ApplySharedPref") // We want to apply this right away. It's important
    private fun setLanguage(language: Language, languageViewModel: LanguageViewModel) {
        PreferenceManager
            .getDefaultSharedPreferences(this)
            .edit()
            .putString(SettingsActivity.SETTING_LANGUAGE, language.toString())
            .commit()

        language.apply(this)?.let { language ->
            languageViewModel.updateLanguage(makeLocaleString(language))
        }

        LocalBroadcastManager
            .getInstance(this)
            .sendBroadcast(Intent(LOCALE_BROADCAST))
    }

    private fun goToMarketingActivity() {
        startActivity(Intent(this, MarketingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
    }
}
