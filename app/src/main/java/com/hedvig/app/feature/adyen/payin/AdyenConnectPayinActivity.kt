package com.hedvig.app.feature.adyen.payin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.DropInResult
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.adyen.AdyenCurrency
import com.hedvig.app.feature.connectpayin.ConnectPayinType
import com.hedvig.app.feature.connectpayin.ConnectPaymentResultFragment
import com.hedvig.app.feature.connectpayin.ConnectPaymentScreenState
import com.hedvig.app.feature.connectpayin.ConnectPaymentViewModel
import com.hedvig.app.feature.connectpayin.PostSignExplainerFragment
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.getLocale
import com.hedvig.app.isDebug
import e
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AdyenConnectPayinActivity : BaseActivity(R.layout.fragment_container_activity) {

    override val screenName = "connect_payment_adyen"

    private val connectPaymentViewModel: ConnectPaymentViewModel by viewModel()
    private val adyenConnectPayinViewModel: AdyenConnectPayinViewModel by viewModel()

    private val trackingFacade: TrackingFacade by inject()
    private val marketManager: MarketManager by inject()
    private lateinit var paymentMethods: PaymentMethodsApiResponse
    private lateinit var currency: AdyenCurrency

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
                ConnectPaymentScreenState.Explainer ->
                    supportFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.container,
                            PostSignExplainerFragment.newInstance(ConnectPayinType.ADYEN)
                        )
                        .commitAllowingStateLoss()
                is ConnectPaymentScreenState.Connect -> startAdyenPayment()
                is ConnectPaymentScreenState.Result ->
                    supportFragmentManager
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
        val cardConfig = CardConfiguration.Builder(this, getString(R.string.ADYEN_CLIENT_KEY))
            .setShowStorePaymentField(false)
            .setEnvironment(getEnvironment())
            .build()

        val googlePayConfig =
            GooglePayConfiguration.Builder(this, getString(R.string.ADYEN_CLIENT_KEY))
                .setEnvironment(getEnvironment())
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
                AdyenPayinDropInService::class.java,
                getString(R.string.ADYEN_CLIENT_KEY)
            )
            .addCardConfiguration(cardConfig)
            .addGooglePayConfiguration(googlePayConfig)
            .setShopperLocale(getLocale(this, marketManager.market))
            .setEnvironment(getEnvironment())
            .build()

        DropIn.startPayment(this, paymentMethods, dropInConfiguration)
        trackingFacade.track("connect_payment_visible")
    }

    private fun getEnvironment() = if (isDebug()) {
        Environment.TEST
    } else {
        Environment.EUROPE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Replace with new result API when adyens handleActivityResult is updated
        super.onActivityResult(requestCode, resultCode, data)

        when (DropIn.handleActivityResult(requestCode, resultCode, data)) {
            is DropInResult.CancelledByUser -> finish()
            is DropInResult.Error -> connectPaymentViewModel.navigateTo(
                ConnectPaymentScreenState.Result(success = false)
            )
            is DropInResult.Finished -> {
                connectPaymentViewModel.navigateTo(ConnectPaymentScreenState.Result(success = true))
            }
        }
    }

    private fun isPostSign() = intent.getBooleanExtra(IS_POST_SIGN, false)

    companion object {

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
