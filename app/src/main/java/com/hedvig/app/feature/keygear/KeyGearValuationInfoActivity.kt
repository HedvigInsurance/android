package com.hedvig.app.feature.keygear

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityKeyGearValuationInfoBinding
import com.hedvig.app.feature.keygear.ui.ValuationData
import com.hedvig.app.feature.keygear.ui.createitem.label
import com.hedvig.app.util.extensions.setMarkdownText
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.safeLet
import java.util.Locale

class KeyGearValuationInfoActivity : BaseActivity(R.layout.activity_key_gear_valuation_info) {
    private val binding by viewBinding(ActivityKeyGearValuationInfoBinding::bind)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val category = intent.getSerializableExtra(CATEGORY) as? KeyGearItemCategory
        val valuationData = intent.getParcelableExtra<ValuationData>(VALUATION_DATA)

        safeLet(category, valuationData) { c, vd ->
            setPercentage(vd.ratio)
            if (vd.valuationType == ValuationType.FIXED) {
                binding.body.setMarkdownText(
                    getString(
                        R.string.KEY_GEAR_ITEM_VIEW_VALUATION_BODY,
                        getString(c.label).lowercase(Locale.getDefault()),
                        vd.ratio,
                        vd.purchasePrice.toBigDecimal().toInt(),
                        vd.valuationAmount?.toBigDecimal()?.toInt()
                    )
                )
            } else if (vd.valuationType == ValuationType.MARKET_PRICE) {
                binding.body.setMarkdownText(
                    getString(
                        R.string.KEY_GEAR_ITEM_VIEW_VALUATION_MARKET_BODY,
                        getString(c.label),
                        vd.ratio
                    )
                )
            }
        }

        binding.close.setHapticClickListener {
            onBackPressed()
        }
    }

    private fun setPercentage(percentage: Int) {
        binding.valuationPercentage.text = "$percentage%"
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
