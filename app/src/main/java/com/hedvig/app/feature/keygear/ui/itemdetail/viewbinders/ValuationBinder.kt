package com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders

import android.widget.LinearLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.KeyGearValuationActivity
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import kotlinx.android.synthetic.main.key_gear_item_detail_valuation_section.view.*

class ValuationBinder(
    private val root: LinearLayout
) {
    fun bind(data: KeyGearItemQuery.KeyGearItem) {
        when (val valuation = data.fragments.keyGearItemFragment.valuation) {
            is KeyGearItemFragment.AsKeyGearItemValuationFixed -> {
                bindValuation(data.fragments.keyGearItemFragment.id, valuation.ratio)
            }
            is KeyGearItemFragment.AsKeyGearItemValuationMarketValue -> {
                bindValuation(data.fragments.keyGearItemFragment.id, valuation.ratio)
            }
            null -> {
                root.valuationMoreInfo.remove()

                root.addPurchaseInfo.show()
                root.addPurchaseInfo.setHapticClickListener {
                    root.context.startActivity(KeyGearValuationActivity.newInstance(root.context, data.fragments.keyGearItemFragment.id))
                }
            }
        }
        root.deductible.text = buildSpannedString {
            bold {
                scale(2.0f) {
                    append(data.fragments.keyGearItemFragment.deductible.amount.toBigDecimal().toInt().toString())
                }
            }
            append(" ")
            append(root.resources.getString(R.string.KEY_GEAR_ITEM_VIEW_DEDUCTIBLE_CURRENCY))
            append("\n")
        }
    }

    private fun bindValuation(id: String, ratio: Int) {
        root.addPurchaseInfo.remove()

        root.valuationMoreInfo.show()
        root.valuationMoreInfo.setHapticClickListener {
            // TODO: Open the valuation description thing, with ID as reference
        }
        root.valuation.show()
        root.valuation.text = buildSpannedString {
            bold {
                scale(2.0f) {
                    append("$ratio %")
                }
            }
            append("\n")
            append(root.resources.getString(R.string.KEY_GEAR_ITEM_VIEW_VALUATION_PERCENTAGE_LABEL))
        }
    }
}
