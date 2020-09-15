package com.hedvig.app.feature.dismissiblepager

import android.os.Parcelable
import com.hedvig.app.util.apollo.ThemedIconUrls
import kotlinx.android.parcel.Parcelize

sealed class DismissiblePagerModel: Parcelable {
    @Parcelize
    data class TitlePage(
        val imageUrls: ThemedIconUrls,
        val title: String,
        val paragraph: String
    ) : DismissiblePagerModel()

    @Parcelize
    object SwipeOffScreen: DismissiblePagerModel()
}

