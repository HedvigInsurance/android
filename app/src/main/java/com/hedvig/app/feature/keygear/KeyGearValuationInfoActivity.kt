package com.hedvig.app.feature.keygear

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.setMarkdownText
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.interpolateTextKey
import kotlinx.android.synthetic.main.activity_key_gear_valuation_info.*
import org.koin.android.viewmodel.ext.android.viewModel

class KeyGearValuationInfoActivity : BaseActivity(R.layout.activity_key_gear_valuation_info) {

    private val model: KeyGearValuationInfoViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra(ITEM_ID)

        close.setHapticClickListener {
            onBackPressed()
        }

        model.data.observe(this) { data ->
            data?.let {
                val category = data.fragments.keyGearItemFragment.category.toString().toLowerCase()
                val purchasePrice = data.fragments.keyGearItemFragment.purchasePrice?.amount
                val valuationPercentage =
                    (data.fragments.keyGearItemFragment.valuation as KeyGearItemFragment.AsKeyGearItemValuationFixed).ratio
                val valuationPrice =
                    (data.fragments.keyGearItemFragment.valuation as KeyGearItemFragment.AsKeyGearItemValuationFixed).valuation.amount

                setPercentage(valuationPercentage)
                body.setMarkdownText(
                    interpolateTextKey(
                        getString(R.string.KEY_GEAR_ITEM_VIEW_VALUATION_BODY),
                        "ITEM_TYPE" to category,
                        "VALUATION_PERCENTAGE" to valuationPercentage,
                        "PURCHASE_PRICE" to purchasePrice,
                        "VALUATION_PRICE" to valuationPrice
                    )
                )
            }
        }
        model.loadItem(id)
    }

    private fun setPercentage(percentage: Int) {
        valuationPercentage.text = "$percentage%"
    }

    companion object {
        private const val ITEM_ID = "ITEM_ID"

        fun newInstance(context: Context, id: String) =
            Intent(context, KeyGearValuationInfoActivity::class.java).apply {
                putExtra(ITEM_ID, id)
            }
    }
}


