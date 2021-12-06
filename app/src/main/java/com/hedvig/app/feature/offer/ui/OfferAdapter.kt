package com.hedvig.app.feature.offer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.app.BASE_MARGIN
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.BASE_MARGIN_OCTUPLE
import com.hedvig.app.BASE_MARGIN_TRIPLE
import com.hedvig.app.R
import com.hedvig.app.databinding.GenericErrorBinding
import com.hedvig.app.databinding.InfoCardBinding
import com.hedvig.app.databinding.OfferFactAreaBinding
import com.hedvig.app.databinding.OfferFaqBinding
import com.hedvig.app.databinding.OfferFooterBinding
import com.hedvig.app.databinding.OfferHeaderBinding
import com.hedvig.app.databinding.OfferSwitchBinding
import com.hedvig.app.databinding.TextBody2Binding
import com.hedvig.app.databinding.TextHeadline5Binding
import com.hedvig.app.databinding.TextSubtitle1Binding
import com.hedvig.app.databinding.WarningCardBinding
import com.hedvig.app.feature.faq.FAQBottomSheet
import com.hedvig.app.feature.offer.OfferRedeemCodeBottomSheet
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheet
import com.hedvig.app.feature.offer.ui.composable.insurely.InsurelyCard
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.table.generateTable
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.drawableAttr
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.setMarkdownText
import com.hedvig.app.util.extensions.setStrikethrough
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding

