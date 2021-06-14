package com.hedvig.app.feature.adyen.payout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.DropInResult
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.adyen.AdyenCurrency
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.getLocale
import com.hedvig.app.isDebug
import e
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AdyenConnectPayoutActivity : BaseActivity(R.layout.fragment_container_activity) {
    private val model: AdyenConnectPayoutViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adyenCurrency = intent.getSerializableExtra(CURRENCY) as? AdyenCurrency

        if (adyenCurrency == null) {
            e { "Programmer error: CURRENCY not provided to ${this.javaClass.name}" }
            finish()
            return
        }

        model.payoutMethods.observe(this) { response ->
            val dropInConfiguration = DropInConfiguration
                .Builder(
                    this,
                    AdyenPayoutDropInService::class.java,
                    getString(R.string.ADYEN_CLIENT_KEY),
                )
                .setShopperLocale(getLocale(this, marketManager.market))
                .setEnvironment(
                    if (isDebug()) {
                        Environment.TEST
                    } else {
                        Environment.EUROPE
                    }
                )
                .setAmount(
                    Amount().apply {
                        currency = adyenCurrency.toString()
                        value = 0
                    }
                )
                .build()

            DropIn.startPayment(this, response, dropInConfiguration)
        }

        model.shouldClose.observe(this) { shouldClose ->
            if (shouldClose) {
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Replace with new result API when adyens handleActivityResult is updated
        super.onActivityResult(requestCode, resultCode, data)

        when (DropIn.handleActivityResult(requestCode, resultCode, data)) {
            is DropInResult.CancelledByUser -> finish()
            is DropInResult.Error -> {
            }
            is DropInResult.Finished -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, ConnectPayoutResultFragment.newInstance())
                    .commitAllowingStateLoss()
            }
        }
    }

    companion object {
        private const val CURRENCY = "CURRENCY"
        fun newInstance(context: Context, currency: AdyenCurrency) =
            Intent(context, AdyenConnectPayoutActivity::class.java).apply {
                putExtra(CURRENCY, currency)
            }
    }
}
