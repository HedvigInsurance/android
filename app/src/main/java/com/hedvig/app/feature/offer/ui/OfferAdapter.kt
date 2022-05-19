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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
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
import com.hedvig.app.feature.offer.model.CheckoutMethod
import com.hedvig.app.feature.offer.model.checkoutIconRes
import com.hedvig.app.feature.offer.model.quotebundle.getString
import com.hedvig.app.feature.offer.model.quotebundle.toDrawable
import com.hedvig.app.feature.offer.model.quotebundle.toString
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheet
import com.hedvig.app.feature.offer.ui.composable.VariantButton
import com.hedvig.app.feature.offer.ui.composable.insurely.InsurelyCard
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
import java.util.Locale

class OfferAdapter(
    private val fragmentManager: FragmentManager,
    private val locale: Locale,
    private val openQuoteDetails: (quoteID: String) -> Unit,
    private val onRemoveDiscount: () -> Unit,
    private val onSign: (CheckoutMethod, PaymentMethodsApiResponse?) -> Unit,
    private val reload: () -> Unit,
    private val openChat: () -> Unit,
) : ListAdapter<OfferItems, OfferAdapter.ViewHolder>(OfferDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.offer_header -> ViewHolder.Header(
            parent,
            locale,
            fragmentManager,
            onSign,
            onRemoveDiscount
        )
        VARIANT_BUTTON -> ViewHolder.VariantButton(ComposeView(parent.context), locale)
        R.layout.offer_fact_area -> ViewHolder.Facts(parent)
        R.layout.offer_switch -> ViewHolder.Switch(parent)
        R.layout.offer_footer -> ViewHolder.Footer(parent, openChat)
        R.layout.text_headline5 -> ViewHolder.Subheading(parent)
        R.layout.text_body2 -> ViewHolder.Paragraph(parent)
        PRICE_COMPARISON_HEADER -> ViewHolder.PriceComparisonHeader(ComposeView(parent.context))
        INSURELY_DIVIDER -> ViewHolder.InsurelyDivider(ComposeView(parent.context))
        INSURELY_CARD -> ViewHolder.InsurelyCard(ComposeView(parent.context), locale)
        R.layout.text_subtitle1 -> ViewHolder.QuoteDetails(parent, openQuoteDetails)
        R.layout.offer_faq -> ViewHolder.FAQ(parent, fragmentManager)
        R.layout.info_card -> ViewHolder.InfoCard(parent)
        R.layout.warning_card -> ViewHolder.WarningCard(parent)
        R.layout.generic_error -> ViewHolder.Error(parent, reload)
        else -> throw Error("Invalid viewType: $viewType")
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is OfferItems.Header -> R.layout.offer_header
        is OfferItems.VariantButton -> VARIANT_BUTTON
        is OfferItems.Facts -> R.layout.offer_fact_area
        is OfferItems.CurrentInsurer -> R.layout.offer_switch
        is OfferItems.Footer -> R.layout.offer_footer
        is OfferItems.Subheading -> R.layout.text_headline5
        is OfferItems.Paragraph -> R.layout.text_body2
        OfferItems.PriceComparisonHeader -> PRICE_COMPARISON_HEADER
        is OfferItems.InsurelyDivider -> INSURELY_DIVIDER
        is OfferItems.InsurelyCard -> INSURELY_CARD
        is OfferItems.QuoteDetails -> R.layout.text_subtitle1
        is OfferItems.FAQ -> R.layout.offer_faq
        OfferItems.AutomaticSwitchCard -> R.layout.info_card
        OfferItems.ManualSwitchCard -> R.layout.warning_card
        OfferItems.Error -> R.layout.generic_error
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
        abstract fun bind(data: OfferItems)

        class Header(
            parent: ViewGroup,
            private val locale: Locale,
            private val fragmentManager: FragmentManager,
            private val onSign: (CheckoutMethod, PaymentMethodsApiResponse?) -> Unit,
            private val onRemoveDiscount: () -> Unit,
        ) : ViewHolder(parent.inflate(R.layout.offer_header)) {
            private val binding by viewBinding(OfferHeaderBinding::bind)

            override fun bind(data: OfferItems) {
                if (data !is OfferItems.Header) {
                    return invalid(data)
                }
                binding.apply {
                    title.text = data.title ?: itemView.context.getString(R.string.OFFER_INSURANCE_BUNDLE_TITLE)

                    premium.text = data.premium.format(locale)
                    premiumPeriod.text = premiumPeriod.context.getString(R.string.OFFER_PRICE_PER_MONTH)

                    originalPremium.isVisible = data.hasDiscountedPrice
                    if (data.hasDiscountedPrice) {
                        originalPremium.setStrikethrough(true)
                        originalPremium.text =
                            data.originalPremium.format(locale)
                    }

                    startDateContainer.setHapticClickListener {
                        ChangeDateBottomSheet.newInstance(data.changeDateBottomSheetData)
                            .show(fragmentManager, ChangeDateBottomSheet.TAG)
                    }

                    startDateLabel.text = data.startDateLabel.toString(startDateLabel.context)
                    startDate.text = data.startDate.getString(itemView.context)

                    campaign.text = data.incentiveDisplayValue
                    campaign.isVisible = data.incentiveDisplayValue != null

                    discountButton.isVisible = data.showCampaignManagement
                    if (data.hasCampaigns) {
                        discountButton.apply {
                            setText(R.string.OFFER_REMOVE_DISCOUNT_BUTTON)
                            setTextColor(context.colorAttr(R.attr.colorError))
                            icon = null
                            setHapticClickListener {
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
                                OfferRedeemCodeBottomSheet.newInstance(data.quoteCartId)
                                    .show(
                                        fragmentManager,
                                        OfferRedeemCodeBottomSheet.TAG
                                    )
                            }
                        }
                    }

                    with(sign) {
                        text = data.checkoutLabel.toString(context)
                        icon = data.checkoutMethod.checkoutIconRes()?.let {
                            context.compatDrawable(it)
                        }
                        setHapticClickListener {
                            onSign(data.checkoutMethod, data.paymentMethodsApiResponse)
                        }
                    }
                    root.background = data.gradientType.toDrawable(itemView.context)
                }
            }
        }

        class VariantButton(
            private val composeView: ComposeView,
            private val locale: Locale,
        ) : ViewHolder(composeView) {
            init {
                composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            }

            override fun bind(data: OfferItems) {
                if (data !is OfferItems.VariantButton) {
                    return invalid(data)
                }

                composeView.setContent {
                    HedvigTheme {
                        VariantButton(
                            id = data.id,
                            title = data.title,
                            tag = data.tag,
                            description = data.description,
                            cost = data.price.format(locale),
                            selected = data.isSelected,
                            onClick = data.onVariantSelected
                        )
                    }
                }
            }
        }

        class Facts(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_fact_area)) {
            private val binding by viewBinding(OfferFactAreaBinding::bind)

            override fun bind(data: OfferItems) {
                if (data !is OfferItems.Facts) {
                    return invalid(data)
                }
                generateTable(binding.expandableContent, data.table)
            }
        }

        class Switch(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_switch)) {
            private val binding by viewBinding(OfferSwitchBinding::bind)

            override fun bind(data: OfferItems) = with(binding) {
                if (data !is OfferItems.CurrentInsurer) {
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

            override fun bind(data: OfferItems) {
                if (data !is OfferItems.Footer) {
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

            override fun bind(data: OfferItems) = with(binding.root) {
                if (data !is OfferItems.Subheading) {
                    return invalid(data)
                }

                when (data) {
                    OfferItems.Subheading.Coverage -> {
                        setText(R.string.offer_screen_coverage_title)
                        updateMargin(bottom = BASE_MARGIN)
                    }
                    is OfferItems.Subheading.Switcher -> {
                        text = context.resources.getQuantityString(
                            R.plurals.offer_switcher_title,
                            data.amountOfCurrentInsurers
                        )
                        updateMargin(bottom = BASE_MARGIN_DOUBLE)
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

            override fun bind(data: OfferItems) = with(binding.root) {
                if (data !is OfferItems.QuoteDetails) {
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

            override fun bind(data: OfferItems) = with(binding.root) {
                if (data !is OfferItems.Paragraph.Coverage) {
                    return invalid(data)
                }

                setText(R.string.offer_screen_MULTIPLE_INSURANCES_coverage_paragraph)
            }
        }

        class PriceComparisonHeader(
            private val composeView: ComposeView,
        ) : ViewHolder(composeView) {
            init {
                composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            }

            override fun bind(data: OfferItems) {
                if (data !is OfferItems.PriceComparisonHeader) return invalid(data)
                composeView.setContent {
                    HedvigTheme {
                        Text(
                            text = stringResource(R.string.OFFER_PRICE_COMPARISION_HEADER),
                            style = MaterialTheme.typography.h5,
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

            override fun bind(data: OfferItems) {
                if (data !is OfferItems.InsurelyDivider) return invalid(data)
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
            private val locale: Locale,
        ) : ViewHolder(composeView) {
            init {
                composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            }

            override fun bind(data: OfferItems) {
                if (data !is OfferItems.InsurelyCard) return invalid(data)
                composeView.setContent {
                    HedvigTheme {
                        InsurelyCard(
                            data = data,
                            locale = locale,
                            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
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

            override fun bind(data: OfferItems) = with(binding) {
                if (data !is OfferItems.FAQ) {
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

            override fun bind(data: OfferItems) = with(binding) {
                if (data !is OfferItems.AutomaticSwitchCard) {
                    return invalid(data)
                }

                title.setText(R.string.offer_switch_info_card_title)
                body.setText(R.string.offer_switch_info_card_body)
            }
        }

        class WarningCard(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.warning_card)) {
            private val binding by viewBinding(WarningCardBinding::bind)

            override fun bind(data: OfferItems) = with(binding) {
                if (data !is OfferItems.ManualSwitchCard) {
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

            override fun bind(data: OfferItems) = with(binding.retry) {
                if (data !is OfferItems.Error) {
                    return invalid(data)
                }
                setHapticClickListener { reload() }
            }
        }
    }

    companion object {
        const val INSURELY_CARD = 1
        const val PRICE_COMPARISON_HEADER = 2
        const val INSURELY_DIVIDER = 3
        const val VARIANT_BUTTON = 4

        class OfferDiffUtilCallback : DiffUtil.ItemCallback<OfferItems>() {
            override fun areItemsTheSame(oldItem: OfferItems, newItem: OfferItems): Boolean = when {
                oldItem is OfferItems.InsurelyCard && newItem is OfferItems.InsurelyCard -> {
                    oldItem.id == newItem.id
                }
                oldItem is OfferItems.PriceComparisonHeader && newItem is OfferItems.PriceComparisonHeader -> {
                    // Should only display 1 PriceComparisonHeader ever
                    true
                }
                oldItem is OfferItems.Header && newItem is OfferItems.Header -> {
                    true
                }
                oldItem is OfferItems.VariantButton && newItem is OfferItems.VariantButton -> {
                    oldItem.id == newItem.id
                }
                else -> {
                    oldItem == newItem
                }
            }

            override fun areContentsTheSame(oldItem: OfferItems, newItem: OfferItems) = oldItem == newItem
        }
    }
}
