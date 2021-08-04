package com.hedvig.app.feature.profile.ui.payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowCompat
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityPaymentHistoryBinding
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.setupToolbarScrollListener
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentHistoryActivity : BaseActivity(R.layout.activity_payment_history) {
    private val binding by viewBinding(ActivityPaymentHistoryBinding::bind)
    private val model: PaymentViewModel by viewModel()
    private val marketManager: MarketManager by inject()

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

            paymentHistory.adapter = PaymentHistoryAdapter(marketManager)

            model.data.observe(this@PaymentHistoryActivity) { (data, _) ->
                data?.chargeHistory?.let { chargeHistory ->
                    (paymentHistory.adapter as? PaymentHistoryAdapter)?.submitList(
                        listOf(ChargeWrapper.Title) + wrapCharges(chargeHistory)
                    )
                }
            }
        }
    }

    companion object {
        fun newInstance(context: Context): Intent =
            Intent(context, PaymentHistoryActivity::class.java)

        fun wrapCharges(charges: List<PaymentQuery.ChargeHistory>): List<ChargeWrapper> {
            val res = mutableListOf<ChargeWrapper>()
            for (index in charges.indices) {
                if (index == 0) {
                    res.add(ChargeWrapper.Header(charges[index].date.year))
                    res.add(ChargeWrapper.Item(charges[index]))
                    continue
                }
                if (charges[index - 1].date.year != charges[index].date.year) {
                    res.add(ChargeWrapper.Header(charges[index].date.year))
                    res.add(ChargeWrapper.Item(charges[index]))
                    continue
                }
                res.add(ChargeWrapper.Item(charges[index]))
            }

            return res
        }
    }
}
