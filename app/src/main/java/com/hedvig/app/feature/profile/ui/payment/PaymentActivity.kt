package com.hedvig.app.feature.profile.ui.payment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.hedvig.android.owldroid.fragment.IncentiveFragment
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.type.DirectDebitStatus
import com.hedvig.android.owldroid.type.InsuranceStatus
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.profile.service.ProfileTracker
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.referrals.RefetchingRedeemCodeDialog
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.setStrikethrough
import com.hedvig.app.util.extensions.setupLargeTitle
import com.hedvig.app.util.extensions.view.hide
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.viewmodel.DirectDebitViewModel
import kotlinx.android.synthetic.main.activity_payment.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class PaymentActivity : BaseActivity() {
    private val profileViewModel: ProfileViewModel by viewModel()
    private val directDebitViewModel: DirectDebitViewModel by viewModel()

    private val tracker: ProfileTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_payment)
        setupLargeTitle(R.string.PROFILE_PAYMENT_TITLE, R.font.circular_bold, R.drawable.ic_back) {
            onBackPressed()
        }

        nextPaymentGross.setStrikethrough(true)

        seePaymentHistory.setHapticClickListener {
            startActivity(PaymentHistoryActivity.newInstance(this))
        }

        changeBankAccount.setHapticClickListener {
            startActivity(TrustlyActivity.newInstance(this))
        }

        connectBankAccount.setHapticClickListener {
            startActivity(TrustlyActivity.newInstance(this))
        }

        connectBankAccountWithLink.setHapticClickListener {
            connectDirectDebitWithLink()
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
        profileViewModel.data.observe(lifecycleOwner = this) { profileData ->
            loadingSpinner.remove()
            resetViews()

            profileData?.let { pd ->
                bindFailedPaymentsCard(pd)
                bindNextPaymentCard(pd)
                bindCampaignInformation(pd)
                bindPaymentHistory(pd.chargeHistory)
            }

            bindBankAccountInformation()
        }
        directDebitViewModel.data.observe(lifecycleOwner = this) {
            bindBankAccountInformation()
        }
    }

    private fun bindFailedPaymentsCard(data: ProfileQuery.Data) {
        if (data.balance.failedCharges != 0) {
            failedPaymentsCard.show()
            failedPaymentsParagraph.text = "${data.balance.failedCharges}, ${data.nextChargeDate}"
        }
    }

    private fun bindNextPaymentCard(data: ProfileQuery.Data) {
        nextPaymentAmount.text =
            "${data.chargeEstimation.charge.amount.toBigDecimal().toInt()} kr"

        val discount = data.chargeEstimation.discount.amount.toBigDecimal().toInt()
        if (discount > 0) {
            nextPaymentGross.show()
            nextPaymentGross.text =
                "${data.chargeEstimation.subscription.amount.toBigDecimal().toInt()} kr/mån"
        }

        when (data.insurance.status) {
            InsuranceStatus.ACTIVE, InsuranceStatus.INACTIVE_WITH_START_DATE -> {
                nextPaymentDate.text = data.nextChargeDate.toString()
            }
            InsuranceStatus.INACTIVE -> {
                nextPaymentDate.background.compatSetTint(compatColor(R.color.sunflower_300))
                nextPaymentDate.text = "Startdatum ej satt"
            }
            else -> {
                Timber.e(
                    "Invariant detected: Member viewing ${javaClass.simpleName} with status ${data.insurance.status}"
                )
            }
        }

        (data.referralInformation.campaign.incentive as? IncentiveFragment.AsFreeMonths)?.let { fm ->
            freeMonthsSphere.show()
            freeMonths.text = fm.quantity.toString()
        }
    }

    private fun bindCampaignInformation(data: ProfileQuery.Data) {
        when (val incentive = data.referralInformation.campaign.incentive) {
            is IncentiveFragment.AsFreeMonths -> {
                campaignInformationTitle.text = "TODO"
                campaignInformationLabelOne.text = "TODO"
                data.referralInformation.campaign.owner?.displayName?.let { displayName ->
                    campaignInformationFieldOne.text = displayName
                }

                when (data.insurance.status) {
                    InsuranceStatus.ACTIVE, InsuranceStatus.INACTIVE_WITH_START_DATE -> {
                        data.insurance.cost?.freeUntil?.let { freeUntil ->
                            lastFreeDay.text = freeUntil.toString()
                        }
                        lastFreeDay.show()
                        lastFreeDayLabel.show()
                    }
                    InsuranceStatus.INACTIVE -> {
                        willUpdateWhenStartDateIsSet.show()
                    }
                    else -> {
                        Timber.e(
                            "Invariant detected: Member viewing ${javaClass.simpleName} with status ${data.insurance.status}"
                        )
                    }
                }
                campaignInformationContainer.show()
            }
            is IncentiveFragment.AsMonthlyCostDeduction -> {
                campaignInformationTitle.text = "TODO"
                campaignInformationLabelOne.text = "TODO"
                incentive.amount?.amount?.toBigDecimal()?.toInt()?.toString()?.let { amount ->
                    campaignInformationFieldOne.text = "-$amount kr"
                }
                campaignInformationContainer.show()
            }
        }
    }

    private fun bindPaymentHistory(paymentHistory: List<ProfileQuery.ChargeHistory>) {
        if (paymentHistory.isEmpty()) {
            return
        }
        paymentHistoryContainer.show()
    }

    private fun connectDirectDebitWithLink() {
        profileViewModel.trustlyUrl.observe(lifecycleOwner = this) { url ->
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }

        profileViewModel.startTrustlySession()
    }

    private fun resetViews() {
        failedPaymentsCard.remove()

        nextPaymentGross.hide()
        freeMonthsSphere.remove()
        nextPaymentDate.background.setTintList(null)

        campaignInformationContainer.remove()
        lastFreeDayLabel.remove()
        lastFreeDay.remove()
        willUpdateWhenStartDateIsSet.remove()

        paymentHistoryContainer.remove()

        connectBankAccountContainer.remove()
        changeBankAccount.remove()
        separator.remove()
        bankAccountUnderChangeParagraph.remove()
        connectBankAccountWithLink.remove()
    }

    private fun bindBankAccountInformation() {
        val profileData = profileViewModel.data.value ?: return

        when (directDebitViewModel.data.value?.directDebitStatus ?: return) {
            DirectDebitStatus.ACTIVE -> {
                paymentDetailsContainer.show()

                profileData.bankAccount?.let { bankAccount ->
                    bankName.text = bankAccount.bankName
                    accountNumber.text = bankAccount.descriptor

                    toggleBankInfo(true)
                } ?: toggleBankInfo(false)


                separator.show()
                changeBankAccount.show()
            }
            DirectDebitStatus.PENDING -> {
                paymentDetailsContainer.show()

                profileData.bankAccount?.let { bankAccount ->
                    bankName.text = bankAccount.bankName
                    accountNumber.text =
                        resources.getString(R.string.PROFILE_PAYMENT_ACCOUNT_NUMBER_CHANGING)

                    toggleBankInfo(true)
                } ?: toggleBankInfo(false)


                bankAccountUnderChangeParagraph.show()
            }
            DirectDebitStatus.NEEDS_SETUP -> {
                paymentDetailsContainer.show()
                toggleBankInfo(false)
                connectBankAccountContainer.show()
                connectBankAccountWithLink.show()
            }
            else -> {
                Timber.e("Payment fragment direct debit status UNKNOWN!")
            }
        }

        showRedeemCodeOnNoDiscount(profileData)
    }

    private fun toggleBankInfo(show: Boolean) {
        if (show) {
            bankTitle.show()
            bankName.show()
            accountNumber.show()
        } else {
            bankTitle.remove()
            bankName.remove()
            accountNumber.remove()
        }
    }

    private fun showRedeemCodeOnNoDiscount(profileData: ProfileQuery.Data) {
        if (
            profileData.insurance.cost?.fragments?.costFragment?.monthlyDiscount?.amount?.toBigDecimal()?.toInt() == 0
            && profileData.insurance.cost?.freeUntil == null
        ) {
            redeemCode.show()
        }
    }
}
