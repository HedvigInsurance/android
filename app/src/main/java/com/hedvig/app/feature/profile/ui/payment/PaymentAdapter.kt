package com.hedvig.app.feature.profile.ui.payment

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.app.R
import com.hedvig.app.databinding.AdyenPayinDetailsBinding
import com.hedvig.app.databinding.CampaignInformationSectionBinding
import com.hedvig.app.databinding.ConnectPayinCardBinding
import com.hedvig.app.databinding.FailedPaymentsCardBinding
import com.hedvig.app.databinding.NextPaymentCardBinding
import com.hedvig.app.databinding.PaymentHistoryItemBinding
import com.hedvig.app.databinding.PaymentHistoryLinkBinding
import com.hedvig.app.databinding.PaymentRedeemCodeBinding
import com.hedvig.app.databinding.TrustlyPayinDetailsBinding
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.feature.referrals.ui.redeemcode.RefetchingRedeemCodeDialog
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.setStrikethrough
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding

class PaymentAdapter(
    private val marketProvider: MarketProvider,
    private val fragmentManager: FragmentManager,
    private val tracker: PaymentTracker
) :
    ListAdapter<PaymentModel, PaymentAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        PaymentModel.Header -> R.layout.payment_header
        is PaymentModel.FailedPayments -> R.layout.failed_payments_card
        is PaymentModel.NextPayment -> R.layout.next_payment_card
        PaymentModel.ConnectPayment -> R.layout.connect_payin_card
        is PaymentModel.CampaignInformation -> R.layout.campaign_information_section
        PaymentModel.PaymentHistoryHeader -> R.layout.payment_history_header
        is PaymentModel.Charge -> R.layout.payment_history_item
        PaymentModel.PaymentHistoryLink -> R.layout.payment_history_link
        is PaymentModel.TrustlyPayinDetails -> R.layout.trustly_payin_details
        is PaymentModel.AdyenPayinDetails -> R.layout.adyen_payin_details
        is PaymentModel.Link -> R.layout.payment_redeem_code
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.payment_header -> ViewHolder.Header(parent)
        R.layout.failed_payments_card -> ViewHolder.FailedPayments(parent)
        R.layout.next_payment_card -> ViewHolder.NextPayment(parent)
        R.layout.connect_payin_card -> ViewHolder.ConnectPayment(parent)
        R.layout.campaign_information_section -> ViewHolder.CampaignInformation(parent)
        R.layout.payment_history_header -> ViewHolder.PaymentHistoryHeader(parent)
        R.layout.payment_history_item -> ViewHolder.Charge(parent)
        R.layout.payment_history_link -> ViewHolder.PaymentHistoryLink(parent)
        R.layout.trustly_payin_details -> ViewHolder.TrustlyPayinDetails(parent)
        R.layout.adyen_payin_details -> ViewHolder.AdyenPayinDetails(parent)
        R.layout.payment_redeem_code -> ViewHolder.Link(parent)
        else -> throw Error("Invalid viewType: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), marketProvider, fragmentManager, tracker)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            data: PaymentModel,
            marketProvider: MarketProvider,
            fragmentManager: FragmentManager,
            tracker: PaymentTracker
        ): Any?

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.payment_header)) {
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager,
                tracker: PaymentTracker
            ) = Unit
        }

        class FailedPayments(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.failed_payments_card)) {
            private val binding by viewBinding(FailedPaymentsCardBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager,
                tracker: PaymentTracker
            ) = with(binding) {
                if (data !is PaymentModel.FailedPayments) {
                    return invalid(data)
                }

                paragraph.text = paragraph.context.getString(
                    R.string.PAYMENTS_LATE_PAYMENTS_MESSAGE,
                    data.failedCharges,
                    data.nextChargeDate
                )
            }
        }

        class NextPayment(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.next_payment_card)) {
            private val binding by viewBinding(NextPaymentCardBinding::bind)

            init {
                binding.gross.setStrikethrough(true)
            }

            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager,
                tracker: PaymentTracker
            ) = with(binding) {
                if (data !is PaymentModel.NextPayment) {
                    return invalid(data)
                }

                amount.text =
                    data.inner.chargeEstimation.charge.fragments.monetaryAmountFragment.toMonetaryAmount()
                        .format(amount.context)

                val discountAmount =
                    data.inner.chargeEstimation.discount.fragments.monetaryAmountFragment.toMonetaryAmount()
                if (discountAmount.isPositive && data.inner.balance.failedCharges == 0) {
                    gross.show()
                    data.inner.insuranceCost?.fragments?.costFragment?.monthlyGross?.fragments?.monetaryAmountFragment?.toMonetaryAmount()
                        ?.format(gross.context)?.let { gross.text = it }
                }

                if (isActive(data.inner.contracts)) {
                    date.text =
                        data.inner.nextChargeDate?.format(PaymentActivity.DATE_FORMAT)
                } else if (isPending(data.inner.contracts)) {
                    date.background.compatSetTint(date.context.compatColor(R.color.sunflower_300))
                    date.setTextColor(date.context.compatColor(R.color.off_black))
                    date.setText(R.string.PAYMENTS_CARD_NO_STARTDATE)
                }

                val incentive =
                    data.inner.redeemedCampaigns.getOrNull(0)?.fragments?.incentiveFragment?.incentive
                discount.isVisible =
                    incentive?.asFreeMonths?.quantity != null || incentive?.asPercentageDiscountMonths != null
                incentive?.asFreeMonths?.let { freeMonthsIncentive ->
                    freeMonthsIncentive.quantity?.let { quantity ->
                        discount.text = discount.resources.getQuantityString(
                            R.plurals.payment_screen_free_month_discount_label,
                            quantity,
                            quantity
                        )
                    }
                }
                incentive?.asPercentageDiscountMonths?.let { percentageDiscountMonthsIncentive ->
                    discount.text = discount.resources.getQuantityString(
                        R.plurals.payment_screen_percentage_discount_label,
                        percentageDiscountMonthsIncentive.pdmQuantity,
                        percentageDiscountMonthsIncentive.percentageDiscount.toInt(),
                        percentageDiscountMonthsIncentive.pdmQuantity
                    )
                }
            }
        }

        class ConnectPayment(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.connect_payin_card)) {
            private val binding by viewBinding(ConnectPayinCardBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager,
                tracker: PaymentTracker
            ) = with(binding) {
                connect.setHapticClickListener {
                    marketProvider.market?.connectPayin(connect.context)
                        ?.let { connect.context.startActivity(it) }
                }
            }
        }

        class CampaignInformation(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.campaign_information_section)) {
            private val binding by viewBinding(CampaignInformationSectionBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager,
                tracker: PaymentTracker
            ) = with(binding) {
                if (data !is PaymentModel.CampaignInformation) {
                    return invalid(data)
                }

                val incentive =
                    data.inner.redeemedCampaigns.getOrNull(0)?.fragments?.incentiveFragment?.incentive
                incentive?.asFreeMonths?.let {
                    campaignInformationTitle.setText(R.string.PAYMENTS_SUBTITLE_CAMPAIGN)
                    campaignInformationLabelOne.setText(R.string.PAYMENTS_CAMPAIGN_OWNER)
                    data.inner.redeemedCampaigns.getOrNull(0)?.owner?.displayName?.let { displayName ->
                        campaignInformationFieldOne.text = displayName
                    }

                    when {
                        isActive(data.inner.contracts) -> {
                            data.inner.insuranceCost?.freeUntil?.let { freeUntil ->
                                lastFreeDay.text =
                                    freeUntil.format(PaymentActivity.DATE_FORMAT)
                            }
                            lastFreeDay.show()
                            lastFreeDayLabel.show()
                        }
                        isPending(data.inner.contracts) -> {
                            willUpdateWhenStartDateIsSet.show()
                        }
                        else -> {
                        }
                    }
                }
                incentive?.asMonthlyCostDeduction?.let { monthlyCostDeductionIncentive ->
                    campaignInformationTitle.setText(R.string.PAYMENTS_SUBTITLE_DISCOUNT)
                    campaignInformationLabelOne.setText(R.string.PAYMENTS_DISCOUNT_ZERO)
                    monthlyCostDeductionIncentive.amount?.amount?.toBigDecimal()?.toInt()
                        ?.toString()
                        ?.let { amount ->
                            campaignInformationFieldOne.text =
                                campaignInformationFieldOne.context.getString(
                                    R.string.PAYMENTS_DISCOUNT_AMOUNT,
                                    amount
                                )
                        }
                }
            }
        }

        class PaymentHistoryHeader(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.payment_history_header)) {
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager,
                tracker: PaymentTracker
            ) = Unit
        }

        class Charge(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.payment_history_item)) {
            private val binding by viewBinding(PaymentHistoryItemBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager,
                tracker: PaymentTracker
            ) = with(binding) {
                if (data !is PaymentModel.Charge) {
                    return invalid(data)
                }

                date.text =
                    data.inner.date.format(PaymentActivity.DATE_FORMAT)
                amount.text = data.inner.amount.fragments.monetaryAmountFragment.toMonetaryAmount()
                    .format(amount.context)
            }
        }

        class PaymentHistoryLink(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.payment_history_link)) {
            private val binding by viewBinding(PaymentHistoryLinkBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager,
                tracker: PaymentTracker
            ) = with(binding) {
                root.setHapticClickListener {
                    root.context.startActivity(
                        PaymentHistoryActivity.newInstance(
                            root.context
                        )
                    )
                }
            }
        }

        class TrustlyPayinDetails(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.trustly_payin_details)) {
            private val binding by viewBinding(TrustlyPayinDetailsBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager,
                tracker: PaymentTracker
            ) = with(binding) {
                if (data !is PaymentModel.TrustlyPayinDetails) {
                    return invalid(data)
                }

                accountNumber.text =
                    "${data.bankAccount.fragments.bankAccountFragment.bankName} ${data.bankAccount.fragments.bankAccountFragment.descriptor}"
                when (data.status) {
                    PayinMethodStatus.ACTIVE -> directDebitStatus.setText(
                        R.string.PAYMENTS_DIRECT_DEBIT_ACTIVE
                    )
                    PayinMethodStatus.PENDING -> directDebitStatus.setText(
                        R.string.PAYMENTS_DIRECT_DEBIT_PENDING
                    )
                    PayinMethodStatus.NEEDS_SETUP -> directDebitStatus.setText(
                        R.string.PAYMENTS_DIRECT_DEBIT_NEEDS_SETUP
                    )
                    else -> {
                    }
                }
                bankAccountUnderChangeParagraph.isVisible =
                    data.status == PayinMethodStatus.PENDING
            }
        }

        class AdyenPayinDetails(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.adyen_payin_details)) {
            private val binding by viewBinding(AdyenPayinDetailsBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager,
                tracker: PaymentTracker
            ) = with(binding) {
                if (data !is PaymentModel.AdyenPayinDetails) {
                    return invalid(data)
                }

                data.inner.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.brand?.let {
                    cardType.text = it
                }
                maskedCardNumber.text =
                    maskedCardNumber.context.getString(
                        R.string.payment_screen_credit_card_masking,
                        data.inner.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.lastFourDigits
                    )
            }
        }

        class Link(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.payment_redeem_code)) {
            private val binding by viewBinding(PaymentRedeemCodeBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager,
                tracker: PaymentTracker
            ) = with(binding) {
                if (data !is PaymentModel.Link) {
                    return invalid(data)
                }

                root.setText(
                    when (data) {
                        PaymentModel.Link.RedeemDiscountCode -> R.string.REFERRAL_ADDCOUPON_HEADLINE
                        PaymentModel.Link.TrustlyChangePayin -> R.string.PROFILE_PAYMENT_CHANGE_BANK_ACCOUNT
                        PaymentModel.Link.AdyenChangePayin -> R.string.MY_PAYMENT_CHANGE_CREDIT_CARD_BUTTON
                    }
                )

                root.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, when (data) {
                        PaymentModel.Link.RedeemDiscountCode -> R.drawable.ic_add_circle
                        PaymentModel.Link.TrustlyChangePayin,
                        PaymentModel.Link.AdyenChangePayin -> R.drawable.ic_edit
                    }, 0
                )

                root.setHapticClickListener(when (data) {
                    PaymentModel.Link.TrustlyChangePayin,
                    PaymentModel.Link.AdyenChangePayin -> { _ ->
                        marketProvider.market?.connectPayin(
                            root.context
                        )?.let { root.context.startActivity(it) }
                    }
                    PaymentModel.Link.RedeemDiscountCode -> { _ ->
                        tracker.clickRedeemCode()
                        RefetchingRedeemCodeDialog.newInstance()
                            .show(
                                fragmentManager,
                                RefetchingRedeemCodeDialog.TAG
                            )
                    }
                })
            }
        }
    }

    companion object {
        private fun isActive(contracts: List<PaymentQuery.Contract>) = contracts.any {
            it.status.fragments.contractStatusFragment.asActiveStatus != null
                || it.status.fragments.contractStatusFragment.asTerminatedInFutureStatus != null
                || it.status.fragments.contractStatusFragment.asTerminatedTodayStatus != null
        }

        private fun isPending(contracts: List<PaymentQuery.Contract>) = contracts.all {
            it.status.fragments.contractStatusFragment.asPendingStatus != null
                || it.status.fragments.contractStatusFragment.asActiveInFutureStatus != null
                || it.status.fragments.contractStatusFragment.asActiveInFutureAndTerminatedInFutureStatus != null
        }
    }
}
