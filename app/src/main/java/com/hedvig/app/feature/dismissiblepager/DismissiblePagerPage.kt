package com.hedvig.app.feature.dismissiblepager

import android.os.Parcelable
import com.hedvig.app.util.apollo.ThemedIconUrls
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DismissiblePagerPage(
    val imageUrls: ThemedIconUrls,
    val title: String,
    val paragraph: String
) : Parcelable
