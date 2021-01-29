package com.hedvig.app.feature.embark.passages.numberaction

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NumberActionParams(
    val key: String,
    val placeholder: String,
    val link: String,
) : Parcelable
