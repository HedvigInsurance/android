package com.hedvig.app.feature.embark.passages.numberaction

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NumberActionParams(
    val key: String,
    val placeholder: String,
    val unit: String?,
    val maxValue: Int?,
    val minValue: Int?,
    val link: String,
    val submitLabel: String,
) : Parcelable
