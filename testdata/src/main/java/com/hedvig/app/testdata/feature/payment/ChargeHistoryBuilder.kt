package com.hedvig.app.testdata.feature.payment

import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.util.months
import java.time.LocalDate

data class ChargeHistoryBuilder(
    private val amount: String = "139.00",
    private val currency: String = "SEK",
    private val date: LocalDate = LocalDate.now() - 1.months
) {
    fun build() = PaymentQuery.ChargeHistory(
        amount = PaymentQuery.Amount(
            fragments = PaymentQuery.Amount.Fragments(
                MonetaryAmountFragment(
                    amount = amount,
                    currency = currency
                )
            )
        ),
        date = date
    )
}

