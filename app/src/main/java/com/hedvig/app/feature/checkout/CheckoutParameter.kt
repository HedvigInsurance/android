package com.hedvig.app.feature.checkout

import android.os.Parcelable
import com.hedvig.app.feature.offer.model.QuoteCartId
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckoutParameter(
    val quoteIds: List<String>,
    val quoteCartId: QuoteCartId?,
) : Parcelable
