package com.hedvig.app.feature.profile.ui.payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.setupLargeTitle
import kotlinx.android.synthetic.main.activity_payment_history.*
import org.koin.android.viewmodel.ext.android.viewModel

class PaymentHistoryActivity : BaseActivity() {
    private val profileViewModel: ProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_history)

        setupLargeTitle(
            R.string.PAYMENT_HISTORY_TITLE,
            R.font.circular_bold,
            R.drawable.ic_back,
            backAction = {
                onBackPressed()
            })

        profileViewModel.data.observe(lifecycleOwner = this) { data ->
            data?.chargeHistory?.let { chargeHistory ->
                paymentHistory.adapter = PaymentHistoryAdapter(wrapCharges(chargeHistory))
            }
        }
    }

    companion object {
        fun newInstance(context: Context): Intent =
            Intent(context, PaymentHistoryActivity::class.java)

        fun wrapCharges(charges: List<ProfileQuery.ChargeHistory>): List<ChargeWrapper> {
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
