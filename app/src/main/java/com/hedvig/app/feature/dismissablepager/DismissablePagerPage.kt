package com.hedvig.app.feature.dismissablepager

import android.os.Parcelable
import com.hedvig.app.util.apollo.ThemedIconUrls
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DismissablePagerPage(
    val imageUrls: ThemedIconUrls,
    val title: String,
    val paragraph: String
) : Parcelable
