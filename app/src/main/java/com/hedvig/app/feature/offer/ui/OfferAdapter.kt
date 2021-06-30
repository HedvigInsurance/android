package com.hedvig.app.feature.offer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnNextLayout
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.BASE_MARGIN_SEPTUPLE
import com.hedvig.app.BASE_MARGIN_TRIPLE
import com.hedvig.app.R
import com.hedvig.app.databinding.OfferFactAreaBinding
import com.hedvig.app.databinding.OfferFaqBinding
import com.hedvig.app.databinding.OfferFooterBinding
import com.hedvig.app.databinding.OfferHeaderBinding
import com.hedvig.app.databinding.OfferSwitchBinding
import com.hedvig.app.databinding.TextBody2Binding
import com.hedvig.app.databinding.TextHeadline5Binding
import com.hedvig.app.databinding.TextSubtitle1Binding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.offer.OfferRedeemCodeBottomSheet
import com.hedvig.app.feature.offer.OfferSignDialog
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheet
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.table.generateTable
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.drawableAttr
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.app.util.extensions.setMarkdownText
import com.hedvig.app.util.extensions.setStrikethrough
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding
import e
import javax.money.MonetaryAmount

class OfferAdapter(
    private val fragmentManager: FragmentManager,
    private val tracker: OfferTracker,
    private val marketManager: MarketManager,
    private val removeDiscount: () -> Unit,
    private val openQuoteDetails: (quoteID: String) -> Unit,
) : ListAdapter<OfferModel, OfferAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.offer_header -> ViewHolder.Header(parent)
        R.layout.offer_fact_area -> ViewHolder.Facts(parent)
        R.layout.offer_switch -> ViewHolder.Switch(parent)
        R.layout.offer_footer -> ViewHolder.Footer(parent)
        R.layout.text_headline5 -> ViewHolder.Subheading(parent)
        R.layout.text_body2 -> ViewHolder.Paragraph(parent)
        R.layout.text_subtitle1 -> ViewHolder.QuoteDetails(parent, openQuoteDetails)
        R.layout.offer_faq -> ViewHolder.FAQ(parent)
        R.layout.offer_loading_header -> ViewHolder.Loading(parent)
        else -> throw Error("Invalid viewType: $viewType")
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is OfferModel.Header -> R.layout.offer_header
        is OfferModel.Facts -> R.layout.offer_fact_area
        is OfferModel.Switcher -> R.layout.offer_switch
        is OfferModel.Footer -> R.layout.offer_footer
        is OfferModel.Subheading -> R.layout.text_headline5
        is OfferModel.Paragraph -> R.layout.text_body2
        is OfferModel.QuoteDetails -> R.layout.text_subtitle1
        is OfferModel.FAQ -> R.layout.offer_faq
        OfferModel.Loading -> R.layout.offer_loading_header
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
                    title.text = data.title ?: itemView.context.getString(R.string.OFFER_INSURANCE_BUNDLE_TITLE)
                    premium.text = data.netMonthlyCost.format(premium.context, marketManager.market)
                    premiumPeriod.text = premiumPeriod.context.getString(R.string.OFFER_PRICE_PER_MONTH)

                    if (!(data.grossMonthlyCost - data.netMonthlyCost).isZero) {
                        grossPremium.setStrikethrough(true)
                        grossPremium.text = data.grossMonthlyCost.format(grossPremium.context, marketManager.market)
                    } else {
                        grossPremium.setStrikethrough(false)
                    }

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
                marketManager: MarketManager,
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

        class Subheading(parent: ViewGroup) : OfferAdapter.ViewHolder(parent.inflate(R.layout.text_headline5)) {
            private val binding by viewBinding(TextHeadline5Binding::bind)

            init {
                binding.root.updateMargin(
                    start = BASE_MARGIN_DOUBLE,
                    end = BASE_MARGIN_DOUBLE,
                )
            }

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager,
            ) = with(binding.root) {
                if (data !is OfferModel.Subheading) {
                    return invalid(data)
                }

                when (data) {
                    OfferModel.Subheading.Coverage -> {
                        updateMargin(
                            top = BASE_MARGIN_SEPTUPLE
                        )
                        setText(R.string.offer_screen_coverage_title)
                    }
                }
            }
        }

        class QuoteDetails(
            parent: ViewGroup,
            private val openQuoteDetails: (quoteID: String) -> Unit,
        ) : OfferAdapter.ViewHolder(parent.inflate(R.layout.text_subtitle1)) {
            private val binding by viewBinding(TextSubtitle1Binding::bind)

            init {
                with(binding.root) {
                    updatePaddingRelative(
                        start = BASE_MARGIN_DOUBLE,
                        top = BASE_MARGIN_DOUBLE,
                        end = BASE_MARGIN_DOUBLE,
                        bottom = BASE_MARGIN_DOUBLE,
                    )
                    setBackgroundResource(context.drawableAttr(android.R.attr.selectableItemBackground))
                }
            }

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager,
            ) = with(binding.root) {
                if (data !is OfferModel.QuoteDetails) {
                    return invalid(data)
                }
                text = data.name
                setHapticClickListener { openQuoteDetails(data.id) }
            }
        }

        class Paragraph(parent: ViewGroup) : OfferAdapter.ViewHolder(parent.inflate(R.layout.text_body2)) {
            private val binding by viewBinding(TextBody2Binding::bind)

            init {
                binding.root.updateMargin(
                    start = BASE_MARGIN_DOUBLE,
                    top = BASE_MARGIN_DOUBLE,
                    end = BASE_MARGIN_DOUBLE,
                    bottom = BASE_MARGIN_TRIPLE,
                )
            }

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager,
            ) = with(binding.root) {
                if (data !is OfferModel.Paragraph) {
                    return invalid(data)
                }

                setText(
                    when (data) {
                        OfferModel.Paragraph.Coverage -> R.string.offer_screen_MULTIPLE_INSURANCES_coverage_paragraph
                    }
                )
            }
        }

        class FAQ(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_faq)) {
            private val binding by viewBinding(OfferFaqBinding::bind)

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager,
            ) = with(binding) {
                if (data !is OfferModel.FAQ) {
                    return invalid(data)
                }

                rowContainer.removeAllViews()

                val layoutInflater = LayoutInflater.from(rowContainer.context)

                data.items.forEach { (headline, _) ->
                    val rowBinding = TextSubtitle1Binding.inflate(
                        layoutInflater,
                        rowContainer,
                        false
                    )

                    with(rowBinding.root) {
                        updatePaddingRelative(
                            start = BASE_MARGIN_DOUBLE,
                            top = BASE_MARGIN_DOUBLE,
                            end = BASE_MARGIN_DOUBLE,
                            bottom = BASE_MARGIN_DOUBLE,
                        )
                        setBackgroundResource(context.drawableAttr(android.R.attr.selectableItemBackground))
                        text = headline
                    }

                    rowContainer.addView(rowBinding.root)
                }
            }
        }

        class Loading(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_loading_header)) {
            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager,
            ) = Unit
        }
    }
}
