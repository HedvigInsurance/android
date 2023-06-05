package com.hedvig.app.feature.checkout

import android.os.Parcelable
import com.hedvig.android.core.common.android.QuoteCartId
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckoutParameter(
  val selectedVariantId: String,
  val quoteCartId: QuoteCartId?,
) : Parcelable
