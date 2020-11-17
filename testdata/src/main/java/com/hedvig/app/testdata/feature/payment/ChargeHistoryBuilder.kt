package com.hedvig.app.testdata.feature.payment

import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.graphql.PaymentQuery
import java.time.LocalDate
import java.time.Period
import java.time.temporal.TemporalAmount

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

inline val Int.months: TemporalAmount
    get() = Period.ofMonths(this)
