package com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders

import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.KeyGearItemDetailValuationSectionBinding
import com.hedvig.app.feature.keygear.KeyGearTracker
import com.hedvig.app.feature.keygear.KeyGearValuationActivity
import com.hedvig.app.feature.keygear.KeyGearValuationInfoActivity
import com.hedvig.app.feature.keygear.ValuationType
import com.hedvig.app.feature.keygear.ui.ValuationData
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.safeLet

class ValuationBinder(
    private val binding: KeyGearItemDetailValuationSectionBinding,
    private val tracker: KeyGearTracker,
) {
    fun bind(data: KeyGearItemQuery.KeyGearItem) {
        val valuation =
            data.fragments.keyGearItemFragment.fragments.keyGearItemValuationFragment.valuation
        if (valuation == null) {
            binding.valuationMoreInfo.remove()

            binding.addPurchaseInfo.show()
            binding.addPurchaseInfo.setHapticClickListener {
                tracker.addPurchaseInfo()
                binding.root.context.startActivity(
                    KeyGearValuationActivity.newInstance(
                        binding.root.context,
                        data.fragments.keyGearItemFragment.id
                    )
                )
            }
        }
        valuation?.asKeyGearItemValuationFixed?.let { fixedValuation ->
            bindValuation(data, fixedValuation.ratio)
        }
        valuation?.asKeyGearItemValuationMarketValue?.let { marketValuation ->
            bindValuation(data, marketValuation.ratio)
        }

        binding.deductible.text = buildSpannedString {
            bold {
                scale(2.0f) {
                    append(
                        data.fragments.keyGearItemFragment.deductible.amount.toBigDecimal().toInt()
                            .toString()
                    )
                }
            }
            append(" ")
            append(binding.root.resources.getString(R.string.KEY_GEAR_ITEM_VIEW_DEDUCTIBLE_CURRENCY))
            append("\n")
        }
    }

    private fun valuationType(item: KeyGearItemQuery.KeyGearItem): ValuationType? {
        val valuation =
            item.fragments.keyGearItemFragment.fragments.keyGearItemValuationFragment.valuation
        if (valuation?.asKeyGearItemValuationFixed != null) {
            return ValuationType.FIXED
        }
        if (valuation?.asKeyGearItemValuationMarketValue != null) {
            return ValuationType.MARKET_PRICE
        }
        return null
    }

    private fun bindValuation(data: KeyGearItemQuery.KeyGearItem, ratio: Int) {
        binding.addPurchaseInfo.remove()

        binding.valuationMoreInfo.show()
        val category = data.fragments.keyGearItemFragment.category
        binding.valuationMoreInfo.setHapticClickListener {
            tracker.valuationMoreInfo()
            safeLet(
                data.fragments.keyGearItemFragment.purchasePrice?.amount,
                data
            ) { amount, item ->
                val type = valuationType(item)
                if (type != null) {
                    if (type == ValuationType.FIXED) {
                        val valuation =
                            item
                                .fragments
                                .keyGearItemFragment
                                .fragments
                                .keyGearItemValuationFragment
                                .valuation
                                ?.asKeyGearItemValuationFixed
                                ?: return@safeLet
                        binding.root.context.startActivity(
                            KeyGearValuationInfoActivity.newInstance(
                                binding.root.context,
                                category,
                                ValuationData.from(
                                    amount,
                                    type,
                                    valuation.ratio,
                                    valuation.valuation.amount
                                )
                            ),
                            null
                        )
                    } else if (type == ValuationType.MARKET_PRICE) {
                        val valuation =
                            item
                                .fragments
                                .keyGearItemFragment
                                .fragments
                                .keyGearItemValuationFragment
                                .valuation
                                ?.asKeyGearItemValuationMarketValue
                                ?: return@safeLet
                        binding.root.context.startActivity(
                            KeyGearValuationInfoActivity.newInstance(
                                binding.root.context,
                                item.fragments.keyGearItemFragment.category,
                                ValuationData.from(
                                    amount,
                                    type,
                                    valuation.ratio
                                )
                            ),
                            null
                        )
                    }
                }
            }
        }
        binding.valuation.show()
        binding.valuation.text = buildSpannedString {
            bold {
                scale(2.0f) {
                    append("$ratio %")
                }
            }
            append("\n")
            append(binding.root.resources.getString(R.string.KEY_GEAR_ITEM_VIEW_VALUATION_PERCENTAGE_LABEL))
        }
    }
}
