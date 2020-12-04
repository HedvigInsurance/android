package com.hedvig.app.feature.adyen.payin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.payments.Amount
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.adyen.AdyenCurrency
import com.hedvig.app.feature.adyen.AdyenDropInService
import com.hedvig.app.feature.connectpayin.ConnectPayinType
import com.hedvig.app.feature.connectpayin.ConnectPaymentResultFragment
import com.hedvig.app.feature.connectpayin.ConnectPaymentScreenState
import com.hedvig.app.feature.connectpayin.ConnectPaymentViewModel
import com.hedvig.app.feature.connectpayin.PostSignExplainerFragment
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.getLocale
import com.hedvig.app.isDebug
import e
import org.koin.android.viewmodel.ext.android.viewModel

class AdyenConnectPayinActivity : BaseActivity(R.layout.fragment_container_activity) {
    private val connectPaymentViewModel: ConnectPaymentViewModel by viewModel()
    private val adyenConnectPayinViewModel: AdyenConnectPayinViewModel by viewModel()

    private lateinit var paymentMethods: PaymentMethodsApiResponse
    private lateinit var currency: AdyenCurrency
    private var hasConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val c = intent.getSerializableExtra(CURRENCY) as? AdyenCurrency

        if (c == null) {
            e { "Programmer error: CURRENCY not provided to ${this.javaClass.name}" }
            finish()
            return
        }

        currency = c

        if (isPostSign()) {
            connectPaymentViewModel.setInitialNavigationDestination(ConnectPaymentScreenState.Explainer)
        }

        connectPaymentViewModel.navigationState.observe(this) { state ->
            when (state) {
                ConnectPaymentScreenState.Explainer -> supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.container,
                        PostSignExplainerFragment.newInstance(ConnectPayinType.ADYEN)
                    )
                    .commitAllowingStateLoss()
                is ConnectPaymentScreenState.Connect -> startAdyenPayment()
                is ConnectPaymentScreenState.Result -> supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.container,
                        ConnectPaymentResultFragment.newInstance(
                            state.success,
                            ConnectPayinType.ADYEN
                        )
                    )
                    .commitAllowingStateLoss()
            }
        }

        connectPaymentViewModel.shouldClose.observe(this) { shouldClose ->
            if (shouldClose) {
                if (isPostSign()) {
                    startActivity(
                        LoggedInActivity.newInstance(
                            this,
                            withoutHistory = true,
                            isFromOnboarding = true
                        )
                    )
                    return@observe
                }
                finish()
            }
        }

        adyenConnectPayinViewModel.paymentMethods.observe(this) {
            paymentMethods = it

            if (isPostSign()) {
                connectPaymentViewModel.isReadyToStart()
            } else {
                startAdyenPayment()
            }
        }
    }

    private fun startAdyenPayment() {
        val cardConfig = CardConfiguration.Builder(this, getString(R.string.ADYEN_PUBLIC_KEY))
            .setShowStorePaymentField(false)
            .build()

        val googlePayConfig =
            GooglePayConfiguration.Builder(this, getString(R.string.ADYEN_MERCHANT_ACCOUNT))
                .setGooglePayEnvironment(
                    if (isDebug()) {
                        GOOGLE_WALLET_ENVIRONMENT_TEST
                    } else {
                        GOOGLE_WALLET_ENVIRONMENT_PRODUCTION
                    }
                )
                .build()
        val dropInConfiguration = DropInConfiguration
            .Builder(
                this,
                intent, AdyenDropInService::class.java
            )
            .addCardConfiguration(cardConfig)
            .addGooglePayConfiguration(googlePayConfig)
            .setShopperLocale(getLocale(this))
            .setEnvironment(
                if (isDebug()) {
                    Environment.TEST
                } else {
                    Environment.EUROPE
                }
            )
            .setAmount(Amount().apply {
                currency = this@AdyenConnectPayinActivity.currency.toString()
                value = 0
            })
            .build()

        DropIn.startPayment(this, paymentMethods, dropInConfiguration)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getStringExtra(DropIn.RESULT_KEY) == ADYEN_RESULT_CODE_AUTHORISED) {
            hasConnected = true
            connectPaymentViewModel.navigateTo(ConnectPaymentScreenState.Result(success = true))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (
            !isPostSign()
            && !hasConnected
            && resultCode == Activity.RESULT_CANCELED
        ) {
            finish()
        }
    }

    private fun isPostSign() = intent.getBooleanExtra(IS_POST_SIGN, false)

    companion object {
        private const val ADYEN_RESULT_CODE_AUTHORISED = "Authorised"

        private const val GOOGLE_WALLET_ENVIRONMENT_PRODUCTION = 1
        private const val GOOGLE_WALLET_ENVIRONMENT_TEST = 3
        fun newInstance(context: Context, currency: AdyenCurrency, isPostSign: Boolean = false) =
            Intent(context, AdyenConnectPayinActivity::class.java).apply {
                putExtra(IS_POST_SIGN, isPostSign)
                putExtra(CURRENCY, currency)
            }

        private const val IS_POST_SIGN = "IS_POST_SIGN"
        private const val CURRENCY = "CURRENCY"
    }
}
