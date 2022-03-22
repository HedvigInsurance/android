package com.hedvig.app.feature.embark.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
value class TooltipsParcel(val tooltips: List<Tooltip>) : Parcelable

@Parcelize
data class Tooltip(val title: String, val description: String) : Parcelable
