package com.hedvig.app.feature.referrals.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.common.android.serializableExtra
import com.hedvig.android.language.LanguageService
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityReferralsInformationBinding
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.setupToolbarScrollListener
import com.hedvig.app.util.extensions.viewBinding
import org.javamoney.moneta.Money
import org.koin.android.ext.android.inject
import slimber.log.e
import java.math.BigDecimal
import javax.money.MonetaryAmount

class ReferralsInformationActivity : AppCompatActivity(R.layout.activity_referrals_information) {
  private val binding by viewBinding(ActivityReferralsInformationBinding::bind)
  private val languageService: LanguageService by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    val termsUrl = intent.getStringExtra(TERMS_URL)
    val incentiveAmount = intent.serializableExtra<BigDecimal>(INCENTIVE_AMOUNT)
    val incentiveCurrency = intent.getStringExtra(INCENTIVE_CURRENCY)

    if (termsUrl == null || incentiveAmount == null || incentiveCurrency == null) {
      e {
        "Programmer error: TERMS_URL ||" +
          "INCENTIVE_AMOUNT ||" +
          "INCENTIVE_CURRENCY not provided to ${this.javaClass.name}"
      }
      return
    }

    binding.apply {
      window.compatSetDecorFitsSystemWindows(false)

      toolbar.applyStatusBarInsets()
      toolbar.setNavigationOnClickListener {
        onBackPressedDispatcher.onBackPressed()
      }

      scrollView.applyNavigationBarInsets()
      scrollView.setupToolbarScrollListener(toolbar)

      val incentive = Money.of(incentiveAmount, incentiveCurrency)
      body.text = getString(
        hedvig.resources.R.string.referrals_info_sheet_body,
        incentive.format(languageService.getLocale()),
      )

      val termsAsUri = Uri.parse(termsUrl)

      termsAndConditions.setHapticClickListener {
        if (canOpenUri(termsAsUri)) {
          openUri(termsAsUri)
        }
      }
    }
  }

  companion object {
    private const val TERMS_URL = "TERMS_URL"

    private const val INCENTIVE_AMOUNT = "INCENTIVE_AMOUNT"
    private const val INCENTIVE_CURRENCY = "INCENTIVE_CURRENCY"

    fun newInstance(context: Context, termsUrl: String, incentive: MonetaryAmount) =
      Intent(context, ReferralsInformationActivity::class.java).apply {
        putExtra(TERMS_URL, termsUrl)
        putExtra(
          INCENTIVE_AMOUNT,
          incentive.number.numberValueExact(BigDecimal::class.java),
        )
        putExtra(INCENTIVE_CURRENCY, incentive.currency.currencyCode)
      }
  }
}
