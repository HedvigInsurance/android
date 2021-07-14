package com.hedvig.app.feature.offer.ui.checkout

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckoutParameter(
    val quoteIds: List<String>,
) : Parcelable
