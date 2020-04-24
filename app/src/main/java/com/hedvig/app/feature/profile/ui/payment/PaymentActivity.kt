package com.hedvig.app.feature.profile.ui.payment

import android.os.Bundle
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.marketpicker.MarketPickerActivity
import com.hedvig.app.feature.profile.ui.payment.connect.ConnectPaymentActivity
import com.hedvig.app.feature.referrals.RefetchingRedeemCodeDialog
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.getMarket
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.setStrikethrough
import com.hedvig.app.util.extensions.setupLargeTitle
import com.hedvig.app.util.extensions.view.hide
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import e
import kotlinx.android.synthetic.main.activity_payment.*
import kotlinx.android.synthetic.main.campaign_information_section.*
import kotlinx.android.synthetic.main.connect_bank_account_card.*
import kotlinx.android.synthetic.main.failed_payments_card.*
import kotlinx.android.synthetic.main.loading_spinner.*
import kotlinx.android.synthetic.main.next_payment_card.*
import kotlinx.android.synthetic.main.payment_details_section.*
import kotlinx.android.synthetic.main.payment_history_section.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.threeten.bp.format.DateTimeFormatter

class PaymentActivity : BaseActivity(R.layout.activity_payment) {
    private val model: PaymentViewModel by viewModel()

    private val tracker: PaymentTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val market = getMarket()
        if (market == null) {
            startActivity(MarketPickerActivity.newInstance(this))
        }

        setupLargeTitle(R.string.PROFILE_PAYMENT_TITLE, R.drawable.ic_back) {
            onBackPressed()
        }

        nextPaymentGross.setStrikethrough(true)

        seePaymentHistory.setHapticClickListener {
            startActivity(PaymentHistoryActivity.newInstance(this))
        }

        changeBankAccount.setHapticClickListener {
            startActivity(ConnectPaymentActivity.newInstance(this))
        }

        connectBankAccount.setHapticClickListener {
            startActivity(ConnectPaymentActivity.newInstance(this))
        }

        redeemCode.setHapticClickListener {
            tracker.clickRedeemCode()
            RefetchingRedeemCodeDialog
                .newInstance()
                .show(supportFragmentManager, RefetchingRedeemCodeDialog.TAG)
        }

