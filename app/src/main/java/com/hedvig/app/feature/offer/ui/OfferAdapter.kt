package com.hedvig.app.feature.offer.ui

import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.R
import com.hedvig.app.databinding.OfferFactAreaBinding
import com.hedvig.app.databinding.OfferFooterBinding
import com.hedvig.app.databinding.OfferHeaderBinding
import com.hedvig.app.databinding.OfferPerilAreaBinding
import com.hedvig.app.databinding.OfferSwitchBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.offer.OfferRedeemCodeBottomSheet
import com.hedvig.app.feature.offer.OfferSignDialog
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheet
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.table.generateTable
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.setMarkdownText
import com.hedvig.app.util.extensions.setStrikethrough
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.svg.buildRequestBuilder
import e
import javax.money.MonetaryAmount

class OfferAdapter(
    private val fragmentManager: FragmentManager,
    private val tracker: OfferTracker,
    private val marketManager: MarketManager,
    private val removeDiscount: () -> Unit,
) : ListAdapter<OfferModel, OfferAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.offer_header -> ViewHolder.Header(parent)
        R.layout.offer_fact_area -> ViewHolder.Facts(parent)
        R.layout.offer_peril_area -> ViewHolder.Perils(parent)
        R.layout.offer_switch -> ViewHolder.Switch(parent)
        R.layout.offer_footer -> ViewHolder.Footer(parent)
        else -> throw Error("Invalid viewType: $viewType")
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is OfferModel.Header -> R.layout.offer_header
        is OfferModel.Facts -> R.layout.offer_fact_area
        is OfferModel.Perils -> R.layout.offer_peril_area
        is OfferModel.Switcher -> R.layout.offer_switch
        is OfferModel.Footer -> R.layout.offer_footer
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), fragmentManager, tracker, removeDiscount, marketManager)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            data: OfferModel,
            fragmentManager: FragmentManager,
            tracker: OfferTracker,
            removeDiscount: () -> Unit,
            marketManager: MarketManager,
        )

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_header)) {
            private val binding by viewBinding(OfferHeaderBinding::bind)

            private operator fun MonetaryAmount.minus(other: MonetaryAmount) = subtract(other)

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager,
            ) {
                if (data !is OfferModel.Header) {
                    return invalid(data)
                }
                binding.apply {
                    title.text = data.title

                    premium.text = data.netMonthlyCost.format(premium.context, marketManager.market)
                    premiumPeriod.text = premiumPeriod.context.getString(R.string.OFFER_PRICE_PER_MONTH)

                    if (!(data.grossMonthlyCost - data.netMonthlyCost).isZero) {
                        grossPremium.setStrikethrough(true)
                        grossPremium.text = data.grossMonthlyCost.format(grossPremium.context, marketManager.market)
                    } else {
                        grossPremium.setStrikethrough(false)
                    }

                    // TODO: This needs to be remade to support multiple start dates
                    // startDateContainer.setHapticClickListener {
                    //    tracker.chooseStartDate()
                    //    ChangeDateBottomSheet.newInstance(
                    //        ChangeDateBottomSheetData(
                    //            quote.id,
                    //            quote.currentInsurer?.switchable == true
                    //        )
                    //    )
                    //        .show(
                    //            fragmentManager,
                    //            ChangeDateBottomSheet.TAG
                    //        )
                    // }

                    // val sd = quote.startDate

                    // if (sd != null) {
                    //    if (sd == LocalDate.now()) {
                    //        startDate.setText(R.string.START_DATE_TODAY)
                    //    } else {
                    //        startDate.text = sd.toString()
                    //    }
                    // } else {
                    //    if (quote.currentInsurer?.switchable == true) {
                    //        startDate.setText(R.string.ACTIVATE_INSURANCE_END_BTN)
                    //    } else {
                    //        startDate.setText(R.string.START_DATE_TODAY)
                    //    }
                    // }

                    val incentiveDisplayValue = data.incentiveDisplayValue
                    if (incentiveDisplayValue != null) {
                        discountButton.setText(R.string.OFFER_REMOVE_DISCOUNT_BUTTON)
                        campaign.text = incentiveDisplayValue
                        discountButton.context.colorAttr(R.attr.colorError)
                        discountButton.setHapticClickListener {
                            tracker.removeDiscount()
                            discountButton.context.showAlert(
                                R.string.OFFER_REMOVE_DISCOUNT_ALERT_TITLE,
                                R.string.OFFER_REMOVE_DISCOUNT_ALERT_DESCRIPTION,
                                R.string.OFFER_REMOVE_DISCOUNT_ALERT_REMOVE,
                                R.string.OFFER_REMOVE_DISCOUNT_ALERT_CANCEL,
                                {
                                    removeDiscount()
                                }
                            )
                        }
                    } else {
                        discountButton.setText(R.string.OFFER_ADD_DISCOUNT_BUTTON)
                        premiumContainer.background = null
                        campaign.remove()
                        discountButton.setHapticClickListener {
                            tracker.addDiscount()
                            OfferRedeemCodeBottomSheet.newInstance()
                                .show(
                                    fragmentManager,
                                    ChangeDateBottomSheet.TAG
                                )
                        }
                    }

                    sign.setHapticClickListener {
                        tracker.floatingSign()
                        OfferSignDialog.newInstance().show(
                            fragmentManager,
                            OfferSignDialog.TAG
                        )
                    }
                }
            }
        }

        class Facts(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_fact_area)) {
            private val binding by viewBinding(OfferFactAreaBinding::bind)

            init {
                binding.expandableContentView.initialize()
            }

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager,
            ) {
                if (data !is OfferModel.Facts) {
                    return invalid(data)
                }
                generateTable(binding.expandableContent, data.table)
                binding.expandableContentView.doOnNextLayout {
                    binding.expandableContentView.contentSizeChanged()
                }
            }
        }

        class Perils(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_peril_area)) {
            private val binding by viewBinding(OfferPerilAreaBinding::bind)

            init {
                binding.perils.addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN_HALF))
            }

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager,
            ) {
                binding.apply {
                    if (perils.adapter == null) {
                        perils.adapter =
                            PerilsAdapter(fragmentManager, perils.context.buildRequestBuilder())
                    }

                    if (data !is OfferModel.Perils) {
                        return invalid(data)
                    }
                    (perils.adapter as? PerilsAdapter)?.submitList(data.inner)

                    // TODO: What do we do here? Idk
                    // when (data.inner.lastQuoteOfMember.asCompleteQuote?.typeOfContract) {
                    //     TypeOfContract.SE_HOUSE -> {
                    //         perilInfo.setText(R.string.OFFER_SCREEN_COVERAGE_BODY_HOUSE)
                    //     }
                    //     TypeOfContract.SE_APARTMENT_BRF,
                    //     TypeOfContract.SE_APARTMENT_STUDENT_BRF,
                    //     TypeOfContract.NO_HOME_CONTENT_OWN,
                    //     TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
                    //     -> {
                    //         perilInfo.setText(R.string.OFFER_SCREEN_COVERAGE_BODY_BRF)
                    //     }
                    //     TypeOfContract.NO_HOME_CONTENT_RENT,
                    //     TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
                    //     TypeOfContract.SE_APARTMENT_RENT,
                    //     TypeOfContract.SE_APARTMENT_STUDENT_RENT,
                    //     -> {
                    //         perilInfo.setText(R.string.OFFER_SCREEN_COVERAGE_BODY_RENTAL)
                    //     }
                    //     else -> {
                    //     }
                    // }
                }
            }
        }

        class Switch(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_switch)) {
            private val binding by viewBinding(OfferSwitchBinding::bind)
            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager,
            ) {
                if (data is OfferModel.Switcher) {
                    val insurer = data.displayName
                        ?: binding.switchTitle.resources.getString(R.string.OTHER_INSURER_OPTION_APP)
                    binding.switchTitle.text = binding.switchTitle.resources.getString(
                        R.string.OFFER_SWITCH_TITLE_APP,
                        insurer
                    )
                    return
                }

                e { "Invariant detected: ${data.javaClass.name} passed to ${this.javaClass.name}::bind" }
            }
        }

        class Footer(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_footer)) {
            private val binding by viewBinding(OfferFooterBinding::bind)
            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager
            ) {
                if (data !is OfferModel.Footer) {
                    return invalid(data)
                }
                binding.chatButton.setHapticClickListener {
                    it.context.startActivity(ChatActivity.newInstance(it.context, true))
                }
                val link = itemView.context.getString(R.string.OFFER_FOOTER_GDPR_INFO, data.url)
                binding.text.setMarkdownText(link)
            }
        }
    }
}
