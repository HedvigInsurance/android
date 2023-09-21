package com.hedvig.app.feature.adyen.payout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.DropInResult
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import com.hedvig.android.core.common.android.serializableExtra
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.app.R
import com.hedvig.app.feature.adyen.AdyenCurrency
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Hedvig paying to member
 */
class AdyenConnectPayoutActivity : AppCompatActivity(R.layout.fragment_container_activity) {
  private val viewModel: AdyenConnectPayoutViewModel by viewModel()
  private val languageService: LanguageService by inject()
  private val hedvigBuildConstants: HedvigBuildConstants by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    val adyenCurrency = intent.serializableExtra<AdyenCurrency>(CURRENCY)

    if (adyenCurrency == null) {
      logcat(LogPriority.ERROR) { "Programmer error: CURRENCY not provided to ${this.javaClass.name}" }
      finish()
      return
    }

    viewModel.payoutMethods.observe(this) { response ->
      val dropInConfiguration = DropInConfiguration
        .Builder(
          this,
          AdyenPayoutDropInService::class.java,
          getString(R.string.ADYEN_CLIENT_KEY),
        )
        .setShopperLocale(languageService.getLocale())
        .setEnvironment(
          if (hedvigBuildConstants.isProduction) {
            Environment.EUROPE
          } else {
            Environment.TEST
          },
        )
        .setAmount(
          Amount().apply {
            currency = adyenCurrency.toString()
            value = 0
          },
        )
        .build()

      DropIn.startPayment(this, response, dropInConfiguration)
    }

    viewModel.shouldClose.observe(this) { shouldClose ->
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
      is DropInResult.Finished -> {
        supportFragmentManager
          .beginTransaction()
          .replace(R.id.container, ConnectPayoutResultFragment.newInstance())
          .commitAllowingStateLoss()
      }
      is DropInResult.Error,
      null,
      -> {
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
