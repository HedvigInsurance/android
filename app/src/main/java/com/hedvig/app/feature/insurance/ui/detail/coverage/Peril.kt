package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Peril(
    val title: String,
    val description: String,
    val iconUrl: String,
    val exception: List<String>,
    val covered: List<String>,
    val info: String
) : Parcelable
