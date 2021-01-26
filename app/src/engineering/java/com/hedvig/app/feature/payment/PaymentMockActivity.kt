package com.hedvig.app.feature.payment

import com.hedvig.app.MockActivity
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.feature.profile.ui.payment.PaymentViewModel
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.marketProviderModule
import com.hedvig.app.mocks.MockMarketProvider
import com.hedvig.app.paymentModule
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_NEEDS_SETUP
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_PENDING
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_ADYEN_CONNECTED
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_FAILED_PAYMENTS
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_FREE_MONTHS
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_HISTORIC_PAYMENTS
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_MANY_ITEMS
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_NOT_CONNECTED
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_PAYOUT_CONNECTED
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_PAYOUT_NOT_CONNECTED
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_PERCENTAGE_CAMPAIGN
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_REFERRAL
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_TRUSTLY_CONNECTED
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class PaymentMockActivity : MockActivity() {
    private val marketProvider = MockMarketProvider()
    override val original = listOf(paymentModule, marketProviderModule)
    override val mocks = listOf(
        module {
            viewModel<PaymentViewModel> { MockPaymentViewModel() }
            single<MarketProvider> { marketProvider }
        }
    )

    override fun adapter() = genericDevelopmentAdapter {
        header("Payment Screen")
        clickableItem("Active + Connect Payment") {
            MockPaymentViewModel.apply {
                paymentData = PAYMENT_DATA_NOT_CONNECTED
                payinStatusData = PAYIN_STATUS_DATA_NEEDS_SETUP
            }

            startActivity(PaymentActivity.newInstance(context))
        }
        clickableItem("Active + Trustly Connected") {
            MockPaymentViewModel.apply {
                paymentData = PAYMENT_DATA_TRUSTLY_CONNECTED
                payinStatusData = PAYIN_STATUS_DATA_ACTIVE
            }

            startActivity(PaymentActivity.newInstance(context))
        }
        clickableItem("Active + Trustly, Recently changed") {
            MockPaymentViewModel.apply {
                paymentData = PAYMENT_DATA_TRUSTLY_CONNECTED
                payinStatusData = PAYIN_STATUS_DATA_PENDING
            }

            startActivity(PaymentActivity.newInstance(context))
        }
        clickableItem("Active + Adyen Connected") {
            MockPaymentViewModel.apply {
                paymentData = PAYMENT_DATA_ADYEN_CONNECTED
                payinStatusData = PAYIN_STATUS_DATA_ACTIVE
            }

            startActivity(PaymentActivity.newInstance(context))
        }
        clickableItem("Active + Referral") {
            MockPaymentViewModel.apply {
                paymentData = PAYMENT_DATA_REFERRAL
                payinStatusData = PAYIN_STATUS_DATA_ACTIVE
            }

            startActivity(PaymentActivity.newInstance(context))
        }
        clickableItem("Active + Percentage Discount") {
            MockPaymentViewModel.apply {
                paymentData = PAYMENT_DATA_PERCENTAGE_CAMPAIGN
                payinStatusData = PAYIN_STATUS_DATA_ACTIVE
            }

            startActivity(PaymentActivity.newInstance(context))
        }
        clickableItem("Active + Free Months Campaign") {
            MockPaymentViewModel.apply {
                paymentData = PAYMENT_DATA_FREE_MONTHS
                payinStatusData = PAYIN_STATUS_DATA_ACTIVE
            }

            startActivity(PaymentActivity.newInstance(context))
        }
        clickableItem("Failed Payments") {
            MockPaymentViewModel.apply {
                paymentData = PAYMENT_DATA_FAILED_PAYMENTS
                payinStatusData = PAYIN_STATUS_DATA_ACTIVE
            }

            startActivity(PaymentActivity.newInstance(context))
        }
        clickableItem("Payment History") {
            MockPaymentViewModel.apply {
                paymentData = PAYMENT_DATA_HISTORIC_PAYMENTS
                payinStatusData = PAYIN_STATUS_DATA_ACTIVE
            }

            startActivity(PaymentActivity.newInstance(context))
        }
        clickableItem("Payout connected") {
            MockPaymentViewModel.apply {
                paymentData = PAYMENT_DATA_PAYOUT_CONNECTED
                payinStatusData = PAYIN_STATUS_DATA_ACTIVE
            }
            startActivity(PaymentActivity.newInstance(context))
        }
        clickableItem("Payout not connected") {
            MockPaymentViewModel.apply {
                paymentData = PAYMENT_DATA_PAYOUT_NOT_CONNECTED
                payinStatusData = PAYIN_STATUS_DATA_ACTIVE
            }
            startActivity(PaymentActivity.newInstance(context))
        }
        clickableItem("Norway + As many items as possible") {
            MockPaymentViewModel.apply {
                paymentData = PAYMENT_DATA_MANY_ITEMS
                payinStatusData = PAYIN_STATUS_DATA_NEEDS_SETUP
            }
            startActivity(PaymentActivity.newInstance(context))
        }
        marketSpinner { MockMarketProvider.mockedMarket = it }
    }
}
