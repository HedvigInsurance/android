package com.hedvig.app.feature.adyen.payout

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.adyen.checkout.base.model.payments.Amount
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.adyen.AdyenCurrency
import com.hedvig.app.getLocale
import com.hedvig.app.isDebug
import e
import org.koin.android.viewmodel.ext.android.viewModel

class AdyenConnectPayoutActivity : BaseActivity(R.layout.fragment_container_activity) {
    private val model: AdyenConnectPayoutViewModel by viewModel()

    private var hasConnected = false

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
                    intent, AdyenPayoutDropInService::class.java
                )
                .setShopperLocale(getLocale(this))
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        when (intent?.getStringExtra(DropIn.RESULT_KEY)) {
            ADYEN_RESULT_CODE_RECEIVED -> {
                hasConnected = true
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, ConnectPayoutResultFragment.newInstance())
                    .commitAllowingStateLoss()
            }
            ADYEN_RESULT_CODE_CANCELLED -> finish()
            else -> {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Handler(mainLooper).postDelayed(
            {
                if (!hasConnected && resultCode == Activity.RESULT_CANCELED) {
                    finish()
                }
            },
            10
        ) // Needed in order to allow the new intent to arrive in the activity
    }

    companion object {
        private const val ADYEN_RESULT_CODE_RECEIVED = "Received"
        private const val ADYEN_RESULT_CODE_CANCELLED = "Cancelled"

        private const val CURRENCY = "CURRENCY"
        fun newInstance(context: Context, currency: AdyenCurrency) =
            Intent(context, AdyenConnectPayoutActivity::class.java).apply {
                putExtra(CURRENCY, currency)
            }
    }
}
