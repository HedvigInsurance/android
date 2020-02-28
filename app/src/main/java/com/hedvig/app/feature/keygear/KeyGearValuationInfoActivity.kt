package com.hedvig.app.feature.keygear

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.ValuationData
import com.hedvig.app.feature.keygear.ui.createitem.label
import com.hedvig.app.util.extensions.setMarkdownText
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.interpolateTextKey
import com.hedvig.app.util.safeLet
import kotlinx.android.synthetic.main.activity_key_gear_valuation_info.*

class KeyGearValuationInfoActivity : BaseActivity(R.layout.activity_key_gear_valuation_info) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val category = intent.getSerializableExtra(CATEGORY) as? KeyGearItemCategory
        val valuationData = intent.getParcelableExtra<ValuationData>(VALUATION_DATA)

        safeLet(category, valuationData) { c, vd ->
            setPercentage(vd.ratio)
            if (vd.valuationType == ValuationType.FIXED) {
                body.setMarkdownText(
                    interpolateTextKey(
                        getString(R.string.KEY_GEAR_ITEM_VIEW_VALUATION_BODY),
                        "ITEM_TYPE" to getString(c.label).toLowerCase(),
                        "VALUATION_PERCENTAGE" to vd.ratio,
                        "PURCHASE_PRICE" to vd.purchasePrice.toBigDecimal().toInt(),
                        "VALUATION_PRICE" to vd.valuationAmount?.toBigDecimal()?.toInt()
                    )
                )
            } else if (vd.valuationType == ValuationType.MARKET_PRICE) {
                //TODO
                body.setMarkdownText(
                    interpolateTextKey(
                        getString(R.string.KEY_GEAR_ITEM_VIEW_VALUATION_MARKET_BODY),
                        "ITEM_TYPE" to getString(c.label),
                        "VALUATION_PERCENTAGE" to vd.ratio
                    )
                )
            }
        }

        close.setHapticClickListener {
            onBackPressed()
        }
    }

    private fun setPercentage(percentage: Int) {
        valuationPercentage.text = "$percentage%"
    }

    companion object {
        private const val CATEGORY = "CATEGORY"
        private const val VALUATION_DATA = "VALUATION_DATA"

        fun newInstance(
            context: Context,
            category: KeyGearItemCategory,
            valuationData: ValuationData
        ) =
            Intent(context, KeyGearValuationInfoActivity::class.java).apply {
                putExtra(CATEGORY, category)
                putExtra(VALUATION_DATA, valuationData)
            }
    }
}