class OfferAdapter(
    private val fragmentManager: FragmentManager,
    private val tracker: OfferTracker,
    private val marketManager: MarketManager,
    private val openQuoteDetails: (quoteID: String) -> Unit,
    private val onRemoveDiscount: () -> Unit,
    private val onSign: (SignMethod) -> Unit,
    private val reload: () -> Unit,
    private val openChat: () -> Unit,
) : ListAdapter<OfferModel, OfferAdapter.ViewHolder>(OfferDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.offer_header -> ViewHolder.Header(
            parent,
            marketManager,
            fragmentManager,
            tracker,
            onSign,
            onRemoveDiscount
        )
        R.layout.offer_fact_area -> ViewHolder.Facts(parent)
        R.layout.offer_switch -> ViewHolder.Switch(parent)
        R.layout.offer_footer -> ViewHolder.Footer(parent, openChat)
        R.layout.text_headline5 -> ViewHolder.Subheading(parent)
        R.layout.text_body2 -> ViewHolder.Paragraph(parent)
        INSURELY_HEADER -> ViewHolder.InsurelyHeader(ComposeView(parent.context))
        INSURELY_DIVIDER -> ViewHolder.InsurelyDivider(ComposeView(parent.context))
        INSURELY_CARD -> ViewHolder.InsurelyCard(ComposeView(parent.context))
        R.layout.text_subtitle1 -> ViewHolder.QuoteDetails(parent, openQuoteDetails)
        R.layout.offer_faq -> ViewHolder.FAQ(parent, fragmentManager)
        R.layout.info_card -> ViewHolder.InfoCard(parent)
        R.layout.warning_card -> ViewHolder.WarningCard(parent)
        R.layout.generic_error -> ViewHolder.Error(parent, reload)
        else -> throw Error("Invalid viewType: $viewType")
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is OfferModel.Header -> R.layout.offer_header
        is OfferModel.Facts -> R.layout.offer_fact_area
        is OfferModel.CurrentInsurer -> R.layout.offer_switch
        is OfferModel.Footer -> R.layout.offer_footer
        is OfferModel.Subheading -> R.layout.text_headline5
        is OfferModel.Paragraph -> R.layout.text_body2
        is OfferModel.CurrentInsurancesHeader -> INSURELY_HEADER
        is OfferModel.InsurelyDivider -> INSURELY_DIVIDER
        is OfferModel.InsurelyCard -> INSURELY_CARD
        is OfferModel.QuoteDetails -> R.layout.text_subtitle1
        is OfferModel.FAQ -> R.layout.offer_faq
        OfferModel.AutomaticSwitchCard -> R.layout.info_card
        OfferModel.ManualSwitchCard -> R.layout.warning_card
        OfferModel.Error -> R.layout.generic_error
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ViewHolder) {
        val itemView = holder.itemView
        if (itemView is ComposeView) {
            itemView.disposeComposition()
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: OfferModel)

        class Header(
            parent: ViewGroup,
            private val marketManager: MarketManager,
            private val fragmentManager: FragmentManager,
            private val tracker: OfferTracker,
            private val onSign: (SignMethod) -> Unit,
            private val onRemoveDiscount: () -> Unit,
        ) : ViewHolder(parent.inflate(R.layout.offer_header)) {
            private val binding by viewBinding(OfferHeaderBinding::bind)

            override fun bind(data: OfferModel) {
                if (data !is OfferModel.Header) {
                    return invalid(data)
                }
                binding.apply {
                    title.text = data.title ?: itemView.context.getString(R.string.OFFER_INSURANCE_BUNDLE_TITLE)

                    premium.text = data.premium.format(premium.context, marketManager.market)
                    premiumPeriod.text = premiumPeriod.context.getString(R.string.OFFER_PRICE_PER_MONTH)

                    originalPremium.isVisible = data.hasDiscountedPrice
                    if (data.hasDiscountedPrice) {
                        originalPremium.setStrikethrough(true)
                        originalPremium.text =
                            data.originalPremium.format(originalPremium.context, marketManager.market)
                    }

                    startDateContainer.setHapticClickListener {
                        tracker.chooseStartDate()
                        ChangeDateBottomSheet.newInstance(data.changeDateBottomSheetData)
                            .show(fragmentManager, ChangeDateBottomSheet.TAG)
                    }

                    startDateLabel.text = data.startDateLabel.toString(startDateLabel.context)
                    startDate.text = data.startDate.getString(itemView.context)

                    val campaignText = data.incentiveDisplayValue.joinToString()
                    campaign.text = campaignText
                    campaign.isVisible = campaignText.isNotBlank()

                    discountButton.isVisible = data.showCampaignManagement
                    if (data.hasCampaigns) {
                        discountButton.apply {
                            setText(R.string.OFFER_REMOVE_DISCOUNT_BUTTON)
                            setTextColor(context.colorAttr(R.attr.colorError))
                            icon = null
                            setHapticClickListener {
                                tracker.removeDiscount()
                                discountButton.context.showAlert(
                                    R.string.OFFER_REMOVE_DISCOUNT_ALERT_TITLE,
                                    R.string.OFFER_REMOVE_DISCOUNT_ALERT_DESCRIPTION,
                                    R.string.OFFER_REMOVE_DISCOUNT_ALERT_REMOVE,
                                    R.string.OFFER_REMOVE_DISCOUNT_ALERT_CANCEL,
                                    {
                                        onRemoveDiscount()
                                    }
                                )
                            }
                        }
                    } else {
                        discountButton.apply {
                            setText(R.string.OFFER_ADD_DISCOUNT_BUTTON)
                            setTextColor(context.getColor(R.color.textColorSecondary))
                            icon = context.compatDrawable(R.drawable.ic_add_circle)
                            setHapticClickListener {
                                tracker.addDiscount()
                                OfferRedeemCodeBottomSheet.newInstance()
                                    .show(
                                        fragmentManager,
                                        OfferRedeemCodeBottomSheet.TAG
                                    )
                            }
                        }
                    }

                    with(sign) {
                        text = data.checkoutLabel.toString(context)
                        icon = data.signMethod.checkoutIconRes()?.let {
                            context.compatDrawable(it)
                        }
                        setHapticClickListener {
                            tracker.checkoutHeader(data.checkoutLabel.localizationKey(context))
                            onSign(data.signMethod)
                        }
                    }
                    root.background = data.gradientType.toDrawable(itemView.context)
                }
            }
        }

        class Facts(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_fact_area)) {
            private val binding by viewBinding(OfferFactAreaBinding::bind)

            init {
                binding.expandableContentView.initialize()
            }

            override fun bind(data: OfferModel) {
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

            override fun bind(data: OfferModel) = with(binding) {
                if (data !is OfferModel.CurrentInsurer) {
                    return invalid(data)
                }

                associatedQuote.isVisible = data.associatedQuote != null
                data.associatedQuote?.let { associatedQuote.text = it }
                currentInsurer.text = data.displayName
            }
        }

        class Footer(
            parent: ViewGroup,
            openChat: () -> Unit,
        ) : ViewHolder(parent.inflate(R.layout.offer_footer)) {
            private val binding by viewBinding(OfferFooterBinding::bind)

            init {
                binding.chatButton.setHapticClickListener { openChat() }
            }

            override fun bind(data: OfferModel) {
                if (data !is OfferModel.Footer) {
                    return invalid(data)
                }
                val checkoutString = data.checkoutLabel.toString(itemView.context)
                val link = itemView.context.getString(
                    R.string.OFFER_FOOTER_GDPR_INFO,
                    checkoutString,
                    itemView.context.getString(R.string.PRIVACY_POLICY_URL)
                )
                binding.text.setMarkdownText(link)
            }
        }

        class Subheading(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.text_headline5)) {
            private val binding by viewBinding(TextHeadline5Binding::bind)

            init {
                binding.root.updateMargin(
                    start = BASE_MARGIN_DOUBLE,
                    top = BASE_MARGIN_DOUBLE,
                    end = BASE_MARGIN_DOUBLE,
                )
            }

            override fun bind(data: OfferModel) = with(binding.root) {
                if (data !is OfferModel.Subheading) {
                    return invalid(data)
                }

                when (data) {
                    OfferModel.Subheading.Coverage -> {
                        setText(R.string.offer_screen_coverage_title)
                        updateMargin(bottom = BASE_MARGIN)
                    }
                }
            }
        }

        class QuoteDetails(
            parent: ViewGroup,
            private val openQuoteDetails: (quoteID: String) -> Unit,
        ) : ViewHolder(parent.inflate(R.layout.text_subtitle1)) {
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

            override fun bind(data: OfferModel) = with(binding.root) {
                if (data !is OfferModel.QuoteDetails) {
                    return invalid(data)
                }
                text = data.name
                setHapticClickListener { openQuoteDetails(data.id) }
            }
        }

        class Paragraph(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.text_body2)) {
            private val binding by viewBinding(TextBody2Binding::bind)

            init {
                binding.root.updateMargin(
                    start = BASE_MARGIN_DOUBLE,
                    top = BASE_MARGIN_DOUBLE,
                    end = BASE_MARGIN_DOUBLE,
                    bottom = BASE_MARGIN_TRIPLE,
                )
            }

            override fun bind(data: OfferModel) = with(binding.root) {
                if (data !is OfferModel.Paragraph.Coverage) {
                    return invalid(data)
                }

                setText(R.string.offer_screen_MULTIPLE_INSURANCES_coverage_paragraph)
            }
        }

        class InsurelyHeader(
            private val composeView: ComposeView,
        ) : ViewHolder(composeView) {
            init {
                composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            }

            override fun bind(data: OfferModel) {
                if (data !is OfferModel.CurrentInsurancesHeader) return invalid(data)
                val resources = composeView.context.resources
                composeView.setContent {
                    HedvigTheme {
                        Text(
                            text = resources.getQuantityString(
                                R.plurals.offer_switcher_title,
                                data.amountOfCurrentInsurances,
                            ),
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 48.dp)
                        )
                    }
                }
            }
        }

        class InsurelyDivider(
            private val composeView: ComposeView,
        ) : ViewHolder(composeView) {
            init {
                composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            }

            override fun bind(data: OfferModel) {
                if (data !is OfferModel.InsurelyDivider) return invalid(data)
                composeView.setContent {
                    HedvigTheme {
                        Divider(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = data.topPadding)
                        )
                    }
                }
            }
        }

        class InsurelyCard(
            private val composeView: ComposeView,
        ) : ViewHolder(composeView) {
            init {
                composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            }

            override fun bind(data: OfferModel) {
                if (data !is OfferModel.InsurelyCard) return invalid(data)
                composeView.setContent {
                    HedvigTheme {
                        InsurelyCard(
                            data = data,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        class FAQ(
            parent: ViewGroup,
            private val fragmentManager: FragmentManager,
        ) : ViewHolder(parent.inflate(R.layout.offer_faq)) {
            private val binding by viewBinding(OfferFaqBinding::bind)

            override fun bind(data: OfferModel) = with(binding) {
                if (data !is OfferModel.FAQ) {
                    return invalid(data)
                }

                rowContainer.removeAllViews()

                val layoutInflater = LayoutInflater.from(rowContainer.context)

                data.items.forEach { item ->
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
                        text = item.headline
                        setHapticClickListener {
                            FAQBottomSheet
                                .newInstance(item)
                                .show(fragmentManager, FAQBottomSheet.TAG)
                        }
                    }

                    rowContainer.addView(rowBinding.root)
                }
            }
        }

        class InfoCard(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.info_card)) {
            private val binding by viewBinding(InfoCardBinding::bind)

            override fun bind(data: OfferModel) = with(binding) {
                if (data !is OfferModel.AutomaticSwitchCard) {
                    return invalid(data)
                }

                title.setText(R.string.offer_switch_info_card_title)
                body.setText(R.string.offer_switch_info_card_body)
            }
        }

        class WarningCard(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.warning_card)) {
            private val binding by viewBinding(WarningCardBinding::bind)

            override fun bind(data: OfferModel) = with(binding) {
                if (data !is OfferModel.ManualSwitchCard) {
                    return invalid(data)
                }

                title.setText(R.string.offer_manual_switch_card_title)
                body.setText(R.string.offer_manual_switch_card_body)
            }
        }

        class Error(
            parent: ViewGroup,
            private val reload: () -> Unit,
        ) : ViewHolder(parent.inflate(R.layout.generic_error)) {
            private val binding by viewBinding(GenericErrorBinding::bind)

            init {
                binding.root.setPadding(0, BASE_MARGIN_OCTUPLE, 0, 0)
                binding.root.setBackgroundColor(binding.root.context.colorAttr(android.R.attr.colorBackground))
            }

            override fun bind(data: OfferModel) = with(binding.retry) {
                if (data !is OfferModel.Error) {
                    return invalid(data)
                }
                setHapticClickListener { reload() }
            }
        }
    }

    companion object {
        const val INSURELY_CARD = 1
        const val INSURELY_HEADER = 2
        const val INSURELY_DIVIDER = 3

        class OfferDiffUtilCallback : DiffUtil.ItemCallback<OfferModel>() {
            override fun areItemsTheSame(oldItem: OfferModel, newItem: OfferModel): Boolean = when {
                oldItem is OfferModel.InsurelyCard && newItem is OfferModel.InsurelyCard -> {
                    oldItem.id == newItem.id
                }
                oldItem is OfferModel.CurrentInsurancesHeader && newItem is OfferModel.CurrentInsurancesHeader -> {
                    // Should only display 1 CurrentInsurancesHeader ever
                    true
                }
                else -> {
                    oldItem == newItem
                }
            }

            override fun areContentsTheSame(oldItem: OfferModel, newItem: OfferModel) = oldItem == newItem
        }
    }
}
