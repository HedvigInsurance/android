package com.hedvig.app.feature.marketpicker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.language.LanguageAndMarketViewModel
import com.hedvig.app.feature.language.LanguageSelectionTracker
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.makeLocaleString
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.activity_market_picker.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class MarketPickerActivity : BaseActivity(R.layout.activity_market_picker) {
    private val model: LanguageAndMarketViewModel by viewModel()
    private val tracker: LanguageSelectionTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var marketAdapter = MarketAdapter(model, 0)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        model.preselectedMarket.observe(this) { marketString ->
            marketString?.let {
                val market = Market.valueOf(marketString)
                marketAdapter = MarketAdapter(model, market.ordinal)
                marketList.adapter = marketAdapter
                marketList.addItemDecoration(
                    DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
                        compatDrawable(R.drawable.divider)?.let { setDrawable(it) }
                    }
                )
            }

        }
        model.loadGeo()

        languageList.adapter = LanguageAdapterNew(model, Market.SE)
        model.selectedMarket.observe(this) { market ->
            market?.let {
                languageList.adapter = LanguageAdapterNew(model, market)
            }
        }
        languageList.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
                compatDrawable(R.drawable.divider)?.let { setDrawable(it) }
            }
        )

        save.setHapticClickListener {
            marketAdapter.let {
                marketAdapter.getSelectedMarket().let { market ->
                    sharedPreferences.edit()
                        .putInt(Market.MARKET_SHARED_PREF, market)
                        .commit()
                }
            }
            val language = model.selectedLanguage.value
            language?.let {
                setLanguage(language)
            }
            goToMarketingActivity()
        }

        model.selectedLanguage.observe(this) { language ->
            save.isEnabled = language != null
        }
    }

    @SuppressLint("ApplySharedPref") // We want to apply this right away. It's important
    private fun setLanguage(
        language: Language
    ) {
        PreferenceManager
            .getDefaultSharedPreferences(this)
            .edit()
            .putString(SettingsActivity.SETTING_LANGUAGE, language.toString())
            .commit()

        language.apply(this)?.let { language ->
            model.updateLanguage(makeLocaleString(language))
        }

        LocalBroadcastManager
            .getInstance(this)
            .sendBroadcast(Intent(LOCALE_BROADCAST))
    }

    private fun goToMarketingActivity() {
        startActivity(MarketingActivity.newInstance(this, true))
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, MarketPickerActivity::class.java)
    }
}
