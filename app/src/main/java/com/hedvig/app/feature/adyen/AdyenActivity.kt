package com.hedvig.app.feature.adyen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.observe
import org.koin.android.viewmodel.ext.android.viewModel

class AdyenActivity : BaseActivity(R.layout.activity_adyen) {
    private val model: AdyenViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cardConfig = CardConfiguration.Builder(this, "SECRET")
            .build()

        val googlePayConfig = GooglePayConfiguration.Builder(this, "SECRET").build()
        val dropInConfiguration = DropInConfiguration
            .Builder(this, newInstance(this), AdyenDropInService::class.java)
            .addCardConfiguration(cardConfig)
            .addGooglePayConfiguration(googlePayConfig)
            .setEnvironment(Environment.TEST)
            .build()

        model.paymentMethods.observe(this) { methods ->
            methods?.let { m ->
                DropIn.startPayment(this, m, dropInConfiguration)
            }
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, AdyenActivity::class.java)
    }
}
