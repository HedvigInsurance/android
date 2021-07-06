package com.hedvig.app.feature.offer.ui.checkout

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckoutParameter(
    val quoteIds: List<String>,
    val title: String,
    val subtitle: String,
    val gdprUrl: String
) : Parcelable
