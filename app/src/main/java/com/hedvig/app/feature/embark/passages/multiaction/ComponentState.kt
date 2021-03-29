package com.hedvig.app.feature.embark.passages.multiaction

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ComponentState(
    val id: Long,
    val dropDownSelection: String,
    val input: String,
    val switch: Boolean,
) : Parcelable
