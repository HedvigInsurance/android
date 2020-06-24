package com.hedvig.app.feature.referrals.ui.tab

import android.animation.ValueAnimator
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.doOnDetach
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hedvig.android.owldroid.fragment.ReferralFragment
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.R
import com.hedvig.app.feature.referrals.ui.PieChartSegment
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.boundedColorLerp
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.copyToClipboard
import com.hedvig.app.util.extensions.view.hide
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.safeLet
import e
import kotlinx.android.synthetic.main.referrals_code.view.*
import kotlinx.android.synthetic.main.referrals_error.view.*
import kotlinx.android.synthetic.main.referrals_header.view.*
import kotlinx.android.synthetic.main.referrals_header.view.placeholders
import kotlinx.android.synthetic.main.referrals_row.view.*

class ReferralsAdapter(
    private val reload: () -> Unit
) : RecyclerView.Adapter<ReferralsAdapter.ViewHolder>() {
    var items: List<ReferralsModel> = LOADING_STATE
        set(value) {
            val diff = DiffUtil.calculateDiff(
                ReferralsDiffCallback(
                    field,
                    value
                )
            )
            field = value
            diff.dispatchUpdatesTo(this)
        }

    fun setLoading() {
        items = LOADING_STATE
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        ReferralsModel.Title -> R.layout.referrals_title
        is ReferralsModel.Header -> R.layout.referrals_header
        is ReferralsModel.Code -> R.layout.referrals_code
        ReferralsModel.InvitesHeader -> R.layout.referrals_invites_header
        is ReferralsModel.Referral -> R.layout.referrals_row
        ReferralsModel.Error -> R.layout.referrals_error
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.referrals_title -> ViewHolder.TitleViewHolder(parent)
        R.layout.referrals_header -> ViewHolder.HeaderViewHolder(
            parent
        )
        R.layout.referrals_code -> ViewHolder.CodeViewHolder(
            parent
        )
        R.layout.referrals_invites_header -> ViewHolder.InvitesHeaderViewHolder(
            parent
        )
        R.layout.referrals_row -> ViewHolder.ReferralViewHolder(
            parent
        )
        R.layout.referrals_error -> ViewHolder.ErrorViewHolder(
            parent
        )
        else -> throw Error("Invalid viewType")
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], reload)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: ReferralsModel, reload: () -> Unit)

        class TitleViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.referrals_title, parent, false)
        ) {
            override fun bind(data: ReferralsModel, reload: () -> Unit) = Unit
        }

        class HeaderViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.referrals_header, parent, false)
        ) {
            private val emptyTexts = itemView.emptyTexts
            private val nonEmptyTexts = itemView.nonEmptyTexts
            private val placeholders = itemView.placeholders
            private val piechartPlaceholder = itemView.piechartPlaceholder
            private val loadedData = itemView.loadedData
            private val piechart = itemView.piechart
            private val grossPrice = itemView.grossPrice
            private val discountPerMonth = itemView.discountPerMonth
            private val newPrice = itemView.newPrice
            private val otherDiscountBox = itemView.otherDiscountBox

            override fun bind(data: ReferralsModel, reload: () -> Unit) {
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
                        bindPiechart(data.inner)
                        placeholders.remove()
                        emptyTexts.show()
                        loadedData.remove()
                        nonEmptyTexts.remove()
                        otherDiscountBox.remove()
                    }
                    is ReferralsModel.Header.LoadedHeader -> {
                        bindPiechart(data.inner)
                        placeholders.remove()
                        emptyTexts.remove()
                        nonEmptyTexts.show()
                        loadedData.show()
                        data.inner.referralInformation.costReducedIndefiniteDiscount?.fragments?.costFragment?.monthlyDiscount?.fragments?.monetaryAmountFragment?.toMonetaryAmount()
                            ?.negate()?.format(discountPerMonth.context)
                            ?.let { discountPerMonth.text = it }
                        data.inner.referralInformation.costReducedIndefiniteDiscount?.fragments?.costFragment?.monthlyNet?.fragments?.monetaryAmountFragment?.toMonetaryAmount()
                            ?.let { referralNet ->
                                newPrice.text = referralNet.format(newPrice.context)
                                otherDiscountBox.isVisible =
                                    data.inner.insuranceCost?.fragments?.costFragment?.monthlyNet?.fragments?.monetaryAmountFragment?.toMonetaryAmount() != referralNet
                            }
                    }
                    else -> {
                        e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
                    }
                }
            }

            private fun bindPiechart(data: ReferralsQuery.Data) {
                (piechart.getTag(R.id.slice_blink_animation) as? ValueAnimator)?.cancel()
                piechartPlaceholder.hideShimmer()
                grossPrice.show()
                val grossPriceAmount =
                    data.referralInformation.costReducedIndefiniteDiscount?.fragments?.costFragment?.monthlyGross?.fragments?.monetaryAmountFragment?.toMonetaryAmount()
                grossPriceAmount?.let { grossPrice.text = it.format(grossPrice.context) }
                val potentialDiscountAmount =
                    data.referralInformation.campaign.incentive?.asMonthlyCostDeduction?.amount?.fragments?.monetaryAmountFragment?.toMonetaryAmount()
                val currentDiscountAmount =
                    data.referralInformation.costReducedIndefiniteDiscount?.fragments?.costFragment?.monthlyDiscount?.fragments?.monetaryAmountFragment?.toMonetaryAmount()


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

                    val segments = listOfNotNull(
                        if (cdaAsPercentage != 0f) {
                            PieChartSegment(
                                CURRENT_DISCOUNT_SLICE,
                                cdaAsPercentage,
                                piechart.context.colorAttr(R.attr.colorOnPrimary)
                            )
                        } else {
                            null
                        },
                        PieChartSegment(
                            POTENTIAL_DISCOUNT_SLICE,
                            pdaAsPercentage,
                            Color.RED
                        ),
                        PieChartSegment(
                            REST_SLICE,
                            rest,
                            Color.BLUE
                        )
                    )
                    piechart.reveal(
                        segments
                    ) {
                        val animator = ValueAnimator.ofFloat(1f, 0f).apply {
                            duration = 800
                            repeatCount = ValueAnimator.INFINITE
                            repeatMode = ValueAnimator.REVERSE
                            interpolator = AccelerateDecelerateInterpolator()
                            addUpdateListener { va ->
                                piechart.segments = segments.map { segment ->
                                    if (segment.color == Color.RED) {
                                        return@map segment.copy(
                                            color = boundedColorLerp(
                                                Color.RED,
                                                Color.TRANSPARENT,
                                                va.animatedFraction
                                            )
                                        )
                                    }
                                    segment
                                }
                            }
                            start()
                        }
                        piechart.setTag(R.id.slice_blink_animation, animator)
                        piechart.doOnDetach { animator.cancel() }
                    }
                }
            }

            companion object {
                private const val CURRENT_DISCOUNT_SLICE = 0
                private const val POTENTIAL_DISCOUNT_SLICE = 1
                private const val REST_SLICE = 2
                private const val LOADING_SLICE = 3
            }
        }

        class CodeViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.referrals_code, parent, false)
        ) {
            private val placeholder = itemView.codePlaceholder
            private val code = itemView.code
            private val container = itemView.codeContainer

            override fun bind(data: ReferralsModel, reload: () -> Unit) {
                when (data) {
                    ReferralsModel.Code.LoadingCode -> {
                        placeholder.show()
                        code.remove()
                    }
                    is ReferralsModel.Code.LoadedCode -> {
                        placeholder.remove()
                        code.show()
                        code.text = data.code
                        container.setOnLongClickListener {
                            code.context.copyToClipboard(data.code)
                            Snackbar
                                .make(
                                    code,
                                    R.string.referrals_active__toast_text,
                                    Snackbar.LENGTH_SHORT
                                )
                                .setAnchorView(R.id.bottomTabs)
                                .show()
                            true
                        }
                    }
                    else -> {
                        e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
                    }
                }
            }
        }

        class InvitesHeaderViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.referrals_invites_header, parent, false)
        ) {
            override fun bind(data: ReferralsModel, reload: () -> Unit) = Unit
        }

        class ReferralViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.referrals_row, parent, false)
        ) {
            private val placeholders = itemView.placeholders
            private val texts = itemView.texts
            private val name = itemView.name
            private val refereeLabel = itemView.refereeLabel
            private val icon = itemView.icon
            private val status = itemView.status

            override fun bind(data: ReferralsModel, reload: () -> Unit) {
                when (data) {
                    ReferralsModel.Referral.LoadingReferral -> {
                        placeholders.show()
                        texts.remove()
                        icon.remove()
                        status.remove()
                    }
                    is ReferralsModel.Referral.Referee -> {
                        bindReferral(data.inner)
                        refereeLabel.show()
                    }
                    is ReferralsModel.Referral.LoadedReferral -> {
                        bindReferral(data.inner)
                        refereeLabel.remove()
                    }
                    else -> {
                        e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
                    }
                }
            }

            private fun bindReferral(data: ReferralFragment) {
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
                    status.text = discountAsNegative.format(status.context)
                }
                data.asInProgressReferral?.let {
                    icon.setImageResource(R.drawable.ic_clock_colorless)
                    status.background = null
                    status.text =
                        status.context.getString(R.string.referalls_invitee_states_awaiting___)
                }
                data.asTerminatedReferral?.let {
                    icon.setImageResource(R.drawable.ic_terminated_colorless)
                    status.background = null
                    status.text =
                        status.context.getString(R.string.referalls_invitee_states_terminated)
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

        class ErrorViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.referrals_error, parent, false)
        ) {
            private val retry = itemView.retry
            override fun bind(data: ReferralsModel, reload: () -> Unit) {
                retry.setHapticClickListener { reload() }
            }
        }
    }

    companion object {
        private val LOADING_STATE = listOf(
            ReferralsModel.Title,
            ReferralsModel.Header.LoadingHeader,
            ReferralsModel.Code.LoadingCode,
            ReferralsModel.InvitesHeader,
            ReferralsModel.Referral.LoadingReferral
        )
    }
}
