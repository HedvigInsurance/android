package com.hedvig.app.feature.checkout

import android.os.Parcelable
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckoutParameter(
    val quoteIds: List<String>,
    val quoteCartId: CreateQuoteCartUseCase.QuoteCartId?,
) : Parcelable
