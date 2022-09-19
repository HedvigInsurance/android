package com.hedvig.app.feature.profile.ui.payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hedvig.android.apollo.graphql.PaymentQuery
import com.hedvig.android.market.MarketManager
import com.hedvig.app.LanguageService
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityPaymentHistoryBinding
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.setupToolbarScrollListener
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentHistoryActivity : AppCompatActivity(R.layout.activity_payment_history) {
  private val binding by viewBinding(ActivityPaymentHistoryBinding::bind)
  private val viewModel: PaymentViewModel by viewModel()
  private val marketManager: MarketManager by inject()
  private val languageService: LanguageService by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding.apply {
      window.compatSetDecorFitsSystemWindows(false)
      paymentHistory.applyNavigationBarInsets()
      toolbar.applyStatusBarInsets()
      toolbar.setNavigationOnClickListener {
        onBackPressed()
      }
      paymentHistory.setupToolbarScrollListener(toolbar)

      paymentHistory.adapter = PaymentHistoryAdapter(marketManager, languageService)

      viewModel
        .data
        .flowWithLifecycle(lifecycle)
        .onEach { (data, _) ->
          data?.chargeHistory?.let { chargeHistory ->
            (paymentHistory.adapter as? PaymentHistoryAdapter)?.submitList(
              listOf(ChargeWrapper.Title) + wrapCharges(chargeHistory),
            )
          }
        }
        .launchIn(lifecycleScope)
    }
  }

  companion object {
    fun newInstance(context: Context): Intent =
      Intent(context, PaymentHistoryActivity::class.java)

    fun wrapCharges(charges: List<PaymentQuery.ChargeHistory>): List<ChargeWrapper> {
      return buildList {
        for (index in charges.indices) {
          if (index == 0) {
            add(ChargeWrapper.Header(charges[index].date.year))
            add(ChargeWrapper.Item(charges[index]))
            continue
          }
          if (charges[index - 1].date.year != charges[index].date.year) {
            add(ChargeWrapper.Header(charges[index].date.year))
            add(ChargeWrapper.Item(charges[index]))
            continue
          }
          add(ChargeWrapper.Item(charges[index]))
        }
      }
    }
  }
}
