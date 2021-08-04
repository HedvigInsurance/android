package com.hedvig.app.feature.profile.ui.payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowCompat
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.android.owldroid.type.PayoutMethodStatus
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityPaymentBinding
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.safeLet
import e
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.format.DateTimeFormatter

class PaymentActivity : BaseActivity(R.layout.activity_payment) {
    private val binding by viewBinding(ActivityPaymentBinding::bind)
    private val model: PaymentViewModel by viewModel()

    private val tracker: PaymentTracker by inject()
    private val marketManager: MarketManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            window.compatSetDecorFitsSystemWindows(false)
            toolbar.applyStatusBarInsets()
            toolbar.setNavigationOnClickListener { onBackPressed() }

            recycler.applyNavigationBarInsets()
            recycler.adapter = PaymentAdapter(marketManager, supportFragmentManager, tracker)

            model.data.observe(this@PaymentActivity) { (paymentData, payinStatusData) ->
                if (paymentData == null || payinStatusData == null) {
                    return@observe
                }

                (recycler.adapter as? PaymentAdapter)?.submitList(
                    listOfNotNull(
                        PaymentModel.Header,
                        failedPayments(paymentData),
                        connectPayment(payinStatusData),
                        PaymentModel.NextPayment(paymentData),
                        campaign(paymentData),
                        *paymentHistory(paymentData),
                        redeemCampaign(paymentData),
                        *payinDetails(paymentData, payinStatusData),
                        *paymentData.toPayoutDetails()
                    )
                )
            }
        }
    }

    private fun campaign(data: PaymentQuery.Data) =
        data.redeemedCampaigns.getOrNull(0)?.let { campaign ->
            if (
                campaign.fragments.incentiveFragment.incentive?.asFreeMonths != null ||
                campaign.fragments.incentiveFragment.incentive?.asMonthlyCostDeduction != null
            ) {
                PaymentModel.CampaignInformation(data)
            } else {
                null
            }
        }

    private fun failedPayments(data: PaymentQuery.Data) = safeLet(
        data.balance.failedCharges,
        data.nextChargeDate
    ) { failedCharges, nextChargeDate ->
        if (failedCharges > 0) {
            PaymentModel.FailedPayments(failedCharges, nextChargeDate)
        } else {
            null
        }
    }

    private fun connectPayment(data: PayinStatusQuery.Data) =
        if (data.payinMethodStatus == PayinMethodStatus.NEEDS_SETUP) {
            PaymentModel.ConnectPayment
        } else {
            null
        }

    private fun paymentHistory(data: PaymentQuery.Data) = if (data.chargeHistory.isNotEmpty()) {
        listOfNotNull(
            PaymentModel.PaymentHistoryHeader,
            data.chargeHistory.getOrNull(0)?.let { PaymentModel.Charge(it) },
            data.chargeHistory.getOrNull(1)?.let { PaymentModel.Charge(it) },
            PaymentModel.PaymentHistoryLink
        ).toTypedArray()
    } else {
        emptyArray()
    }

    private fun payinDetails(
        paymentData: PaymentQuery.Data,
        payinStatusData: PayinStatusQuery.Data,
    ): Array<PaymentModel> {
        paymentData.bankAccount?.let { bankAccount ->
            return arrayOf(
                PaymentModel.TrustlyPayinDetails(bankAccount, payinStatusData.payinMethodStatus),
                PaymentModel.Link.TrustlyChangePayin,
            )
        }
        paymentData.activePaymentMethods?.let { activePaymentMethods ->
            return arrayOf(
                PaymentModel.AdyenPayinDetails(activePaymentMethods),
                PaymentModel.Link.AdyenChangePayin,
            )
        }
        return emptyArray()
    }

    private fun PaymentQuery.Data.toPayoutDetails() = activePayoutMethods?.let { apm ->
        when (apm.status) {
            PayoutMethodStatus.ACTIVE,
            PayoutMethodStatus.PENDING,
            -> arrayOf(
                PaymentModel.PayoutDetailsHeader,
                PaymentModel.PayoutConnectionStatus(apm.status),
                PaymentModel.PayoutDetailsParagraph(apm.status),
                PaymentModel.Link.AdyenChangePayout,
            )
            PayoutMethodStatus.NEEDS_SETUP -> arrayOf(
                PaymentModel.PayoutDetailsHeader,
                PaymentModel.Link.AdyenAddPayout,
                PaymentModel.PayoutDetailsParagraph(apm.status),
            )
            PayoutMethodStatus.UNKNOWN__ -> {
                e { "Unknown `PayoutMethodStatus`" }
                emptyArray()
            }
        }
    } ?: emptyArray()

    private fun redeemCampaign(data: PaymentQuery.Data) = if (data.redeemedCampaigns.isEmpty()) {
        PaymentModel.Link.RedeemDiscountCode
    } else {
        null
    }

    companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        fun newInstance(context: Context) = Intent(context, PaymentActivity::class.java)
    }
}
