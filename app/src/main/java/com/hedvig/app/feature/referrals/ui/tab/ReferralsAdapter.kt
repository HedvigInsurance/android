package com.hedvig.app.feature.referrals.ui.tab

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.view.doOnDetach
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hedvig.android.owldroid.fragment.ReferralFragment
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.ReferralsCodeBinding
import com.hedvig.app.databinding.ReferralsErrorBinding
import com.hedvig.app.databinding.ReferralsHeaderBinding
import com.hedvig.app.databinding.ReferralsRowBinding
import com.hedvig.app.feature.referrals.service.ReferralsTracker
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.ui.compose.composables.banner.InfoBanner
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.boundedColorLerp
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.copyToClipboard
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.hide
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.safeLet
import e
import org.javamoney.moneta.Money

class ReferralsAdapter(
    private val reload: () -> Unit,
    private val onBannerClicked: () -> Unit,
    private val tracker: ReferralsTracker,
    private val marketManager: MarketManager,
) : ListAdapter<ReferralsModel, ReferralsAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        ReferralsModel.Title -> R.layout.referrals_title
        is ReferralsModel.Header -> R.layout.referrals_header
        is ReferralsModel.Code -> R.layout.referrals_code
        ReferralsModel.InvitesHeader -> R.layout.referrals_invites_header
        is ReferralsModel.Referral -> R.layout.referrals_row
        ReferralsModel.Error -> R.layout.referrals_error
        is ReferralsModel.ReferralTopBar -> INFO_BANNER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.referrals_title -> ViewHolder.TitleViewHolder(parent)
        R.layout.referrals_header -> ViewHolder.HeaderViewHolder(parent)
        R.layout.referrals_code -> ViewHolder.CodeViewHolder(parent)
        R.layout.referrals_invites_header -> ViewHolder.InvitesHeaderViewHolder(parent)
        R.layout.referrals_row -> ViewHolder.ReferralViewHolder(parent)
        R.layout.referrals_error -> ViewHolder.ErrorViewHolder(parent)
        INFO_BANNER -> ViewHolder.InfoBanner(ComposeView(parent.context), onBannerClicked)
        else -> throw Error("Invalid viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), reload, tracker, marketManager)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            data: ReferralsModel,
            reload: () -> Unit,
            tracker: ReferralsTracker,
            marketManager: MarketManager
        )

        class TitleViewHolder(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.referrals_title)) {
            override fun bind(
                data: ReferralsModel,
                reload: () -> Unit,
                tracker: ReferralsTracker,
                marketManager: MarketManager
            ) =
                Unit
        }

        class HeaderViewHolder(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.referrals_header)) {
            private val binding by viewBinding(ReferralsHeaderBinding::bind)
            override fun bind(
                data: ReferralsModel,
                reload: () -> Unit,
                tracker: ReferralsTracker,
                marketManager: MarketManager
            ) {
                binding.apply {
                    when (data) {
                        ReferralsModel.Header.LoadingHeader -> {
                            (piechart.getTag(R.id.slice_blink_animation) as? ValueAnimator)?.cancel()
                            piechart.segments = listOf(
                                PieChartSegment(
                                    LOADING_SLICE,
                                    100f,
                                    piechart.context.colorAttr(R.attr.colorPlaceholder)
                                )
                            )
                            piechartPlaceholder.showShimmer(true)
                            grossPrice.hide()
                            emptyTexts.remove()
                            nonEmptyTexts.show()
                            placeholders.show()
                            loadedData.remove()
                            otherDiscountBox.remove()
                        }
                        is ReferralsModel.Header.LoadedEmptyHeader -> {
                            bindPiechart(data.inner, marketManager.market)
                            placeholders.remove()
                            data
                                .inner
                                .referralInformation
                                .campaign
                                .incentive
                                ?.asMonthlyCostDeduction
                                ?.amount
                                ?.fragments
                                ?.monetaryAmountFragment
                                ?.toMonetaryAmount()
                                ?.let { incentiveAmount ->
                                    emptyBody.text = emptyBody.context.getString(
                                        R.string.referrals_empty_body,
                                        incentiveAmount.format(emptyBody.context, marketManager.market),
                                        Money.of(0, incentiveAmount.currency.currencyCode)
                                            .format(emptyBody.context, marketManager.market)
                                    )
                                }
                            emptyTexts.show()
                            loadedData.remove()
                            nonEmptyTexts.remove()
                            otherDiscountBox.remove()
                        }
                        is ReferralsModel.Header.LoadedHeader -> {
                            bindPiechart(data.inner, marketManager.market)
                            placeholders.remove()
                            emptyTexts.remove()
                            nonEmptyTexts.show()
                            loadedData.show()
                            data
                                .inner
                                .referralInformation
                                .costReducedIndefiniteDiscount
                                ?.fragments
                                ?.costFragment
                                ?.monthlyDiscount
                                ?.fragments
                                ?.monetaryAmountFragment
                                ?.toMonetaryAmount()
                                ?.negate()?.format(discountPerMonth.context, marketManager.market)
                                ?.let { discountPerMonth.text = it }
                            data
                                .inner
                                .referralInformation
                                .costReducedIndefiniteDiscount
                                ?.fragments
                                ?.costFragment
                                ?.monthlyNet
                                ?.fragments
                                ?.monetaryAmountFragment
                                ?.toMonetaryAmount()
                                ?.let { referralNet ->
                                    newPrice.text = referralNet.format(newPrice.context, marketManager.market)
                                    otherDiscountBox.isVisible =
                                        data
                                        .inner
                                        .insuranceCost
                                        ?.fragments
                                        ?.costFragment
                                        ?.monthlyNet
                                        ?.fragments
                                        ?.monetaryAmountFragment
                                        ?.toMonetaryAmount() != referralNet
                                }
                        }
                        else -> {
                            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
                        }
                    }
                }
            }

            private fun bindPiechart(data: ReferralsQuery.Data, market: Market?) {
                binding.apply {
                    (piechart.getTag(R.id.slice_blink_animation) as? ValueAnimator)?.cancel()
                    piechartPlaceholder.hideShimmer()
                    grossPrice.show()
                    val grossPriceAmount =
                        data
                            .referralInformation
                            .costReducedIndefiniteDiscount
                            ?.fragments
                            ?.costFragment
                            ?.monthlyGross
                            ?.fragments
                            ?.monetaryAmountFragment
                            ?.toMonetaryAmount()
                    grossPriceAmount?.let { grossPrice.text = it.format(grossPrice.context, market) }
                    val potentialDiscountAmount =
                        data
                            .referralInformation
                            .campaign
                            .incentive
                            ?.asMonthlyCostDeduction
                            ?.amount
                            ?.fragments
                            ?.monetaryAmountFragment
                            ?.toMonetaryAmount()
                    val currentDiscountAmount =
                        data
                            .referralInformation
                            .costReducedIndefiniteDiscount
                            ?.fragments
                            ?.costFragment
                            ?.monthlyDiscount
                            ?.fragments
                            ?.monetaryAmountFragment
                            ?.toMonetaryAmount()

                    safeLet(
                        grossPriceAmount,
                        potentialDiscountAmount,
                        currentDiscountAmount
                    ) { gpa, pda, cda ->
                        val pdaAsPercentage =
                            (pda.number.doubleValueExact() / gpa.number.doubleValueExact()).toFloat() * 100
                        val cdaAsPercentage =
                            (cda.number.doubleValueExact() / gpa.number.doubleValueExact()).toFloat() * 100
                        val rest = 100f - (pdaAsPercentage + cdaAsPercentage)

                        val potentialDiscountColor =
                            piechart.context.compatColor(R.color.forever_orange_300)
                        val restColor = piechart.context.compatColor(R.color.forever_orange_500)

                        val segments = listOfNotNull(
                            if (cdaAsPercentage != 0f) {
                                PieChartSegment(
                                    CURRENT_DISCOUNT_SLICE,
                                    cdaAsPercentage,
                                    piechart.context.colorAttr(R.attr.colorSurface)
                                )
                            } else {
                                null
                            },
                            PieChartSegment(
                                POTENTIAL_DISCOUNT_SLICE,
                                pdaAsPercentage,
                                potentialDiscountColor
                            ),
                            PieChartSegment(
                                REST_SLICE,
                                rest,
                                restColor
                            )
                        )
                        piechart.reveal(
                            segments
                        ) {
                            ValueAnimator.ofFloat(1f, 0f).apply {
                                duration = SLICE_BLINK_DURATION
                                repeatCount = ValueAnimator.INFINITE
                                repeatMode = ValueAnimator.REVERSE
                                interpolator = AccelerateDecelerateInterpolator()
                                addUpdateListener { va ->
                                    piechart.segments = piechart.segments.map { segment ->
                                        if (segment.id == POTENTIAL_DISCOUNT_SLICE) {
                                            return@map segment.copy(
                                                color = boundedColorLerp(
                                                    potentialDiscountColor,
                                                    restColor,
                                                    va.animatedFraction
                                                )
                                            )
                                        }
                                        segment
                                    }
                                }
                                piechart.setTag(R.id.slice_blink_animation, this)
                                piechart.doOnDetach { cancel() }
                                start()
                            }
                        }
                    }
                }
            }

            companion object {
                private const val CURRENT_DISCOUNT_SLICE = 0
                private const val POTENTIAL_DISCOUNT_SLICE = 1
                private const val REST_SLICE = 2
                private const val LOADING_SLICE = 3

                private const val SLICE_BLINK_DURATION = 800L
            }
        }

        class CodeViewHolder(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.referrals_code)) {
            private val binding by viewBinding(ReferralsCodeBinding::bind)
            override fun bind(
                data: ReferralsModel,
                reload: () -> Unit,
                tracker: ReferralsTracker,
                marketManager: MarketManager
            ) {
                binding.apply {
                    when (data) {
                        ReferralsModel.Code.LoadingCode -> {
                            codePlaceholder.show()
                            code.remove()
                            edit.remove()
                        }
                        is ReferralsModel.Code.LoadedCode -> {
                            codePlaceholder.remove()
                            code.show()
                            code.text = data.inner.referralInformation.campaign.code
                            codeContainer.setOnLongClickListener {
                                code.context.copyToClipboard(data.inner.referralInformation.campaign.code)
                                Snackbar
                                    .make(
                                        code,
                                        R.string.referrals_active__toast_text,
                                        Snackbar.LENGTH_SHORT
                                    )
                                    .setAnchorView(R.id.bottomNavigation)
                                    .show()
                                true
                            }
                            data
                                .inner
                                .referralInformation
                                .campaign
                                .incentive
                                ?.asMonthlyCostDeduction
                                ?.amount
                                ?.fragments
                                ?.monetaryAmountFragment
                                ?.toMonetaryAmount()
                                ?.let { incentiveAmount ->
                                    codeFootnote.text = codeFootnote.resources.getString(
                                        R.string.referrals_empty_code_footer,
                                        incentiveAmount.format(codeFootnote.context, marketManager.market)
                                    )
                                }
                            edit.setHapticClickListener {
                                tracker.editCode()
                                edit.context.startActivity(
                                    ReferralsEditCodeActivity.newInstance(
                                        edit.context,
                                        data.inner.referralInformation.campaign.code
                                    )
                                )
                            }
                            edit.show()
                        }
                        else -> {
                            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
                        }
                    }
                }
            }
        }

        class InvitesHeaderViewHolder(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.referrals_invites_header)) {
            override fun bind(
                data: ReferralsModel,
                reload: () -> Unit,
                tracker: ReferralsTracker,
                marketManager: MarketManager
            ) = Unit
        }

        class ReferralViewHolder(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.referrals_row)) {
            private val binding by viewBinding(ReferralsRowBinding::bind)
            override fun bind(
                data: ReferralsModel,
                reload: () -> Unit,
                tracker: ReferralsTracker,
                marketManager: MarketManager
            ) {
                binding.apply {
                    when (data) {
                        ReferralsModel.Referral.LoadingReferral -> {
                            placeholders.show()
                            texts.remove()
                            icon.remove()
                            status.remove()
                        }
                        is ReferralsModel.Referral.Referee -> {
                            bindReferral(data.inner, marketManager.market)
                            refereeLabel.show()
                        }
                        is ReferralsModel.Referral.LoadedReferral -> {
                            bindReferral(data.inner, marketManager.market)
                            refereeLabel.remove()
                        }
                        else -> {
                            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
                        }
                    }
                }
            }

            private fun bindReferral(data: ReferralFragment, market: Market?) {
                binding.apply {
                    placeholders.remove()
                    texts.show()
                    data.name?.let { name.text = it }
                    icon.show()
                    status.show()
                    data.asActiveReferral?.let { activeReferral ->
                        icon.setImageResource(R.drawable.ic_basketball)
                        status.background =
                            status.context.compatDrawable(R.drawable.background_slightly_rounded_corners)
                                ?.apply {
                                    mutate().compatSetTint(status.context.colorAttr(R.attr.colorSurface))
                                }
                        val discountAsNegative =
                            activeReferral.discount.fragments.monetaryAmountFragment.toMonetaryAmount()
                                .negate()
                        status.text = discountAsNegative.format(status.context, market)
                    }
                    data.asInProgressReferral?.let {
                        icon.setImageResource(R.drawable.ic_clock_colorless)
                        status.background = null
                        status.text =
                            status.context.getString(R.string.referalls_invitee_states_awaiting___)
                    }
                    data.asTerminatedReferral?.let {
                        icon.setImageResource(R.drawable.ic_x_in_circle)
                        status.background = null
                        status.text =
                            status.context.getString(R.string.referalls_invitee_states_terminated)
                    }
                }
            }

            companion object {
                private val ReferralFragment.name: String?
                    get() {
                        asActiveReferral?.name?.let { return it }
                        asInProgressReferral?.name?.let { return it }
                        asTerminatedReferral?.name?.let { return it }

                        return null
                    }
            }
        }

        class ErrorViewHolder(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.referrals_error)) {
            private val binding by viewBinding(ReferralsErrorBinding::bind)
            override fun bind(
                data: ReferralsModel,
                reload: () -> Unit,
                tracker: ReferralsTracker,
                marketManager: MarketManager
            ) {
                binding.retry.setHapticClickListener {
                    tracker.reload()
                    reload()
                }
            }
        }

        class InfoBanner(private val composeView: ComposeView, private val onBannerClicked: () -> Unit) :
            ViewHolder(composeView) {
            init {
                composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            }

            override fun bind(
                data: ReferralsModel,
                reload: () -> Unit,
                tracker: ReferralsTracker,
                marketManager: MarketManager
            ) {
                if (data !is ReferralsModel.ReferralTopBar) {
                    return
                }
                composeView.setContent {
                    HedvigTheme {
                        InfoBanner(
                            onClick = onBannerClicked,
                            text = data.description,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }

    companion object {
        val LOADING_STATE = listOf(
            ReferralsModel.Title,
            ReferralsModel.Header.LoadingHeader,
            ReferralsModel.Code.LoadingCode,
            ReferralsModel.InvitesHeader,
            ReferralsModel.Referral.LoadingReferral,
        )

        val ERROR_STATE = listOf(
            ReferralsModel.Title,
            ReferralsModel.Error,
        )

        private const val INFO_BANNER = 1
    }
}
