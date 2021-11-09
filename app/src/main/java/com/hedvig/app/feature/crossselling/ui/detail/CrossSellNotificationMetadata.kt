package com.hedvig.app.feature.crossselling.ui.detail

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CrossSellNotificationMetadata(
    val title: String?,
    val body: String?,
) : Parcelable
