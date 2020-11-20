package com.hedvig.app.feature.profile.ui.payment

import android.view.View
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.app.R
import com.hedvig.app.databinding.AdyenPayinDetailsBinding
import com.hedvig.app.databinding.CampaignInformationSectionBinding
import com.hedvig.app.databinding.ConnectBankAccountCardBinding
import com.hedvig.app.databinding.FailedPaymentsCardBinding
import com.hedvig.app.databinding.NextPaymentCardBinding
import com.hedvig.app.databinding.PaymentHistoryItemBinding
import com.hedvig.app.databinding.PaymentHistoryLinkBinding
import com.hedvig.app.databinding.PaymentLinkBinding
import com.hedvig.app.databinding.TrustlyPayinDetailsBinding
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.setStrikethrough
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import e

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
        PaymentModel.ConnectPayment -> R.layout.connect_bank_account_card
        is PaymentModel.CampaignInformation -> R.layout.campaign_information_section
        PaymentModel.PaymentHistoryHeader -> R.layout.payment_history_header
        is PaymentModel.Charge -> R.layout.payment_history_item
        PaymentModel.PaymentHistoryLink -> R.layout.payment_history_link
        is PaymentModel.TrustlyPayinDetails -> R.layout.trustly_payin_details
        is PaymentModel.AdyenPayinDetails -> R.layout.adyen_payin_details
        is PaymentModel.Link -> R.layout.payment_link
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.payment_header -> ViewHolder.Header(parent)
        R.layout.failed_payments_card -> ViewHolder.FailedPayments(parent)
        R.layout.next_payment_card -> ViewHolder.NextPayment(parent)
        R.layout.connect_bank_account_card -> ViewHolder.ConnectPayment(parent)
        R.layout.campaign_information_section -> ViewHolder.CampaignInformation(parent)
        R.layout.payment_history_header -> ViewHolder.PaymentHistoryHeader(parent)
        R.layout.payment_history_item -> ViewHolder.Charge(parent)
        R.layout.payment_history_link -> ViewHolder.PaymentHistoryLink(parent)
        R.layout.trustly_payin_details -> ViewHolder.TrustlyPayinDetails(parent)
        R.layout.adyen_payin_details -> ViewHolder.AdyenPayinDetails(parent)
        R.layout.payment_link -> ViewHolder.Link(parent)
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

        fun invalid(data: PaymentModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

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

                failedPaymentsParagraph.text = failedPaymentsParagraph.context.getString(
                    com.hedvig.app.R.string.PAYMENTS_LATE_PAYMENTS_MESSAGE,
                    data.failedCharges,
                    data.nextChargeDate
                )
            }
        }

        class NextPayment(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.next_payment_card)) {
            private val binding by viewBinding(NextPaymentCardBinding::bind)

            init {
                binding.nextPaymentGross.setStrikethrough(true)
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

                nextPaymentAmount.text =
                    data.inner.chargeEstimation.charge.fragments.monetaryAmountFragment.toMonetaryAmount()
                        .format(nextPaymentAmount.context)

                val discount =
                    data.inner.chargeEstimation.discount.fragments.monetaryAmountFragment.toMonetaryAmount()
                if (discount.isPositive && data.inner.balance.failedCharges == 0) {
                    nextPaymentGross.show()
                    data.inner.insuranceCost?.fragments?.costFragment?.monthlyGross?.fragments?.monetaryAmountFragment?.toMonetaryAmount()
                        ?.format(nextPaymentGross.context)?.let { nextPaymentGross.text = it }
                }

                if (com.hedvig.app.feature.profile.ui.payment.PaymentActivity.isActive(data.inner.contracts)) {
                    nextPaymentDate.text =
                        data.inner.nextChargeDate?.format(com.hedvig.app.feature.profile.ui.payment.PaymentActivity.DATE_FORMAT)
                } else if (com.hedvig.app.feature.profile.ui.payment.PaymentActivity.isPending(data.inner.contracts)) {
                    nextPaymentDate.background.compatSetTint(nextPaymentDate.context.compatColor(com.hedvig.app.R.color.sunflower_300))
                    nextPaymentDate.setTextColor(nextPaymentDate.context.compatColor(com.hedvig.app.R.color.off_black))
                    nextPaymentDate.setText(com.hedvig.app.R.string.PAYMENTS_CARD_NO_STARTDATE)
                }

                val incentive =
                    data.inner.redeemedCampaigns.getOrNull(0)?.fragments?.incentiveFragment?.incentive
                incentive?.asFreeMonths?.let { freeMonthsIncentive ->
                    freeMonthsIncentive.quantity?.let { quantity ->
                        discountSphereText.text = buildSpannedString {
                            scale(20f / 12f) {
                                append("$quantity\n")
                            }
                            append(
                                if (quantity > 1) {
                                    discountSphere.context.getString(com.hedvig.app.R.string.PAYMENTS_OFFER_MULTIPLE_MONTHS)
                                } else {
                                    discountSphere.context.getString(com.hedvig.app.R.string.PAYMENTS_OFFER_SINGLE_MONTH)
                                }
                            )
                        }
                        discountSphere.show()
                    }
                }
                incentive?.asPercentageDiscountMonths?.let { percentageDiscountMonthsIncentive ->
                    discountSphere.show()
                    discountSphereText.text =
                        if (percentageDiscountMonthsIncentive.pdmQuantity > 1) {
                            discountSphereText.context.getString(
                                com.hedvig.app.R.string.PAYMENTS_DISCOUNT_PERCENTAGE_MONTHS_MANY,
                                percentageDiscountMonthsIncentive.percentageDiscount.toInt(),
                                percentageDiscountMonthsIncentive.pdmQuantity
                            )
                        } else {
                            discountSphere.context.getString(
                                com.hedvig.app.R.string.PAYMENTS_DISCOUNT_PERCENTAGE_MONTHS_ONE,
                                percentageDiscountMonthsIncentive.percentageDiscount.toInt()
                            )
                        }
                }
            }
        }

        class ConnectPayment(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.connect_bank_account_card)) {
            private val binding by viewBinding(ConnectBankAccountCardBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager,
                tracker: PaymentTracker
            ) = with(binding) {
                connectBankAccount.setHapticClickListener {
                    marketProvider.market?.connectPayin(connectBankAccount.context)
                        ?.let { connectBankAccount.context.startActivity(it) }
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
                    campaignInformationTitle.setText(com.hedvig.app.R.string.PAYMENTS_SUBTITLE_CAMPAIGN)
                    campaignInformationLabelOne.setText(com.hedvig.app.R.string.PAYMENTS_CAMPAIGN_OWNER)
                    data.inner.redeemedCampaigns.getOrNull(0)?.owner?.displayName?.let { displayName ->
                        campaignInformationFieldOne.text = displayName
                    }

                    when {
                        com.hedvig.app.feature.profile.ui.payment.PaymentActivity.isActive(data.inner.contracts) -> {
                            data.inner.insuranceCost?.freeUntil?.let { freeUntil ->
                                lastFreeDay.text =
                                    freeUntil.format(com.hedvig.app.feature.profile.ui.payment.PaymentActivity.DATE_FORMAT)
                            }
                            lastFreeDay.show()
                            lastFreeDayLabel.show()
                        }
                        com.hedvig.app.feature.profile.ui.payment.PaymentActivity.isPending(data.inner.contracts) -> {
                            willUpdateWhenStartDateIsSet.show()
                        }
                        else -> {
                        }
                    }
                }
                incentive?.asMonthlyCostDeduction?.let { monthlyCostDeductionIncentive ->
                    campaignInformationTitle.setText(com.hedvig.app.R.string.PAYMENTS_SUBTITLE_DISCOUNT)
                    campaignInformationLabelOne.setText(com.hedvig.app.R.string.PAYMENTS_DISCOUNT_ZERO)
                    monthlyCostDeductionIncentive.amount?.amount?.toBigDecimal()?.toInt()
                        ?.toString()
                        ?.let { amount ->
                            campaignInformationFieldOne.text =
                                campaignInformationFieldOne.context.getString(
                                    com.hedvig.app.R.string.PAYMENTS_DISCOUNT_AMOUNT,
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
                    data.inner.date.format(com.hedvig.app.feature.profile.ui.payment.PaymentActivity.DATE_FORMAT)
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
                        com.hedvig.app.feature.profile.ui.payment.PaymentHistoryActivity.newInstance(
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
                    com.hedvig.android.owldroid.type.PayinMethodStatus.ACTIVE -> directDebitStatus.setText(
                        com.hedvig.app.R.string.PAYMENTS_DIRECT_DEBIT_ACTIVE
                    )
                    com.hedvig.android.owldroid.type.PayinMethodStatus.PENDING -> directDebitStatus.setText(
                        com.hedvig.app.R.string.PAYMENTS_DIRECT_DEBIT_PENDING
                    )
                    com.hedvig.android.owldroid.type.PayinMethodStatus.NEEDS_SETUP -> directDebitStatus.setText(
                        com.hedvig.app.R.string.PAYMENTS_DIRECT_DEBIT_NEEDS_SETUP
                    )
                    else -> {
                    }
                }
                bankAccountUnderChangeParagraph.isVisible =
                    data.status == com.hedvig.android.owldroid.type.PayinMethodStatus.PENDING
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
                    "**** ${data.inner.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.lastFourDigits}"
                validUntil.text =
                    "${data.inner.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.expiryMonth}/${data.inner.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.expiryYear}"
            }
        }

        class Link(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.payment_link)) {
            private val binding by viewBinding(PaymentLinkBinding::bind)
            override fun bind(
                data: PaymentModel,
                marketProvider: MarketProvider,
                fragmentManager: FragmentManager,
                tracker: PaymentTracker
            ) = with(binding) {
                if (data !is PaymentModel.Link) {
                    return invalid(data)
                }

                link.setText(
                    when (data) {
                        com.hedvig.app.feature.profile.ui.payment.PaymentModel.Link.TrustlyChangePayin -> com.hedvig.app.R.string.PROFILE_PAYMENT_CHANGE_BANK_ACCOUNT
                        com.hedvig.app.feature.profile.ui.payment.PaymentModel.Link.AdyenChangePayin -> com.hedvig.app.R.string.MY_PAYMENT_CHANGE_CREDIT_CARD_BUTTON
                        com.hedvig.app.feature.profile.ui.payment.PaymentModel.Link.RedeemDiscountCode -> com.hedvig.app.R.string.REFERRAL_ADDCOUPON_HEADLINE
                    }
                )

                link.setHapticClickListener(when (data) {
                    com.hedvig.app.feature.profile.ui.payment.PaymentModel.Link.TrustlyChangePayin,
                    com.hedvig.app.feature.profile.ui.payment.PaymentModel.Link.AdyenChangePayin -> { _ ->
                        marketProvider.market?.connectPayin(
                            link.context
                        )?.let { link.context.startActivity(it) }
                    }
                    com.hedvig.app.feature.profile.ui.payment.PaymentModel.Link.RedeemDiscountCode -> { _ ->
                        tracker.clickRedeemCode()
                        com.hedvig.app.feature.referrals.ui.redeemcode.RefetchingRedeemCodeDialog.newInstance()
                            .show(
                                fragmentManager,
                                com.hedvig.app.feature.referrals.ui.redeemcode.RefetchingRedeemCodeDialog.TAG
                            )
                    }
                })
            }
        }
    }
}
