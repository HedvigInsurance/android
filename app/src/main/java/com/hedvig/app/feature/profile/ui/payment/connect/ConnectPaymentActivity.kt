package com.hedvig.app.feature.profile.ui.payment.connect

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.payments.Amount
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.adyen.AdyenDropInService
import com.hedvig.app.feature.adyen.AdyenViewModel
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketPickerActivity
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.trustly.TrustlyJavascriptInterface
import com.hedvig.app.feature.trustly.TrustlyTracker
import com.hedvig.app.feature.trustly.TrustlyWebChromeClient
import com.hedvig.app.getLocale
import com.hedvig.app.isDebug
import com.hedvig.app.util.extensions.getMarket
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.enable
import com.hedvig.app.util.extensions.view.fadeIn
import com.hedvig.app.util.extensions.view.fadeOut
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import e
import kotlinx.android.synthetic.main.activity_connect_payment.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class ConnectPaymentActivity : BaseActivity(R.layout.activity_connect_payment) {

    private val profileViewModel: ProfileViewModel by viewModel()
    private val adyenViewModel: AdyenViewModel by viewModel()

    private val tracker: TrustlyTracker by inject()
    private var hasSuccessfullyConnectedDirectDebit = false

    private var paymentMethods: PaymentMethodsApiResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val market = getMarket()
        if (market == null) {
            e { "Programmer error: ${this.javaClass.name} accessed without a Market selected" }
            startActivity(MarketPickerActivity.newInstance(this))
            return
        }

        when (market) {
            Market.SE -> initializeSweden()
            Market.NO -> initializeNorway()
        }

        notNow.setHapticClickListener {
            tracker.notNow()
            showConfirmCloseDialog()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeSweden() {
        trustlyContainer.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            domStorageEnabled = true
            setSupportMultipleWindows(true)
        }
        trustlyContainer.webChromeClient = TrustlyWebChromeClient()
        trustlyContainer.addJavascriptInterface(
            TrustlyJavascriptInterface(this),
            TrustlyJavascriptInterface.NAME
        )

        if (isPostSignDD()) {
            explainerButton.enable()
            loadingSpinner.remove()
            explainerScreen.show()
            explainerButton.setHapticClickListener {
                tracker.explainerConnect()
                explainerScreen.fadeOut({
                    toolbar.show()
                    loadingSpinner.show()
                    profileViewModel.startTrustlySession()
                }, true)
            }
        }
        loadUrl()
    }

    private fun initializeNorway() {
        adyenViewModel.loadPaymentMethods()

        if (isPostSignDD()) {
            loadingSpinner.remove()
            explainerScreen.show()
            explainerButton.setHapticClickListener {
                tracker.explainerConnect()
                startAdyenPayment()
            }
        }

        adyenViewModel.paymentMethods.observe(this) { methods ->
            methods?.let { m ->
                paymentMethods = m
                if (!isPostSignDD()) {
                    loadingSpinner.remove()
                    startAdyenPayment()
                } else {
                    explainerButton.enable()
                }
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
            .Builder(this, newInstance(this, isPostSignDD()), AdyenDropInService::class.java)
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
                currency = "NOK"
                value = 0
            })
            .build()

        paymentMethods?.let { DropIn.startPayment(this, it, dropInConfiguration) }
        if (paymentMethods != null) {
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getStringExtra(DropIn.RESULT_KEY) == ADYEN_RESULT_CODE_AUTHORISED) {
            explainerScreen.remove()
            showSuccess()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!isPostSignDD() && !hasSuccessfullyConnectedDirectDebit && resultCode == Activity.RESULT_CANCELED) {
            finish()
        } else if (isPostSignDD() && !hasSuccessfullyConnectedDirectDebit && resultCode == Activity.RESULT_CANCELED) {
            showConfirmCloseDialog { }
        }
    }

    override fun onDestroy() {
        (trustlyContainer.parent as ViewGroup).removeView(trustlyContainer)

        trustlyContainer.removeAllViews()
        trustlyContainer.destroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (isPostSignDD() && !hasSuccessfullyConnectedDirectDebit) {
            showConfirmCloseDialog()
            return
        }
        close()
    }

    private fun showConfirmCloseDialog(negativeAction: (() -> Unit)? = null) {
        showAlert(
            title = R.string.TRUSTLY_ALERT_TITLE,
            message = R.string.TRUSTLY_ALERT_BODY,
            positiveLabel = R.string.TRUSTLY_ALERT_POSITIVE_ACTION,
            negativeLabel = R.string.TRUSTLY_ALERT_NEGATIVE_ACTION,
            positiveAction = {
                close()
            },
            negativeAction = negativeAction
        )
    }

    private fun loadUrl() {
        profileViewModel.trustlyUrl.observe(lifecycleOwner = this) { url ->
            trustlyContainer.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, loadedUrl: String?) {
                    super.onPageFinished(view, url)
                    if (loadedUrl != url) {
                        return
                    }

                    loadingSpinner.remove()
                    trustlyContainer.fadeIn()
                }

                override fun onPageStarted(view: WebView?, requestedUrl: String, favicon: Bitmap?) {
                    if (requestedUrl.startsWith("bankid")) {
                        view?.stopLoading()
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(requestedUrl)
                        startActivity(intent)
                        return
                    }

                    if (requestedUrl.contains("success")) {
                        view?.stopLoading()
                        showSuccess()
                        return
                    }
                    if (requestedUrl.contains("fail")) {
                        view?.stopLoading()
                        showFailure()
                        return
                    }
                }
            }
            trustlyContainer.loadUrl(url)
        }
        if (isPostSignDD()) {
            return
        }
        profileViewModel.startTrustlySession()
        toolbar.show()
    }

    fun showSuccess() {
        hasSuccessfullyConnectedDirectDebit = true
        tracker.addPaymentInfo()
        trustlyContainer.remove()
        resultIcon.setImageResource(R.drawable.icon_success)
        resultTitle.text = resources.getString(R.string.PROFILE_TRUSTLY_SUCCESS_TITLE)
        resultParagraph.text = getString(R.string.PROFILE_TRUSTLY_SUCCESS_DESCRIPTION)
        notNow.remove()
        resultDoItLater.remove()
        if (isPostSignDD()) {
            resultClose.text = getString(R.string.ONBOARDING_CONNECT_DD_SUCCESS_CTA)
        } else {
            resultClose.text = getString(R.string.PROFILE_TRUSTLY_CLOSE)
        }
        resultClose.setHapticClickListener {
            profileViewModel.refreshBankAccountInfo()
            close()
        }
        resultScreen.show()
    }

    fun showFailure() {
        trustlyContainer.remove()
        resultIcon.setImageResource(R.drawable.icon_failure)
        resultTitle.text = resources.getString(R.string.ONBOARDING_CONNECT_DD_FAILURE_HEADLINE)
        resultParagraph.text = getString(R.string.ONBOARDING_CONNECT_DD_FAILURE_BODY)
        resultDoItLater.show()
        resultDoItLater.setHapticClickListener {
            tracker.doItLater()
            close()
        }
        resultClose.text = getString(R.string.ONBOARDING_CONNECT_DD_FAILURE_CTA_RETRY)
        resultClose.setHapticClickListener {
            tracker.retry()
            loadingSpinner.show()
            resultScreen.remove()
            profileViewModel.startTrustlySession()
        }
        resultScreen.show()
    }

    private fun close() {
        if (isPostSignDD()) {
            startActivity(Intent(this, LoggedInActivity::class.java).apply {
                putExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
            return
        }
        super.onBackPressed()
    }

    private fun isPostSignDD() = intent.getBooleanExtra(WITH_EXPLAINER, false)

    companion object {
        private const val WITH_EXPLAINER = "with_explainer"

        private const val ADYEN_RESULT_CODE_AUTHORISED = "Authorised"

        private const val GOOGLE_WALLET_ENVIRONMENT_PRODUCTION = 1
        private const val GOOGLE_WALLET_ENVIRONMENT_TEST = 3

        fun newInstance(
            context: Context,
            withExplainer: Boolean = false,
            withoutHistory: Boolean = false
        ) =
            Intent(context, ConnectPaymentActivity::class.java).apply {
                putExtra(WITH_EXPLAINER, withExplainer)
                if (withoutHistory) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            }
    }
}
