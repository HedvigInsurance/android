package com.hedvig.app.feature.checkout

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckoutParameter(
    val quoteIds: List<String>,
    val quoteCartId: String?,
) : Parcelable
