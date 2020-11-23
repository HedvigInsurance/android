package com.hedvig.app.feature.profile.ui.payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.updatePadding
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityPaymentBinding
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.safeLet
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.time.format.DateTimeFormatter

class PaymentActivity : BaseActivity(R.layout.activity_payment) {
    private val binding by viewBinding(ActivityPaymentBinding::bind)
    private val model: PaymentViewModel by viewModel()

    private val tracker: PaymentTracker by inject()
    private val marketProvider: MarketProvider by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)

            toolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }
            toolbar.setNavigationOnClickListener { onBackPressed() }

            recycler.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }
            recycler.adapter = PaymentAdapter(marketProvider, supportFragmentManager, tracker)

            model.data.observe(this@PaymentActivity) { (paymentData, payinStatusData) ->
                if (paymentData == null || payinStatusData == null) {
                    return@observe
                }

                (recycler.adapter as? PaymentAdapter)?.submitList(
                    listOfNotNull(
                        failedPayments(paymentData),
                        PaymentModel.NextPayment(paymentData),
                        connectPayment(payinStatusData),
                        campaign(paymentData),
                        *paymentHistory(paymentData),
                        *payinDetails(paymentData, payinStatusData),
                        redeemCampaign(paymentData),
                    )
                )
            }
        }
    }

    private fun campaign(data: PaymentQuery.Data) =
        data.redeemedCampaigns.getOrNull(0)?.let { campaign ->
            if (campaign.fragments.incentiveFragment.incentive?.asFreeMonths != null || campaign.fragments.incentiveFragment.incentive?.asMonthlyCostDeduction != null) {
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
        payinStatusData: PayinStatusQuery.Data
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

    private fun redeemCampaign(data: PaymentQuery.Data) = if (data.redeemedCampaigns.isEmpty()) {
        PaymentModel.Link.RedeemDiscountCode
    } else {
        null
    }

    companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd, LLL yyyy")

        fun newInstance(context: Context) = Intent(context, PaymentActivity::class.java)
    }
}

