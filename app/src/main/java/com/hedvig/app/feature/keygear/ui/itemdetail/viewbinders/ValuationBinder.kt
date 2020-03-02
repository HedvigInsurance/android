package com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders

import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import com.hedvig.android.owldroid.fragment.KeyGearItemValuationFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.KeyGearTracker
import com.hedvig.app.feature.keygear.KeyGearValuationActivity
import com.hedvig.app.feature.keygear.KeyGearValuationInfoActivity
import com.hedvig.app.feature.keygear.ValuationType
import com.hedvig.app.feature.keygear.ui.ValuationData
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.safeLet
import kotlinx.android.synthetic.main.key_gear_item_detail_valuation_section.view.*

class ValuationBinder(
    private val root: LinearLayout,
    private val tracker: KeyGearTracker
) {

    fun bind(data: KeyGearItemQuery.KeyGearItem) {
        when (val valuation = data.fragments.keyGearItemFragment.fragments.keyGearItemValuationFragment.valuation) {
            is KeyGearItemValuationFragment.AsKeyGearItemValuationFixed -> {
                bindValuation(data, valuation.ratio)
            }
            is KeyGearItemValuationFragment.AsKeyGearItemValuationMarketValue -> {
                bindValuation(data, valuation.ratio)
            }
            null -> {
                root.valuationMoreInfo.remove()

                root.addPurchaseInfo.show()
                root.addPurchaseInfo.setHapticClickListener {
                    tracker.addPurchaseInfo()
                    root.context.startActivity(
                        KeyGearValuationActivity.newInstance(
                            root.context,
                            data.fragments.keyGearItemFragment.id
                        )
                    )
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

    private fun valuationType(item: KeyGearItemQuery.KeyGearItem): ValuationType? {
        return when (item.fragments.keyGearItemFragment.fragments.keyGearItemValuationFragment.valuation) {
            is KeyGearItemValuationFragment.AsKeyGearItemValuationFixed -> ValuationType.FIXED
            is KeyGearItemValuationFragment.AsKeyGearItemValuationMarketValue -> ValuationType.MARKET_PRICE
            else -> null
        }
    }

    private fun bindValuation(data: KeyGearItemQuery.KeyGearItem, ratio: Int) {
        root.addPurchaseInfo.remove()

        root.valuationMoreInfo.show()
        val category = data.fragments.keyGearItemFragment.category
        root.valuationMoreInfo.setHapticClickListener {
            tracker.valuationMoreInfo()
            safeLet(
                data.fragments.keyGearItemFragment.purchasePrice?.amount,
                data
            ) { amount, item ->
                val type = valuationType(item)
                if (type != null) {
                    if (type == ValuationType.FIXED) {
                        ContextCompat.startActivity(
                            root.context,
                            KeyGearValuationInfoActivity.newInstance(
                                root.context,
                                category,
                                ValuationData.from(
                                    amount,
                                    type,
                                    (item.fragments.keyGearItemFragment.fragments.keyGearItemValuationFragment.valuation as KeyGearItemValuationFragment.AsKeyGearItemValuationFixed).ratio,
                                    (item.fragments.keyGearItemFragment.fragments.keyGearItemValuationFragment.valuation as KeyGearItemValuationFragment.AsKeyGearItemValuationFixed).valuation.amount
                                )
                            ),
                            null
                        )
                    } else if (type == ValuationType.MARKET_PRICE) {
                        ContextCompat.startActivity(
                            root.context,
                            KeyGearValuationInfoActivity.newInstance(
                                root.context,
                                item.fragments.keyGearItemFragment.category,
                                ValuationData.from(
                                    amount,
                                    type,
                                    (item.fragments.keyGearItemFragment.fragments.keyGearItemValuationFragment.valuation as KeyGearItemValuationFragment.AsKeyGearItemValuationMarketValue).ratio
                                )
                            ),
                            null
                        )
                    }
                }
            }
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
