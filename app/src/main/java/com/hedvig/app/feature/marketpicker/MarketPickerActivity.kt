package com.hedvig.app.feature.marketpicker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.language.LanguageAndMarketViewModel
import com.hedvig.app.feature.language.LanguageSelectionTracker
import com.hedvig.app.feature.marketing.ui.MarketingActivity
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

        marketList.adapter = MarketAdapter(model)
        marketList.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
                compatDrawable(R.drawable.divider)?.let { setDrawable(it) }
            }
        )

        model.markets.observe(this) { list ->
            list?.let {
                (marketList.adapter as? MarketAdapter)?.items = list
            }
        }
        model.loadGeo()
        val languageAdapter = LanguageAdapterNew(model)
        languageList.adapter = languageAdapter
        model.markets.observe(this) { markets ->
            markets?.let {
                val language = model.languages.value
                language?.let { list ->
                    val availableLanguages = list.filter { it.available }
                    languageAdapter.items = availableLanguages
                }
                try {
                    val market = model.markets.value?.first { it.selected }?.market
                    market?.let { market ->
                        languageAdapter.selectedMarket = market
                    }
                } catch (e: Exception) {

                }
            }
        }
        languageList.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
                compatDrawable(R.drawable.divider)?.let { setDrawable(it) }
            }
        )

        save.setHapticClickListener {
            model.save()
            goToMarketingActivity()
        }

        model.isLanguageSelected.observe(this) { isLanguageSelected ->
            isLanguageSelected?.let {
                save.isEnabled = isLanguageSelected
            }
        }
    }

    private fun goToMarketingActivity() {
        startActivity(MarketingActivity.newInstance(this, true))
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, MarketPickerActivity::class.java)
    }
}
