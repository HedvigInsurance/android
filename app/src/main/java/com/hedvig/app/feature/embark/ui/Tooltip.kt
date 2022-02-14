package com.hedvig.app.feature.embark.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tooltip(val title: String, val description: String) : Parcelable
