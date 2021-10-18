package com.hedvig.app.feature.crossselling.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.offer.quotedetail.QuoteDetailAction
import com.hedvig.app.feature.offer.quotedetail.QuoteDetailActivity
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows

class CrossSellDetailActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crossSell = intent.getParcelableExtra<CrossSellData>(CROSS_SELL)
            ?: throw IllegalArgumentException("Programmer error: CROSS_SELL not passed to ${this.javaClass.name}")

        window.compatSetDecorFitsSystemWindows(false)

        setContent {
            HedvigTheme {
                CrossSellDetailScreen(
                    onCtaClick = { handleAction(this, crossSell.action) },
                    onUpClick = { finish() },
                    onCoverageClick = { openCoverage(crossSell) },
                    data = crossSell,
                )
            }
        }
    }

    private fun openCoverage(crossSell: CrossSellData) {
        val perils = crossSell.perils.map { PerilItem.Peril(it) }
        startActivity(
            QuoteDetailActivity.newInstance(
                context = this,
                title = getString(R.string.cross_sell_info_full_coverage_row),
                perils = perils,
                insurableLimits = crossSell.insurableLimits,
                documents = crossSell.terms,
                action = QuoteDetailAction(
                    action = crossSell.action,
                    label = crossSell.callToAction,
                )
            )
        )
    }

    companion object {
        private const val CROSS_SELL = "CROSS_SELL"
        fun newInstance(context: Context, crossSell: CrossSellData) = Intent(
            context,
            CrossSellDetailActivity::class.java,
        ).apply {
            putExtra(CROSS_SELL, crossSell)
        }
    }
}
