package com.hedvig.app.feature.dismissablepager

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DismissablePagerPage(
    val imageUrl: String,
    val title: String,
    val paragraph: String
) : Parcelable