        loadData()
    }

    private fun loadData() {
        model.data.observe(lifecycleOwner = this) { data ->
            if (data == null) {
                return@observe
            }

            val (profileData, payinStatusData) = data

            loadingSpinner.remove()
            resetViews()

            profileData?.let { pd ->
                bindFailedPaymentsCard(pd)
                bindNextPaymentCard(pd)
                bindCampaignInformation(pd)
                bindPaymentDetails(pd)
                bindPaymentHistory(pd.chargeHistory)
            }
            payinStatusData?.let { bindDirectDebitStatus(it) }
        }
    }

    private fun bindDirectDebitStatus(data: PayinStatusQuery.Data) {
        when (data.payinMethodStatus) {
            PayinMethodStatus.ACTIVE -> {
                paymentDetailsContainer.show()
                directDebitStatus.text = getString(R.string.PAYMENTS_DIRECT_DEBIT_ACTIVE)
                endSeparator.show()
                changeBankAccount.show()
                connectBankAccountCard.remove()
            }
            PayinMethodStatus.PENDING -> {
                paymentDetailsContainer.show()
                directDebitStatus.text = getString(R.string.PAYMENTS_DIRECT_DEBIT_PENDING)

                connectBankAccountCard.remove()
                bankAccountUnderChangeParagraph.show()
            }
            PayinMethodStatus.NEEDS_SETUP -> {
                paymentDetailsContainer.remove()

                directDebitStatus.text = getString(R.string.PAYMENTS_DIRECT_DEBIT_NEEDS_SETUP)

                toggleBankInfo(false)
                connectBankAccountCard.show()
            }
            else -> {
                e { "Payment fragment direct debit status UNKNOWN!" }
            }
        }
    }

    private fun bindFailedPaymentsCard(data: ProfileQuery.Data) {
        if (data.balance.failedCharges != 0) {
            failedPaymentsCard.show()
            failedPaymentsParagraph.text = interpolateTextKey(
                getString(R.string.PAYMENTS_LATE_PAYMENTS_MESSAGE),
                "MONTHS_LATE" to data.balance.failedCharges,
                "NEXT_PAYMENT_DATE" to data.nextChargeDate?.format(DATE_FORMAT)
            )
        }
    }

    private fun bindNextPaymentCard(data: ProfileQuery.Data) {
        nextPaymentAmount.text =
            interpolateTextKey(
                getString(R.string.PAYMENTS_CURRENT_PREMIUM),
                "CURRENT_PREMIUM" to data.chargeEstimation.charge.amount.toBigDecimal().toInt()
            )

        val discount = data.chargeEstimation.discount.amount.toBigDecimal().toInt()
        if (discount > 0 && data.balance.failedCharges == 0) {
            nextPaymentGross.show()
            nextPaymentGross.text =
                interpolateTextKey(
                    getString(R.string.PAYMENTS_FULL_PREMIUM),
                    "FULL_PREMIUM" to data.insuranceCost?.fragments?.costFragment?.monthlyGross?.amount?.toBigDecimal()
                        ?.toInt()
                )
        }

        if (isActive(data.contracts)) {
            nextPaymentDate.text = data.nextChargeDate?.format(DATE_FORMAT)
        } else if (isPending(data.contracts)) {
            nextPaymentDate.background.compatSetTint(compatColor(R.color.sunflower_300))
            nextPaymentDate.setTextColor(compatColor(R.color.off_black))
            nextPaymentDate.text = getString(R.string.PAYMENTS_CARD_NO_STARTDATE)
        }

        val incentive = data.redeemedCampaigns.getOrNull(0)?.fragments?.incentiveFragment?.incentive
        incentive?.asFreeMonths?.let { freeMonthsIncentive ->
            freeMonthsIncentive.quantity?.let { quantity ->
                discountSphereText.text = buildSpannedString {
                    scale(20f / 12f) {
                        append("$quantity\n")
                    }
                    append(
                        if (quantity > 1) {
                            getString(R.string.PAYMENTS_OFFER_MULTIPLE_MONTHS)
                        } else {
                            getString(R.string.PAYMENTS_OFFER_SINGLE_MONTH)
                        }
                    )
                }
                discountSphere.show()
            }
        }
        incentive?.asPercentageDiscountMonths?.let { percentageDiscountMonthsIncentive ->
            discountSphereText.text = if (percentageDiscountMonthsIncentive.pdmQuantity > 1) {
                interpolateTextKey(
                    "{percentage}% rabatt i {months} månader",
                    "percentage" to percentageDiscountMonthsIncentive.percentageDiscount.toInt(),
                    "months" to percentageDiscountMonthsIncentive.pdmQuantity
                )
            } else {
                interpolateTextKey(
                    "{percentage}% rabatt i en månad",
                    "percentage" to percentageDiscountMonthsIncentive.percentageDiscount.toInt()
                )
            }
            discountSphere.show()
        }
    }

    private fun bindCampaignInformation(data: ProfileQuery.Data) {
        val incentive = data.redeemedCampaigns.getOrNull(0)?.fragments?.incentiveFragment?.incentive
        incentive?.asFreeMonths?.let {
            campaignInformationTitle.text = getString(R.string.PAYMENTS_SUBTITLE_CAMPAIGN)
            campaignInformationLabelOne.text = getString(R.string.PAYMENTS_CAMPAIGN_OWNER)
            data.redeemedCampaigns.getOrNull(0)?.owner?.displayName?.let { displayName ->
                campaignInformationFieldOne.text = displayName
            }

            if (isActive(data.contracts)) {
                data.insuranceCost?.freeUntil?.let { freeUntil ->
                    lastFreeDay.text = freeUntil.format(DATE_FORMAT)
                }
                lastFreeDay.show()
                lastFreeDayLabel.show()
            } else if (isPending(data.contracts)) {
                willUpdateWhenStartDateIsSet.show()
            }

            campaignInformationContainer.show()
            campaignInformationSeparator.show()
        }
        incentive?.asMonthlyCostDeduction?.let { monthlyCostDeductionIncentive ->
            campaignInformationTitle.text = getString(R.string.PAYMENTS_SUBTITLE_DISCOUNT)
            campaignInformationLabelOne.text = getString(R.string.PAYMENTS_DISCOUNT_ZERO)
            monthlyCostDeductionIncentive.amount?.amount?.toBigDecimal()?.toInt()?.toString()
                ?.let { amount ->
                    campaignInformationFieldOne.text = interpolateTextKey(
                        getString(R.string.PAYMENTS_DISCOUNT_AMOUNT),
                        "DISCOUNT" to amount
                    )
                }
            campaignInformationContainer.show()
            campaignInformationSeparator.show()
        }
    }

    private fun bindPaymentHistory(paymentHistory: List<ProfileQuery.ChargeHistory>) {
        if (paymentHistory.isEmpty()) {
            return
        }

        paymentHistory.getOrNull(0)?.let { lastMonthsCharge ->
            lastChargeDate.text = lastMonthsCharge.date.format(DATE_FORMAT)
            lastChargeAmount.text = interpolateTextKey(
                getString(R.string.PAYMENT_HISTORY_AMOUNT),
                "AMOUNT" to lastMonthsCharge.amount.amount.toBigDecimal().toInt()
            )
            lastChargeDate.show()
            lastChargeAmount.show()
        }

        paymentHistory.getOrNull(1)?.let { prevLastMonthsCharge ->
            prevLastChargeDate.text = prevLastMonthsCharge.date.format(DATE_FORMAT)
            prevLastChargeAmount.text = interpolateTextKey(
                getString(R.string.PAYMENT_HISTORY_AMOUNT),
                "AMOUNT" to prevLastMonthsCharge.amount.amount.toBigDecimal().toInt()
            )

            prevLastChargeDate.show()
            prevLastChargeAmount.show()
        }

        paymentHistoryContainer.show()

        paymentHistorySeparator.show()
    }

    private fun bindPaymentDetails(pd: ProfileQuery.Data) {
        pd.bankAccount?.let { bankAccount ->
            changeBankAccount.text = getString(R.string.PROFILE_PAYMENT_CHANGE_BANK_ACCOUNT)
            bankAccountContainer.show()
            accountNumber.text =
                "${bankAccount.fragments.bankAccountFragment.bankName} ${bankAccount.fragments.bankAccountFragment.descriptor}"
            toggleBankInfo(true)
        } ?: toggleBankInfo(false)

        pd.activePaymentMethods?.let { activePaymentMethods ->
            changeBankAccount.text = getString(R.string.MY_PAYMENT_CHANGE_CREDIT_CARD_BUTTON)
            adyenActivePaymentMethodContainer.show()
            cardType.text =
                activePaymentMethods.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.brand
            maskedCardNumber.text =
                "**** ${activePaymentMethods.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.lastFourDigits}"
            validUntil.text =
                "${activePaymentMethods.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.expiryMonth}/${activePaymentMethods.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.expiryYear}"
        }

        showRedeemCodeOnNoDiscount(pd)
    }

    private fun resetViews() {
        failedPaymentsCard.remove()

        nextPaymentGross.hide()
        discountSphere.remove()
        nextPaymentDate.background.setTintList(null)
        nextPaymentDate.setTextColor(colorAttr(android.R.attr.textColorPrimary))

        campaignInformationContainer.remove()
        lastFreeDayLabel.remove()
        lastFreeDay.remove()
        willUpdateWhenStartDateIsSet.remove()
        campaignInformationSeparator.remove()

        paymentHistoryContainer.remove()
        lastChargeDate.remove()
        lastChargeAmount.remove()
        prevLastChargeDate.remove()
        prevLastChargeAmount.remove()
        paymentHistorySeparator.remove()

        connectBankAccountCard.remove()
        changeBankAccount.remove()
        endSeparator.remove()
        bankAccountUnderChangeParagraph.remove()
    }

    private fun toggleBankInfo(show: Boolean) {
        if (show) {
            accountNumberLabel.show()
            accountNumber.show()
        } else {
            accountNumberLabel.remove()
            accountNumber.remove()
        }
    }

    private fun showRedeemCodeOnNoDiscount(profileData: ProfileQuery.Data) {
        if (
            profileData.insuranceCost?.fragments?.costFragment?.monthlyDiscount?.amount?.toBigDecimal()
                ?.toInt() == 0
            && profileData.insuranceCost.freeUntil == null
        ) {
            redeemCode.show()
        }
    }

    companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd, LLL YYYY")

        fun isActive(contracts: List<ProfileQuery.Contract>) = contracts.any {
            it.status.fragments.contractStatusFragment.asActiveStatus != null
                || it.status.fragments.contractStatusFragment.asTerminatedInFutureStatus != null
                || it.status.fragments.contractStatusFragment.asTerminatedTodayStatus != null
        }

        fun isPending(contracts: List<ProfileQuery.Contract>) = contracts.all {
            it.status.fragments.contractStatusFragment.asPendingStatus != null
                || it.status.fragments.contractStatusFragment.asActiveInFutureStatus != null
                || it.status.fragments.contractStatusFragment.asActiveInFutureAndTerminatedInFutureStatus != null
        }
    }
}
